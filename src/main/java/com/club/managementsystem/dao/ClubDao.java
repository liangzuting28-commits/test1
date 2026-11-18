package com.club.managementsystem.dao;

import com.club.managementsystem.entity.Club;
import java.util.List;
import java.util.Optional;

/**
 * Club DAO接口 - 定义社团数据访问方法
 */
public interface ClubDao {

    /**
     * 保存社团（新增或更新）
     */
    Club save(Club club);

    /**
     * 根据ID查找社团
     */
    Optional<Club> findById(Integer id);

    /**
     * 查找所有社团
     */
    List<Club> findAll();

    /**
     * 根据ID删除社团
     */
    void deleteById(Integer id);

    /**
     * 检查社团是否存在
     */
    boolean existsById(Integer id);

    /**
     * 根据名称查找社团
     */
    Optional<Club> findByName(String name);

    /**
     * 根据状态查找社团
     */
    List<Club> findByStatus(String status);
}
