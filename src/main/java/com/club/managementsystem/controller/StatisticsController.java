package com.club.managementsystem.controller;

import com.club.managementsystem.dto.ApiResponse;
import com.club.managementsystem.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
@CrossOrigin(origins = "*")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    // 获取系统概览统计
    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOverviewStatistics() {
        try {
            Map<String, Object> stats = statisticsService.getOverviewStatistics();
            return ResponseEntity.ok(ApiResponse.success(stats));
        } catch (Exception e) {
            return ResponseEntity.ok((ApiResponse<Map<String, Object>>) 
                    ApiResponse.error("获取统计信息失败: " + e.getMessage()));
        }
    }
    
    // 获取管理员仪表盘统计数据
    @GetMapping("/admin-dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAdminDashboardStatistics() {
        try {
            Map<String, Object> stats = statisticsService.getAdminDashboardStatistics();
            return ResponseEntity.ok(ApiResponse.success(stats));
        } catch (Exception e) {
            return ResponseEntity.ok((ApiResponse<Map<String, Object>>) 
                    ApiResponse.error("获取管理员仪表盘统计信息失败: " + e.getMessage()));
        }
    }

    // 获取活动统计
    @GetMapping("/activities")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getActivityStatistics() {
        try {
            Map<String, Object> stats = statisticsService.getActivityStatistics();
            return ResponseEntity.ok(ApiResponse.success(stats));
        } catch (Exception e) {
            return ResponseEntity.ok((ApiResponse<Map<String, Object>>)
                    ApiResponse.error("获取活动统计失败: " + e.getMessage()));
        }
    }

    // 获取社团统计
    @GetMapping("/club-stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getClubStatistics() {
        try {
            Map<String, Object> stats = statisticsService.getClubStatistics();
            return ResponseEntity.ok(ApiResponse.success(stats));
        } catch (Exception e) {
            return ResponseEntity.ok((ApiResponse<Map<String, Object>>)
                    ApiResponse.error("获取社团统计失败: " + e.getMessage()));
        }
    }

    // 获取积分统计
    @GetMapping("/points")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPointsStatistics() {
        try {
            Map<String, Object> stats = statisticsService.getPointsStatistics();
            return ResponseEntity.ok(ApiResponse.success(stats));
        } catch (Exception e) {
            return ResponseEntity.ok((ApiResponse<Map<String, Object>>)
                    ApiResponse.error("获取积分统计失败: " + e.getMessage()));
        }
    }


    
    // 获取活动类型分布
    @GetMapping("/activity-types")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getActivityTypeDistribution(
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) String clubId) {
        try {
            List<Map<String, Object>> distribution = statisticsService.getActivityTypeDistribution(semester, clubId);
            return ResponseEntity.ok(ApiResponse.success(distribution));
        } catch (Exception e) {
            return ResponseEntity.ok((ApiResponse<List<Map<String, Object>>>)
                    ApiResponse.error("获取活动类型分布失败: " + e.getMessage()));
        }
    }
    
    // 获取社团活动数量
    @GetMapping("/club-activities")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getClubActivityCount(
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) String activityType) {
        try {
            List<Map<String, Object>> clubActivities = statisticsService.getClubActivityCount(semester, activityType);
            return ResponseEntity.ok(ApiResponse.success(clubActivities));
        } catch (Exception e) {
            return ResponseEntity.ok((ApiResponse<List<Map<String, Object>>>)
                    ApiResponse.error("获取社团活动数量失败: " + e.getMessage()));
        }
    }
    
    // 获取积分发放趋势
    @GetMapping("/points-trend")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getPointsTrend(
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) String clubId) {
        try {
            List<Map<String, Object>> pointsTrend = statisticsService.getPointsTrend(semester, clubId);
            return ResponseEntity.ok(ApiResponse.success(pointsTrend));
        } catch (Exception e) {
            return ResponseEntity.ok((ApiResponse<List<Map<String, Object>>>)
                    ApiResponse.error("获取积分发放趋势失败: " + e.getMessage()));
        }
    }
    
    // 获取活动参与度排行
    @GetMapping("/participation-ranking")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getParticipationRanking(
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) String clubId,
            @RequestParam(required = false) String activityType,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<Map<String, Object>> ranking = statisticsService.getParticipationRanking(semester, clubId, activityType, limit);
            return ResponseEntity.ok(ApiResponse.success(ranking));
        } catch (Exception e) {
            return ResponseEntity.ok((ApiResponse<List<Map<String, Object>>>)
                    ApiResponse.error("获取活动参与度排行失败: " + e.getMessage()));
        }
    }
    
    // 获取所有社团列表（用于筛选）
    @GetMapping("/clubs")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllClubs() {
        try {
            List<Map<String, Object>> clubs = statisticsService.getAllClubs();
            return ResponseEntity.ok(ApiResponse.success(clubs));
        } catch (Exception e) {
            return ResponseEntity.ok((ApiResponse<List<Map<String, Object>>>)
                    ApiResponse.error("获取社团列表失败: " + e.getMessage()));
        }
    }
}