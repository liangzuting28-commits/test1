package com.club.managementsystem.dao;

import com.club.managementsystem.entity.ActivityRegistration;
import java.util.List;
import java.util.Optional;

/**
 * ActivityRegistration DAO接口 - 定义活动报名数据访问方法
 */
public interface ActivityRegistrationDao {

    ActivityRegistration save(ActivityRegistration registration);

    Optional<ActivityRegistration> findById(Integer id);

    List<ActivityRegistration> findAll();

    void deleteById(Integer id);

    boolean existsById(Integer id);

    List<ActivityRegistration> findByStudentId(String studentId);

    List<ActivityRegistration> findByActivityId(Integer activityId);

    List<ActivityRegistration> findByStatus(ActivityRegistration.RegistrationStatus status);
}
