package com.club.managementsystem.service;

import com.club.managementsystem.dto.ActivityRequest;
import com.club.managementsystem.entity.*;
import com.club.managementsystem.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ActivityService {
    private final ActivityRepository activityRepository;
    private final ClubRepository clubRepository;
    private final StudentRepository studentRepository;
    private final ActivityRegistrationRepository activityRegistrationRepository;
    private final PointsRuleRepository pointsRuleRepository;
    private final PointsRecordRepository pointsRecordRepository;
    private final CheckInRepository checkInRepository;
    
    public ActivityService(ActivityRepository activityRepository,
                         ClubRepository clubRepository,
                         StudentRepository studentRepository,
                         ActivityRegistrationRepository activityRegistrationRepository,
                         PointsRuleRepository pointsRuleRepository,
                         PointsRecordRepository pointsRecordRepository,
                         CheckInRepository checkInRepository) {
        this.activityRepository = activityRepository;
        this.clubRepository = clubRepository;
        this.studentRepository = studentRepository;
        this.activityRegistrationRepository = activityRegistrationRepository;
        this.pointsRuleRepository = pointsRuleRepository;
        this.pointsRecordRepository = pointsRecordRepository;
        this.checkInRepository = checkInRepository;
    }

    // 社长创建活动
    public Activity createActivity(ActivityRequest request, String presidentId) {
        // 验证社长是否有权限为该社团创建活动
        Optional<Club> club = clubRepository.findById(request.getClubId());
        if (club.isEmpty() || !club.get().getPresidentId().equals(presidentId)) {
            throw new RuntimeException("无权为该社团创建活动");
        }

        // 根据活动类型获取积分规则
        Integer awardPoints = request.getAwardPoints();
        if (request.getActivityType() != null && !request.getActivityType().isEmpty()) {
            Optional<PointsRule> pointsRule = pointsRuleRepository.findByActivityType(request.getActivityType());
            if (pointsRule.isPresent()) {
                // 使用积分规则中的积分值，确保与规则一致
                awardPoints = pointsRule.get().getBasePointsValue();
                System.out.println("使用活动类型 '" + request.getActivityType() + "' 的积分规则: " + awardPoints + " 积分");
            } else {
                System.out.println("未找到活动类型 '" + request.getActivityType() + "' 的积分规则，使用默认积分值");
            }
        }

        Activity activity = new Activity();
        activity.setActivityName(request.getActivityName());
        activity.setClubId(request.getClubId());
        activity.setActivityTime(request.getActivityTime());
        activity.setLocation(request.getLocation());
        activity.setMaxParticipants(request.getMaxParticipants());
        activity.setAwardPoints(awardPoints);
        activity.setDescription(request.getDescription());
        activity.setStatus(Activity.Status.PENDING); // 初始状态为待审核
        activity.setActivityType(request.getActivityType()); // 设置活动类型

        return activityRepository.save(activity);
    }

    /**
     * 结束活动并发放积分
     */
    @Transactional
    public Activity endActivity(Integer activityId, String presidentId) {
        Optional<Activity> activityOpt = activityRepository.findById(activityId);
        if (activityOpt.isEmpty()) {
            throw new RuntimeException("活动不存在");
        }

        Activity activity = activityOpt.get();

        // 验证社长权限
        if (!isPresidentOfActivity(presidentId, activityId)) {
            throw new RuntimeException("无权结束该活动");
        }

        // 只有已发布的活动可以结束
        if (activity.getStatus() != Activity.Status.PUBLISHED) {
            throw new RuntimeException("只有已发布的活动可以结束");
        }

        activity.setStatus(Activity.Status.ENDED);
        Activity endedActivity = activityRepository.save(activity);

        // 为已签到的学生发放积分
        awardPointsForEndedActivity(activityId);

        return endedActivity;
    }

    /**
     * 为结束活动的已签到学生发放积分
     */
    private void awardPointsForEndedActivity(Integer activityId) {
        List<ActivityRegistration> registrations = activityRegistrationRepository.findByActivityId(activityId);
        System.out.println("开始为活动 " + activityId + " 的已签到学生发放积分");

        for (ActivityRegistration registration : registrations) {
            if (registration.getCheckInStatus()) {
                // 检查是否已经发放过积分 - 修复：使用正确的repository和方法
                List<PointsRecord> existingRecords = pointsRecordRepository.findByStudentId(registration.getStudentId())
                        .stream()
                        .filter(record -> record.getActivityId().equals(activityId))
                        .collect(Collectors.toList());

                if (existingRecords.isEmpty()) {
                    // 发放积分
                    Optional<Activity> activityOpt = activityRepository.findById(activityId);
                    if (activityOpt.isPresent()) {
                        Activity activity = activityOpt.get();

                        PointsRecord pointsRecord = new PointsRecord();
                        pointsRecord.setStudentId(registration.getStudentId());
                        pointsRecord.setActivityId(activityId);
                        pointsRecord.setPointsEarned(activity.getAwardPoints());
                        pointsRecordRepository.save(pointsRecord);

                        // 更新学生总积分
                        Optional<Student> studentOpt = studentRepository.findByStudentId(registration.getStudentId());
                        if (studentOpt.isPresent()) {
                            Student student = studentOpt.get();
                            student.setTotalPoints(student.getTotalPoints() + activity.getAwardPoints());
                            studentRepository.save(student);

                            System.out.println("为学生 " + registration.getStudentId() + " 发放积分 " + activity.getAwardPoints());
                        }
                    }
                } else {
                    System.out.println("学生 " + registration.getStudentId() + " 已获得活动 " + activityId + " 的积分");
                }
            } else {
                System.out.println("学生 " + registration.getStudentId() + " 未签到活动 " + activityId);
            }
        }

        System.out.println("活动 " + activityId + " 积分发放完成");
    }

    // 社长查看自己社团的活动
    public List<Activity> getActivitiesByPresident(String presidentId) {
        List<Club> clubs = clubRepository.findByPresidentId(presidentId);
        return clubs.stream()
                .flatMap(club -> activityRepository.findByClubId(club.getClubId()).stream())
                .collect(Collectors.toList());
    }

    // 老师获取待审核活动
    public List<Activity> getPendingActivities() {
        return activityRepository.findByStatus(Activity.Status.PENDING);
    }
    
    // 老师获取已通过活动
    public List<Activity> getApprovedActivities() {
        return activityRepository.findByStatus(Activity.Status.PUBLISHED);
    }
    
    // 老师获取已驳回活动
    public List<Activity> getRejectedActivities() {
        return activityRepository.findByStatus(Activity.Status.REJECTED);
    }

    // 老师审核活动
    public Activity updateActivityStatus(Integer activityId, Activity.Status status, String reason) {
        Optional<Activity> activityOpt = activityRepository.findById(activityId);
        if (activityOpt.isEmpty()) {
            throw new RuntimeException("活动不存在");
        }

        Activity activity = activityOpt.get();
        activity.setStatus(status);

        // 如果是驳回，记录驳回原因
        if (status == Activity.Status.REJECTED) {
            activity.setRejectReason(reason);
        } else {
            // 其他状态清空驳回原因
            activity.setRejectReason(null);
        }

        return activityRepository.save(activity);
    }

    // 学生查看已发布的活动
    public List<Activity> getPublishedActivities() {
        return activityRepository.findByStatus(Activity.Status.PUBLISHED);
    }

    // 学生报名活动
    public ActivityRegistration registerActivity(String studentId, Integer activityId) {
        Optional<Activity> activityOpt = activityRepository.findById(activityId);
        if (activityOpt.isEmpty()) {
            throw new RuntimeException("活动不存在");
        }

        Activity activity = activityOpt.get();

        // 检查活动状态
        if (activity.getStatus() != Activity.Status.PUBLISHED) {
            throw new RuntimeException("活动未发布，无法报名");
        }

        // 检查是否已经报名
        if (activityRegistrationRepository.existsByStudentIdAndActivityId(studentId, activityId)) {
            throw new RuntimeException("您已经报名该活动");
        }

        // 检查人数限制
        long currentParticipants = activityRegistrationRepository.countByActivityId(activityId);
        if (activity.getMaxParticipants() > 0 && currentParticipants >= activity.getMaxParticipants()) {
            throw new RuntimeException("活动人数已满，无法报名");
        }

        // 创建报名记录
        ActivityRegistration registration = new ActivityRegistration();
        registration.setStudentId(studentId);
        registration.setActivityId(activityId);

        return activityRegistrationRepository.save(registration);
    }

    // 学生取消报名
    public void cancelRegistration(String studentId, Integer activityId) {
        Optional<ActivityRegistration> registrationOpt =
                activityRegistrationRepository.findByStudentIdAndActivityId(studentId, activityId);

        if (registrationOpt.isEmpty()) {
            throw new RuntimeException("未找到报名记录");
        }

        ActivityRegistration registration = registrationOpt.get();

        // 检查是否已经签到
        if (registration.getCheckInStatus()) {
            throw new RuntimeException("已签到活动无法取消报名");
        }

        activityRegistrationRepository.delete(registration);
    }

    // 获取学生报名的活动（包含签到状态）
    public List<Map<String, Object>> getStudentRegisteredActivitiesWithCheckIn(String studentId) {
        List<ActivityRegistration> registrations = activityRegistrationRepository.findByStudentId(studentId);
        return registrations.stream()
                .map(registration -> {
                    Optional<Activity> activityOpt = activityRepository.findById(registration.getActivityId());
                    if (activityOpt.isPresent()) {
                        Activity activity = activityOpt.get();
                        Map<String, Object> activityInfo = new HashMap<>();
                        activityInfo.put("activityId", activity.getActivityId());
                        activityInfo.put("activityName", activity.getActivityName());
                        activityInfo.put("activityTime", activity.getActivityTime());
                        activityInfo.put("location", activity.getLocation());
                        activityInfo.put("status", activity.getStatus());
                        activityInfo.put("awardPoints", activity.getAwardPoints());
                        activityInfo.put("checkInStatus", registration.getCheckInStatus());
                        activityInfo.put("registrationTime", registration.getRegistrationTime());
                        // 获取社团名称
                        if (activity.getClubId() != null) {
                            String clubName = clubRepository.findById(activity.getClubId())
                                    .map(Club::getClubName)
                                    .orElse("未知社团");
                            activityInfo.put("clubName", clubName);
                        } else {
                            activityInfo.put("clubName", "未知社团");
                        }
                        return activityInfo;
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // 获取学生报名的活动（仅返回Activity对象，用于其他接口）
    public List<Activity> getStudentRegisteredActivities(String studentId) {
        List<ActivityRegistration> registrations = activityRegistrationRepository.findByStudentId(studentId);
        return registrations.stream()
                .map(registration -> activityRepository.findById(registration.getActivityId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    // 获取活动的报名学生列表
    public List<Student> getActivityRegisteredStudents(Integer activityId) {
        List<ActivityRegistration> registrations = activityRegistrationRepository.findByActivityId(activityId);
        return registrations.stream()
                .map(registration -> studentRepository.findByStudentId(registration.getStudentId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    /**
     * 获取活动的详细信息，包括驳回原因
     */
    public Activity getActivityWithDetails(Integer activityId) {
        Optional<Activity> activityOpt = activityRepository.findById(activityId);
        if (activityOpt.isEmpty()) {
            throw new RuntimeException("活动不存在");
        }
        return activityOpt.get();
    }

    /**
     * 检查社长是否有权限管理该活动
     */
    public boolean isPresidentOfActivity(String presidentId, Integer activityId) {
        Optional<Activity> activityOpt = activityRepository.findById(activityId);
        if (activityOpt.isEmpty()) {
            return false;
        }
        Activity activity = activityOpt.get();
        Optional<Club> clubOpt = clubRepository.findById(activity.getClubId());
        return clubOpt.isPresent() && clubOpt.get().getPresidentId().equals(presidentId);
    }

    /**
     * 获取社长管理的社团活动（包含状态信息）
     */
    public List<Activity> getPresidentActivitiesWithStatus(String presidentId) {
        List<Club> clubs = clubRepository.findByPresidentId(presidentId);
        return clubs.stream()
                .flatMap(club -> activityRepository.findByClubId(club.getClubId()).stream())
                .collect(Collectors.toList());
    }

    /**
     * 获取待审核的活动列表
     */
    public List<Activity> getPendingActivitiesForTeacher() {
        return activityRepository.findByStatus(Activity.Status.PENDING);
    }

    /**
     * 审核活动并更新状态
     */
    public Activity reviewActivity(Integer activityId, Activity.Status status, String reason) {
        Optional<Activity> activityOpt = activityRepository.findById(activityId);
        if (activityOpt.isEmpty()) {
            throw new RuntimeException("活动不存在");
        }

        Activity activity = activityOpt.get();
        activity.setStatus(status);

        if (status == Activity.Status.REJECTED) {
            activity.setRejectReason(reason);
        } else {
            activity.setRejectReason(null);
        }

        return activityRepository.save(activity);
    }

    // 基础方法
    public List<Activity> findAll() {
        return activityRepository.findAll();
    }

    public Optional<Activity> findById(Integer activityId) {
        return activityRepository.findById(activityId);
    }

    public Activity save(Activity activity) {
        return activityRepository.save(activity);
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, rollbackFor = Exception.class)
    public void deleteById(Integer activityId, String userId) {
        // 验证用户是否有权限删除该活动
        Optional<Activity> activityOpt = activityRepository.findById(activityId);
        if (activityOpt.isEmpty()) {
            throw new RuntimeException("活动不存在");
        }
        Activity activity = activityOpt.get();
        
        // 检查用户是否为管理员
        Optional<Student> studentOpt = studentRepository.findByStudentId(userId);
        if (studentOpt.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }
        Student currentUser = studentOpt.get();
        boolean isAdmin = currentUser.getRole() == Student.Role.ADMIN;
        
        // 检查用户是否为社长
        boolean isPresident = isPresidentOfActivity(userId, activityId);
        
        // 只有管理员或社长才能删除活动
        if (!isAdmin && !isPresident) {
            throw new RuntimeException("您没有权限删除该活动");
        }
        
        // 先删除与该活动相关的所有记录
        // 删除积分记录
        pointsRecordRepository.deleteByActivityId(activityId);
        // 删除签到记录
        checkInRepository.deleteByActivityId(activityId);
        // 删除报名记录
        activityRegistrationRepository.deleteByActivityId(activityId);
        // 最后删除活动本身
        activityRepository.delete(activity);
    }

    public List<Activity> findByClubId(Integer clubId) {
        return activityRepository.findByClubId(clubId);
    }

    public List<Activity> findByStatus(Activity.Status status) {
        return activityRepository.findByStatus(status);
    }

    public List<Activity> findByClubIdAndStatus(Integer clubId, Activity.Status status) {
        return activityRepository.findByClubIdAndStatus(clubId, status);
    }

    /**
     * 根据活动码获取活动信息
     */
    public Activity getActivityByCode(String code) {
        return activityRepository.findByCode(code);
    }



    /**
     * 根据签到数获取活动信息
     */
    public Activity findByCheckInNumber(String checkInNumber) {
        return activityRepository.findByCheckInNumber(checkInNumber);
    }

    // 获取活动的报名学生列表
    public List<ActivityRegistration> getActivityParticipants(Integer activityId) {
        return activityRegistrationRepository.findByActivityId(activityId);
    }

    // 获取活动的报名学生列表（包含学生详细信息）
    public List<Map<String, Object>> getActivityParticipantsWithDetails(Integer activityId) {
        System.out.println("获取活动 " + activityId + " 的报名学生列表");
        List<ActivityRegistration> registrations = activityRegistrationRepository.findByActivityId(activityId);
        System.out.println("找到 " + registrations.size() + " 条报名记录");
        return registrations.stream().map(registration -> {
            Map<String, Object> participant = new HashMap<>();
            participant.put("registrationId", registration.getRegistrationId());
            participant.put("studentId", registration.getStudentId());
            participant.put("activityId", registration.getActivityId());
            participant.put("registrationTime", registration.getRegistrationTime());
            participant.put("checkInStatus", registration.getCheckInStatus());
            participant.put("checkInTime", registration.getCheckInTime());
            participant.put("checkInMethod", registration.getCheckInMethod() != null ? registration.getCheckInMethod().toString() : null);

            // 获取学生详细信息
            try {
                System.out.println("获取学生 " + registration.getStudentId() + " 的详细信息");
                Student student = studentRepository.findByStudentId(registration.getStudentId()).orElse(null);
                if (student != null) {
                    System.out.println("找到学生信息: " + student.getName());
                    participant.put("name", student.getName());
                    participant.put("college", student.getCollege());
                    participant.put("majorClass", student.getMajorClass());
                    participant.put("phone", student.getPhone());
                    participant.put("email", student.getEmail());
                    participant.put("totalPoints", student.getTotalPoints());
                } else {
                    System.out.println("未找到学生 " + registration.getStudentId() + " 的信息");
                    participant.put("name", "未知姓名");
                    participant.put("college", "未填写");
                    participant.put("majorClass", "未填写");
                    participant.put("phone", "未填写");
                    participant.put("email", "未填写");
                    participant.put("totalPoints", 0);
                }
            } catch (Exception e) {
                System.out.println("获取学生 " + registration.getStudentId() + " 信息时出错: " + e.getMessage());
                participant.put("name", "未知姓名");
                participant.put("college", "未填写");
                participant.put("majorClass", "未填写");
                participant.put("phone", "未填写");
                participant.put("email", "未填写");
                participant.put("totalPoints", 0);
            }

            return participant;
        }).collect(Collectors.toList());
    }
}