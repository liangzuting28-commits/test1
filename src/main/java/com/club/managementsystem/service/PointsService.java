package com.club.managementsystem.service;

import com.club.managementsystem.entity.Activity;
import com.club.managementsystem.entity.PointsRecord;
import com.club.managementsystem.entity.PointsRule;
import com.club.managementsystem.entity.Student;
import com.club.managementsystem.repository.ActivityRepository;
import com.club.managementsystem.repository.PointsRecordRepository;
import com.club.managementsystem.repository.PointsRuleRepository;
import com.club.managementsystem.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PointsService {

    private final PointsRecordRepository pointsRecordRepository;
    private final PointsRuleRepository pointsRuleRepository;
    private final StudentRepository studentRepository;
    private final ActivityRepository activityRepository;
    
    public PointsService(PointsRecordRepository pointsRecordRepository,
                        PointsRuleRepository pointsRuleRepository,
                        StudentRepository studentRepository,
                        ActivityRepository activityRepository) {
        this.pointsRecordRepository = pointsRecordRepository;
        this.pointsRuleRepository = pointsRuleRepository;
        this.studentRepository = studentRepository;
        this.activityRepository = activityRepository;
    }

    // 获取学生的积分记录，包含活动详细信息
    public List<Map<String, Object>> getStudentPoints(String studentId) {
        List<PointsRecord> pointsRecords = pointsRecordRepository.findByStudentId(studentId);
        return pointsRecords.stream()
                .map(record -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("recordId", record.getRecordId());
                    result.put("studentId", record.getStudentId());
                    result.put("activityId", record.getActivityId());
                    result.put("pointsEarned", record.getPointsEarned());
                    result.put("earnedTime", record.getEarnedTime());
                    
                    // 查询活动信息
                    Optional<Activity> activityOpt = activityRepository.findById(record.getActivityId());
                    if (activityOpt.isPresent()) {
                        Activity activity = activityOpt.get();
                        result.put("activityName", activity.getActivityName());
                        result.put("location", activity.getLocation());
                        result.put("startTime", activity.getActivityTime());
                        result.put("activityType", activity.getActivityType());
                        result.put("clubName", ""); // 暂时留空，后续可以关联社团信息
                    }
                    
                    return result;
                })
                .collect(Collectors.toList());
    }

    // 获取学生的总积分
    public Integer getStudentTotalPoints(String studentId) {
        Optional<Student> student = studentRepository.findByStudentId(studentId);
        return student.map(Student::getTotalPoints).orElse(0);
    }

    // 获取积分排行榜
    public List<Map<String, Object>> getPointsRanking() {
        List<Student> students = studentRepository.findAll();

        return students.stream()
                .filter(student -> student.getTotalPoints() > 0)
                .sorted((s1, s2) -> s2.getTotalPoints().compareTo(s1.getTotalPoints()))
                .map(student -> {
                    Map<String, Object> studentInfo = new HashMap<>();
                    studentInfo.put("studentId", student.getStudentId());
                    studentInfo.put("name", student.getName());
                    studentInfo.put("college", student.getCollege());
                    studentInfo.put("majorClass", student.getMajorClass());
                    studentInfo.put("totalPoints", student.getTotalPoints());
                    return studentInfo;
                })
                .collect(Collectors.toList());
    }

    // 获取所有积分规则
    public List<PointsRule> getAllPointsRules() {
        return pointsRuleRepository.findAll();
    }

    // 更新积分规则
    public PointsRule updatePointsRule(Integer ruleId, PointsRule pointsRule) {
        Optional<PointsRule> existingRule = pointsRuleRepository.findById(ruleId);
        if (existingRule.isEmpty()) {
            throw new RuntimeException("积分规则不存在");
        }

        PointsRule rule = existingRule.get();
        rule.setActivityType(pointsRule.getActivityType());
        rule.setBasePointsValue(pointsRule.getBasePointsValue());

        return pointsRuleRepository.save(rule);
    }

    // 添加积分规则
    public PointsRule addPointsRule(PointsRule pointsRule) {
        // 检查是否已存在相同类型的规则
        Optional<PointsRule> existingRule = pointsRuleRepository.findByActivityType(pointsRule.getActivityType());
        if (existingRule.isPresent()) {
            throw new RuntimeException("已存在相同活动类型的积分规则");
        }

        return pointsRuleRepository.save(pointsRule);
    }

    // 删除积分规则
    public void deletePointsRule(Integer ruleId) {
        if (!pointsRuleRepository.existsById(ruleId)) {
            throw new RuntimeException("积分规则不存在");
        }
        pointsRuleRepository.deleteById(ruleId);
    }

    // 根据活动类型获取积分规则
    public PointsRule getPointsRuleByActivityType(String activityType) {
        Optional<PointsRule> rule = pointsRuleRepository.findByActivityType(activityType);
        return rule.orElse(null);
    }
    
    /**
     * 为学生发放活动积分，确保事务性和防重复发放
     * @param studentId 学生ID
     * @param activityId 活动ID
     * @param points 积分值
     * @return 是否发放成功
     */
    @Transactional
    public boolean awardPointsToStudent(String studentId, Integer activityId, Integer points) {
        // 检查是否已经发放过积分
        PointsRecord existingRecord = pointsRecordRepository.findByStudentIdAndActivityId(studentId, activityId);
        if (existingRecord != null) {
            return false; // 已经发放过积分
        }
        
        // 创建积分记录
        PointsRecord pointsRecord = new PointsRecord();
        pointsRecord.setStudentId(studentId);
        pointsRecord.setActivityId(activityId);
        pointsRecord.setPointsEarned(points);
        pointsRecordRepository.save(pointsRecord);
        
        // 更新学生总积分
        Optional<Student> studentOpt = studentRepository.findByStudentId(studentId);
        if (studentOpt.isPresent()) {
            Student student = studentOpt.get();
            student.setTotalPoints(student.getTotalPoints() + points);
            studentRepository.save(student);
            return true;
        }
        
        return false;
    }
}