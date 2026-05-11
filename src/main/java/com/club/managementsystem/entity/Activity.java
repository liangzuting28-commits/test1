package com.club.managementsystem.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "activities")
@Data
public class Activity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "activity_id")
    private Integer activityId;

    @Column(name = "activity_name", nullable = false, length = 100)
    private String activityName;

    @Column(name = "club_id")
    private Integer clubId;

    @Column(name = "activity_time")
    private LocalDateTime activityTime;

    @Column(name = "location", length = 100)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private Status status;

    @Column(name = "max_participants")
    private Integer maxParticipants;

    @Column(name = "award_points")
    private Integer awardPoints;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "qr_code", length = 255)
    private String qrCode;

    @Column(name = "code", length = 50)
    private String code;

    @Column(name = "check_in_number", length = 6)
    private String checkInNumber;

    @Column(name = "reject_reason", length = 500)
    private String rejectReason;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
    // 在 Activity 实体中添加活动类型字段（如果还没有）
    @Column(name = "activity_type", length = 20)
    private String activityType;

    public enum Status {
        PENDING,       // 待审核
        REJECTED,      // 已驳回
        PUBLISHED,     // 已发布
        REGISTERING,   // 报名中
        ENDED,         // 已结束
        CANCELLED      // 已取消
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = Status.PENDING; // 默认状态为待审核
        }
    }
}