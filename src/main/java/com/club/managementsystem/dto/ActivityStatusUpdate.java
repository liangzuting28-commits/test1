package com.club.managementsystem.dto;

import lombok.Data;

@Data
public class ActivityStatusUpdate {
    private String status;
    private String reason; // 驳回原因
}