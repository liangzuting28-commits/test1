package com.club.managementsystem.dao.impl;

import com.club.managementsystem.dao.SystemConfigDao;
import com.club.managementsystem.entity.SystemConfig;
import com.club.managementsystem.repository.SystemConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class SystemConfigDaoImpl implements SystemConfigDao {

    @Autowired
    private SystemConfigRepository repository;

    @Override
    public SystemConfig save(SystemConfig config) {
        return repository.save(config);
    }

    @Override
    public Optional<SystemConfig> findById(Integer id) {
        return repository.findById(id);
    }

    @Override
    public List<SystemConfig> findAll() {
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
    public Optional<SystemConfig> findByConfigKey(String configKey) {
        return repository.findAll().stream()
                .filter(c -> configKey.equals(c.getConfigKey()))
                .findFirst();
    }
}
