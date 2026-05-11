package com.club.managementsystem.controller;

import com.club.managementsystem.dto.ApiResponse;
import com.club.managementsystem.entity.Activity;
import com.club.managementsystem.entity.Student;
import com.club.managementsystem.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/activities")
@CrossOrigin(origins = "*")
public class ActivityRegistrationController {
    private final ActivityService activityService;
    
    public ActivityRegistrationController(ActivityService activityService) {
        this.activityService = activityService;
    }

    /**
     * 学生报名活动
     */
    @PostMapping("/{activityId}/register")
    public ResponseEntity<ApiResponse<?>> registerActivity(
            @PathVariable Integer activityId,
            @RequestHeader("X-User-Id") String studentId) {
        try {
            activityService.registerActivity(studentId, activityId);
            return ResponseEntity.ok(ApiResponse.success("报名成功", null));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("报名失败: " + e.getMessage()));
        }
    }

    /**
     * 学生取消报名
     */
    @DeleteMapping("/{activityId}/register")
    public ResponseEntity<ApiResponse<?>> cancelRegistration(
            @PathVariable Integer activityId,
            @RequestHeader("X-User-Id") String studentId) {
        try {
            activityService.cancelRegistration(studentId, activityId);
            return ResponseEntity.ok(ApiResponse.success("取消报名成功", null));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("取消报名失败: " + e.getMessage()));
        }
    }

    /**
     * 获取学生报名的活动
     */
    @GetMapping("/my-registrations")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getMyRegistrations(
            @RequestHeader("X-User-Id") String studentId) {
        try {
            List<Map<String, Object>> activitiesWithCheckIn = activityService.getStudentRegisteredActivitiesWithCheckIn(studentId);
            return ResponseEntity.ok(ApiResponse.success(activitiesWithCheckIn));
        } catch (Exception e) {
            ApiResponse<List<Map<String, Object>>> errorResponse = new ApiResponse<>();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("获取报名活动失败: " + e.getMessage());
            errorResponse.setData(null);
            return ResponseEntity.ok(errorResponse);
        }
    }

    /**
     * 获取活动的报名学生列表
     */
    @GetMapping("/{activityId}/participants")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getActivityParticipants(@PathVariable Integer activityId) {
        try {
            List<Map<String, Object>> participants = activityService.getActivityParticipantsWithDetails(activityId);
            return ResponseEntity.ok(ApiResponse.success(participants));
        } catch (Exception e) {
            ApiResponse<List<Map<String, Object>>> errorResponse = new ApiResponse<>();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("获取参与学生失败: " + e.getMessage());
            errorResponse.setData(null);
            return ResponseEntity.ok(errorResponse);
        }
    }
}