package com.club.managementsystem.controller;

import com.club.managementsystem.dto.ApiResponse;
import com.club.managementsystem.dto.RemoveMemberRequest;
import com.club.managementsystem.entity.Club;
import com.club.managementsystem.entity.ClubMembership;
import com.club.managementsystem.entity.Student;
import com.club.managementsystem.service.ClubMembershipService;
import com.club.managementsystem.service.ClubService;
import com.club.managementsystem.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/member-manage")
@CrossOrigin(origins = "*")
public class MemberManageController {

    private final ClubMembershipService clubMembershipService;
    private final StudentService studentService;
    private final ClubService clubService;
    
    public MemberManageController(ClubMembershipService clubMembershipService, 
                                 StudentService studentService, 
                                 ClubService clubService) {
        this.clubMembershipService = clubMembershipService;
        this.studentService = studentService;
        this.clubService = clubService;
    }

    // 社长获取社团成员（含学生详情）
    @GetMapping("/club/{clubId}/members")
    public ResponseEntity<ApiResponse<List<Student>>> getClubMembersWithDetails(
            @PathVariable Integer clubId,
            @RequestHeader("X-User-Id") String operatorId) {
        try {
            // 验证社长权限
            if (!clubMembershipService.isPresident(operatorId, clubId)) {
                throw new RuntimeException("权限不足：仅社团社长可查看成员列表");
            }
            
            List<ClubMembership> memberships = clubMembershipService.getClubMembers(clubId);
            List<Student> members = memberships.stream()
                    .filter(m -> "APPROVED".equals(m.getApplicationStatus()))
                    .map(m -> studentService.findByStudentId(m.getStudentId()))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponse.success(members));
        } catch (Exception e) {
            return ResponseEntity.ok((ApiResponse<List<Student>>) ApiResponse.error("获取失败：" + e.getMessage()));
        }
    }

    // 社长移除成员
    @DeleteMapping("/club/{clubId}/member/{studentId}")
    public ResponseEntity<ApiResponse<?>> removeMember(
            @PathVariable Integer clubId,
            @PathVariable String studentId,
            @RequestBody RemoveMemberRequest request,
            @RequestHeader("X-User-Id") String operatorId) {
        try {
            clubMembershipService.removeMember(clubId, studentId, request.getReason(), operatorId);
            return ResponseEntity.ok(ApiResponse.success("成员移除成功", null));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("移除失败：" + e.getMessage()));
        }
    }

    // 社长更新成员角色
    @PutMapping("/{membershipId}/role")
    public ResponseEntity<ApiResponse<ClubMembership>> updateMemberRole(
            @PathVariable Integer membershipId,
            @RequestParam ClubMembership.Role role,
            @RequestHeader("X-User-Id") String operatorId) {
        try {
            // 验证社长权限
            ClubMembership membership = clubMembershipService.getMembershipById(membershipId);
            
            if (!clubMembershipService.isPresident(operatorId, membership.getClubId())) {
                throw new RuntimeException("权限不足：仅社团社长可修改成员角色");
            }
            
            ClubMembership mem = clubMembershipService.updateMemberRole(membershipId, role, operatorId);
            return ResponseEntity.ok(ApiResponse.success("角色更新成功", mem));
        } catch (Exception e) {
            return ResponseEntity.ok((ApiResponse<ClubMembership>) ApiResponse.error("更新失败：" + e.getMessage()));
        }
    }

    // 社长获取待审核申请（含详情）
    @GetMapping("/club/{clubId}/pending-applications")
    public ResponseEntity<ApiResponse<List<ClubMembership>>> getPendingApplicationsWithDetails(
            @PathVariable Integer clubId,
            @RequestHeader("X-User-Id") String presidentId) {
        try {
            // 验证社长权限
            if (!clubMembershipService.isPresident(presidentId, clubId)) {
                throw new RuntimeException("权限不足：仅社团社长可查看待审核申请");
            }
            
            List<ClubMembership> apps = clubMembershipService.getPendingApplications(clubId);
            return ResponseEntity.ok(ApiResponse.success(apps));
        } catch (Exception e) {
            return ResponseEntity.ok((ApiResponse<List<ClubMembership>>) ApiResponse.error("获取失败：" + e.getMessage()));
        }
    }
    
    // 学生获取已加入的社团
    @GetMapping("/student/{studentId}/clubs")
    public ResponseEntity<ApiResponse<List<Object>>> getStudentClubsWithDetails(@PathVariable String studentId) {
        try {
            // 获取学生的所有社团成员关系
            List<ClubMembership> memberships = clubMembershipService.getStudentClubs(studentId);
            
            // 转换为包含社团详情的列表
            List<Object> clubDetails = memberships.stream().map(membership -> {
                // 获取社团详情
                Optional<Club> clubOpt = clubService.findById(membership.getClubId());
                if (clubOpt.isEmpty()) {
                    return null;
                }
                
                Club club = clubOpt.get();
                // 创建包含社团和成员关系信息的对象
                return new Object() {
                    public final Integer clubId = club.getClubId();
                    public final String clubName = club.getClubName();
                    public final String clubType = club.getClubType();
                    public final String presidentId = club.getPresidentId();
                    public final String presidentName = getPresidentName(club.getPresidentId());
                    public final String contactInfo = club.getContactInfo();
                    public final String introduction = club.getIntroduction();
                    public final String applicationStatus = membership.getApplicationStatus();
                    public final java.time.LocalDateTime joinDate = membership.getJoinDate();
                    
                    // 获取社长姓名
                    private String getPresidentName(String presidentId) {
                        try {
                            Optional<Student> presidentOpt = studentService.findByStudentId(presidentId);
                            return presidentOpt.map(Student::getName).orElse(presidentId);
                        } catch (Exception e) {
                            return presidentId;
                        }
                    }
                };
            }).filter(obj -> obj != null).collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success(clubDetails));
        } catch (Exception e) {
            return ResponseEntity.ok((ApiResponse<List<Object>>) ApiResponse.error("获取失败：" + e.getMessage()));
        }
    }
    
    // 学生申请退出社团
    @PostMapping("/club/{clubId}/member/{studentId}/request-leave")
    public ResponseEntity<ApiResponse<?>> requestLeaveClub(
            @PathVariable Integer clubId,
            @PathVariable String studentId,
            @RequestBody Map<String, String> request) {
        try {
            String reason = request.get("reason");
            if (reason == null || reason.trim().isEmpty()) {
                return ResponseEntity.ok(ApiResponse.error("请提供退出原因"));
            }
            
            clubMembershipService.requestLeaveClub(clubId, studentId, reason);
            return ResponseEntity.ok(ApiResponse.success("退出申请已提交，等待社长审批", null));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("申请失败：" + e.getMessage()));
        }
    }
}