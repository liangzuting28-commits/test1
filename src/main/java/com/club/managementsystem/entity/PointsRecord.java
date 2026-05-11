package com.club.managementsystem.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "points_records")
@Data
public class PointsRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    private Integer recordId;

    @Column(name = "student_id", length = 10)
    private String studentId;

    @Column(name = "activity_id")
    private Integer activityId;

    @Column(name = "points_earned")
    private Integer pointsEarned;

    @Column(name = "earned_time")
    private LocalDateTime earnedTime;

    @PrePersist
    protected void onEarn() {
        earnedTime = LocalDateTime.now();
    }
}