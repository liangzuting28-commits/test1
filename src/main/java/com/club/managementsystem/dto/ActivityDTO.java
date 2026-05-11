package com.club.managementsystem.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ActivityDTO {
    private Integer activityId;
    private String activityName;
    private Integer clubId;
    private String clubName;
    private LocalDateTime activityTime;
    private String location;
    private String status;
    private Integer maxParticipants;
    private Integer awardPoints;
    private String description;
    private Integer currentParticipants;
}