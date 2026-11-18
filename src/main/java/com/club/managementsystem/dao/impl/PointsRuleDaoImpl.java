package com.club.managementsystem.dao.impl;

import com.club.managementsystem.dao.PointsRuleDao;
import com.club.managementsystem.entity.PointsRule;
import com.club.managementsystem.repository.PointsRuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class PointsRuleDaoImpl implements PointsRuleDao {

    @Autowired
    private PointsRuleRepository repository;

    @Override
    public PointsRule save(PointsRule rule) {
        return repository.save(rule);
    }

    @Override
    public Optional<PointsRule> findById(Integer id) {
        return repository.findById(id);
    }

    @Override
    public List<PointsRule> findAll() {
        return repository.findAll();
    }

    @Override
    public void deleteById(Integer id) {
        repository.deleteById(id);
    }

    @Override
    public boolean existsById(Integer id) {
        return repository.existsById(id);
    }

    @Override
    public Optional<PointsRule> findByActivityType(String activityType) {
        return repository.findAll().stream()
                .filter(r -> activityType.equals(r.getActivityType()))
                .findFirst();
    }
}
