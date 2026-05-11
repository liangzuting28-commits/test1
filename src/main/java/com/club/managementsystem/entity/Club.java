package com.club.managementsystem.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "clubs")
@Data
public class Club {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "club_id")
    private Integer clubId;

    @Column(name = "club_name", nullable = false, length = 50)
    private String clubName;

    @Column(name = "introduction", length = 500)
    private String introduction;

    @Column(name = "club_type", length = 20)
    private String clubType;

    @Column(name = "establish_date")
    private LocalDateTime establishDate;

    @Column(name = "president_id", length = 10)
    private String presidentId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 10)
    private String createdBy;

    @Column(name = "status", length = 20)
    private String status = "ACTIVE";

    @Column(name = "logo_url", length = 255)
    private String logoUrl;

    @Column(name = "contact_info", length = 100)
    private String contactInfo;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}