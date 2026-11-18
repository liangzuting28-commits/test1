package com.club.managementsystem.dao.impl;

import com.club.managementsystem.dao.FileRecordDao;
import com.club.managementsystem.entity.FileRecord;
import com.club.managementsystem.repository.FileRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class FileRecordDaoImpl implements FileRecordDao {

    @Autowired
    private FileRecordRepository repository;

    @Override
    public FileRecord save(FileRecord record) {
        return repository.save(record);
    }

    @Override
    public Optional<FileRecord> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<FileRecord> findAll() {
        return repository.findAll();
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return repository.existsById(id);
    }

    @Override
    public List<FileRecord> findByUploaderId(String uploaderId) {
        return repository.findAll().stream()
                .filter(f -> uploaderId.equals(f.getUploaderId()))
                .toList();
    }
}
