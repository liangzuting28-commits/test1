package com.club.managementsystem.dao;

import com.club.managementsystem.entity.CheckIn;
import java.util.List;
import java.util.Optional;

/**
 * CheckIn DAO接口 - 定义签到记录数据访问方法
 */
public interface CheckInDao {

    CheckIn save(CheckIn checkIn);

    Optional<CheckIn> findById(Integer id);

    List<CheckIn> findAll();

    void deleteById(Integer id);

    boolean existsById(Integer id);

    List<CheckIn> findByStudentId(String studentId);

    List<CheckIn> findByActivityId(Integer activityId);
}
