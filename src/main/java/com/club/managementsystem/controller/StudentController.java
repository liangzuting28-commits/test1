package com.club.managementsystem.controller;

import com.club.managementsystem.dto.ApiResponse;
import com.club.managementsystem.entity.Student;
import com.club.managementsystem.service.StudentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "*")
public class StudentController {
    private final StudentService studentService;
    
    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Student>>> getAllStudents() {
        List<Student> students = studentService.findAll();
        return ResponseEntity.ok(ApiResponse.success(students));
    }

    @GetMapping("/{studentId}")
    public ResponseEntity<ApiResponse<Student>> getStudentDetails(@PathVariable String studentId) {
        try {
            Student student = studentService.getStudentById(studentId);
            if (student != null) {
                return ResponseEntity.ok(ApiResponse.success(student));
            }
            return ResponseEntity.ok((ApiResponse<Student>) ApiResponse.error("未找到该学生信息"));
        } catch (Exception e) {
            return ResponseEntity.ok((ApiResponse<Student>) ApiResponse.error("获取学生信息失败：" + e.getMessage()));
        }
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<Student>> addStudent(@RequestBody Student student) {
        try {
            // 这里应该添加权限检查，确保只有管理员才能添加用户
            // 由于系统使用简化的认证机制，暂时先注释掉权限检查
            // 实际项目中应该从token中获取当前用户信息并检查角色
            
            // 详细的参数验证
            if (student == null) {
                return ResponseEntity.ok((ApiResponse<Student>) ApiResponse.error("请求参数不能为空"));
            }
            
            if (student.getStudentId() == null || student.getStudentId().trim().isEmpty()) {
                return ResponseEntity.ok((ApiResponse<Student>) ApiResponse.error("用户ID不能为空"));
            }
            
            if (student.getStudentId().trim().length() > 20) {
                return ResponseEntity.ok((ApiResponse<Student>) ApiResponse.error("用户ID长度不能超过20个字符"));
            }
            
            if (student.getName() == null || student.getName().trim().isEmpty()) {
                return ResponseEntity.ok((ApiResponse<Student>) ApiResponse.error("用户姓名不能为空"));
            }
            
            if (student.getName().trim().length() > 20) {
                return ResponseEntity.ok((ApiResponse<Student>) ApiResponse.error("用户姓名长度不能超过20个字符"));
            }
            
            if (student.getRole() == null) {
                return ResponseEntity.ok((ApiResponse<Student>) ApiResponse.error("用户角色不能为空"));
            }
            
            if (student.getPassword() == null || student.getPassword().trim().isEmpty()) {
                return ResponseEntity.ok((ApiResponse<Student>) ApiResponse.error("用户密码不能为空"));
            }
            
            if (student.getPassword().trim().length() < 6) {
                return ResponseEntity.ok((ApiResponse<Student>) ApiResponse.error("用户密码长度不能少于6个字符"));
            }
            
            // 检查用户是否已存在
            if (studentService.existsByStudentId(student.getStudentId())) {
                return ResponseEntity.ok((ApiResponse<Student>) ApiResponse.error("用户ID已存在"));
            }
            
            // 设置创建时间
            student.setCreatedAt(LocalDateTime.now());
            
            // 保存新用户
            Student savedStudent = studentService.save(student);
            return ResponseEntity.ok(ApiResponse.success("用户添加成功", savedStudent));
        } catch (jakarta.persistence.PersistenceException e) {
            // 数据库持久化异常处理
            String errorMessage = "添加用户失败：数据库操作错误";
            if (e.getMessage().contains("duplicate key")) {
                errorMessage = "添加用户失败：用户ID已存在";
            } else if (e.getMessage().contains("not-null constraint")) {
                errorMessage = "添加用户失败：必填字段不能为空";
            }
            return ResponseEntity.ok((ApiResponse<Student>) ApiResponse.error(errorMessage));
        } catch (IllegalArgumentException e) {
            // 参数验证异常处理
            return ResponseEntity.ok((ApiResponse<Student>) ApiResponse.error("添加用户失败：参数错误：" + e.getMessage()));
        } catch (Exception e) {
            // 其他异常处理
            return ResponseEntity.ok((ApiResponse<Student>) ApiResponse.error("添加用户失败：" + e.getMessage()));
        }
    }
    
    @DeleteMapping("/{studentId}")
    public ResponseEntity<ApiResponse<Void>> deleteStudent(@PathVariable String studentId, 
                                                           @RequestHeader("X-User-Id") String operatorId) {
        try {
            // 检查操作员是否为管理员
            java.util.Optional<Student> operatorOpt = studentService.findByStudentId(operatorId);
            if (!operatorOpt.isPresent()) {
                return ResponseEntity.ok((ApiResponse<Void>) ApiResponse.error("操作员不存在"));
            }
            
            Student operator = operatorOpt.get();
            if (operator.getRole() != Student.Role.ADMIN) {
                return ResponseEntity.ok((ApiResponse<Void>) ApiResponse.error("权限不足：仅管理员可删除用户"));
            }
            
            // 检查目标用户是否存在
            if (!studentService.existsByStudentId(studentId)) {
                return ResponseEntity.ok((ApiResponse<Void>) ApiResponse.error("用户不存在"));
            }
            
            // 删除用户
            studentService.deleteById(studentId);
            return ResponseEntity.ok(ApiResponse.success("用户删除成功", null));
        } catch (RuntimeException e) {
            // 处理运行时异常，显示具体错误信息
            return ResponseEntity.ok((ApiResponse<Void>) ApiResponse.error("删除用户失败：" + e.getMessage()));
        } catch (Exception e) {
            // 处理其他未知异常
            return ResponseEntity.ok((ApiResponse<Void>) ApiResponse.error("删除用户失败：系统错误，请联系管理员"));
        }
    }
}