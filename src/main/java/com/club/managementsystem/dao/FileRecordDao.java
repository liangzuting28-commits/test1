package com.club.managementsystem.dao;

import com.club.managementsystem.entity.FileRecord;
import java.util.List;
import java.util.Optional;

/**
 * FileRecord DAO接口 - 定义文件记录数据访问方法
 */
public interface FileRecordDao {

    FileRecord save(FileRecord record);

    Optional<FileRecord> findById(Long id);

    List<FileRecord> findAll();

    void deleteById(Long id);

    boolean existsById(Long id);

    List<FileRecord> findByUploaderId(String uploaderId);
}
