package com.club.managementsystem.service;

import com.club.managementsystem.entity.Activity;
import com.club.managementsystem.entity.ActivityRegistration;
import com.club.managementsystem.entity.CheckIn;
import com.club.managementsystem.entity.Club;
import com.club.managementsystem.entity.PointsRecord;
import com.club.managementsystem.entity.Student;
import com.club.managementsystem.repository.ActivityRegistrationRepository;
import com.club.managementsystem.repository.ActivityRepository;
import com.club.managementsystem.repository.CheckInRepository;
import com.club.managementsystem.repository.ClubRepository;
import com.club.managementsystem.repository.PointsRecordRepository;
import com.club.managementsystem.repository.StudentRepository;
import com.club.managementsystem.util.QRCodeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CheckInService {
    private final QRCodeUtil qrCodeUtil;
    private final ActivityRepository activityRepository;
    private final ActivityRegistrationRepository activityRegistrationRepository;
    private final CheckInRepository checkInRepository;
    private final PointsRecordRepository pointsRecordRepository;
    private final StudentRepository studentRepository;
    private final ClubRepository clubRepository;
    
    public CheckInService(QRCodeUtil qrCodeUtil,
                         ActivityRepository activityRepository,
                         ActivityRegistrationRepository activityRegistrationRepository,
                         CheckInRepository checkInRepository,
                         PointsRecordRepository pointsRecordRepository,
                         StudentRepository studentRepository,
                         ClubRepository clubRepository) {
        this.qrCodeUtil = qrCodeUtil;
        this.activityRepository = activityRepository;
        this.activityRegistrationRepository = activityRegistrationRepository;
        this.checkInRepository = checkInRepository;
        this.pointsRecordRepository = pointsRecordRepository;
        this.studentRepository = studentRepository;
        this.clubRepository = clubRepository;
    }

    /**
     * 为活动生成签到二维码
     */
    public String generateCheckInQRCode(Integer activityId) throws Exception {
        Optional<Activity> activityOpt = activityRepository.findById(activityId);
        if (activityOpt.isEmpty()) {
            throw new RuntimeException("活动不存在");
        }

        Activity activity = activityOpt.get();
        // 只有已发布的活动才能生成签到码
        if (activity.getStatus() != Activity.Status.PUBLISHED) {
            throw new RuntimeException("只有已发布的活动才能生成签到码");
        }

        // 如果活动还没有签到数，生成一个六位数的唯一签到数
        String checkInNumber = activity.getCheckInNumber();
        if (checkInNumber == null || checkInNumber.isEmpty()) {
            checkInNumber = generateUniqueCheckInNumber();
            activity.setCheckInNumber(checkInNumber);
            activityRepository.save(activity);
            System.out.println("为活动 " + activityId + " 生成了签到数: " + checkInNumber);
        } else {
            System.out.println("活动 " + activityId + " 已有签到数: " + checkInNumber);
        }

        // 使用新的方法生成包含签到数的二维码
        String qrCode = qrCodeUtil.generateActivityCheckInQRCodeWithNumber(activityId, checkInNumber);
        System.out.println("为活动 " + activityId + " 生成了二维码");

        return qrCode;
    }

    /**
     * 生成六位数的唯一签到数
     */
    private String generateUniqueCheckInNumber() {
        // 生成六位数的随机数
        String checkInNumber;
        do {
            checkInNumber = String.format("%06d", (int)(Math.random() * 1000000));
        } while (activityRepository.findByCheckInNumber(checkInNumber) != null);

        return checkInNumber;
    }

    /**
     * 学生扫码签到
     */
    public boolean checkIn(String studentId, Integer activityId) {
        System.out.println("开始处理签到请求: 学生ID=" + studentId + ", 活动ID=" + activityId);

        // 检查活动是否存在
        Optional<Activity> activityOpt = activityRepository.findById(activityId);
        if (activityOpt.isEmpty()) {
            System.out.println("签到失败: 活动不存在，活动ID=" + activityId);
            throw new RuntimeException("活动不存在，请检查活动ID是否正确");
        }

        Activity activity = activityOpt.get();
        System.out.println("找到活动: " + activity.getActivityName() + ", 状态=" + activity.getStatus());

        // 检查活动状态
        if (activity.getStatus() != Activity.Status.PUBLISHED) {
            System.out.println("签到失败: 活动未发布，活动状态=" + activity.getStatus());
            throw new RuntimeException("活动尚未开始或已结束，无法签到");
        }

        // 检查学生是否报名了该活动
        Optional<ActivityRegistration> registrationOpt =
                activityRegistrationRepository.findByStudentIdAndActivityId(studentId, activityId);

        if (registrationOpt.isEmpty()) {
            System.out.println("签到失败: 学生未报名该活动，学生ID=" + studentId + ", 活动ID=" + activityId);
            throw new RuntimeException("您未报名该活动，请先报名后再签到");
        }

        ActivityRegistration registration = registrationOpt.get();
        System.out.println("找到报名记录: 签到状态=" + registration.getCheckInStatus());

        // 检查是否已经签到
        if (registration.getCheckInStatus()) {
            System.out.println("签到失败: 学生已经签到过了，学生ID=" + studentId + ", 活动ID=" + activityId);
            throw new RuntimeException("您已经签到过了，无需重复签到");
        }

        try {
            // 更新签到状态
            registration.setCheckInStatus(true);
            registration.setCheckInTime(LocalDateTime.now());
            activityRegistrationRepository.save(registration);
            System.out.println("签到状态已更新: 签到时间=" + registration.getCheckInTime());

            // 注意：积分将在活动结束后由社长统一发放，而不是签到时立即发放
            System.out.println("学生 " + studentId + " 已签到活动 " + activityId + "，积分将在活动结束后发放");

            return true;
        } catch (Exception e) {
            System.err.println("更新签到状态失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("系统繁忙，请稍后再试");
        }
    }

    /**
     * 为学生增加活动积分
     */
    private void addPointsForActivity(String studentId, Activity activity) {
        PointsRecord pointsRecord = new PointsRecord();
        pointsRecord.setStudentId(studentId);
        pointsRecord.setActivityId(activity.getActivityId());
        pointsRecord.setPointsEarned(activity.getAwardPoints());
        pointsRecordRepository.save(pointsRecord);

        // 更新学生的总积分
        Optional<Student> studentOpt = studentRepository.findByStudentId(studentId);
        if (studentOpt.isPresent()) {
            Student student = studentOpt.get();
            student.setTotalPoints(student.getTotalPoints() + activity.getAwardPoints());
            studentRepository.save(student);
        }
    }

    /**
     * 获取活动的签到情况
     */
    public Object getActivityCheckInInfo(Integer activityId) {
        // 获取活动信息
        Optional<Activity> activityOpt = activityRepository.findById(activityId);
        if (activityOpt.isEmpty()) {
            throw new RuntimeException("活动不存在");
        }

        // 获取报名该活动的所有记录
        List<ActivityRegistration> registrations = activityRegistrationRepository.findByActivityId(activityId);

        // 统计签到情况
        long totalParticipants = registrations.size();
        long checkedInParticipants = registrations.stream()
                .filter(ActivityRegistration::getCheckInStatus)
                .count();

        return Map.of(
                "activity", activityOpt.get(),
                "totalParticipants", totalParticipants,
                "checkedInParticipants", checkedInParticipants,
                "checkInRate", totalParticipants == 0 ? 0 : (double) checkedInParticipants / totalParticipants
        );
    }

    /**
     * 社长手动为学生签到
     */
    public boolean manualCheckIn(String presidentId, Integer activityId, String studentId) {
        // 检查活动是否存在
        Optional<Activity> activityOpt = activityRepository.findById(activityId);
        if (activityOpt.isEmpty()) {
            throw new RuntimeException("活动不存在");
        }

        Activity activity = activityOpt.get();

        // 检查社长是否有权限为该活动签到
        // 需要通过clubRepository获取社团信息
        Optional<Club> clubOpt = clubRepository.findById(activity.getClubId());
        if (clubOpt.isEmpty() || !clubOpt.get().getPresidentId().equals(presidentId)) {
            throw new RuntimeException("您没有权限为该活动进行手动签到");
        }

        // 检查学生是否报名了该活动
        Optional<ActivityRegistration> registrationOpt =
                activityRegistrationRepository.findByStudentIdAndActivityId(studentId, activityId);

        if (registrationOpt.isEmpty()) {
            throw new RuntimeException("该学生未报名此活动，无法签到");
        }

        ActivityRegistration registration = registrationOpt.get();

        // 检查是否已经签到
        if (registration.getCheckInStatus()) {
            throw new RuntimeException("该学生已经签到过了");
        }

        // 更新签到状态
        registration.setCheckInStatus(true);
        registration.setCheckInTime(LocalDateTime.now());
        registration.setCheckInMethod(ActivityRegistration.CheckInMethod.MANUAL); // 设置为手动签到
        activityRegistrationRepository.save(registration);

        // 为学生增加积分
        addPointsForActivity(studentId, activity);

        return true;
    }

    /**
     * 保存签到记录
     */
    public CheckIn save(CheckIn checkIn) {
        return checkInRepository.save(checkIn);
    }

    /**
     * 获取学生的签到记录
     */
    public List<CheckIn> getCheckInRecordsByStudentId(String studentId, int page, int size) {
        // 这里需要实现分页查询
        return checkInRepository.findByStudentId(studentId);
    }

    /**
     * 获取活动的签到记录
     */
    public List<CheckIn> getCheckInRecordsByActivityId(Integer activityId, int page, int size) {
        // 这里需要实现分页查询
        return checkInRepository.findByActivityId(activityId);
    }
}