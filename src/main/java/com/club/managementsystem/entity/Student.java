package com.club.managementsystem.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "students")
@Data
public class Student {
    @Id
    @Column(name = "student_id", length = 20)
    private String studentId;

    @Column(name = "name", nullable = false, length = 20)
    private String name;

    @Column(name = "college", length = 50)
    private String college;

    @Column(name = "major_class", length = 30)
    private String majorClass;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "total_points")
    private Integer totalPoints = 0;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email", length = 50)
    private String email;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public enum Role {
        STUDENT, PRESIDENT, TEACHER, ADMIN
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}