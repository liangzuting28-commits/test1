package com.club.managementsystem.controller;

import com.club.managementsystem.dto.ApiResponse;
import com.club.managementsystem.entity.PointsRecord;
import com.club.managementsystem.entity.PointsRule;
import com.club.managementsystem.service.PointsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/points")
@CrossOrigin(origins = "*")
public class PointsController {

    @Autowired
    private PointsService pointsService;

    // 获取学生的积分记录
    @GetMapping("/student/{studentId}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getStudentPoints(
            @PathVariable String studentId) {
        try {
            List<Map<String, Object>> records = pointsService.getStudentPoints(studentId);
            return ResponseEntity.ok(ApiResponse.success(records));
        } catch (Exception e) {
            return ResponseEntity.ok((ApiResponse<List<Map<String, Object>>>)
                    ApiResponse.error("获取积分记录失败: " + e.getMessage()));
        }
    }

    // 获取学生的总积分
    @GetMapping("/student/{studentId}/total")
    public ResponseEntity<ApiResponse<Integer>> getStudentTotalPoints(
            @PathVariable String studentId) {
        try {
            Integer totalPoints = pointsService.getStudentTotalPoints(studentId);
            return ResponseEntity.ok(ApiResponse.success(totalPoints));
        } catch (Exception e) {
            return ResponseEntity.ok((ApiResponse<Integer>)
                    ApiResponse.error("获取总积分失败: " + e.getMessage()));
        }
    }

    // 获取积分排行榜
    @GetMapping("/ranking")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getPointsRanking() {
        try {
            List<Map<String, Object>> ranking = pointsService.getPointsRanking();
            return ResponseEntity.ok(ApiResponse.success(ranking));
        } catch (Exception e) {
            return ResponseEntity.ok((ApiResponse<List<Map<String, Object>>>)
                    ApiResponse.error("获取排行榜失败: " + e.getMessage()));
        }
    }

    // 获取所有积分规则（老师用）
    @GetMapping("/rules")
    public ResponseEntity<ApiResponse<List<PointsRule>>> getAllPointsRules() {
        try {
            List<PointsRule> rules = pointsService.getAllPointsRules();
            return ResponseEntity.ok(ApiResponse.success(rules));
        } catch (Exception e) {
            return ResponseEntity.ok((ApiResponse<List<PointsRule>>)
                    ApiResponse.error("获取积分规则失败: " + e.getMessage()));
        }
    }

    // 更新积分规则（老师用）
    @PutMapping("/rules/{ruleId}")
    public ResponseEntity<ApiResponse<PointsRule>> updatePointsRule(
            @PathVariable Integer ruleId,
            @RequestBody PointsRule pointsRule) {
        try {
            PointsRule updatedRule = pointsService.updatePointsRule(ruleId, pointsRule);
            return ResponseEntity.ok(ApiResponse.success("积分规则更新成功", updatedRule));
        } catch (Exception e) {
            return ResponseEntity.ok((ApiResponse<PointsRule>)
                    ApiResponse.error("更新积分规则失败: " + e.getMessage()));
        }
    }

    // 添加积分规则（老师用）
    @PostMapping("/rules")
    public ResponseEntity<ApiResponse<PointsRule>> addPointsRule(
            @RequestBody PointsRule pointsRule) {
        try {
            PointsRule newRule = pointsService.addPointsRule(pointsRule);
            return ResponseEntity.ok(ApiResponse.success("积分规则添加成功", newRule));
        } catch (Exception e) {
            return ResponseEntity.ok((ApiResponse<PointsRule>)
                    ApiResponse.error("添加积分规则失败: " + e.getMessage()));
        }
    }

    // 删除积分规则（老师用）
    @DeleteMapping("/rules/{ruleId}")
    public ResponseEntity<ApiResponse<?>> deletePointsRule(@PathVariable Integer ruleId) {
        try {
            pointsService.deletePointsRule(ruleId);
            return ResponseEntity.ok(ApiResponse.success("积分规则删除成功", null));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("删除积分规则失败: " + e.getMessage()));
        }
    }

    // 根据活动类型获取积分规则
    @GetMapping("/rules/type/{activityType}")
    public ResponseEntity<ApiResponse<PointsRule>> getPointsRuleByType(
            @PathVariable String activityType) {
        try {
            PointsRule rule = pointsService.getPointsRuleByActivityType(activityType);
            if (rule != null) {
                return ResponseEntity.ok(ApiResponse.success(rule));
            } else {
                return ResponseEntity.ok((ApiResponse<PointsRule>)
                        ApiResponse.error("未找到对应的积分规则"));
            }
        } catch (Exception e) {
            return ResponseEntity.ok((ApiResponse<PointsRule>)
                    ApiResponse.error("获取积分规则失败: " + e.getMessage()));
        }
    }
}