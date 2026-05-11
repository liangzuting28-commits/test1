package com.club.managementsystem.repository;

import com.club.managementsystem.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Integer> {
    List<Activity> findByClubId(Integer clubId);
    List<Activity> findByStatus(Activity.Status status);
    List<Activity> findByClubIdAndStatus(Integer clubId, Activity.Status status);
    Activity findByCode(String code);
    Activity findByCheckInNumber(String checkInNumber);
}