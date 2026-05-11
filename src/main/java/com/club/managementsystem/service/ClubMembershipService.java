package com.club.managementsystem.service;

import com.club.managementsystem.entity.Club;
import com.club.managementsystem.entity.ClubMembership;
import com.club.managementsystem.entity.Student;
import com.club.managementsystem.repository.ClubMembershipRepository;
import com.club.managementsystem.repository.ClubRepository;
import com.club.managementsystem.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClubMembershipService {

    private final ClubMembershipRepository clubMembershipRepository;
    private final ClubRepository clubRepository;
    private final StudentRepository studentRepository;
    
    public ClubMembershipService(ClubMembershipRepository clubMembershipRepository,
                                ClubRepository clubRepository,
                                StudentRepository studentRepository) {
        this.clubMembershipRepository = clubMembershipRepository;
        this.clubRepository = clubRepository;
        this.studentRepository = studentRepository;
    }

    /**
     * 获取社团的已通过成员
     */
    public List<ClubMembership> getApprovedClubMembers(Integer clubId) {
        return clubMembershipRepository.findByClubIdAndApplicationStatus(clubId, "APPROVED");
    }

    /**
     * 获取社团的待审核申请
     */
    public List<ClubMembership> getPendingApplications(Integer clubId) {
        // 获取加入申请和退出申请
        List<ClubMembership> joinApplications = clubMembershipRepository.findByClubIdAndApplicationStatus(clubId, "PENDING");
        List<ClubMembership> leaveApplications = clubMembershipRepository.findByClubIdAndApplicationStatus(clubId, "PENDING_LEAVE");
        
        // 合并两个列表
        joinApplications.addAll(leaveApplications);
        return joinApplications;
    }

    /**
     * 根据成员ID获取成员信息
     */
    public ClubMembership getMembershipById(Integer membershipId) {
        Optional<ClubMembership> membershipOpt = clubMembershipRepository.findById(membershipId);
        if (membershipOpt.isEmpty()) {
            throw new RuntimeException("成员记录不存在");
        }
        return membershipOpt.get();
    }

    /**
     * 社长移除成员（权限校验）
     */
    public void removeMember(Integer clubId, String targetStudentId, String reason, String operatorId) {
        // 1. 校验操作者是否为社长
        if (!isPresident(operatorId, clubId)) {
            throw new RuntimeException("权限不足：仅社长可移除成员");
        }

        // 2. 校验目标成员是否存在
        List<ClubMembership> memberships = clubMembershipRepository.findByStudentIdAndClubId(targetStudentId, clubId);
        if (memberships.isEmpty()) {
            throw new RuntimeException("该学生不是社团成员");
        }

        // 查找已批准的成员记录
        ClubMembership target = null;
        for (ClubMembership membership : memberships) {
            if ("APPROVED".equals(membership.getApplicationStatus())) {
                target = membership;
                break;
            }
        }
        
        if (target == null) {
            throw new RuntimeException("该学生不是社团的正式成员");
        }

        // 3. 移除成员（包括社长）
        // 注意：移除社长将导致社团没有社长，需要后续处理

        // 4. 记录移除原因
        target.setApplicationStatus("REMOVED");
        target.setRemovalReason(reason);
        target.setRemovalDate(java.time.LocalDateTime.now());
        clubMembershipRepository.save(target);
    }

    /**
     * 校验是否为社团社长
     */
    public boolean isPresident(String studentId, Integer clubId) {
        Optional<Club> clubOpt = clubRepository.findById(clubId);
        return clubOpt.isPresent() && clubOpt.get().getPresidentId().equals(studentId);
    }

    // 其他基础方法
    public List<ClubMembership> getClubMembers(Integer clubId) {
        return clubMembershipRepository.findByClubIdAndApplicationStatus(clubId, "APPROVED");
    }

    public List<ClubMembership> getStudentClubs(String studentId) {
        // 返回已加入(APPROVED)和待审核(PENDING)的社团记录，但过滤掉已退出(LEFT/REMOVED/PENDING_LEAVE)的记录
        List<ClubMembership> allMemberships = clubMembershipRepository.findByStudentId(studentId);
        return allMemberships.stream()
                .filter(membership -> "APPROVED".equals(membership.getApplicationStatus())
                                    || "PENDING".equals(membership.getApplicationStatus()))
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 学生申请退出社团
     */
    public void requestLeaveClub(Integer clubId, String studentId, String reason) {
        System.out.println("申请退出社团: clubId=" + clubId + ", studentId=" + studentId + ", reason=" + reason);
        
        // 检查学生是否是该社团成员
        List<ClubMembership> memberships = clubMembershipRepository.findByStudentIdAndClubId(studentId, clubId);
        if (memberships.isEmpty()) {
            System.out.println("错误: 学生不是该社团的成员");
            throw new RuntimeException("您不是该社团的成员");
        }
        
        // 查找已批准的成员记录
        ClubMembership membership = null;
        for (ClubMembership m : memberships) {
            if ("APPROVED".equals(m.getApplicationStatus())) {
                membership = m;
                break;
            }
        }
        
        if (membership == null) {
            System.out.println("错误: 只有已通过审核的成员才能申请退出");
            throw new RuntimeException("只有已通过审核的成员才能申请退出");
        }
        System.out.println("找到成员关系: " + membership);
        System.out.println("当前状态: " + membership.getApplicationStatus());
        

        
        // 更新状态为待审核退出
        membership.setApplicationStatus("PENDING_LEAVE");
        membership.setRemovalReason(reason);
        ClubMembership saved = clubMembershipRepository.save(membership);
        System.out.println("保存后的成员关系: " + saved);
        System.out.println("保存后的状态: " + saved.getApplicationStatus());
    }

    public ClubMembership updateMemberRole(Integer membershipId, ClubMembership.Role role, String operatorId) {
        Optional<ClubMembership> membershipOpt = clubMembershipRepository.findById(membershipId);
        if (membershipOpt.isEmpty()) {
            throw new RuntimeException("成员关系不存在");
        }
        ClubMembership membership = membershipOpt.get();
        // 仅社长可修改角色
        if (!isPresident(operatorId, membership.getClubId())) {
            throw new RuntimeException("权限不足：仅社长可修改成员角色");
        }
        // 禁止修改社长角色
        if (membership.getRole() == ClubMembership.Role.PRESIDENT) {
            throw new RuntimeException("无法修改社长角色");
        }
        membership.setRole(role);
        return clubMembershipRepository.save(membership);
    }

    // 社长获取社团成员（验证社长权限）
    public List<ClubMembership> getClubMembersByPresident(Integer clubId, String presidentId) {
        // 校验：社长是否有权管理该社团
        if (!isPresident(presidentId, clubId)) {
            throw new RuntimeException("无权查看该社团成员");
        }
        // 返回该社团所有已通过的成员
        return clubMembershipRepository.findByClubIdAndApplicationStatus(clubId, "APPROVED");
    }

    // 社长移除成员（验证社长权限）
    public void removeMemberByPresident(Integer clubId, String targetStudentId, String presidentId) {
        // 校验：社长权限
        if (!isPresident(presidentId, clubId)) {
            throw new RuntimeException("无权移除该社团成员");
        }

        // 校验：目标成员是否存在
        List<ClubMembership> memberships = clubMembershipRepository.findByStudentIdAndClubId(targetStudentId, clubId);
        if (memberships.isEmpty()) {
            throw new RuntimeException("该学生不是社团成员");
        }

        // 查找已批准的成员记录
        ClubMembership target = null;
        for (ClubMembership membership : memberships) {
            if ("APPROVED".equals(membership.getApplicationStatus())) {
                target = membership;
                break;
            }
        }
        
        if (target == null) {
            throw new RuntimeException("该学生不是社团的正式成员");
        }

        // 移除成员（包括社长）
        // 注意：移除社长将导致社团没有社长，需要后续处理

        // 移除成员
        clubMembershipRepository.delete(target);
    }
    
    /**
     * 学生取消申请
     */
    public void cancelApplication(Integer clubId, String studentId, String reason) {
        // 检查申请是否存在
        List<ClubMembership> memberships = clubMembershipRepository.findByStudentIdAndClubId(studentId, clubId);
        if (memberships.isEmpty()) {
            throw new RuntimeException("申请记录不存在");
        }
        
        // 查找待审核状态的申请
        ClubMembership membership = null;
        for (ClubMembership m : memberships) {
            if ("PENDING".equals(m.getApplicationStatus()) || "PENDING_LEAVE".equals(m.getApplicationStatus())) {
                membership = m;
                break;
            }
        }
        
        if (membership == null) {
            // 添加调试信息
            System.out.println("学生 " + studentId + " 对社团 " + clubId + " 的申请状态:");
            for (ClubMembership m : memberships) {
                System.out.println("- 状态: " + m.getApplicationStatus());
            }
            throw new RuntimeException("只有待审核状态的申请才能取消");
        }
        
        // 删除申请记录
        clubMembershipRepository.delete(membership);
    }
}