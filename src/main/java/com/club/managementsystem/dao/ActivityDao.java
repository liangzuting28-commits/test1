package com.club.managementsystem.dao;

import com.club.managementsystem.entity.Activity;
import java.util.List;
import java.util.Optional;

/**
 * Activity DAO接口 - 定义活动数据访问方法
 */
public interface ActivityDao {

    /**
     * 保存活动（新增或更新）
     */
    Activity save(Activity activity);

    /**
     * 根据ID查找活动
     */
    Optional<Activity> findById(Integer id);

    /**
     * 查找所有活动
     */
    List<Activity> findAll();

    /**
     * 根据ID删除活动
     */
    void deleteById(Integer id);

    /**
     * 检查活动是否存在
     */
    boolean existsById(Integer id);

    /**
     * 根据状态查找活动
     */
    List<Activity> findByStatus(Activity.Status status);

    /**
     * 根据社团ID查找活动
     */
    List<Activity> findByClubId(Integer clubId);
}
