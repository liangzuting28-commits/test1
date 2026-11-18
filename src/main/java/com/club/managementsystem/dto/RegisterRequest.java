package com.club.managementsystem.dto;

import com.club.managementsystem.entity.Student;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "学号/工号不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码长度不能少于6位")
    private String password;

    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    @NotBlank(message = "姓名不能为空")
    private String name;

    private String role;  // 可选，默认 STUDENT

    private String college;
    private String majorClass;

    public Student.Role getRoleEnum() {
        if (role == null || role.isBlank()) {
            return Student.Role.STUDENT;
        }
        try {
            return Student.Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
