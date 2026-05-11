package com.club.managementsystem.repository;

import com.club.managementsystem.entity.CheckIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CheckInRepository extends JpaRepository<CheckIn, Integer> {
    /**
     * 根据学生ID获取签到记录
     */
    List<CheckIn> findByStudentId(String studentId);

    /**
     * 根据活动ID获取签到记录
     */
    List<CheckIn> findByActivityId(Integer activityId);

    /**
     * 根据活动ID删除签到记录
     * @param activityId 活动ID
     */
    void deleteByActivityId(Integer activityId);

    /**
     * 根据学生ID分页获取签到记录
     */
    List<CheckIn> findByStudentIdOrderByCheckInTimeDesc(String studentId);
}