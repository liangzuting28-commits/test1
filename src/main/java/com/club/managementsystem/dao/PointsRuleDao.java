package com.club.managementsystem.dao;

import com.club.managementsystem.entity.PointsRule;
import java.util.List;
import java.util.Optional;

/**
 * PointsRule DAO接口 - 定义积分规则数据访问方法
 */
public interface PointsRuleDao {

    PointsRule save(PointsRule rule);

    Optional<PointsRule> findById(Integer id);

    List<PointsRule> findAll();

    void deleteById(Integer id);

    boolean existsById(Integer id);

    Optional<PointsRule> findByActivityType(String activityType);
}
