package com.club.managementsystem.dao.impl;

import com.club.managementsystem.dao.ActivityRegistrationDao;
import com.club.managementsystem.entity.ActivityRegistration;
import com.club.managementsystem.repository.ActivityRegistrationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ActivityRegistrationDaoImpl implements ActivityRegistrationDao {

    @Autowired
    private ActivityRegistrationRepository repository;

    @Override
    public ActivityRegistration save(ActivityRegistration registration) {
        return repository.save(registration);
    }

    @Override
    public Optional<ActivityRegistration> findById(Integer id) {
        return repository.findById(id);
    }

    @Override
    public List<ActivityRegistration> findAll() {
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
    public List<ActivityRegistration> findByStudentId(String studentId) {
        return repository.findAll().stream()
                .filter(r -> studentId.equals(r.getStudentId()))
                .toList();
    }

    @Override
    public List<ActivityRegistration> findByActivityId(Integer activityId) {
        return repository.findAll().stream()
                .filter(r -> activityId.equals(r.getActivityId()))
                .toList();
    }

    @Override
    public List<ActivityRegistration> findByStatus(ActivityRegistration.RegistrationStatus status) {
        return repository.findAll().stream()
                .filter(r -> status.equals(r.getStatus()))
                .toList();
    }
}
