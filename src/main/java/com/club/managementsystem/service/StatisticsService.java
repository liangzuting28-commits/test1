package com.club.managementsystem.service;

import com.club.managementsystem.entity.*;
import com.club.managementsystem.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    private final StudentRepository studentRepository;
    private final ClubRepository clubRepository;
    private final ActivityRepository activityRepository;
    private final ActivityRegistrationRepository activityRegistrationRepository;
    private final PointsRecordRepository pointsRecordRepository;
    private final ClubMembershipRepository clubMembershipRepository;
    
    public StatisticsService(StudentRepository studentRepository,
                           ClubRepository clubRepository,
                           ActivityRepository activityRepository,
                           ActivityRegistrationRepository activityRegistrationRepository,
                           PointsRecordRepository pointsRecordRepository,
                           ClubMembershipRepository clubMembershipRepository) {
        this.studentRepository = studentRepository;
        this.clubRepository = clubRepository;
        this.activityRepository = activityRepository;
        this.activityRegistrationRepository = activityRegistrationRepository;
        this.pointsRecordRepository = pointsRecordRepository;
        this.clubMembershipRepository = clubMembershipRepository;
    }

    // 获取系统概览统计
    public Map<String, Object> getOverviewStatistics() {
        Map<String, Object> stats = new HashMap<>();

        try {
            // 学生统计
            List<Student> students = studentRepository.findAll();
            long studentCount = students.stream()
                    .filter(s -> s.getRole() == Student.Role.STUDENT)
                    .count();
            long presidentCount = students.stream()
                    .filter(s -> s.getRole() == Student.Role.PRESIDENT)
                    .count();
            long teacherCount = students.stream()
                    .filter(s -> s.getRole() == Student.Role.TEACHER)
                    .count();

            // 计算活跃学生数（参与过活动的学生）
            List<PointsRecord> pointsRecords = pointsRecordRepository.findAll();
            long activeStudentCount = pointsRecords.stream()
                    .map(PointsRecord::getStudentId)
                    .distinct()
                    .count();

            // 社团统计
            List<Club> clubs = clubRepository.findAll();
            long clubCount = clubs.size();

            // 社团类型统计
            Map<String, Long> clubTypeCount = clubs.stream()
                    .collect(Collectors.groupingBy(Club::getClubType, Collectors.counting()));

            // 活动统计
            List<Activity> activities = activityRepository.findAll();
            long activityCount = activities.size();
            long publishedActivityCount = activities.stream()
                    .filter(a -> a.getStatus() == Activity.Status.PUBLISHED)
                    .count();
            long ongoingActivityCount = activities.stream()
                    .filter(a -> a.getStatus() == Activity.Status.REGISTERING)
                    .count();
            long endedActivityCount = activities.stream()
                    .filter(a -> a.getStatus() == Activity.Status.ENDED)
                    .count();

            // 积分统计
            Integer totalPoints = students.stream()
                    .mapToInt(Student::getTotalPoints)
                    .sum();

            stats.put("studentCount", studentCount);
            stats.put("activeStudentCount", activeStudentCount);
            stats.put("presidentCount", presidentCount);
            stats.put("teacherCount", teacherCount);
            stats.put("clubCount", clubCount);
            stats.put("clubTypeCount", clubTypeCount);
            stats.put("activityCount", activityCount);
            stats.put("publishedActivityCount", publishedActivityCount);
            stats.put("ongoingActivityCount", ongoingActivityCount);
            stats.put("endedActivityCount", endedActivityCount);
            stats.put("totalPoints", totalPoints);
            
            // 添加时间戳，确保前端知道数据是最新的
            stats.put("lastUpdated", System.currentTimeMillis());
        } catch (Exception e) {
            // 记录错误但继续返回空统计，避免前端崩溃
            System.err.println("获取系统概览统计时出错: " + e.getMessage());
            e.printStackTrace();
            
            // 返回默认值
            stats.put("studentCount", 0);
            stats.put("activeStudentCount", 0);
            stats.put("presidentCount", 0);
            stats.put("teacherCount", 0);
            stats.put("clubCount", 0);
            stats.put("clubTypeCount", new HashMap<>());
            stats.put("activityCount", 0);
            stats.put("publishedActivityCount", 0);
            stats.put("ongoingActivityCount", 0);
            stats.put("endedActivityCount", 0);
            stats.put("totalPoints", 0);
            stats.put("lastUpdated", System.currentTimeMillis());
        }

        return stats;
    }
    
    // 获取管理员仪表盘统计数据
    public Map<String, Object> getAdminDashboardStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // 学生统计
            List<Student> students = studentRepository.findAll();
            long totalUserCount = students.size();
            long studentCount = students.stream()
                    .filter(s -> s.getRole() == Student.Role.STUDENT)
                    .count();
            long teacherCount = students.stream()
                    .filter(s -> s.getRole() == Student.Role.TEACHER)
                    .count();
            
            // 活动统计
            List<Activity> activities = activityRepository.findAll();
            long totalActivityCount = activities.size();
            
            stats.put("totalUserCount", totalUserCount);
            stats.put("totalActivityCount", totalActivityCount);
            stats.put("studentCount", studentCount);
            stats.put("teacherCount", teacherCount);
            
            // 添加时间戳，确保前端知道数据是最新的
            stats.put("lastUpdated", System.currentTimeMillis());
        } catch (Exception e) {
            // 记录错误但继续返回空统计，避免前端崩溃
            System.err.println("获取管理员仪表盘统计时出错: " + e.getMessage());
            e.printStackTrace();
            
            // 返回默认值
            stats.put("totalUserCount", 0);
            stats.put("totalActivityCount", 0);
            stats.put("studentCount", 0);
            stats.put("teacherCount", 0);
            stats.put("lastUpdated", System.currentTimeMillis());
        }
        
        return stats;
    }

    // 获取活动统计
    public Map<String, Object> getActivityStatistics() {
        Map<String, Object> stats = new HashMap<>();

        try {
            List<Activity> activities = activityRepository.findAll();

            // 按状态统计
            Map<Activity.Status, Long> statusCount = activities.stream()
                    .collect(Collectors.groupingBy(Activity::getStatus, Collectors.counting()));

            // 按社团统计
            Map<Integer, Long> clubActivityCount = activities.stream()
                    .collect(Collectors.groupingBy(Activity::getClubId, Collectors.counting()));

            // 参与人数统计
            Map<Integer, Long> activityParticipants = new HashMap<>();
            for (Activity activity : activities) {
                long participantCount = activityRegistrationRepository.countByActivityId(activity.getActivityId());
                activityParticipants.put(activity.getActivityId(), participantCount);
            }

            stats.put("statusCount", statusCount);
            stats.put("clubActivityCount", clubActivityCount);
            stats.put("activityParticipants", activityParticipants);
            stats.put("lastUpdated", System.currentTimeMillis());
        } catch (Exception e) {
            System.err.println("获取活动统计时出错: " + e.getMessage());
            e.printStackTrace();
            
            // 返回默认值
            stats.put("statusCount", new HashMap<>());
            stats.put("clubActivityCount", new HashMap<>());
            stats.put("activityParticipants", new HashMap<>());
            stats.put("lastUpdated", System.currentTimeMillis());
        }

        return stats;
    }

    // 获取社团统计
    public Map<String, Object> getClubStatistics() {
        Map<String, Object> stats = new HashMap<>();

        try {
            List<Club> clubs = clubRepository.findAll();
            List<Activity> activities = activityRepository.findAll();

            // 社团活动数量
            List<Map<String, Object>> clubStats = clubs.stream().map(club -> {
                Map<String, Object> clubInfo = new HashMap<>();
                long activityCount = activities.stream()
                        .filter(activity -> activity.getClubId().equals(club.getClubId()))
                        .count();

                clubInfo.put("clubId", club.getClubId());
                clubInfo.put("clubName", club.getClubName());
                clubInfo.put("activityCount", activityCount);
                clubInfo.put("clubType", club.getClubType());

                return clubInfo;
            }).collect(Collectors.toList());

            // 按类型统计社团
            Map<String, Long> clubTypeCount = clubs.stream()
                    .collect(Collectors.groupingBy(Club::getClubType, Collectors.counting()));

            stats.put("clubStats", clubStats);
            stats.put("clubTypeCount", clubTypeCount);
            stats.put("lastUpdated", System.currentTimeMillis());
        } catch (Exception e) {
            System.err.println("获取社团统计时出错: " + e.getMessage());
            e.printStackTrace();
            
            // 返回默认值
            stats.put("clubStats", new ArrayList<>());
            stats.put("clubTypeCount", new HashMap<>());
            stats.put("lastUpdated", System.currentTimeMillis());
        }

        return stats;
    }

    // 获取积分统计
    public Map<String, Object> getPointsStatistics() {
        Map<String, Object> stats = new HashMap<>();

        try {
            List<Student> students = studentRepository.findAll();
            List<PointsRecord> pointsRecords = pointsRecordRepository.findAll();

            // 积分分布
            Map<String, Long> pointsDistribution = students.stream()
                    .collect(Collectors.groupingBy(
                            student -> {
                                int points = student.getTotalPoints();
                                if (points == 0) return "0";
                                else if (points <= 10) return "1-10";
                                else if (points <= 50) return "11-50";
                                else if (points <= 100) return "51-100";
                                else return "100+";
                            },
                            Collectors.counting()
                    ));

            // 积分来源统计
            Map<Integer, Long> pointsByActivity = pointsRecords.stream()
                    .collect(Collectors.groupingBy(
                            PointsRecord::getActivityId,
                            Collectors.summingLong(PointsRecord::getPointsEarned)
                    ));

            stats.put("pointsDistribution", pointsDistribution);
            stats.put("pointsByActivity", pointsByActivity);
            stats.put("totalPointsEarned", pointsRecords.stream()
                    .mapToInt(PointsRecord::getPointsEarned)
                    .sum());
            stats.put("lastUpdated", System.currentTimeMillis());
        } catch (Exception e) {
            System.err.println("获取积分统计时出错: " + e.getMessage());
            e.printStackTrace();
            
            // 返回默认值
            stats.put("pointsDistribution", new HashMap<>());
            stats.put("pointsByActivity", new HashMap<>());
            stats.put("totalPointsEarned", 0);
            stats.put("lastUpdated", System.currentTimeMillis());
        }

        return stats;
    }



    
    // 获取活动类型分布
    public List<Map<String, Object>> getActivityTypeDistribution(String semester, String clubId) {
        List<Activity> activities = activityRepository.findAll();
        
        // 根据条件筛选
        if (semester != null && !semester.equals("all")) {
            // 根据学期筛选活动
            activities = activities.stream()
                    .filter(a -> {
                        if (a.getCreatedAt() == null) return false;
                        int year = a.getCreatedAt().getYear(); // LocalDateTime.getYear() 返回实际年份，无需调整
                        int month = a.getCreatedAt().getMonthValue();
                        
                        // 根据学期参数筛选
                        if (semester.equals("2023-1")) {
                            return year == 2023 && (month >= 1 && month <= 6);
                        } else if (semester.equals("2023-2")) {
                            return year == 2023 && (month >= 9 && month <= 12);
                        } else if (semester.equals("2024-1")) {
                            return year == 2024 && (month >= 1 && month <= 6);
                        } else if (semester.equals("2024-2")) {
                            return year == 2024 && (month >= 9 && month <= 12);
                        } else if (semester.equals("2025-1")) {
                            return year == 2025 && (month >= 1 && month <= 6);
                        } else if (semester.equals("2025-2")) {
                            return year == 2025 && (month >= 9 && month <= 12);
                        }
                        return false;
                    })
                    .collect(Collectors.toList());
        }
        
        if (clubId != null && !clubId.equals("all")) {
            activities = activities.stream()
                    .filter(a -> clubId.equals(String.valueOf(a.getClubId())))
                    .collect(Collectors.toList());
        }
        
        // 按活动类型统计
        Map<String, Long> typeCount = activities.stream()
                .filter(a -> a.getActivityType() != null)
                .collect(Collectors.groupingBy(Activity::getActivityType, Collectors.counting()));
        
        // 转换为前端需要的格式
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Long> entry : typeCount.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("type", entry.getKey());
            item.put("count", entry.getValue());
            result.add(item);
        }
        
        return result;
    }
    
    // 获取社团活动数量
    public List<Map<String, Object>> getClubActivityCount(String semester, String activityType) {
        List<Activity> activities = activityRepository.findAll();
        List<Club> clubs = clubRepository.findAll();
        
        // 根据条件筛选
        if (semester != null && !semester.equals("all")) {
            // 根据学期筛选活动
            activities = activities.stream()
                    .filter(a -> {
                        if (a.getCreatedAt() == null) return false;
                        int year = a.getCreatedAt().getYear(); // LocalDateTime.getYear() 返回实际年份，无需调整
                        int month = a.getCreatedAt().getMonthValue();
                        
                        // 根据学期参数筛选
                        if (semester.equals("2023-1")) {
                            return year == 2023 && (month >= 1 && month <= 6);
                        } else if (semester.equals("2023-2")) {
                            return year == 2023 && (month >= 9 && month <= 12);
                        } else if (semester.equals("2024-1")) {
                            return year == 2024 && (month >= 1 && month <= 6);
                        } else if (semester.equals("2024-2")) {
                            return year == 2024 && (month >= 9 && month <= 12);
                        } else if (semester.equals("2025-1")) {
                            return year == 2025 && (month >= 1 && month <= 6);
                        } else if (semester.equals("2025-2")) {
                            return year == 2025 && (month >= 9 && month <= 12);
                        }
                        return false;
                    })
                    .collect(Collectors.toList());
        }
        
        if (activityType != null && !activityType.equals("all")) {
            activities = activities.stream()
                    .filter(a -> activityType.equals(a.getActivityType()))
                    .collect(Collectors.toList());
        }
        
        // 按社团统计活动数量
        Map<Integer, Long> clubActivityCount = activities.stream()
                .collect(Collectors.groupingBy(Activity::getClubId, Collectors.counting()));
        
        // 转换为前端需要的格式，包含社团名称
        List<Map<String, Object>> result = new ArrayList<>();
        for (Club club : clubs) {
            Long count = clubActivityCount.getOrDefault(club.getClubId(), 0L);
            Map<String, Object> item = new HashMap<>();
            item.put("clubId", club.getClubId());
            item.put("clubName", club.getClubName());
            item.put("count", count);
            result.add(item);
        }
        
        // 按活动数量排序
        result.sort((a, b) -> ((Long) b.get("count")).compareTo((Long) a.get("count")));
        
        return result;
    }
    
    // 获取积分发放趋势
    public List<Map<String, Object>> getPointsTrend(String semester, String clubId) {
        List<PointsRecord> pointsRecords = pointsRecordRepository.findAll();
        List<Activity> activities = activityRepository.findAll();
        
        // 根据条件筛选
        if (semester != null && !semester.equals("all")) {
            // 获取指定学期的活动
            Set<Integer> semesterActivityIds = activities.stream()
                    .filter(a -> {
                        if (a.getCreatedAt() == null) return false;
                        int year = a.getCreatedAt().getYear();
                        int month = a.getCreatedAt().getMonthValue();
                        
                        // 根据学期参数筛选
                        if (semester.equals("2023-1")) {
                            return year == 2023 && (month >= 1 && month <= 6);
                        } else if (semester.equals("2023-2")) {
                            return year == 2023 && (month >= 9 && month <= 12);
                        } else if (semester.equals("2024-1")) {
                            return year == 2024 && (month >= 1 && month <= 6);
                        } else if (semester.equals("2024-2")) {
                            return year == 2024 && (month >= 9 && month <= 12);
                        } else if (semester.equals("2025-1")) {
                            return year == 2025 && (month >= 1 && month <= 6);
                        } else if (semester.equals("2025-2")) {
                            return year == 2025 && (month >= 9 && month <= 12);
                        }
                        return false;
                    })
                    .map(Activity::getActivityId)
                    .collect(Collectors.toSet());
            
            // 只保留这些活动的积分记录
            pointsRecords = pointsRecords.stream()
                    .filter(r -> semesterActivityIds.contains(r.getActivityId()))
                    .collect(Collectors.toList());
        }
        
        if (clubId != null && !clubId.equals("all")) {
            // 获取指定社团的活动
            Set<Integer> clubActivityIds = activities.stream()
                    .filter(a -> clubId.equals(String.valueOf(a.getClubId())))
                    .map(Activity::getActivityId)
                    .collect(Collectors.toSet());
            
            // 只保留这些活动的积分记录
            pointsRecords = pointsRecords.stream()
                    .filter(r -> clubActivityIds.contains(r.getActivityId()))
                    .collect(Collectors.toList());
        }
        
        // 按月份统计积分发放量
        Map<String, Integer> monthlyPoints = new HashMap<>();
        
        // 初始化最近6个月的数据
        for (int i = 5; i >= 0; i--) {
            java.time.LocalDate date = java.time.LocalDate.now().minusMonths(i);
            String monthKey = date.getYear() + "-" + String.format("%02d", date.getMonthValue());
            monthlyPoints.put(monthKey, 0);
        }
        
        // 统计每月积分
        for (PointsRecord record : pointsRecords) {
            if (record.getEarnedTime() != null) {
                java.time.LocalDate date = record.getEarnedTime().toLocalDate();
                String monthKey = date.getYear() + "-" + String.format("%02d", date.getMonthValue());
                
                if (monthlyPoints.containsKey(monthKey)) {
                    monthlyPoints.put(monthKey, monthlyPoints.get(monthKey) + record.getPointsEarned());
                }
            }
        }
        
        // 转换为前端需要的格式
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : monthlyPoints.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("month", entry.getKey());
            item.put("points", entry.getValue());
            result.add(item);
        }
        
        return result;
    }
    
    // 获取活动参与度排行
    public List<Map<String, Object>> getParticipationRanking(String semester, String clubId, String activityType, int limit) {
        List<Activity> activities = activityRepository.findAll();
        
        // 根据条件筛选
        if (semester != null && !semester.equals("all")) {
            // 根据学期筛选活动
            activities = activities.stream()
                    .filter(a -> {
                        if (a.getCreatedAt() == null) return false;
                        int year = a.getCreatedAt().getYear();
                        int month = a.getCreatedAt().getMonthValue();
                        
                        // 根据学期参数筛选
                        if (semester.equals("2023-1")) {
                            return year == 2023 && (month >= 1 && month <= 6);
                        } else if (semester.equals("2023-2")) {
                            return year == 2023 && (month >= 9 && month <= 12);
                        } else if (semester.equals("2024-1")) {
                            return year == 2024 && (month >= 1 && month <= 6);
                        } else if (semester.equals("2024-2")) {
                            return year == 2024 && (month >= 9 && month <= 12);
                        } else if (semester.equals("2025-1")) {
                            return year == 2025 && (month >= 1 && month <= 6);
                        } else if (semester.equals("2025-2")) {
                            return year == 2025 && (month >= 9 && month <= 12);
                        }
                        return false;
                    })
                    .collect(Collectors.toList());
        }
        
        if (clubId != null && !clubId.equals("all")) {
            activities = activities.stream()
                    .filter(a -> clubId.equals(String.valueOf(a.getClubId())))
                    .collect(Collectors.toList());
        }
        
        if (activityType != null && !activityType.equals("all")) {
            activities = activities.stream()
                    .filter(a -> activityType.equals(a.getActivityType()))
                    .collect(Collectors.toList());
        }
        
        // 只统计已发布的活动
        activities = activities.stream()
                .filter(a -> a.getStatus() == Activity.Status.PUBLISHED || a.getStatus() == Activity.Status.ENDED)
                .collect(Collectors.toList());
        
        // 计算每个活动的参与度
        List<Map<String, Object>> result = new ArrayList<>();
        for (Activity activity : activities) {
            long participantCount = activityRegistrationRepository.countByActivityId(activity.getActivityId());
            int maxParticipants = activity.getMaxParticipants();
            
            // 计算参与率
            double participationRate = maxParticipants > 0 ? (double) participantCount / maxParticipants * 100 : 0;
            
            Map<String, Object> item = new HashMap<>();
            item.put("activityId", activity.getActivityId());
            item.put("activityName", activity.getActivityName());
            item.put("clubId", activity.getClubId());
            
            // 获取社团名称
            Optional<Club> club = clubRepository.findById(activity.getClubId());
            if (club.isPresent()) {
                item.put("clubName", club.get().getClubName());
            } else {
                item.put("clubName", "未知社团");
            }
            
            item.put("activityType", activity.getActivityType());
            item.put("participantCount", participantCount);
            item.put("maxParticipants", maxParticipants);
            item.put("participationRate", Math.round(participationRate * 10) / 10.0); // 保留一位小数
            item.put("activityTime", activity.getActivityTime());
            
            result.add(item);
        }
        
        // 按参与率排序
        result.sort((a, b) -> ((Double) b.get("participationRate")).compareTo((Double) a.get("participationRate")));
        
        // 限制返回数量
        if (limit > 0 && result.size() > limit) {
            result = result.subList(0, limit);
        }
        
        return result;
    }
    
    // 获取所有社团列表（用于筛选）
    public List<Map<String, Object>> getAllClubs() {
        List<Club> clubs = clubRepository.findAll();
        
        return clubs.stream().map(club -> {
            Map<String, Object> item = new HashMap<>();
            item.put("clubId", club.getClubId());
            item.put("clubName", club.getClubName());
            item.put("clubType", club.getClubType());
            return item;
        }).collect(Collectors.toList());
    }
}