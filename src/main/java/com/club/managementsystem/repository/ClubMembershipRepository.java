package com.club.managementsystem.repository;

import com.club.managementsystem.entity.ClubMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClubMembershipRepository extends JpaRepository<ClubMembership, Integer> {
    // 根据学生ID查询其所有社团成员关系
    List<ClubMembership> findByStudentId(String studentId);

    // 根据社团ID查询所有成员关系
    List<ClubMembership> findByClubId(Integer clubId);

    // 查询学生在指定社团的成员关系
    List<ClubMembership> findByStudentIdAndClubId(String studentId, Integer clubId);

    // 检查学生是否已加入指定社团
    boolean existsByStudentIdAndClubId(String studentId, Integer clubId);

    // 统计社团成员总数
    long countByClubId(Integer clubId);

    // 根据社团ID和角色查询成员（如查询社长）
    List<ClubMembership> findByClubIdAndRole(Integer clubId, ClubMembership.Role role);

    /**
     * 关键方法：查询社团的待审核申请（社长审核用）
     * @param clubId 社团ID
     * @param applicationStatus 申请状态（PENDING）
     * @return 待审核申请列表
     */
    List<ClubMembership> findByClubIdAndApplicationStatus(Integer clubId, String applicationStatus);

    // 查询学生的待审核申请
    List<ClubMembership> findByStudentIdAndApplicationStatus(String studentId, String applicationStatus);

    // 统计社团待审核申请数
    long countByClubIdAndApplicationStatus(Integer clubId, String applicationStatus);
}