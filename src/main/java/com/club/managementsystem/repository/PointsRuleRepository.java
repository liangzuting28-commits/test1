package com.club.managementsystem.repository;

import com.club.managementsystem.entity.PointsRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PointsRuleRepository extends JpaRepository<PointsRule, Integer> {
    Optional<PointsRule> findByActivityType(String activityType);
}