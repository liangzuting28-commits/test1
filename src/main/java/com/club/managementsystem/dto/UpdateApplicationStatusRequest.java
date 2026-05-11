package com.club.managementsystem.dto;

import lombok.Data;

@Data
public class UpdateApplicationStatusRequest {
    private String status; // APPROVED or REJECTED
    private String reason; // 拒绝原因
}