package com.club.managementsystem.config;

import com.club.managementsystem.entity.*;
import com.club.managementsystem.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private ClubRepository clubRepository;
    @Autowired
    private ClubMembershipRepository clubMembershipRepository;
    @Autowired
    private ActivityRepository activityRepository;
    @Autowired
    private PointsRuleRepository pointsRuleRepository;
    @Autowired
    private ActivityRegistrationRepository activityRegistrationRepository;
    @Autowired
    private PointsRecordRepository pointsRecordRepository;

    @Override
    public void run(String... args) throws Exception {
        // 1. 初始化学生（含4个社长，已有数据无需修改）
        initStudents();
        // 2. 初始化3个社团，分别绑定不同社长（核心修改）
        initClubs();
        // 3. 初始化积分规则（不变）
        initPointsRules();
        // 4. 初始化活动（不变）
        initActivities();
        // 5. 初始化报名记录（不变）
        initActivityRegistrations();
        // 6. 初始化积分记录（不变）
        initPointsRecords();
        // 7. 初始化社团成员（社长仅加入自己的社团，核心修改）
        initClubMemberships();

        System.out.println("数据初始化完成！");
        System.out.println("王社长（计算机协会）: 2020001001 / 123456");
        System.out.println("刘社长（篮球社）: 2020002001 / 123456");
        System.out.println("陈社长（音乐社）: 2020003001 / 123456");
        System.out.println("老师账号: T001 / 123456");
    }

    // -------------------------- 核心修改1：初始化3个社团绑定不同社长 --------------------------
    private void initClubs() {
        if (clubRepository.count() == 0) {
            // 1. 计算机协会 → 王社长（2020001001，计算机学院）
            Club computerClub = new Club();
            computerClub.setClubName("计算机协会");
            computerClub.setIntroduction("计算机技术学习和交流社区，开展编程讲座、算法竞赛等活动");
            computerClub.setClubType("学术科技");
            computerClub.setEstablishDate(LocalDateTime.of(2020, 9, 1, 0, 0));
            computerClub.setPresidentId("2020001001"); // 绑定王社长
            clubRepository.save(computerClub);

            // 2. 篮球社 → 刘社长（2020002001，人文学院）
            Club basketballClub = new Club();
            basketballClub.setClubName("篮球社");
            basketballClub.setIntroduction("篮球运动爱好者社团，组织日常训练、校园联赛等活动");
            basketballClub.setClubType("文化体育");
            basketballClub.setEstablishDate(LocalDateTime.of(2019, 3, 15, 0, 0));
            basketballClub.setPresidentId("2020002001"); // 绑定刘社长
            clubRepository.save(basketballClub);

            // 3. 音乐社 → 陈社长（2020003001，经济学院）
            Club musicClub = new Club();
            musicClub.setClubName("音乐社");
            musicClub.setIntroduction("音乐创作和表演社团，举办音乐节、校园歌手大赛等活动");
            musicClub.setClubType("艺术表演");
            musicClub.setEstablishDate(LocalDateTime.of(2021, 5, 20, 0, 0));
            musicClub.setPresidentId("2020003001"); // 绑定陈社长
            clubRepository.save(musicClub);
        }
    }

    // -------------------------- 核心修改2：社长仅加入自己管理的社团 --------------------------
    private void initClubMemberships() {
        if (clubMembershipRepository.count() == 0) {
            // 1. 王社长 → 计算机协会（PRESIDENT，自动通过）
            ClubMembership wangMembership = new ClubMembership();
            wangMembership.setStudentId("2020001001");
            wangMembership.setClubId(1); // 计算机协会ID=1
            wangMembership.setRole(ClubMembership.Role.PRESIDENT);
            wangMembership.setApplicationStatus("APPROVED");
            clubMembershipRepository.save(wangMembership);

            // 2. 刘社长 → 篮球社（PRESIDENT，自动通过）
            ClubMembership liuMembership = new ClubMembership();
            liuMembership.setStudentId("2020002001");
            liuMembership.setClubId(2); // 篮球社ID=2
            liuMembership.setRole(ClubMembership.Role.PRESIDENT);
            liuMembership.setApplicationStatus("APPROVED");
            clubMembershipRepository.save(liuMembership);

            // 3. 陈社长 → 音乐社（PRESIDENT，自动通过）
            ClubMembership chenMembership = new ClubMembership();
            chenMembership.setStudentId("2020003001");
            chenMembership.setClubId(3); // 音乐社ID=3
            chenMembership.setRole(ClubMembership.Role.PRESIDENT);
            chenMembership.setApplicationStatus("APPROVED");
            clubMembershipRepository.save(chenMembership);

            // 4. 普通学生申请（测试数据：张三申请计算机协会，李四申请篮球社）
            // 张三→计算机协会（待审核，供王社长测试）
            ClubMembership zhangSanApply = new ClubMembership();
            zhangSanApply.setStudentId("2021001001");
            zhangSanApply.setClubId(1);
            zhangSanApply.setRole(ClubMembership.Role.MEMBER);
            zhangSanApply.setApplicationStatus("PENDING");
            clubMembershipRepository.save(zhangSanApply);

            // 李四→篮球社（待审核，供刘社长测试）
            ClubMembership liSiApply = new ClubMembership();
            liSiApply.setStudentId("2021001002");
            liSiApply.setClubId(2);
            liSiApply.setRole(ClubMembership.Role.MEMBER);
            liSiApply.setApplicationStatus("PENDING");
            clubMembershipRepository.save(liSiApply);
        }
    }

    // -------------------------- 以下方法无需修改（保持原有逻辑） --------------------------
    private void initStudents() {
        if (studentRepository.count() == 0) {
            // 1. 王社长（计算机协会）
            Student wangPresident = new Student();
            wangPresident.setStudentId("2020001001");
            wangPresident.setName("王社长");
            wangPresident.setCollege("计算机学院");
            wangPresident.setMajorClass("计算机科学与技术3班");
            wangPresident.setRole(Student.Role.PRESIDENT);
            wangPresident.setPassword("123456");
            wangPresident.setTotalPoints(0);
            wangPresident.setPhone("13800138001");
            wangPresident.setEmail("wang_president@club-system.com");
            studentRepository.save(wangPresident);

            // 2. 刘社长（篮球社）
            Student liuPresident = new Student();
            liuPresident.setStudentId("2020002001");
            liuPresident.setName("刘社长");
            liuPresident.setCollege("人文学院");
            liuPresident.setMajorClass("汉语言文学1班");
            liuPresident.setRole(Student.Role.PRESIDENT);
            liuPresident.setPassword("123456");
            liuPresident.setTotalPoints(0);
            liuPresident.setPhone("13800138002");
            liuPresident.setEmail("liu_president@club-system.com");
            studentRepository.save(liuPresident);

            // 3. 陈社长（音乐社）
            Student chenPresident = new Student();
            chenPresident.setStudentId("2020003001");
            chenPresident.setName("陈社长");
            chenPresident.setCollege("经济学院");
            chenPresident.setMajorClass("金融学2班");
            chenPresident.setRole(Student.Role.PRESIDENT);
            chenPresident.setPassword("123456");
            chenPresident.setTotalPoints(0);
            chenPresident.setPhone("13800138003");
            chenPresident.setEmail("chen_president@club-system.com");
            studentRepository.save(chenPresident);

            // 4. 杨社长（备用，无初始社团）
            Student yangPresident = new Student();
            yangPresident.setStudentId("2020004001");
            yangPresident.setName("杨社长");
            yangPresident.setCollege("建筑学院");
            yangPresident.setMajorClass("建筑学3班");
            yangPresident.setRole(Student.Role.PRESIDENT);
            yangPresident.setPassword("123456");
            yangPresident.setTotalPoints(0);
            yangPresident.setPhone("13800138004");
            yangPresident.setEmail("yang_president@club-system.com");
            studentRepository.save(yangPresident);

            // 5. 普通学生
            Student zhangSan = new Student();
            zhangSan.setStudentId("2021001001");
            zhangSan.setName("张三");
            zhangSan.setCollege("计算机学院");
            zhangSan.setMajorClass("计算机科学与技术1班");
            zhangSan.setRole(Student.Role.STUDENT);
            zhangSan.setPassword("123456");
            zhangSan.setTotalPoints(8);
            zhangSan.setPhone("13800138005");
            zhangSan.setEmail("zhang_san@club-system.com");
            studentRepository.save(zhangSan);

            Student liSi = new Student();
            liSi.setStudentId("2021001002");
            liSi.setName("李四");
            liSi.setCollege("计算机学院");
            liSi.setMajorClass("计算机科学与技术2班");
            liSi.setRole(Student.Role.STUDENT);
            liSi.setPassword("123456");
            liSi.setTotalPoints(0);
            liSi.setPhone("13800138006");
            liSi.setEmail("li_si@club-system.com");
            studentRepository.save(liSi);

            // 6. 老师（负责创建社团）
            Student teacherZhang = new Student();
            teacherZhang.setStudentId("T001");
            teacherZhang.setName("张老师");
            teacherZhang.setCollege("团委");
            teacherZhang.setMajorClass("教师");
            teacherZhang.setRole(Student.Role.TEACHER);
            teacherZhang.setPassword("123456");
            teacherZhang.setTotalPoints(0);
            teacherZhang.setPhone("13800138007");
            teacherZhang.setEmail("zhang_teacher@club-system.com");
            studentRepository.save(teacherZhang);
        }
    }

    private void initPointsRules() {
        if (pointsRuleRepository.count() == 0) {
            PointsRule rule1 = new PointsRule();
            rule1.setActivityType("学术讲座");
            rule1.setBasePointsValue(5);
            pointsRuleRepository.save(rule1);

            PointsRule rule2 = new PointsRule();
            rule2.setActivityType("文体活动");
            rule2.setBasePointsValue(3);
            pointsRuleRepository.save(rule2);

            PointsRule rule3 = new PointsRule();
            rule3.setActivityType("志愿公益");
            rule3.setBasePointsValue(8);
            pointsRuleRepository.save(rule3);

            PointsRule rule4 = new PointsRule();
            rule4.setActivityType("竞赛获奖");
            rule4.setBasePointsValue(10);
            pointsRuleRepository.save(rule4);

            PointsRule rule5 = new PointsRule();
            rule5.setActivityType("社会实践");
            rule5.setBasePointsValue(6);
            pointsRuleRepository.save(rule5);
        }
    }

    private void initActivities() {
        // 查找是否已有活动记录
        List<Activity> existingActivities = activityRepository.findAll();
        Map<String, Activity> activityMap = new HashMap<>();
        for (Activity activity : existingActivities) {
            activityMap.put(activity.getActivityName(), activity);
        }
        
        // 计算机协会活动
        Activity javaLecture = activityMap.getOrDefault("Java编程入门讲座", new Activity());
        javaLecture.setActivityName("Java编程入门讲座");
        javaLecture.setClubId(1);
        javaLecture.setActivityTime(LocalDateTime.now().plusDays(7));
        javaLecture.setLocation("教学楼A101");
        javaLecture.setStatus(Activity.Status.PUBLISHED);
        javaLecture.setMaxParticipants(100);
        javaLecture.setAwardPoints(5);
        javaLecture.setDescription("面向初学者的Java编程入门讲座，讲解基础语法和开发环境搭建");
        javaLecture.setActivityType("学术讲座");
        activityRepository.save(javaLecture);

        // 篮球社活动
        Activity basketballMatch = activityMap.getOrDefault("校园篮球比赛", new Activity());
        basketballMatch.setActivityName("校园篮球比赛");
        basketballMatch.setClubId(2);
        basketballMatch.setActivityTime(LocalDateTime.now().plusDays(14));
        basketballMatch.setLocation("体育馆");
        basketballMatch.setStatus(Activity.Status.PUBLISHED);
        basketballMatch.setMaxParticipants(50);
        basketballMatch.setAwardPoints(3);
        basketballMatch.setDescription("年度校园篮球比赛，各学院组队参赛，奖励前三名");
        basketballMatch.setActivityType("文艺活动");
        activityRepository.save(basketballMatch);

        // 音乐社活动
        Activity musicFestival = activityMap.getOrDefault("新年音乐节表演", new Activity());
        musicFestival.setActivityName("新年音乐节表演");
        musicFestival.setClubId(3);
        musicFestival.setActivityTime(LocalDateTime.now().plusDays(21));
        musicFestival.setLocation("学生活动中心");
        musicFestival.setStatus(Activity.Status.PUBLISHED);
        musicFestival.setMaxParticipants(200);
        musicFestival.setAwardPoints(3);
        musicFestival.setDescription("新年音乐节表演，展示社团成员原创和翻唱作品");
        musicFestival.setActivityType("文体活动");
        activityRepository.save(musicFestival);
    }

    private void initActivityRegistrations() {
        if (activityRegistrationRepository.count() == 0) {
            // 张三报名Java讲座
            ActivityRegistration zhangSanReg = new ActivityRegistration();
            zhangSanReg.setStudentId("2021001001");
            zhangSanReg.setActivityId(1);
            activityRegistrationRepository.save(zhangSanReg);

            // 李四报名篮球比赛
            ActivityRegistration liSiReg = new ActivityRegistration();
            liSiReg.setStudentId("2021001002");
            liSiReg.setActivityId(2);
            activityRegistrationRepository.save(liSiReg);
        }
    }

    private void initPointsRecords() {
        if (pointsRecordRepository.count() == 0) {
            // 张三获取Java讲座积分
            PointsRecord zhangSanPoint = new PointsRecord();
            zhangSanPoint.setStudentId("2021001001");
            zhangSanPoint.setActivityId(1);
            zhangSanPoint.setPointsEarned(5);
            pointsRecordRepository.save(zhangSanPoint);

            // 更新学生总积分
            updateStudentTotalPoints();
        }
    }

    private void updateStudentTotalPoints() {
        List<Student> students = studentRepository.findAll();
        for (Student student : students) {
            List<PointsRecord> records = pointsRecordRepository.findByStudentId(student.getStudentId());
            int total = records.stream().mapToInt(PointsRecord::getPointsEarned).sum();
            student.setTotalPoints(total);
            studentRepository.save(student);
        }
    }
}