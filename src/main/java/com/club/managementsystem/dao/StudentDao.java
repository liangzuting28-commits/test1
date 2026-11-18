package com.club.managementsystem.dao;

import com.club.managementsystem.entity.Student;
import java.util.List;
import java.util.Optional;

/**
 * Student DAO接口 - 定义数据访问方法
 */
public interface StudentDao {

    /**
     * 保存学生（新增或更新）
     */
    Student save(Student student);

    /**
     * 根据学号查找学生
     */
    Optional<Student> findById(String studentId);

    /**
     * 查找所有学生
     */
    List<Student> findAll();

    /**
     * 根据学号删除学生
     */
    void deleteById(String studentId);

    /**
     * 检查学生是否存在
     */
    boolean existsById(String studentId);

    /**
     * 根据姓名查找学生
     */
    List<Student> findByName(String name);

    /**
     * 根据角色查找学生
     */
    List<Student> findByRole(Student.Role role);
}
