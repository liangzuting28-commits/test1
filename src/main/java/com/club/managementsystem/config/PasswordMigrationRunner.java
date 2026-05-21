package com.club.managementsystem.config;

import com.club.managementsystem.entity.Student;
import com.club.managementsystem.repository.StudentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(1)
public class PasswordMigrationRunner implements CommandLineRunner {

    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordMigrationRunner(StudentRepository studentRepository, PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        List<Student> students = studentRepository.findAll();
        int migrated = 0;

        for (Student student : students) {
            String password = student.getPassword();
            // BCrypt 哈希以 $2a$ 或 $2b$ 开头，明文密码不会以这些开头
            if (password != null && !password.startsWith("$2a$") && !password.startsWith("$2b$")) {
                student.setPassword(passwordEncoder.encode(password));
                studentRepository.save(student);
                migrated++;
            }
        }

        if (migrated > 0) {
            System.out.println("密码迁移完成：已将 " + migrated + " 个用户的明文密码转为 BCrypt 加密");
        }
    }
}
