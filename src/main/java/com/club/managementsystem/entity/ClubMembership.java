package com.club.managementsystem.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "club_memberships", 
       uniqueConstraints = {@UniqueConstraint(columnNames = {"student_id", "club_id"})})
@Data
public class ClubMembership {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "membership_id")
    private Integer membershipId;

    @Column(name = "student_id", length = 10)
    private String studentId;

    @Column(name = "club_id")
    private Integer clubId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20)
    private Role role;

    @Column(name = "join_date")
    private LocalDateTime joinDate;

    @Column(name = "status", length = 20)
    private String status = "ACTIVE";

    // 新增申请状态字段
    @Column(name = "application_status", length = 20)
    private String applicationStatus = "PENDING"; // PENDING, APPROVED, REJECTED, REMOVED, PENDING_LEAVE, LEFT

    // 移除原因
    @Column(name = "removal_reason", length = 255)
    private String removalReason;

    // 移除日期
    @Column(name = "removal_date")
    private LocalDateTime removalDate;

    public enum Role {
        MEMBER,       // 普通成员
        MANAGER,      // 社团管理员
        PRESIDENT     // 社长
    }

    @PrePersist
    protected void onCreate() {
        joinDate = LocalDateTime.now();
        if (status == null) {
            status = "ACTIVE";
        }
        if (applicationStatus == null) {
            applicationStatus = "PENDING";
        }
    }
}