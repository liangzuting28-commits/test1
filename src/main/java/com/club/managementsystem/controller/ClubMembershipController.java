package com.club.managementsystem.controller;

import com.club.managementsystem.dto.ApiResponse;
import com.club.managementsystem.entity.ClubMembership;
import com.club.managementsystem.service.ClubMembershipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/club-memberships")
@CrossOrigin(origins = "*")
public class ClubMembershipController {

    @Autowired
    private ClubMembershipService clubMembershipService;

    // 1. 社长获取社团成员
    @GetMapping("/president/members/{clubId}")
    public ResponseEntity<ApiResponse<List<ClubMembership>>> getClubMembersByPresident(
            @PathVariable Integer clubId,
            @RequestHeader("X-User-Id") String presidentId) {
        try {
            List<ClubMembership> members = clubMembershipService.getClubMembersByPresident(clubId, presidentId);
            return ResponseEntity.ok(ApiResponse.success(members));
        } catch (Exception e) {
            return ResponseEntity.ok((ApiResponse<List<ClubMembership>>) ApiResponse.error("获取成员列表失败: " + e.getMessage()));
        }
    }

    // 2. 社长移除成员
    @DeleteMapping("/president/remove/{clubId}/{studentId}")
    public ResponseEntity<ApiResponse<?>> removeMemberByPresident(
            @PathVariable Integer clubId,
            @PathVariable String studentId,
            @RequestHeader("X-User-Id") String presidentId) {
        try {
            clubMembershipService.removeMemberByPresident(clubId, studentId, presidentId);
            return ResponseEntity.ok(ApiResponse.success("成员移除成功", null));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("移除成员失败: " + e.getMessage()));
        }
    }

    // 3. 学生取消申请
    @DeleteMapping("/club/{clubId}/member/{studentId}")
    public ResponseEntity<ApiResponse<?>> cancelApplication(
            @PathVariable Integer clubId,
            @PathVariable String studentId,
            @RequestBody java.util.Map<String, String> request,
            @RequestHeader("X-User-Id") String operatorId) {
        // 添加调试日志
        System.out.println("取消申请请求: clubId=" + clubId + ", studentId=" + studentId + ", operatorId=" + operatorId);
        try {
            // 验证操作者是否为申请人本人
            if (!operatorId.equals(studentId)) {
                return ResponseEntity.ok(ApiResponse.error("权限不足：仅申请人本人可取消申请"));
            }
            
            String reason = request.get("reason");
            System.out.println("取消原因: " + reason);

            clubMembershipService.cancelApplication(clubId, studentId, reason);
            return ResponseEntity.ok(ApiResponse.success("取消申请成功", null));
        } catch (Exception e) {
            System.out.println("取消申请异常: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(ApiResponse.error("取消申请失败：" + e.getMessage()));
        }
    }
    
    // 其他原有方法保持不变...
}