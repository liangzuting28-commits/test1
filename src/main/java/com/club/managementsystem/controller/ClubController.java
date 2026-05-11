package com.club.managementsystem.controller;

import com.club.managementsystem.dto.*;
import com.club.managementsystem.entity.Club;
import com.club.managementsystem.entity.ClubMembership;
import com.club.managementsystem.service.ClubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/clubs")
@CrossOrigin(origins = "*")
public class ClubController {

    private final ClubService clubService;

    public ClubController(ClubService clubService) {
        this.clubService = clubService;
    }

    // 1. 老师创建社团
    @PostMapping
    public ResponseEntity<?> createClub(
            @RequestBody CreateClubRequest request,
            @RequestHeader("X-User-Id") String creatorId) {
        try {
            Club club = clubService.createClub(request, creatorId);
            return ResponseEntity.ok(ApiResponse.success("社团创建成功", club));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("创建失败：" + e.getMessage()));
        }
    }

    // 2. 学生申请加入社团
    @PostMapping("/{clubId}/apply")
    public ResponseEntity<?> applyForClub(
            @PathVariable Integer clubId,
            @RequestHeader("X-User-Id") String studentId) {
        try {
            ClubMembership mem = clubService.applyForClub(studentId, clubId, "");
            return ResponseEntity.ok(ApiResponse.success("申请提交成功，等待审核", mem));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("申请失败：" + e.getMessage()));
        }
    }

    // 3. 社长获取自己社团的待审核申请
    @GetMapping("/{clubId}/pending-applications")
    public ResponseEntity<?> getPendingApplications(
            @PathVariable Integer clubId,
            @RequestHeader("X-User-Id") String presidentId) {
        try {
            // 验证社长是否有权访问该社团
            clubService.validatePresidentAccess(clubId, presidentId);
            List<ClubMembership> apps = clubService.getPendingApplications(clubId, presidentId);
            return ResponseEntity.ok(ApiResponse.success(apps));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("获取失败：" + e.getMessage()));
        }
    }

    // 4. 社长审核成员申请
    @PutMapping("/applications/{membershipId}/review")
    public ResponseEntity<?> reviewApplication(
            @PathVariable Integer membershipId,
            @RequestBody UpdateApplicationStatusRequest request,
            @RequestHeader("X-User-Id") String operatorId) {
        try {
            // 首先获取申请信息，验证社长权限
            ClubMembership membership = clubService.getMembershipById(membershipId);
            clubService.validatePresidentAccess(membership.getClubId(), operatorId);

            ClubMembership mem = clubService.reviewApplication(
                    membershipId, request.getStatus(), request.getReason(), operatorId);
            String msg = "APPROVED".equals(request.getStatus()) ? "申请已通过" : "申请已拒绝";
            return ResponseEntity.ok(ApiResponse.success(msg, mem));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("审核失败：" + e.getMessage()));
        }
    }

    // 5. 社长获取自己管理的社团
    @GetMapping("/my-clubs")
    public ResponseEntity<?> getMyClubs(
            @RequestHeader("X-User-Id") String presidentId) {
        try {
            List<Club> clubs = clubService.findByPresidentId(presidentId);
            return ResponseEntity.ok(ApiResponse.success(clubs));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("获取失败：" + e.getMessage()));
        }
    }

    // 6. 基础接口
    @GetMapping
    public ResponseEntity<?> getAllClubs() {
        try {
            return ResponseEntity.ok(ApiResponse.success(clubService.findAll()));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("获取失败：" + e.getMessage()));
        }
    }

    @GetMapping("/{clubId}/detail")
    public ResponseEntity<?> getClubDetail(@PathVariable Integer clubId) {
        try {
            return ResponseEntity.ok(ApiResponse.success(clubService.getClubDetail(clubId)));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("获取失败：" + e.getMessage()));
        }
    }

    // 根据ID获取社团基本信息
    @GetMapping("/{clubId}")
    public ResponseEntity<?> getClubById(@PathVariable Integer clubId) {
        try {
            Optional<Club> clubOpt = clubService.findById(clubId);
            if (clubOpt.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(clubOpt.get()));
            } else {
                return ResponseEntity.ok(ApiResponse.error("社团不存在"));
            }
        } catch (Exception e) {
            return ResponseEntity.ok((ApiResponse<Club>) ApiResponse.error("获取失败：" + e.getMessage()));
        }
    }

    @DeleteMapping("/{clubId}")
    public ResponseEntity<ApiResponse<?>> deleteClub(
            @PathVariable Integer clubId,
            @RequestHeader("X-User-Id") String operatorId) {
        try {
            // 验证操作者权限（老师或社长）
            clubService.validateDeleteClubAccess(clubId, operatorId);
            clubService.deleteClub(clubId, operatorId);
            return ResponseEntity.ok(ApiResponse.success("社团删除成功", null));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("删除失败：" + e.getMessage()));
        }
    }
    
    // 根据社长ID获取社团信息
    @GetMapping("/by-president/{presidentId}")
    public ResponseEntity<?> getClubsByPresidentId(@PathVariable String presidentId) {
        try {
            List<Club> clubs = clubService.findByPresidentId(presidentId);
            return ResponseEntity.ok(ApiResponse.success(clubs));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("获取失败：" + e.getMessage()));
        }
    }

    // 学生删除自己的社团申请记录
    @DeleteMapping("/my-applications/{membershipId}")
    public ResponseEntity<ApiResponse<?>> deleteStudentApplication(
            @PathVariable Integer membershipId,
            @RequestHeader("X-User-Id") String studentId) {
        try {
            clubService.deleteStudentApplication(membershipId, studentId);
            return ResponseEntity.ok(ApiResponse.success("申请记录删除成功", null));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("删除失败：" + e.getMessage()));
        }
    }
    
    // 更新社团信息
    @PutMapping("/{clubId}")
    public ResponseEntity<ApiResponse<Club>> updateClub(
            @PathVariable Integer clubId,
            @RequestBody UpdateClubRequest request,
            @RequestHeader("X-User-Id") String userId) {
        try {
            Club updatedClub = clubService.updateClub(clubId, request, userId);
            return ResponseEntity.ok(ApiResponse.success("社团信息更新成功", updatedClub));
        } catch (Exception e) {
            return ResponseEntity.ok((ApiResponse<Club>) ApiResponse.error("更新失败：" + e.getMessage()));
        }
    }

    // 更换社长（教师操作）
    @PutMapping("/{clubId}/president")
    public ResponseEntity<ApiResponse<Club>> changePresident(
            @PathVariable Integer clubId,
            @RequestBody PresidentChangeRequest request,
            @RequestHeader("X-User-Id") String operatorId) {
        try {
            Club updatedClub = clubService.changePresident(clubId, request.getNewPresidentId(), operatorId);
            return ResponseEntity.ok(ApiResponse.success("社长更换成功", updatedClub));
        } catch (Exception e) {
            return ResponseEntity.ok((ApiResponse<Club>) ApiResponse.error("更换失败：" + e.getMessage()));
        }
    }

    // 移除社长（教师操作）
    @DeleteMapping("/{clubId}/president")
    public ResponseEntity<ApiResponse<Club>> removePresident(
            @PathVariable Integer clubId,
            @RequestHeader("X-User-Id") String operatorId) {
        try {
            Club updatedClub = clubService.removePresident(clubId, operatorId);
            return ResponseEntity.ok(ApiResponse.success("社长移除成功", updatedClub));
        } catch (Exception e) {
            return ResponseEntity.ok((ApiResponse<Club>) ApiResponse.error("移除失败：" + e.getMessage()));
        }
    }
}