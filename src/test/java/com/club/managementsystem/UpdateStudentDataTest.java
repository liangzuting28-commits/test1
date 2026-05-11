package com.club.managementsystem;

import com.club.managementsystem.entity.Student;
import com.club.managementsystem.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
public class UpdateStudentDataTest {

    @Autowired
    private StudentRepository studentRepository;

    @Test
    void updateStudentPhoneAndEmail() {
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
        System.out.println("开始更新学生的手机号和邮箱...");
        
        for (Map.Entry<String, StudentData> entry : studentDataMap.entrySet()) {
            String studentId = entry.getKey();
            StudentData data = entry.getValue();
            
            Student student = studentRepository.findByStudentId(studentId).orElse(null);
            if (student != null) {
                student.setPhone(data.phone);
                student.setEmail(data.email);
                studentRepository.save(student);
                System.out.printf("已更新：学号=%s, 姓名=%s, 电话=%s, 邮箱=%s%n", 
                    studentId, student.getName(), student.getPhone(), student.getEmail());
            } else {
                System.out.printf("未找到学号为 %s 的学生%n", studentId);
            }
        }
        
        // 验证更新结果
        System.out.println("\n更新结果验证：");
        studentDataMap.keySet().forEach(studentId -> {
            Student student = studentRepository.findByStudentId(studentId).orElse(null);
            if (student != null) {
                System.out.printf("学号=%s, 姓名=%s, 电话=%s, 邮箱=%s%n", 
                    student.getStudentId(), student.getName(), student.getPhone(), student.getEmail());
            }
        });
        
        System.out.println("\n学生数据更新完成！");
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
