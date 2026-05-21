package com.club.managementsystem.controller;

import com.club.managementsystem.dto.ApiResponse;
import com.club.managementsystem.dto.LoginRequest;
import com.club.managementsystem.dto.LoginResponse;
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
            // 设置 HttpOnly Cookie（自动登录）
            ResponseCookie cookie = ResponseCookie.from("jwt_token", loginResponse.getToken())
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(7 * 24 * 60 * 60)
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
    public ResponseEntity<ApiResponse<LoginResponse.UserInfo>> me(
            @CookieValue(value = "jwt_token", required = false) String cookieToken,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        // 优先从 Cookie 读取，其次从 Authorization header 读取（小程序不支持 Cookie）
        String token = cookieToken;
        if ((token == null || token.isEmpty()) && authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }
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
