package com.club.managementsystem.service;

import com.club.managementsystem.dto.LoginRequest;
import com.club.managementsystem.dto.LoginResponse;
import com.club.managementsystem.entity.Student;
import com.club.managementsystem.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class AuthService {
    private final StudentRepository studentRepository;
    
    public AuthService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public LoginResponse login(LoginRequest loginRequest) {
        LoginResponse response = new LoginResponse();

        // 验证角色
        Student.Role role = loginRequest.getRoleEnum();
        if (role == null) {
            response.setSuccess(false);
            response.setMessage("无效的角色类型");
            return response;
        }

        // 查找学生
        Optional<Student> studentOptional = studentRepository.findByStudentId(loginRequest.getUsername());
        if (studentOptional.isEmpty()) {
            response.setSuccess(false);
            response.setMessage("用户不存在");
            return response;
        }

        Student student = studentOptional.get();

        // 验证角色
        if (student.getRole() != role) {
            response.setSuccess(false);
            response.setMessage("角色不匹配");
            return response;
        }

        // 验证密码（简化版，实际应该使用加密密码）
        if (!student.getPassword().equals(loginRequest.getPassword())) {
            response.setSuccess(false);
            response.setMessage("密码错误");
            return response;
        }

        // 生成token（简化版，实际应该使用JWT）
        String token = generateToken(student);
        response.setSuccess(true);
        response.setMessage("登录成功");
        response.setToken(token);
        response.setUser(LoginResponse.UserInfo.fromStudent(student));
        return response;
    }

    private String generateToken(Student student) {
        // 简化版token生成，实际应该使用JWT
        return "token_" + student.getStudentId() + "_" + System.currentTimeMillis();
    }
}