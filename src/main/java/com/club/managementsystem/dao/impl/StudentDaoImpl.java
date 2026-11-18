package com.club.managementsystem.dao.impl;

import com.club.managementsystem.dao.StudentDao;
import com.club.managementsystem.entity.Student;
import com.club.managementsystem.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Student DAO实现类 - 实现具体的数据访问逻辑
 */
@Repository
public class StudentDaoImpl implements StudentDao {

    @Autowired
    private StudentRepository studentRepository;

    @Override
    public Student save(Student student) {
        return studentRepository.save(student);
    }

    @Override
    public Optional<Student> findById(String studentId) {
        return studentRepository.findByStudentId(studentId);
    }

    @Override
    public List<Student> findAll() {
        return studentRepository.findAll();
    }

    @Override
    public void deleteById(String studentId) {
        studentRepository.deleteById(studentId);
    }

    @Override
    public boolean existsById(String studentId) {
        return studentRepository.existsByStudentId(studentId);
    }

    @Override
    public List<Student> findByName(String name) {
        // 使用JPA自定义查询
        return studentRepository.findAll().stream()
                .filter(s -> s.getName().equals(name))
                .toList();
    }

    @Override
    public List<Student> findByRole(Student.Role role) {
        return studentRepository.findAll().stream()
                .filter(s -> s.getRole() == role)
                .toList();
    }
}
