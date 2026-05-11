package com.club.managementsystem.dto;

import com.club.managementsystem.entity.Student;
import lombok.Data;

@Data
public class LoginResponse {
    private boolean success;
    private String message;
    private String token;
    private UserInfo user;

    @Data
    public static class UserInfo {
        private String studentId;
        private String name;
        private String role;
        private String college;
        private String majorClass;
        private Integer totalPoints;

        public static UserInfo fromStudent(Student student) {
            UserInfo info = new UserInfo();
            info.setStudentId(student.getStudentId());
            info.setName(student.getName());
            info.setRole(student.getRole().name());
            info.setCollege(student.getCollege());
            info.setMajorClass(student.getMajorClass());
            info.setTotalPoints(student.getTotalPoints());
            return info;
        }
    }
}