package com.club.managementsystem.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ClubDTO {
    private Integer clubId;
    private String clubName;
    private String introduction;
    private String clubType;
    private LocalDateTime establishDate;
    private String presidentId;
    private String presidentName;
    private LocalDateTime createdAt;
    private String createdBy;
    private String status;
    private String logoUrl;
    private String contactInfo;
    private Integer memberCount;
    private Integer activityCount;
    private MemberDetailDTO presidentDetail; // 社长详细信息
    private List<MemberDetailDTO> members; // 社团成员列表
}