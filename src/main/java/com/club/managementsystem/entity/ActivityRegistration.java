package com.club.managementsystem.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "activity_registrations")
@Data
public class ActivityRegistration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "registration_id")
    private Integer registrationId;

    @Column(name = "student_id", length = 10)
    private String studentId;

    @Column(name = "activity_id")
    private Integer activityId;

    @Column(name = "registration_time")
    private LocalDateTime registrationTime;

    @Column(name = "check_in_status")
    private Boolean checkInStatus = false;

    @Column(name = "check_in_time")
    private LocalDateTime checkInTime;

    @Column(name = "check_in_method")
    @Enumerated(EnumType.STRING)
    private CheckInMethod checkInMethod;

    // 签到方式枚举
    public enum CheckInMethod {
        QR_CODE,  // 二维码扫码签到
        MANUAL    // 手动签到
    }

    @PrePersist
    protected void onRegistration() {
        registrationTime = LocalDateTime.now();
    }
}