package com.club.managementsystem.repository;

import com.club.managementsystem.entity.PointsRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PointsRecordRepository extends JpaRepository<PointsRecord, Integer> {
    List<PointsRecord> findByStudentId(String studentId);
    List<PointsRecord> findByActivityId(Integer activityId);
    
    /**
     * 检查学生是否已经因某个活动获得积分
     * @param studentId 学生ID
     * @param activityId 活动ID
     * @return 积分记录
     */
    PointsRecord findByStudentIdAndActivityId(String studentId, Integer activityId);
    
    /**
     * 根据活动ID删除积分记录
     * @param activityId 活动ID
     */
    void deleteByActivityId(Integer activityId);
}