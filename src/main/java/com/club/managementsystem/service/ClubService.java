package com.club.managementsystem.service;

import com.club.managementsystem.dto.ClubDTO;
import com.club.managementsystem.dto.CreateClubRequest;
import com.club.managementsystem.dto.MemberDetailDTO;
import com.club.managementsystem.dto.UpdateClubRequest;
import com.club.managementsystem.entity.Club;
import com.club.managementsystem.entity.ClubMembership;
import com.club.managementsystem.entity.Student;
import com.club.managementsystem.repository.ActivityRepository;
import com.club.managementsystem.repository.ClubMembershipRepository;
import com.club.managementsystem.repository.ClubRepository;
import com.club.managementsystem.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ClubService {

    private final ClubRepository clubRepository;
    private final StudentRepository studentRepository;
    private final ClubMembershipRepository clubMembershipRepository;
    private final ActivityRepository activityRepository;
    
    public ClubService(ClubRepository clubRepository, 
                      StudentRepository studentRepository,
                      ClubMembershipRepository clubMembershipRepository,

                      ActivityRepository activityRepository) {
        this.clubRepository = clubRepository;
        this.studentRepository = studentRepository;
        this.clubMembershipRepository = clubMembershipRepository;
        this.activityRepository = activityRepository;
    }

    /**
     * 老师创建社团（自动关联社长）
     */
    public Club createClub(CreateClubRequest request, String creatorId) {
        // 1. 校验创建者权限（仅老师可创建）
        Optional<Student> creatorOpt = studentRepository.findByStudentId(creatorId);
        if (creatorOpt.isEmpty() || creatorOpt.get().getRole() != Student.Role.TEACHER) {
            throw new RuntimeException("权限不足：仅老师可创建社团");
        }

        // 2. 校验社长是否存在且为学生身份
        Optional<Student> presidentOpt = studentRepository.findByStudentId(request.getPresidentId());
        if (presidentOpt.isEmpty() || presidentOpt.get().getRole() != Student.Role.STUDENT) {
            throw new RuntimeException("社长不存在或身份不正确");
        }

        // 3. 校验社团名称唯一性
        if (clubRepository.findByClubName(request.getClubName()).isPresent()) {
            throw new RuntimeException("社团名称已存在");
        }

        // 4. 创建社团（关联社长ID）
        Club club = new Club();
        club.setClubName(request.getClubName());
        club.setIntroduction(request.getIntroduction());
        club.setClubType(request.getClubType());
        club.setContactInfo(request.getContactInfo());
        club.setLogoUrl(request.getLogoUrl());
        club.setEstablishDate(LocalDateTime.now());
        club.setCreatedBy(creatorId);
        club.setPresidentId(request.getPresidentId()); // 核心：关联社长
        club.setStatus("ACTIVE"); // 设置默认状态
        Club savedClub = clubRepository.save(club);

        // 5. 自动创建社长的成员关系（状态：已通过）
        ClubMembership presidentMembership = new ClubMembership();
        presidentMembership.setStudentId(request.getPresidentId());
        presidentMembership.setClubId(savedClub.getClubId());
        presidentMembership.setRole(ClubMembership.Role.PRESIDENT);
        presidentMembership.setApplicationStatus("APPROVED"); // 社长无需审核
        presidentMembership.setJoinDate(LocalDateTime.now()); // 设置加入时间
        clubMembershipRepository.save(presidentMembership);

        return savedClub;
    }

    /**
     * 学生申请加入社团
     */
    public ClubMembership applyForClub(String studentId, Integer clubId, String message) {
        // 1. 校验是否已申请/已加入
        List<ClubMembership> existing = clubMembershipRepository.findByStudentIdAndClubId(studentId, clubId);
        
        // 检查是否有待审核的申请
        for (ClubMembership membership : existing) {
            if ("PENDING".equals(membership.getApplicationStatus())) {
                throw new RuntimeException("已提交申请，请勿重复提交");
            } else if ("APPROVED".equals(membership.getApplicationStatus())) {
                throw new RuntimeException("您已是该社团成员");
            }
        }
        
        // 检查是否有被拒绝的申请，如果有则更新该申请
        for (ClubMembership membership : existing) {
            if ("REJECTED".equals(membership.getApplicationStatus())) {
                // 被拒绝后可重新申请
                membership.setApplicationStatus("PENDING");
                membership.setJoinDate(LocalDateTime.now());
                return clubMembershipRepository.save(membership);
            }
        }

        // 2. 校验社团和学生是否存在
        if (clubRepository.findById(clubId).isEmpty()) {
            throw new RuntimeException("社团不存在");
        }
        if (studentRepository.findByStudentId(studentId).isEmpty()) {
            throw new RuntimeException("学生不存在");
        }

        // 3. 创建申请记录（状态：待审核）
        ClubMembership membership = new ClubMembership();
        membership.setStudentId(studentId);
        membership.setClubId(clubId);
        membership.setRole(ClubMembership.Role.MEMBER);
        membership.setApplicationStatus("PENDING");
        return clubMembershipRepository.save(membership);
    }

    /**
     * 社长审核成员申请
     */
    public ClubMembership reviewApplication(Integer membershipId, String status, String reason, String operatorId) {
        // 1. 校验申请记录是否存在
        Optional<ClubMembership> membershipOpt = clubMembershipRepository.findById(membershipId);
        if (membershipOpt.isEmpty()) {
            throw new RuntimeException("申请记录不存在");
        }
        ClubMembership membership = membershipOpt.get();
        Integer clubId = membership.getClubId();

        // 2. 校验社团是否存在
        Optional<Club> clubOpt = clubRepository.findById(clubId);
        if (clubOpt.isEmpty()) {
            throw new RuntimeException("社团已删除，无法审核");
        }

        // 3. 检查操作人是否为社长
        Club club = clubOpt.get();
        if (!club.getPresidentId().equals(operatorId)) {
            throw new RuntimeException("权限不足：仅社长可审核成员申请");
        }

        // 4. 检查是否是退团申请
        boolean isLeaveApplication = "PENDING_LEAVE".equals(membership.getApplicationStatus());
        
        // 5. 校验状态合法性
        if (!"APPROVED".equals(status) && !"REJECTED".equals(status)) {
            throw new RuntimeException("状态非法：仅支持 APPROVED（通过）/ REJECTED（拒绝）");
        }

        // 6. 更新申请状态
        if (isLeaveApplication) {
            // 处理退团申请
            if ("APPROVED".equals(status)) {
                // 批准退团申请，将状态更新为已离开
                membership.setApplicationStatus("LEFT");
                membership.setRemovalDate(LocalDateTime.now());
                membership.setRemovalReason(reason != null ? reason : "社长批准退团");
            } else {
                // 拒绝退团申请，恢复为正常成员状态
                membership.setApplicationStatus("APPROVED");
                membership.setRemovalReason(null);
            }
        } else {
            // 处理入团申请
            membership.setApplicationStatus(status);
            if ("APPROVED".equals(status)) {
                membership.setJoinDate(LocalDateTime.now()); // 通过时记录加入时间
            }
        }
        
        return clubMembershipRepository.save(membership);
    }

    /**
     * 验证是否有权删除社团
     */
    public void validateDeleteClubAccess(Integer clubId, String operatorId) {
        Optional<Student> operatorOpt = studentRepository.findById(operatorId);
        if (operatorOpt.isEmpty()) {
            throw new RuntimeException("操作者不存在");
        }
        
        // 老师或社长可删除社团
        Student.Role role = operatorOpt.get().getRole();
        if (role != Student.Role.TEACHER) {
            // 如果不是老师，检查是否是社长
            Optional<Club> clubOpt = clubRepository.findById(clubId);
            if (clubOpt.isEmpty() || !clubOpt.get().getPresidentId().equals(operatorId)) {
                throw new RuntimeException("权限不足：仅老师或社长可删除社团");
            }
        }
    }

    /**
     * 验证社长是否有权访问该社团
     */
    public void validatePresidentAccess(Integer clubId, String presidentId) {
        Optional<Club> clubOpt = clubRepository.findById(clubId);
        if (clubOpt.isEmpty() || !clubOpt.get().getPresidentId().equals(presidentId)) {
            throw new RuntimeException("无权访问该社团");
        }
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
     * 社长获取自己社团的待审核申请
     */
    public List<ClubMembership> getPendingApplications(Integer clubId, String presidentId) {
        // 查询待审核申请（包括加入申请和退团申请）
        List<ClubMembership> joinApplications = clubMembershipRepository.findByClubIdAndApplicationStatus(clubId, "PENDING");
        List<ClubMembership> leaveApplications = clubMembershipRepository.findByClubIdAndApplicationStatus(clubId, "PENDING_LEAVE");
        
        // 合并两个列表
        joinApplications.addAll(leaveApplications);
        return joinApplications;
    }

    /**
     * 获取学生已加入的社团
     */
    public List<ClubMembership> getStudentClubs(String studentId) {
        return clubMembershipRepository.findByStudentIdAndApplicationStatus(studentId, "APPROVED");
    }

    /**
     * 获取学生申请的社团（包括所有状态的申请）
     */
    public List<ClubMembership> getStudentApplications(String studentId) {
        return clubMembershipRepository.findByStudentId(studentId);
    }

    /**
     * 获取社团详情（含成员数、活动数）
     */
    public ClubDTO getClubDetail(Integer clubId) {
        Optional<Club> clubOpt = clubRepository.findById(clubId);
        if (clubOpt.isEmpty()) {
            throw new RuntimeException("社团不存在");
        }
        Club club = clubOpt.get();
        ClubDTO dto = new ClubDTO();

        // 基础信息
        dto.setClubId(club.getClubId());
        dto.setClubName(club.getClubName());
        dto.setIntroduction(club.getIntroduction());
        dto.setClubType(club.getClubType());
        dto.setEstablishDate(club.getEstablishDate());
        dto.setPresidentId(club.getPresidentId());
        dto.setCreatedAt(club.getCreatedAt());
        dto.setCreatedBy(club.getCreatedBy());
        dto.setStatus(club.getStatus());
        dto.setLogoUrl(club.getLogoUrl());
        dto.setContactInfo(club.getContactInfo());

        // 社长详情
        studentRepository.findByStudentId(club.getPresidentId())
                .ifPresent(president -> {
                    dto.setPresidentName(president.getName());
                    // 创建社长详细信息DTO
                    MemberDetailDTO presidentDetail = new MemberDetailDTO();
                    presidentDetail.setStudentId(president.getStudentId());
                    presidentDetail.setName(president.getName());
                    presidentDetail.setCollege(president.getCollege());
                    presidentDetail.setMajorClass(president.getMajorClass());
                    presidentDetail.setPhone(president.getPhone());
                    presidentDetail.setEmail(president.getEmail());
                    presidentDetail.setTotalPoints(president.getTotalPoints());
                    presidentDetail.setRole(ClubMembership.Role.PRESIDENT);
                    dto.setPresidentDetail(presidentDetail);
                });

        // 成员列表
        List<ClubMembership> members = clubMembershipRepository.findByClubIdAndApplicationStatus(clubId, "APPROVED");
        List<MemberDetailDTO> memberDTOs = members.stream()
                .map(membership -> {
                    MemberDetailDTO memberDTO = new MemberDetailDTO();
                    studentRepository.findByStudentId(membership.getStudentId())
                            .ifPresent(student -> {
                                memberDTO.setStudentId(student.getStudentId());
                                memberDTO.setName(student.getName());
                                memberDTO.setCollege(student.getCollege());
                                memberDTO.setMajorClass(student.getMajorClass());
                                memberDTO.setPhone(student.getPhone());
                                memberDTO.setEmail(student.getEmail());
                                memberDTO.setTotalPoints(student.getTotalPoints());
                            });
                    memberDTO.setRole(membership.getRole());
                    memberDTO.setJoinDate(membership.getJoinDate());
                    return memberDTO;
                })
                .collect(java.util.stream.Collectors.toList());
        dto.setMembers(memberDTOs);

        // 成员数（已通过）
        dto.setMemberCount(memberDTOs.size());

        // 活动数
        long activityCount = activityRepository.findByClubId(clubId).size();
        dto.setActivityCount((int) activityCount);

        return dto;
    }

    // 基础CRUD方法
    public List<Club> findAll() {
        return clubRepository.findAll();
    }

    public Optional<Club> findById(Integer clubId) {
        return clubRepository.findById(clubId);
    }

    public List<Club> findByPresidentId(String presidentId) {
        return clubRepository.findByPresidentId(presidentId);
    }

    public void deleteClub(Integer clubId, String operatorId) {
        // 验证删除权限
        validateDeleteClubAccess(clubId, operatorId);
        // 删除社团前先删除关联成员
        List<ClubMembership> memberships = clubMembershipRepository.findByClubId(clubId);
        clubMembershipRepository.deleteAll(memberships);
        // 删除社团
        clubRepository.deleteById(clubId);
    }

    /**
     * 学生删除自己的社团申请记录
     */
    public void deleteStudentApplication(Integer membershipId, String studentId) {
        // 1. 校验申请记录是否存在
        Optional<ClubMembership> membershipOpt = clubMembershipRepository.findById(membershipId);
        if (membershipOpt.isEmpty()) {
            throw new RuntimeException("申请记录不存在");
        }
        ClubMembership membership = membershipOpt.get();

        // 2. 校验是否是学生自己的申请记录
        if (!membership.getStudentId().equals(studentId)) {
            throw new RuntimeException("权限不足：只能删除自己的申请记录");
        }

        // 3. 校验申请记录状态（只能删除待审核的申请）
        if (!"PENDING".equals(membership.getApplicationStatus())) {
            throw new RuntimeException("只能删除待审核的申请记录");
        }

        // 4. 删除申请记录
        clubMembershipRepository.delete(membership);
    }

    /**
     * 更新社团信息
     */
    public Club updateClub(Integer clubId, UpdateClubRequest request, String userId) {
        // 1. 校验社团是否存在
        Optional<Club> clubOpt = clubRepository.findById(clubId);
        if (clubOpt.isEmpty()) {
            throw new RuntimeException("社团不存在");
        }
        Club club = clubOpt.get();

        // 2. 校验是否为社长操作
        if (!club.getPresidentId().equals(userId)) {
            throw new RuntimeException("权限不足：仅社长可修改社团信息");
        }

        // 3. 校验社团名称唯一性（如果修改了名称）
        if (!club.getClubName().equals(request.getClubName())) {
            Optional<Club> existingClub = clubRepository.findByClubName(request.getClubName());
            if (existingClub.isPresent()) {
                throw new RuntimeException("社团名称已存在");
            }
        }

        // 4. 更新社团信息
        club.setClubName(request.getClubName());
        club.setIntroduction(request.getIntroduction());
        club.setClubType(request.getClubType());
        club.setContactInfo(request.getContactInfo());
        
        // 如果有logoUrl则更新
        if (request.getLogoUrl() != null && !request.getLogoUrl().isEmpty()) {
            club.setLogoUrl(request.getLogoUrl());
        }

        // 如果有status则更新
        if (request.getStatus() != null) {
            club.setStatus(request.getStatus());
        }

        // 5. 保存更新后的社团信息
        return clubRepository.save(club);
    }

    /**
     * 更换社长（教师操作）
     */
    public Club changePresident(Integer clubId, String newPresidentId, String operatorId) {
        // 1. 校验社团是否存在
        Optional<Club> clubOpt = clubRepository.findById(clubId);
        if (clubOpt.isEmpty()) {
            throw new RuntimeException("社团不存在");
        }
        Club club = clubOpt.get();

        // 2. 校验操作人是否为教师
        Optional<Student> operatorOpt = studentRepository.findByStudentId(operatorId);
        if (operatorOpt.isEmpty() || operatorOpt.get().getRole() != Student.Role.TEACHER) {
            throw new RuntimeException("权限不足：仅教师可更换社长");
        }

        // 3. 校验新社长是否存在
        Optional<Student> newPresidentOpt = studentRepository.findByStudentId(newPresidentId);
        if (newPresidentOpt.isEmpty()) {
            throw new RuntimeException("新社长不存在");
        }

        // 4. 校验新社长是否已为该社团成员
        List<ClubMembership> memberships = clubMembershipRepository.findByStudentIdAndClubId(newPresidentId, clubId);
        boolean isMember = memberships.stream().anyMatch(m -> "APPROVED".equals(m.getApplicationStatus()));

        if (isMember) {
            // 如果是现有成员，更新其角色为社长
            for (ClubMembership membership : memberships) {
                if ("APPROVED".equals(membership.getApplicationStatus())) {
                    membership.setRole(ClubMembership.Role.PRESIDENT);
                    clubMembershipRepository.save(membership);
                }
            }
        } else {
            // 如果不是现有成员，创建新的社长成员关系
            ClubMembership presidentMembership = new ClubMembership();
            presidentMembership.setStudentId(newPresidentId);
            presidentMembership.setClubId(clubId);
            presidentMembership.setRole(ClubMembership.Role.PRESIDENT);
            presidentMembership.setApplicationStatus("APPROVED");
            presidentMembership.setJoinDate(LocalDateTime.now());
            clubMembershipRepository.save(presidentMembership);
        }

        // 5. 更新原社长的角色为普通成员
        String oldPresidentId = club.getPresidentId();
        List<ClubMembership> oldMemberships = clubMembershipRepository.findByStudentIdAndClubId(oldPresidentId, clubId);
        for (ClubMembership membership : oldMemberships) {
            if ("APPROVED".equals(membership.getApplicationStatus())) {
                membership.setRole(ClubMembership.Role.MEMBER);
                clubMembershipRepository.save(membership);
            }
        }

        // 6. 更新社团的社长ID
        club.setPresidentId(newPresidentId);
        return clubRepository.save(club);
    }

    /**
     * 移除社长（教师操作）
     */
    public Club removePresident(Integer clubId, String operatorId) {
        // 1. 校验社团是否存在
        Optional<Club> clubOpt = clubRepository.findById(clubId);
        if (clubOpt.isEmpty()) {
            throw new RuntimeException("社团不存在");
        }
        Club club = clubOpt.get();

        // 2. 校验操作人是否为教师
        Optional<Student> operatorOpt = studentRepository.findByStudentId(operatorId);
        if (operatorOpt.isEmpty() || operatorOpt.get().getRole() != Student.Role.TEACHER) {
            throw new RuntimeException("权限不足：仅教师可移除社长");
        }

        // 3. 移除原社长的成员关系
        String presidentId = club.getPresidentId();
        List<ClubMembership> memberships = clubMembershipRepository.findByStudentIdAndClubId(presidentId, clubId);
        for (ClubMembership membership : memberships) {
            membership.setApplicationStatus("LEFT");
            membership.setRemovalDate(LocalDateTime.now());
            membership.setRemovalReason("教师移除社长");
            clubMembershipRepository.save(membership);
        }

        // 4. 更新社团的社长ID为null
        club.setPresidentId(null);
        return clubRepository.save(club);
    }
}