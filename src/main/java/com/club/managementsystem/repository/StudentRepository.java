package com.club.managementsystem.repository;

import com.club.managementsystem.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, String> {
    Optional<Student> findByStudentId(String studentId);
    boolean existsByStudentId(String studentId);
    Optional<Student> findByStudentIdAndRole(String studentId, Student.Role role);
}