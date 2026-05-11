package com.club.managementsystem.dto;

import lombok.Data;

@Data
public class ClubApplicationRequest {
    private Integer clubId;
    private String message; // 申请留言
}