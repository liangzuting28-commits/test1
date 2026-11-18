package com.club.managementsystem.dao;

import com.club.managementsystem.entity.SystemConfig;
import java.util.List;
import java.util.Optional;

/**
 * SystemConfig DAO接口 - 定义系统配置数据访问方法
 */
public interface SystemConfigDao {

    SystemConfig save(SystemConfig config);

    Optional<SystemConfig> findById(Integer id);

    List<SystemConfig> findAll();

    void deleteById(Integer id);

    boolean existsById(Integer id);

    Optional<SystemConfig> findByConfigKey(String configKey);
}
