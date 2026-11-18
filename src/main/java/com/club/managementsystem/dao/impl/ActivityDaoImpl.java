package com.club.managementsystem.dao.impl;

import com.club.managementsystem.dao.ActivityDao;
import com.club.managementsystem.entity.Activity;
import com.club.managementsystem.repository.ActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Activity DAO实现类 - 实现活动数据访问逻辑
 */
@Repository
public class ActivityDaoImpl implements ActivityDao {

    @Autowired
    private ActivityRepository activityRepository;

    @Override
    public Activity save(Activity activity) {
        return activityRepository.save(activity);
    }

    @Override
    public Optional<Activity> findById(Integer id) {
        return activityRepository.findById(id);
    }

    @Override
    public List<Activity> findAll() {
        return activityRepository.findAll();
    }

    @Override
    public void deleteById(Integer id) {
        activityRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Integer id) {
        return activityRepository.existsById(id);
    }

    @Override
    public List<Activity> findByStatus(Activity.Status status) {
        return activityRepository.findAll().stream()
                .filter(a -> a.getStatus() == status)
                .toList();
    }

    @Override
    public List<Activity> findByClubId(Integer clubId) {
        return activityRepository.findAll().stream()
                .filter(a -> clubId.equals(a.getClubId()))
                .toList();
    }
}
