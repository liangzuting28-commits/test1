package com.club.managementsystem.controller;

import com.club.managementsystem.dto.ApiResponse;
import com.club.managementsystem.entity.Activity;
import com.club.managementsystem.entity.ActivityRegistration;
import com.club.managementsystem.entity.Student;
import com.club.managementsystem.repository.ClubRepository;
import com.club.managementsystem.service.ActivityService;
import com.club.managementsystem.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/activities")
@CrossOrigin(origins = "*")
public class ActivityController {
    private final ActivityService activityService;
    private final ClubRepository clubRepository;
    private final StudentService studentService;
    
    public ActivityController(ActivityService activityService, ClubRepository clubRepository, StudentService studentService) {
        this.activityService = activityService;
        this.clubRepository = clubRepository;
        this.studentService = studentService;
    }

    // 获取所有活动（根据用户角色返回不同结果：教师/管理员返回所有活动，学生/社长只返回已发布的活动）
    @GetMapping
    public ResponseEntity<?> getAllActivities(@RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            List<Activity> activities;
            
            // 如果没有提供用户ID，默认返回已发布的活动
            if (userId == null) {
                activities = activityService.getPublishedActivities();
            } else {
                // 根据用户角色决定返回哪些活动
                Optional<Student> studentOpt = studentService.findByStudentId(userId);
                if (studentOpt.isPresent()) {
                    Student student = studentOpt.get();
                    // 教师和管理员可以查看所有活动
                    if (student.getRole() == Student.Role.TEACHER || student.getRole() == Student.Role.ADMIN) {
                        activities = activityService.findAll();
                    } else {
                        // 学生和社长只能查看已发布的活动
                        activities = activityService.getPublishedActivities();
                    }
                } else {
                    // 如果用户不存在，默认返回已发布的活动
                    activities = activityService.getPublishedActivities();
                }
            }
            
            // 转换为包含社团名称的活动信息
            List<Map<String, Object>> activitiesWithClubName = activities.stream().map(activity -> {
                Map<String, Object> activityInfo = new HashMap<>();
                activityInfo.put("activityId", activity.getActivityId());
                activityInfo.put("activityName", activity.getActivityName());
                activityInfo.put("clubId", activity.getClubId());
                activityInfo.put("activityTime", activity.getActivityTime());
                activityInfo.put("location", activity.getLocation());
                activityInfo.put("maxParticipants", activity.getMaxParticipants());
                activityInfo.put("awardPoints", activity.getAwardPoints());
                activityInfo.put("description", activity.getDescription());
                activityInfo.put("status", activity.getStatus());
                activityInfo.put("activityType", activity.getActivityType());
                activityInfo.put("posterFileId", activity.getPosterFileId());
                // 获取真实的社团名称
                if (activity.getClubId() != null) {
                    String clubName = clubRepository.findById(activity.getClubId())
                            .map(club -> club.getClubName())
                            .orElse("未知社团");
                    activityInfo.put("clubName", clubName);
                } else {
                    activityInfo.put("clubName", "未知社团");
                }
                return activityInfo;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponse.success(activitiesWithClubName));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("获取活动列表失败: " + e.getMessage()));
        }
    }

    @GetMapping("/{activityId}")
    public ResponseEntity<?> getActivityById(@PathVariable Integer activityId) {
        Optional<Activity> activity = activityService.findById(activityId);
        if (activity.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success(activity.get()));
        } else {
            return ResponseEntity.ok(ApiResponse.error("活动不存在"));
        }
    }

    @GetMapping("/club/{clubId}")
    public ResponseEntity<ApiResponse<List<Activity>>> getActivitiesByClub(@PathVariable Integer clubId) {
        List<Activity> activities = activityService.findByClubId(clubId);
        return ResponseEntity.ok(ApiResponse.success(activities));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Activity>> createActivity(@RequestBody Activity activity) {
        Activity savedActivity = activityService.save(activity);
        return ResponseEntity.ok(ApiResponse.success("活动创建成功", savedActivity));
    }

}