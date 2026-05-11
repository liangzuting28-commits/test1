package com.club.managementsystem.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ActivityRequest {
    private String activityName;
    private Integer clubId;
    private LocalDateTime activityTime;
    private String location;
    private Integer maxParticipants;
    private Integer awardPoints;
    private String description;
    private String activityType; // 添加活动类型字段
}