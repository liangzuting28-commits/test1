package com.club.managementsystem.controller;

import com.club.managementsystem.dto.ApiResponse;
import com.club.managementsystem.dto.LoginRequest;
import com.club.managementsystem.dto.LoginResponse;
import com.club.managementsystem.dto.RegisterRequest;
import com.club.managementsystem.entity.Student;
import com.club.managementsystem.repository.StudentRepository;
import com.club.managementsystem.service.AuthService;
import com.club.managementsystem.util.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final StudentRepository studentRepository;

    public AuthController(AuthService authService, JwtUtil jwtUtil, StudentRepository studentRepository) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
        this.studentRepository = studentRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest,
                                                            HttpServletResponse response) {
        LoginResponse loginResponse = authService.login(loginRequest);
        if (loginResponse.isSuccess()) {
            // 根据 autoLogin 参数决定 cookie 有效期
            boolean autoLogin = loginRequest.getAutoLogin() == null || loginRequest.getAutoLogin();
            long maxAge = autoLogin ? 7 * 24 * 60 * 60 : -1; // 7天 或 浏览器会话结束

            ResponseCookie cookie = ResponseCookie.from("jwt_token", loginResponse.getToken())
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(maxAge)
                    .sameSite("Lax")
                    .build();
            response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());

            return ResponseEntity.ok(ApiResponse.success("登录成功", loginResponse));
        } else {
            ApiResponse<LoginResponse> errorResponse = new ApiResponse<>();
            errorResponse.setSuccess(false);
            errorResponse.setMessage(loginResponse.getMessage());
            errorResponse.setData(null);
            return ResponseEntity.ok(errorResponse);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest registerRequest) {
        LoginResponse registerResponse = authService.register(registerRequest);
        if (registerResponse.isSuccess()) {
            return ResponseEntity.ok(ApiResponse.success(registerResponse.getMessage(), null));
        } else {
            ApiResponse<String> errorResponse = new ApiResponse<>();
            errorResponse.setSuccess(false);
            errorResponse.setMessage(registerResponse.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("jwt_token", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok(ApiResponse.success("退出成功", null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<LoginResponse.UserInfo>> me(@CookieValue(value = "jwt_token", required = false) String token) {
        if (token == null || !jwtUtil.validateToken(token)) {
            ApiResponse<LoginResponse.UserInfo> err = new ApiResponse<>();
            err.setSuccess(false);
            err.setMessage("未登录");
            return ResponseEntity.ok(err);
        }

        String studentId = jwtUtil.getStudentIdFromToken(token);
        Optional<Student> studentOpt = studentRepository.findByStudentId(studentId);
        if (studentOpt.isEmpty()) {
            ApiResponse<LoginResponse.UserInfo> err = new ApiResponse<>();
            err.setSuccess(false);
            err.setMessage("用户不存在");
            return ResponseEntity.ok(err);
        }

        return ResponseEntity.ok(ApiResponse.success("已登录", LoginResponse.UserInfo.fromStudent(studentOpt.get())));
    }

    @GetMapping("/test")
    public ResponseEntity<ApiResponse<String>> test() {
        return ResponseEntity.ok(ApiResponse.success("后端服务运行正常", null));
    }
}
