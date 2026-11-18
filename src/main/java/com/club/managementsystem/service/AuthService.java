package com.club.managementsystem.service;

import com.club.managementsystem.dto.LoginRequest;
import com.club.managementsystem.dto.LoginResponse;
import com.club.managementsystem.dto.RegisterRequest;
import com.club.managementsystem.entity.Student;
import com.club.managementsystem.repository.StudentRepository;
import com.club.managementsystem.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthService {
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(StudentRepository studentRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
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

        // 验证密码（BCrypt 加密）
        if (!passwordEncoder.matches(loginRequest.getPassword(), student.getPassword())) {
            response.setSuccess(false);
            response.setMessage("密码错误");
            return response;
        }

        // 生成 JWT token
        String token = jwtUtil.generateToken(student.getStudentId(), student.getName(), student.getRole().name());
        response.setSuccess(true);
        response.setMessage("登录成功");
        response.setToken(token);
        response.setUser(LoginResponse.UserInfo.fromStudent(student));
        return response;
    }

    public LoginResponse register(RegisterRequest request) {
        LoginResponse response = new LoginResponse();

        // 验证两次密码是否一致
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            response.setSuccess(false);
            response.setMessage("两次输入的密码不一致");
            return response;
        }

        // 验证密码长度
        if (request.getPassword().length() < 6) {
            response.setSuccess(false);
            response.setMessage("密码长度不能少于6位");
            return response;
        }

        // 验证角色
        Student.Role role = request.getRoleEnum();
        if (role == null) {
            response.setSuccess(false);
            response.setMessage("无效的角色类型");
            return response;
        }

        // 检查账号是否已存在
        if (studentRepository.existsByStudentId(request.getUsername())) {
            response.setSuccess(false);
            response.setMessage("该账号已注册");
            return response;
        }

        // 创建新用户
        Student student = new Student();
        student.setStudentId(request.getUsername());
        student.setName(request.getName());
        student.setPassword(passwordEncoder.encode(request.getPassword()));
        student.setRole(role);
        student.setCollege(request.getCollege());
        student.setMajorClass(request.getMajorClass());
        student.setTotalPoints(0);
        student.setCreatedAt(LocalDateTime.now());

        studentRepository.save(student);

        response.setSuccess(true);
        response.setMessage("注册成功");
        return response;
    }
}
