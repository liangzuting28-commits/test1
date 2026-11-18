package com.club.managementsystem.dao.impl;

import com.club.managementsystem.dao.PointsRecordDao;
import com.club.managementsystem.entity.PointsRecord;
import com.club.managementsystem.repository.PointsRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class PointsRecordDaoImpl implements PointsRecordDao {

    @Autowired
    private PointsRecordRepository repository;

    @Override
    public PointsRecord save(PointsRecord record) {
        return repository.save(record);
    }

    @Override
    public Optional<PointsRecord> findById(Integer id) {
        return repository.findById(id);
    }

    @Override
    public List<PointsRecord> findAll() {
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
    public List<PointsRecord> findByStudentId(String studentId) {
        return repository.findAll().stream()
                .filter(r -> studentId.equals(r.getStudentId()))
                .toList();
    }

    @Override
    public List<PointsRecord> findByActivityId(Integer activityId) {
        return repository.findAll().stream()
                .filter(r -> activityId.equals(r.getActivityId()))
                .toList();
    }
}
