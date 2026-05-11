package com.club.managementsystem.controller;

import com.club.managementsystem.dto.ApiResponse;
import com.club.managementsystem.entity.Activity;
import com.club.managementsystem.entity.CheckIn;
import com.club.managementsystem.entity.Student;
import com.club.managementsystem.service.ActivityService;
import com.club.managementsystem.service.CheckInService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/checkin")
@CrossOrigin(origins = "*")
public class CheckInController {
    @Autowired
    private ActivityService activityService;

    @Autowired
    private CheckInService checkInService;

    /**
     * 获取活动信息
     */
    @GetMapping("/activity/{code}")
    public ResponseEntity<ApiResponse<Activity>> getActivityByCode(@PathVariable String code) {
        try {
            Activity activity = activityService.getActivityByCode(code);
            if (activity == null) {
                return ResponseEntity.ok((ApiResponse<Activity>) ApiResponse.error("活动不存在或已过期"));
            }
            return ResponseEntity.ok(ApiResponse.success(activity));
        } catch (Exception e) {
            return ResponseEntity.ok((ApiResponse<Activity>) ApiResponse.error("获取活动信息失败：" + e.getMessage()));
        }
    }

    /**
     * 生成活动签到二维码
     */
    @GetMapping("/activity/{activityId}/qrcode")
    public ResponseEntity<ApiResponse<Map<String, Object>>> generateQRCode(@PathVariable Integer activityId) {
        try {
            System.out.println("开始为活动 " + activityId + " 生成签到二维码");

            // 生成二维码，同时生成并保存签到数
            String qrCode = checkInService.generateCheckInQRCode(activityId);
            System.out.println("生成二维码成功，长度: " + (qrCode != null ? qrCode.length() : 0));

            // 获取活动信息以获取签到数
            Optional<Activity> activityOpt = activityService.findById(activityId);
            String checkInNumber = activityOpt.map(Activity::getCheckInNumber).orElse("");
            System.out.println("获取签到数: " + checkInNumber);

            Map<String, Object> result = new HashMap<>();
            result.put("qrCode", qrCode);
            result.put("checkInNumber", checkInNumber);

            System.out.println("返回结果: qrCode长度=" + (qrCode != null ? qrCode.length() : 0) + ", checkInNumber=" + checkInNumber);

            return ResponseEntity.ok(ApiResponse.success("二维码生成成功", result));
        } catch (Exception e) {
            System.err.println("生成二维码失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok((ApiResponse<Map<String, Object>>) ApiResponse.error("二维码生成失败: " + e.getMessage()));
        }
    }

    /**
     * 扫码签到
     */
    @PostMapping("/scan")
    public ResponseEntity<ApiResponse<Map<String, Object>>> scanCheckIn(
            @RequestBody Map<String, Object> request,
            @RequestHeader("X-User-Id") String studentId) {
        try {
            String code = (String) request.get("code");
            String activityId = (String) request.get("activityId");
            System.out.println("收到扫码签到请求: 学生ID=" + studentId + ", 码=" + code + ", 活动ID=" + activityId);

            // 验证活动码，支持活动码和签到数两种方式
            Activity activity;
            // 先尝试作为活动码查找
            activity = activityService.getActivityByCode(code);
            // 如果找不到，尝试作为签到数查找
            if (activity == null) {
                System.out.println("作为活动码未找到，尝试作为签到数查找");
                activity = activityService.findByCheckInNumber(code);
            }

            if (activity == null) {
                System.out.println("签到失败: 找不到对应的活动，码=" + code);
                return ResponseEntity.ok((ApiResponse<Map<String, Object>>) ApiResponse.error("活动不存在或已过期"));
            }

            System.out.println("找到活动: ID=" + activity.getActivityId() + ", 名称=" + activity.getActivityName());

            // 调用签到服务，处理签到逻辑
            boolean success = checkInService.checkIn(studentId, activity.getActivityId());

            if (success) {
                System.out.println("签到成功: 学生ID=" + studentId + ", 活动ID=" + activity.getActivityId());
                return ResponseEntity.ok(ApiResponse.success(Map.of(
                    "message", "扫码签到成功",
                    "activityName", activity.getActivityName(),
                    "checkInTime", LocalDateTime.now()
                )));
            } else {
                System.out.println("签到失败: 学生ID=" + studentId + ", 活动ID=" + activity.getActivityId());
                return ResponseEntity.ok((ApiResponse<Map<String, Object>>) ApiResponse.error("签到失败"));
            }
        } catch (RuntimeException e) {
            // 直接传递业务异常信息
            System.err.println("扫码签到业务异常: " + e.getMessage());
            return ResponseEntity.ok((ApiResponse<Map<String, Object>>) ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            // 处理系统异常
            System.err.println("扫码签到系统异常: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok((ApiResponse<Map<String, Object>>) ApiResponse.error("系统繁忙，请稍后再试"));
        }
    }

    /**
     * 手动签到
     */
    @PostMapping("/manual")
    public ResponseEntity<ApiResponse<Map<String, Object>>> manualCheckIn(
            @RequestBody Map<String, Object> request,
            @RequestHeader("X-User-Id") String studentId) {
        try {
            String code = (String) request.get("code");
            String activityId = (String) request.get("activityId");
            System.out.println("收到手动签到请求: 学生ID=" + studentId + ", 码=" + code + ", 活动ID=" + activityId);

            // 验证活动
            Optional<Activity> activityOpt = activityService.findById(Integer.parseInt(activityId));
            if (activityOpt.isEmpty()) {
                System.out.println("签到失败: 活动不存在，活动ID=" + activityId);
                return ResponseEntity.ok((ApiResponse<Map<String, Object>>) ApiResponse.error("活动不存在"));
            }
            Activity activity = activityOpt.get();
            System.out.println("找到活动: ID=" + activity.getActivityId() + ", 名称=" + activity.getActivityName());
            System.out.println("活动签到数: " + activity.getCheckInNumber());

            // 验证活动码或签到数是否匹配
            if (!code.equals(activity.getCode()) && !code.equals(activity.getCheckInNumber())) {
                System.out.println("签到失败: 签到数不匹配，输入=" + code + ", 期望=" + activity.getCheckInNumber());
                return ResponseEntity.ok((ApiResponse<Map<String, Object>>) ApiResponse.error("活动码或签到数不正确"));
            }

            // 调用签到服务，处理签到逻辑
            boolean success = checkInService.checkIn(studentId, activity.getActivityId());

            if (success) {
                System.out.println("签到成功: 学生ID=" + studentId + ", 活动ID=" + activity.getActivityId());
                return ResponseEntity.ok(ApiResponse.success(Map.of(
                    "message", "手动签到成功",
                    "activityName", activity.getActivityName(),
                    "checkInTime", LocalDateTime.now()
                )));
            } else {
                System.out.println("签到失败: 学生ID=" + studentId + ", 活动ID=" + activity.getActivityId());
                return ResponseEntity.ok((ApiResponse<Map<String, Object>>) ApiResponse.error("签到失败"));
            }
        } catch (RuntimeException e) {
            // 直接传递业务异常信息
            System.err.println("手动签到业务异常: " + e.getMessage());
            return ResponseEntity.ok((ApiResponse<Map<String, Object>>) ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            // 处理系统异常
            System.err.println("手动签到系统异常: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok((ApiResponse<Map<String, Object>>) ApiResponse.error("系统繁忙，请稍后再试"));
        }
    }

    /**
     * 获取学生的签到记录
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<ApiResponse<List<CheckIn>>> getStudentCheckInRecords(
            @PathVariable String studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            List<CheckIn> records = checkInService.getCheckInRecordsByStudentId(studentId, page, size);
            return ResponseEntity.ok(ApiResponse.success(records));
        } catch (Exception e) {
            return ResponseEntity.ok((ApiResponse<List<CheckIn>>) ApiResponse.error("获取签到记录失败：" + e.getMessage()));
        }
    }

    /**
     * 获取活动的签到记录
     */
    @GetMapping("/activity/{activityId}")
    public ResponseEntity<ApiResponse<List<CheckIn>>> getActivityCheckInRecords(
            @PathVariable Integer activityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            List<CheckIn> records = checkInService.getCheckInRecordsByActivityId(activityId, page, size);
            return ResponseEntity.ok(ApiResponse.success(records));
        } catch (Exception e) {
            return ResponseEntity.ok((ApiResponse<List<CheckIn>>) ApiResponse.error("获取签到记录失败：" + e.getMessage()));
        }
    }

    /**
     * 学生扫码签到
     */
    @PostMapping("/activity/{activityId}/checkin")
    public ResponseEntity<ApiResponse<?>> checkIn(
            @PathVariable Integer activityId,
            @RequestHeader("X-User-Id") String studentId) {
        try {
            boolean success = checkInService.checkIn(studentId, activityId);
            return ResponseEntity.ok(ApiResponse.success("签到成功", null));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("签到失败: " + e.getMessage()));
        }
    }

    /**
     * 获取活动签到情况
     */
    @GetMapping("/activity/{activityId}/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCheckInInfo(@PathVariable Integer activityId) {
        try {
            Object checkInInfo = checkInService.getActivityCheckInInfo(activityId);
            return ResponseEntity.ok(ApiResponse.success((Map<String, Object>) checkInInfo));
        } catch (Exception e) {
            return ResponseEntity.ok((ApiResponse<Map<String, Object>>) ApiResponse.error("获取签到信息失败: " + e.getMessage()));
        }
    }

    /**
     * 社长手动为学生签到
     */
    @PostMapping("/activity/{activityId}/manual-checkin")
    public ResponseEntity<ApiResponse<?>> manualCheckIn(
            @PathVariable Integer activityId,
            @RequestHeader("X-User-Id") String presidentId,
            @RequestBody Map<String, String> request) {
        try {
            String studentId = request.get("studentId");
            if (studentId == null || studentId.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.error("学生ID不能为空"));
            }

            boolean success = checkInService.manualCheckIn(presidentId, activityId, studentId);
            return ResponseEntity.ok(ApiResponse.success("手动签到成功", null));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("手动签到失败: " + e.getMessage()));
        }
    }

    /**
     * 根据签到数获取活动信息
     */
    @GetMapping("/activity/by-number/{checkInNumber}")
    public ResponseEntity<ApiResponse<Activity>> getActivityByCheckInNumber(@PathVariable String checkInNumber) {
        try {
            Activity activity = activityService.findByCheckInNumber(checkInNumber);
            if (activity == null) {
                return ResponseEntity.ok((ApiResponse<Activity>) ApiResponse.error("找不到对应的活动"));
            }
            return ResponseEntity.ok(ApiResponse.success(activity));
        } catch (Exception e) {
            return ResponseEntity.ok((ApiResponse<Activity>) ApiResponse.error("获取活动信息失败: " + e.getMessage()));
        }
    }
}