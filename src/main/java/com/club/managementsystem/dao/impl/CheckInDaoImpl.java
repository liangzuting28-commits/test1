package com.club.managementsystem.dao.impl;

import com.club.managementsystem.dao.CheckInDao;
import com.club.managementsystem.entity.CheckIn;
import com.club.managementsystem.repository.CheckInRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CheckInDaoImpl implements CheckInDao {

    @Autowired
    private CheckInRepository repository;

    @Override
    public CheckIn save(CheckIn checkIn) {
        return repository.save(checkIn);
    }

    @Override
    public Optional<CheckIn> findById(Integer id) {
        return repository.findById(id);
    }

    @Override
    public List<CheckIn> findAll() {
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
    public List<CheckIn> findByStudentId(String studentId) {
        return repository.findAll().stream()
                .filter(c -> studentId.equals(c.getStudentId()))
                .toList();
    }

    @Override
    public List<CheckIn> findByActivityId(Integer activityId) {
        return repository.findAll().stream()
                .filter(c -> activityId.equals(c.getActivityId()))
                .toList();
    }
}
