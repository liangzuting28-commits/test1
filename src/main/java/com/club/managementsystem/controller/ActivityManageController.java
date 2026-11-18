package com.club.managementsystem.controller;

import com.club.managementsystem.dto.ActivityRequest;
import com.club.managementsystem.dto.ActivityStatusUpdate;
import com.club.managementsystem.dto.ApiResponse;
import com.club.managementsystem.entity.Activity;
import com.club.managementsystem.entity.Club;
import com.club.managementsystem.entity.FileRecord;
import com.club.managementsystem.repository.ClubRepository;
import com.club.managementsystem.repository.FileRecordRepository;
import com.club.managementsystem.service.ActivityService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/activity-manage")
@CrossOrigin(origins = "*")
public class ActivityManageController {
    private final ActivityService activityService;
    private final ClubRepository clubRepository;
    private final FileRecordRepository fileRecordRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public ActivityManageController(ActivityService activityService, ClubRepository clubRepository, FileRecordRepository fileRecordRepository) {
        this.activityService = activityService;
        this.clubRepository = clubRepository;
        this.fileRecordRepository = fileRecordRepository;
    }

    // 社长创建活动（支持文件上传）
    @PostMapping("/create")
    public ResponseEntity<?> createActivity(
            @RequestPart("data") ActivityRequest request,
            @RequestPart(value = "posterFile", required = false) MultipartFile posterFile,
            @RequestPart(value = "planFile", required = false) MultipartFile planFile,
            @RequestHeader("X-User-Id") String presidentId) {
        try {
            Activity activity = activityService.createActivity(request, presidentId);

            // 保存宣传图片
            if (posterFile != null && !posterFile.isEmpty()) {
                FileRecord record = saveFile(posterFile, presidentId);
                if (record != null) {
                    activity.setPosterFileId(record.getFileId());
                }
            }

            // 保存策划书
            if (planFile != null && !planFile.isEmpty()) {
                FileRecord record = saveFile(planFile, presidentId);
                if (record != null) {
                    activity.setPlanFileId(record.getFileId());
                }
            }

            // 更新活动（如果有文件引用）
            if (activity.getPosterFileId() != null || activity.getPlanFileId() != null) {
                activity = activityService.save(activity);
            }

            Map<String, Object> activityInfo = buildActivityInfo(activity);
            return ResponseEntity.ok(ApiResponse.success("活动创建成功，等待审核", activityInfo));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("活动创建失败: " + e.getMessage()));
        }
    }

    private FileRecord saveFile(MultipartFile file, String uploaderId) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String storedName = UUID.randomUUID().toString() + extension;
        Path filePath = uploadPath.resolve(storedName);
        file.transferTo(filePath.toFile());

        FileRecord record = new FileRecord();
        record.setOriginalName(originalFilename);
        record.setStoredName(storedName);
        record.setFilePath(filePath.toString());
        record.setFileSize(file.getSize());
        record.setContentType(file.getContentType());
        record.setUploaderId(uploaderId);
        return fileRecordRepository.save(record);
    }

    private Map<String, Object> buildActivityInfo(Activity activity) {
        Map<String, Object> info = new HashMap<>();
        info.put("activityId", activity.getActivityId());
        info.put("activityName", activity.getActivityName());
        info.put("clubId", activity.getClubId());
        info.put("activityTime", activity.getActivityTime());
        info.put("location", activity.getLocation());
        info.put("maxParticipants", activity.getMaxParticipants());
        info.put("awardPoints", activity.getAwardPoints());
        info.put("description", activity.getDescription());
        info.put("status", activity.getStatus());
        info.put("activityType", activity.getActivityType());
        info.put("createdAt", activity.getCreatedAt());
        info.put("posterFileId", activity.getPosterFileId());
        info.put("planFileId", activity.getPlanFileId());
        if (activity.getClubId() != null) {
            String clubName = clubRepository.findById(activity.getClubId())
                    .map(Club::getClubName)
                    .orElse("未知社团");
            info.put("clubName", clubName);
        } else {
            info.put("clubName", "未知社团");
        }
        return info;
    }

    // 社长结束活动
    @PutMapping("/{activityId}/end")
    public ResponseEntity<?> endActivity(
            @PathVariable Integer activityId,
            @RequestHeader("X-User-Id") String presidentId) {
        try {
            Activity activity = activityService.endActivity(activityId, presidentId);
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
            activityInfo.put("createdAt", activity.getCreatedAt());
            activityInfo.put("posterFileId", activity.getPosterFileId());
            activityInfo.put("planFileId", activity.getPlanFileId());
            // 获取真实的社团名称
            if (activity.getClubId() != null) {
                String clubName = clubRepository.findById(activity.getClubId())
                        .map(Club::getClubName)
                        .orElse("未知社团");
                activityInfo.put("clubName", clubName);
            } else {
                activityInfo.put("clubName", "未知社团");
            }
            return ResponseEntity.ok(ApiResponse.success("活动结束成功", activityInfo));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("结束活动失败: " + e.getMessage()));
        }
    }

// 在 ActivityManageController 类中修改 getMyActivities 方法

    // 社长查看自己社团的活动
    @GetMapping("/my-activities")
    public ResponseEntity<?> getMyActivities(
            @RequestHeader("X-User-Id") String presidentId) {
        try {
            List<Activity> activities = activityService.getActivitiesByPresident(presidentId);
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
                activityInfo.put("createdAt", activity.getCreatedAt());
                activityInfo.put("posterFileId", activity.getPosterFileId());
                activityInfo.put("planFileId", activity.getPlanFileId());
                // 获取真实的社团名称
                if (activity.getClubId() != null) {
                    String clubName = clubRepository.findById(activity.getClubId())
                            .map(Club::getClubName)
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

    // 添加获取单个活动详情的方法
    @GetMapping("/{activityId}")
    public ResponseEntity<?> getActivityDetail(
            @PathVariable Integer activityId,
            @RequestHeader("X-User-Id") String userId) {
        try {
            Optional<Activity> activityOpt = activityService.findById(activityId);
            if (activityOpt.isPresent()) {
                Activity activity = activityOpt.get();
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
                activityInfo.put("createdAt", activity.getCreatedAt());
                activityInfo.put("posterFileId", activity.getPosterFileId());
                activityInfo.put("planFileId", activity.getPlanFileId());
                // 获取真实的社团名称
                if (activity.getClubId() != null) {
                    String clubName = clubRepository.findById(activity.getClubId())
                            .map(Club::getClubName)
                            .orElse("未知社团");
                    activityInfo.put("clubName", clubName);
                } else {
                    activityInfo.put("clubName", "未知社团");
                }
                return ResponseEntity.ok(ApiResponse.success(activityInfo));
            } else {
                return ResponseEntity.ok(ApiResponse.error("活动不存在"));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("获取活动详情失败: " + e.getMessage()));
        }
    }

    // 获取活动报名记录
    @GetMapping("/activity/{activityId}/registrations")
    public ResponseEntity<?> getActivityRegistrations(
            @PathVariable Integer activityId,
            @RequestHeader("X-User-Id") String userId) {
        try {
            // 验证社长权限
            if (!activityService.isPresidentOfActivity(userId, activityId)) {
                return ResponseEntity.ok(ApiResponse.error("无权查看该活动的报名记录"));
            }

            List<Map<String, Object>> registrations = activityService.getActivityParticipantsWithDetails(activityId);
            return ResponseEntity.ok(ApiResponse.success(registrations));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("获取报名记录失败: " + e.getMessage()));
        }
    }

    // 社长通过学生报名
    @PutMapping("/registrations/{registrationId}/approve")
    public ResponseEntity<?> approveRegistration(
            @PathVariable Integer registrationId,
            @RequestHeader("X-User-Id") String userId) {
        try {
            activityService.approveRegistration(registrationId);
            return ResponseEntity.ok(ApiResponse.success("已通过报名", null));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("操作失败: " + e.getMessage()));
        }
    }

    // 社长拒绝学生报名
    @PutMapping("/registrations/{registrationId}/reject")
    public ResponseEntity<?> rejectRegistration(
            @PathVariable Integer registrationId,
            @RequestHeader("X-User-Id") String userId,
            @RequestBody(required = false) Map<String, String> body) {
        try {
            String reason = body != null ? body.get("reason") : null;
            activityService.rejectRegistration(registrationId, reason);
            return ResponseEntity.ok(ApiResponse.success("已拒绝报名", null));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("操作失败: " + e.getMessage()));
        }
    }

    // 获取所有活动（管理员和社长用）
    @GetMapping("/all-activities")
    public ResponseEntity<?> getAllActivities() {
        try {
            List<Activity> activities = activityService.findAll();
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
                activityInfo.put("createdAt", activity.getCreatedAt());
                activityInfo.put("posterFileId", activity.getPosterFileId());
                activityInfo.put("planFileId", activity.getPlanFileId());
                activityInfo.put("rejectReason", activity.getRejectReason());
                // 获取真实的社团名称
                if (activity.getClubId() != null) {
                    String clubName = clubRepository.findById(activity.getClubId())
                            .map(Club::getClubName)
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

    // 老师获取待审核活动
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getPendingActivities() {
        try {
            List<Activity> activities = activityService.getPendingActivities();
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
                activityInfo.put("createdAt", activity.getCreatedAt());
                activityInfo.put("posterFileId", activity.getPosterFileId());
                activityInfo.put("planFileId", activity.getPlanFileId());
                // 获取真实的社团名称
                if (activity.getClubId() != null) {
                    String clubName = clubRepository.findById(activity.getClubId())
                            .map(Club::getClubName)
                            .orElse("未知社团");
                    activityInfo.put("clubName", clubName);
                } else {
                    activityInfo.put("clubName", "未知社团");
                }
                return activityInfo;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponse.success(activitiesWithClubName));
        } catch (Exception e) {
            return ResponseEntity.ok((ApiResponse<List<Map<String, Object>>>) ApiResponse.error("获取待审核活动失败: " + e.getMessage()));
        }
    }

    // 老师获取已通过活动
    @GetMapping("/approved")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getApprovedActivities() {
        try {
            List<Activity> activities = activityService.getApprovedActivities();
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
                activityInfo.put("createdAt", activity.getCreatedAt());
                activityInfo.put("posterFileId", activity.getPosterFileId());
                activityInfo.put("planFileId", activity.getPlanFileId());
                // 获取真实的社团名称
                if (activity.getClubId() != null) {
                    String clubName = clubRepository.findById(activity.getClubId())
                            .map(Club::getClubName)
                            .orElse("未知社团");
                    activityInfo.put("clubName", clubName);
                } else {
                    activityInfo.put("clubName", "未知社团");
                }
                return activityInfo;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponse.success(activitiesWithClubName));
        } catch (Exception e) {
            return ResponseEntity.ok((ApiResponse<List<Map<String, Object>>>) ApiResponse.error("获取已通过活动失败: " + e.getMessage()));
        }
    }

    // 老师获取已驳回活动
    @GetMapping("/rejected")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getRejectedActivities() {
        try {
            List<Activity> activities = activityService.getRejectedActivities();
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
                activityInfo.put("createdAt", activity.getCreatedAt());
                activityInfo.put("posterFileId", activity.getPosterFileId());
                activityInfo.put("planFileId", activity.getPlanFileId());
                // 获取真实的社团名称
                if (activity.getClubId() != null) {
                    String clubName = clubRepository.findById(activity.getClubId())
                            .map(Club::getClubName)
                            .orElse("未知社团");
                    activityInfo.put("clubName", clubName);
                } else {
                    activityInfo.put("clubName", "未知社团");
                }
                return activityInfo;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponse.success(activitiesWithClubName));
        } catch (Exception e) {
            return ResponseEntity.ok((ApiResponse<List<Map<String, Object>>>) ApiResponse.error("获取已驳回活动失败: " + e.getMessage()));
        }
    }

    // 老师审核活动
    @PutMapping("/{activityId}/review")
    public ResponseEntity<?> reviewActivity(
            @PathVariable Integer activityId,
            @RequestBody ActivityStatusUpdate statusUpdate) {
        try {
            Activity.Status status = Activity.Status.valueOf(statusUpdate.getStatus().toUpperCase());
            Activity activity = activityService.updateActivityStatus(activityId, status, statusUpdate.getReason());
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
            activityInfo.put("createdAt", activity.getCreatedAt());
            activityInfo.put("posterFileId", activity.getPosterFileId());
            activityInfo.put("planFileId", activity.getPlanFileId());
            // 获取真实的社团名称
            if (activity.getClubId() != null) {
                String clubName = clubRepository.findById(activity.getClubId())
                        .map(Club::getClubName)
                        .orElse("未知社团");
                activityInfo.put("clubName", clubName);
            } else {
                activityInfo.put("clubName", "未知社团");
            }
            String message = status == Activity.Status.PUBLISHED ? "活动审核通过" : "活动已驳回";
            return ResponseEntity.ok(ApiResponse.success(message, activityInfo));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("审核失败: " + e.getMessage()));
        }
    }

    // 删除活动
    @DeleteMapping("/{activityId}")
    public ResponseEntity<?> deleteActivity(
            @PathVariable Integer activityId,
            @RequestHeader("X-User-Id") String userId) {
        try {
            activityService.deleteById(activityId, userId);
            return ResponseEntity.ok(ApiResponse.success("活动删除成功", null));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("活动删除失败: " + e.getMessage()));
        }
    }

    // 获取学生报名的活动
    @GetMapping("/student/{studentId}/activities")
    public ResponseEntity<?> getStudentActivities(@PathVariable String studentId) {
        try {
            List<Activity> activities = activityService.getStudentRegisteredActivities(studentId);
            List<Map<String, Object>> activitiesWithClubName = activities.stream().map(activity -> {
                Map<String, Object> activityInfo = new HashMap<>();
                activityInfo.put("activityId", activity.getActivityId());
                activityInfo.put("activityName", activity.getActivityName());
                activityInfo.put("activityTime", activity.getActivityTime());
                activityInfo.put("location", activity.getLocation());
                activityInfo.put("status", activity.getStatus());
                activityInfo.put("awardPoints", activity.getAwardPoints());
                // 获取真实的社团名称
                if (activity.getClubId() != null) {
                    String clubName = clubRepository.findById(activity.getClubId())
                            .map(Club::getClubName)
                            .orElse("未知社团");
                    activityInfo.put("clubName", clubName);
                } else {
                    activityInfo.put("clubName", "未知社团");
                }
                return activityInfo;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponse.success(activitiesWithClubName));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("获取活动信息失败: " + e.getMessage()));
        }
    }

    // 获取社长创建的活动
    @GetMapping("/president/{studentId}/activities")
    public ResponseEntity<?> getPresidentActivities(@PathVariable String studentId) {
        try {
            List<Activity> activities = activityService.getActivitiesByPresident(studentId);
            List<Map<String, Object>> activitiesWithClubName = activities.stream().map(activity -> {
                Map<String, Object> activityInfo = new HashMap<>();
                activityInfo.put("activityId", activity.getActivityId());
                activityInfo.put("activityName", activity.getActivityName());
                activityInfo.put("activityTime", activity.getActivityTime());
                activityInfo.put("location", activity.getLocation());
                activityInfo.put("status", activity.getStatus());
                activityInfo.put("awardPoints", activity.getAwardPoints());
                // 获取真实的社团名称
                if (activity.getClubId() != null) {
                    String clubName = clubRepository.findById(activity.getClubId())
                            .map(Club::getClubName)
                            .orElse("未知社团");
                    activityInfo.put("clubName", clubName);
                } else {
                    activityInfo.put("clubName", "未知社团");
                }
                return activityInfo;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponse.success(activitiesWithClubName));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("获取活动信息失败: " + e.getMessage()));
        }
    }

    // 获取教师审核的活动
    @GetMapping("/teacher/{studentId}/activities")
    public ResponseEntity<?> getTeacherActivities(@PathVariable String studentId) {
        try {
            // 获取所有待审核、已通过和已驳回的活动
            List<Activity> pendingActivities = activityService.getPendingActivities();
            List<Activity> approvedActivities = activityService.getApprovedActivities();
            List<Activity> rejectedActivities = activityService.getRejectedActivities();
            
            // 合并所有活动
            List<Activity> allActivities = new ArrayList<>();
            allActivities.addAll(pendingActivities);
            allActivities.addAll(approvedActivities);
            allActivities.addAll(rejectedActivities);
            
            List<Map<String, Object>> activitiesWithClubName = allActivities.stream().map(activity -> {
                Map<String, Object> activityInfo = new HashMap<>();
                activityInfo.put("activityId", activity.getActivityId());
                activityInfo.put("activityName", activity.getActivityName());
                activityInfo.put("activityTime", activity.getActivityTime());
                activityInfo.put("location", activity.getLocation());
                activityInfo.put("status", activity.getStatus());
                activityInfo.put("awardPoints", activity.getAwardPoints());
                // 获取真实的社团名称
                if (activity.getClubId() != null) {
                    String clubName = clubRepository.findById(activity.getClubId())
                            .map(Club::getClubName)
                            .orElse("未知社团");
                    activityInfo.put("clubName", clubName);
                } else {
                    activityInfo.put("clubName", "未知社团");
                }
                return activityInfo;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponse.success(activitiesWithClubName));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("获取活动信息失败: " + e.getMessage()));
        }
    }
}
