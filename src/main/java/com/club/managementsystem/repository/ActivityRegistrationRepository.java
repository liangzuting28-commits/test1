package com.club.managementsystem.repository;

import com.club.managementsystem.entity.ActivityRegistration;
import com.club.managementsystem.entity.ActivityRegistration.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ActivityRegistrationRepository extends JpaRepository<ActivityRegistration, Integer> {
    List<ActivityRegistration> findByStudentId(String studentId);
    List<ActivityRegistration> findByActivityId(Integer activityId);
    Optional<ActivityRegistration> findByStudentIdAndActivityId(String studentId, Integer activityId);
    boolean existsByStudentIdAndActivityId(String studentId, Integer activityId);
    long countByActivityId(Integer activityId);
    long countByActivityIdAndStatus(Integer activityId, RegistrationStatus status);
    
    /**
     * 根据活动ID删除报名记录
     * @param activityId 活动ID
     */
    void deleteByActivityId(Integer activityId);
}