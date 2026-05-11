package com.club.managementsystem.service;

import com.club.managementsystem.entity.Student;
import com.club.managementsystem.entity.Club;
import com.club.managementsystem.entity.ActivityRegistration;
import com.club.managementsystem.entity.CheckIn;
import com.club.managementsystem.entity.PointsRecord;
import com.club.managementsystem.entity.ClubMembership;
import com.club.managementsystem.repository.StudentRepository;
import com.club.managementsystem.repository.ClubMembershipRepository;
import com.club.managementsystem.repository.CheckInRepository;
import com.club.managementsystem.repository.PointsRecordRepository;
import com.club.managementsystem.repository.ActivityRegistrationRepository;
import com.club.managementsystem.repository.ClubRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class StudentService {
    private final StudentRepository studentRepository;
    private final ClubMembershipRepository clubMembershipRepository;
    private final CheckInRepository checkInRepository;
    private final PointsRecordRepository pointsRecordRepository;
    private final ActivityRegistrationRepository activityRegistrationRepository;
    private final ClubRepository clubRepository;
    
    public StudentService(StudentRepository studentRepository, 
                         ClubMembershipRepository clubMembershipRepository,
                         CheckInRepository checkInRepository,
                         PointsRecordRepository pointsRecordRepository,
                         ActivityRegistrationRepository activityRegistrationRepository,
                         ClubRepository clubRepository) {
        this.studentRepository = studentRepository;
        this.clubMembershipRepository = clubMembershipRepository;
        this.checkInRepository = checkInRepository;
        this.pointsRecordRepository = pointsRecordRepository;
        this.activityRegistrationRepository = activityRegistrationRepository;
        this.clubRepository = clubRepository;
    }

    public List<Student> findAll() {
        return studentRepository.findAll();
    }

    public Optional<Student> findById(String studentId) {
        return studentRepository.findById(studentId);
    }

    public Optional<Student> findByStudentId(String studentId) {
        return studentRepository.findByStudentId(studentId);
    }

    public Student getStudentById(String studentId) {
        return studentRepository.findById(studentId).orElse(null);
    }

    public Student save(Student student) {
        return studentRepository.save(student);
    }

    @Transactional
    public void deleteById(String studentId) {
        // 检查学生是否存在
        if (!studentRepository.existsById(studentId)) {
            throw new RuntimeException("用户不存在");
        }
        
        // 级联删除所有与该用户相关的记录
        try {
            // 1. 处理社团社长信息：如果该用户是社团社长，将presidentId设为null
            List<Club> clubs = clubRepository.findByPresidentId(studentId);
            for (Club club : clubs) {
                club.setPresidentId(null);
                clubRepository.save(club);
            }
            
            // 2. 删除活动报名记录
            List<ActivityRegistration> activities = activityRegistrationRepository.findByStudentId(studentId);
            activityRegistrationRepository.deleteAll(activities);
            
            // 3. 删除签到记录
            List<CheckIn> checkIns = checkInRepository.findByStudentId(studentId);
            checkInRepository.deleteAll(checkIns);
            
            // 4. 删除积分记录
            List<PointsRecord> pointsRecords = pointsRecordRepository.findByStudentId(studentId);
            pointsRecordRepository.deleteAll(pointsRecords);
            
            // 5. 删除社团成员关系
            List<ClubMembership> memberships = clubMembershipRepository.findByStudentId(studentId);
            clubMembershipRepository.deleteAll(memberships);
            
            // 6. 最后删除用户
            studentRepository.deleteById(studentId);
            
        } catch (jakarta.persistence.PersistenceException e) {
            // 数据库持久化异常处理
            if (e.getMessage().contains("foreign key constraint")) {
                throw new RuntimeException("删除用户失败：存在关联记录未删除", e);
            } else {
                throw new RuntimeException("删除用户失败：数据库操作错误", e);
            }
        } catch (Exception e) {
            // 其他异常处理
            throw new RuntimeException("删除用户失败：" + e.getMessage(), e);
        }
    }

/**
 * 根据学生ID判断学生是否存在
 * @param studentId 学生ID
 * @return 如果存在返回true，不存在返回false
 */
    public boolean existsByStudentId(String studentId) {
    // 调用studentRepository的方法查询是否存在该学生ID
        return studentRepository.existsByStudentId(studentId);
    }

    public List<Student> findByRole(Student.Role role) {
        return studentRepository.findAll().stream()
                .filter(student -> student.getRole() == role)
                .collect(java.util.stream.Collectors.toList());
    }
}