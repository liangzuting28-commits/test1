package com.club.managementsystem.dto;

import lombok.Data;
import com.club.managementsystem.entity.ClubMembership;
import java.time.LocalDateTime;

@Data
public class MemberDetailDTO {
    private String studentId;
    private String name;
    private String college;
    private String majorClass;
    private String phone;
    private String email;
    private ClubMembership.Role role;
    private LocalDateTime joinDate;
    private Integer totalPoints;
}
