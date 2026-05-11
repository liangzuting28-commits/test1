package com.club.managementsystem.util;

import com.club.managementsystem.entity.Student;
import com.club.managementsystem.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 学生数据更新工具 - 用于更新缺失的手机号和邮箱字段
 */
@Component
@ConditionalOnProperty(name = "app.data-update.enabled", havingValue = "true")
public class StudentDataUpdater implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(StudentDataUpdater.class);

    private final StudentRepository studentRepository;

    public StudentDataUpdater(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Override
    public void run(String... args) {
        logger.info("开始更新学生的手机号和邮箱数据...");

        // 准备要更新的数据
        Map<String, StudentData> studentDataMap = new HashMap<>();
        
        // 社长数据
        studentDataMap.put("2020001001", new StudentData("13800138001", "wang_president@club-system.com"));
        studentDataMap.put("2020002001", new StudentData("13800138002", "liu_president@club-system.com"));
        studentDataMap.put("2020003001", new StudentData("13800138003", "chen_president@club-system.com"));
        studentDataMap.put("2020004001", new StudentData("13800138004", "yang_president@club-system.com"));
        
        // 普通学生数据
        studentDataMap.put("2021001001", new StudentData("13800138005", "zhang_san@club-system.com"));
        studentDataMap.put("2021001002", new StudentData("13800138006", "li_si@club-system.com"));
        
        // 老师数据
        studentDataMap.put("T001", new StudentData("13800138007", "zhang_teacher@club-system.com"));

        // 更新数据
        int updatedCount = 0;
        for (Map.Entry<String, StudentData> entry : studentDataMap.entrySet()) {
            String studentId = entry.getKey();
            StudentData data = entry.getValue();
            
            studentRepository.findByStudentId(studentId).ifPresent(student -> {
                boolean updated = false;
                
                if (student.getPhone() == null || student.getPhone().isEmpty()) {
                    student.setPhone(data.phone);
                    updated = true;
                }
                
                if (student.getEmail() == null || student.getEmail().isEmpty()) {
                    student.setEmail(data.email);
                    updated = true;
                }
                
                if (updated) {
                    studentRepository.save(student);
                    logger.info("已更新学生数据：学号={}, 姓名={}, 电话={}, 邮箱={}", 
                        studentId, student.getName(), student.getPhone(), student.getEmail());
                }
            });
        }

        logger.info("学生数据更新完成！");
    }

    private static class StudentData {
        private final String phone;
        private final String email;

        public StudentData(String phone, String email) {
            this.phone = phone;
            this.email = email;
        }
    }
}
