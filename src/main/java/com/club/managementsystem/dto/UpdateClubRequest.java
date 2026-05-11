package com.club.managementsystem.dto;

import lombok.Data;

@Data
public class UpdateClubRequest {
    private String clubName;
    private String introduction;
    private String clubType;
    private String contactInfo;
    private String logoUrl;
    private String status;
}