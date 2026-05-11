package com.club.managementsystem.repository;

import com.club.managementsystem.entity.Club;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClubRepository extends JpaRepository<Club, Integer> {
    /**
     * 关键方法：根据社长ID查询其管理的所有社团（实现社长-社团关联）
     * @param presidentId 社长学号
     * @return 社长管理的社团列表
     */
    @Query(value = "SELECT * FROM clubs WHERE president_id = :presidentId", nativeQuery = true)
    List<Club> findByPresidentId(@Param("presidentId") String presidentId);

    /**
     * 检查社团名称是否重复
     * @param clubName 社团名称
     * @return 社团信息（存在则返回，不存在则空）
     */
    Optional<Club> findByClubName(String clubName);
}