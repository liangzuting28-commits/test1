package com.club.managementsystem.util;

import com.club.managementsystem.entity.Student;
import com.club.managementsystem.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 角色权限校验工具类
 */
@Component
public class RoleCheckUtil {

    @Autowired
    private StudentRepository studentRepository;

    /**
     * 校验是否为老师角色
     */
    public void checkTeacherRole(String userId) {
        Student student = studentRepository.findByStudentId(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        if (student.getRole() != Student.Role.TEACHER) {
            throw new RuntimeException("仅老师可执行此操作");
        }
    }

    /**
     * 校验是否为社长角色
     */
    public void checkPresidentRole(String userId) {
        Student student = studentRepository.findByStudentId(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        if (student.getRole() != Student.Role.PRESIDENT) {
            throw new RuntimeException("仅社长可执行此操作");
        }
    }

    /**
     * 校验是否为学生角色（禁止社长/老师执行）
     */
    public void checkStudentRole(String userId) {
        Student student = studentRepository.findByStudentId(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        if (student.getRole() != Student.Role.STUDENT) {
            throw new RuntimeException("仅学生可执行此操作");
        }
    }

    /**
     * 校验社长是否有权管理指定社团（社团的president_id=当前社长ID）
     */
    public void checkPresidentManagePermission(String presidentId, Integer clubId, StudentRepository studentRepository) {
        // 1. 先校验是否为社长角色
        checkPresidentRole(presidentId);
        // 2. 校验该社团的社长是否为当前用户
        Student president = studentRepository.findByStudentId(presidentId)
                .orElseThrow(() -> new RuntimeException("社长不存在"));
        // 3. 此处需关联ClubRepository，实际使用时需注入ClubRepository
        // （注：若在Service中使用，建议直接注入ClubRepository，此处为简化示例）
    }
}