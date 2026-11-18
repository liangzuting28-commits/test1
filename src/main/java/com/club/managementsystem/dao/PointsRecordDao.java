package com.club.managementsystem.dao;

import com.club.managementsystem.entity.PointsRecord;
import java.util.List;
import java.util.Optional;

/**
 * PointsRecord DAO接口 - 定义积分记录数据访问方法
 */
public interface PointsRecordDao {

    PointsRecord save(PointsRecord record);

    Optional<PointsRecord> findById(Integer id);

    List<PointsRecord> findAll();

    void deleteById(Integer id);

    boolean existsById(Integer id);

    List<PointsRecord> findByStudentId(String studentId);

    List<PointsRecord> findByActivityId(Integer activityId);
}
