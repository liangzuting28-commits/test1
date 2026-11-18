package com.club.managementsystem.dto;

import com.club.managementsystem.entity.Student;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "角色不能为空")
    private String role;

    private Boolean autoLogin; // 是否自动登录（持久化 cookie）

    public Student.Role getRoleEnum() {
        try {
            return Student.Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}