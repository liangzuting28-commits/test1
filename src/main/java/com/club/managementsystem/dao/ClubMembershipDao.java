package com.club.managementsystem.dao;

import com.club.managementsystem.entity.ClubMembership;
import java.util.List;
import java.util.Optional;

/**
 * ClubMembership DAO接口 - 定义社团成员关系数据访问方法
 */
public interface ClubMembershipDao {

    ClubMembership save(ClubMembership membership);

    Optional<ClubMembership> findById(Integer id);

    List<ClubMembership> findAll();

    void deleteById(Integer id);

    boolean existsById(Integer id);

    List<ClubMembership> findByStudentId(String studentId);

    List<ClubMembership> findByClubId(Integer clubId);

    List<ClubMembership> findByStatus(String status);
}
