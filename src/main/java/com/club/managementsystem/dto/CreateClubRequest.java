package com.club.managementsystem.dto;

import lombok.Data;

@Data
public class CreateClubRequest {
    private String clubName;
    private String introduction;
    private String clubType;
    private String contactInfo;
    private String logoUrl;
    private String presidentId; // 添加社长ID字段
}