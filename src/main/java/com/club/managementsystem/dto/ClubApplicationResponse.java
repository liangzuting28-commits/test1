package com.club.managementsystem.dto;

import lombok.Data;

@Data
public class ClubApplicationResponse {
    private Integer membershipId;
    private String studentId;
    private String studentName;
    private Integer clubId;
    private String clubName;
    private String applicationStatus;
    private String message;
    private String applyTime;
}