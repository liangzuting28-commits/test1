-- 俱乐部管理系统数据插入脚本

-- 6.2.1 学生表数据的输入
INSERT INTO students (student_id, name, college, major_class, role, password, total_points, phone, email, created_at) VALUES ('2020001001', '王社长', '计算机学院', '计算机科学与技术3班', 'PRESIDENT', '123456', 0, '13800138001', 'wang_president@club-system.com', NOW());

INSERT INTO students (student_id, name, college, major_class, role, password, total_points, phone, email, created_at) VALUES ('2020002001', '刘社长', '人文学院', '汉语言文学1班', 'PRESIDENT', '123456', 0, '13800138002', 'liu_president@club-system.com', NOW());

INSERT INTO students (student_id, name, college, major_class, role, password, total_points, phone, email, created_at) VALUES ('2020003001', '陈社长', '经济学院', '金融学2班', 'PRESIDENT', '123456', 0, '13800138003', 'chen_president@club-system.com', NOW());

INSERT INTO students (student_id, name, college, major_class, role, password, total_points, phone, email, created_at) VALUES ('2020004001', '杨社长', '建筑学院', '建筑学3班', 'PRESIDENT', '123456', 0, '13800138004', 'yang_president@club-system.com', NOW());

INSERT INTO students (student_id, name, college, major_class, role, password, total_points, phone, email, created_at) VALUES ('2021001001', '张三', '计算机学院', '计算机科学与技术1班', 'STUDENT', '123456', 8, '13800138005', 'zhang_san@club-system.com', NOW());

INSERT INTO students (student_id, name, college, major_class, role, password, total_points, phone, email, created_at) VALUES ('2021001002', '李四', '计算机学院', '计算机科学与技术2班', 'STUDENT', '123456', 0, '13800138006', 'li_si@club-system.com', NOW());

INSERT INTO students (student_id, name, college, major_class, role, password, total_points, phone, email, created_at) VALUES ('T001', '张老师', '团委', '教师', 'TEACHER', '123456', 0, '13800138007', 'zhang_teacher@club-system.com', NOW());

INSERT INTO students (student_id, name, college, major_class, role, password, total_points, phone, email, created_at) VALUES ('ADMIN001', '系统管理员', '系统管理', '管理员组', 'ADMIN', '123456', 0, '13800138000', 'admin@club-system.com', NOW());

INSERT INTO students (student_id, name, college, major_class, role, password, total_points, phone, email, created_at) VALUES ('ADMIN002', '超级管理员', '系统管理', '管理员组', 'ADMIN', 'admin123', 0, '13900139000', 'superadmin@club-system.com', NOW());

-- 6.2.2 社团表数据的输入
INSERT INTO clubs (club_name, introduction, club_type, establish_date, president_id, created_at, created_by, status, contact_info) VALUES ('计算机协会', '计算机技术学习和交流社区，开展编程讲座、算法竞赛等活动', '学术科技', '2020-09-01 00:00:00', '2020001001', NOW(), 'T001', 'ACTIVE', 'wang_president@club-system.com');

INSERT INTO clubs (club_name, introduction, club_type, establish_date, president_id, created_at, created_by, status, contact_info) VALUES ('篮球社', '篮球运动爱好者社团，组织日常训练、校园联赛等活动', '文化体育', '2019-03-15 00:00:00', '2020002001', NOW(), 'T001', 'ACTIVE', 'liu_president@club-system.com');

INSERT INTO clubs (club_name, introduction, club_type, establish_date, president_id, created_at, created_by, status, contact_info) VALUES ('音乐社', '音乐创作和表演社团，举办音乐节、校园歌手大赛等活动', '艺术表演', '2021-05-20 00:00:00', '2020003001', NOW(), 'T001', 'ACTIVE', 'chen_president@club-system.com');

-- 6.2.3 积分规则表数据的输入
INSERT INTO points_rules (activity_type, base_points_value, last_updated) VALUES ('学术讲座', 5, NOW());

INSERT INTO points_rules (activity_type, base_points_value, last_updated) VALUES ('文体活动', 3, NOW());

INSERT INTO points_rules (activity_type, base_points_value, last_updated) VALUES ('志愿公益', 8, NOW());

INSERT INTO points_rules (activity_type, base_points_value, last_updated) VALUES ('竞赛获奖', 10, NOW());

INSERT INTO points_rules (activity_type, base_points_value, last_updated) VALUES ('社会实践', 6, NOW());

-- 6.2.4 活动表数据的输入
INSERT INTO activities (activity_name, club_id, activity_time, location, status, max_participants, award_points, description, activity_type, created_at) VALUES ('Java编程入门讲座', 1, DATE_ADD(NOW(), INTERVAL 7 DAY), '教学楼A101', 'PUBLISHED', 100, 5, '面向初学者的Java编程入门讲座，讲解基础语法和开发环境搭建', '学术讲座', NOW());

INSERT INTO activities (activity_name, club_id, activity_time, location, status, max_participants, award_points, description, activity_type, created_at) VALUES ('校园篮球比赛', 2, DATE_ADD(NOW(), INTERVAL 14 DAY), '体育馆', 'PUBLISHED', 50, 3, '年度校园篮球比赛，各学院组队参赛，奖励前三名', '文体活动', NOW());

INSERT INTO activities (activity_name, club_id, activity_time, location, status, max_participants, award_points, description, activity_type, created_at) VALUES ('新年音乐节表演', 3, DATE_ADD(NOW(), INTERVAL 21 DAY), '学生活动中心', 'PUBLISHED', 200, 3, '新年音乐节表演，展示社团成员原创和翻唱作品', '文体活动', NOW());

-- 6.2.5 社团成员关系表数据的输入
INSERT INTO club_memberships (student_id, club_id, role, join_date, status, application_status) VALUES ('2020001001', 1, 'PRESIDENT', NOW(), 'ACTIVE', 'APPROVED');

INSERT INTO club_memberships (student_id, club_id, role, join_date, status, application_status) VALUES ('2020002001', 2, 'PRESIDENT', NOW(), 'ACTIVE', 'APPROVED');

INSERT INTO club_memberships (student_id, club_id, role, join_date, status, application_status) VALUES ('2020003001', 3, 'PRESIDENT', NOW(), 'ACTIVE', 'APPROVED');

INSERT INTO club_memberships (student_id, club_id, role, join_date, status, application_status) VALUES ('2021001001', 1, 'MEMBER', NOW(), 'ACTIVE', 'PENDING');

INSERT INTO club_memberships (student_id, club_id, role, join_date, status, application_status) VALUES ('2021001002', 2, 'MEMBER', NOW(), 'ACTIVE', 'PENDING');

-- 6.2.6 活动报名表数据的输入
INSERT INTO activity_registrations (student_id, activity_id, registration_time, check_in_status, check_in_method) VALUES ('2021001001', 1, NOW(), 0, NULL);

INSERT INTO activity_registrations (student_id, activity_id, registration_time, check_in_status, check_in_method) VALUES ('2021001002', 2, NOW(), 0, NULL);

-- 6.2.7 积分记录表数据的输入
INSERT INTO points_records (student_id, activity_id, points_earned, earned_time) VALUES ('2021001001', 1, 5, NOW());

-- 6.2.8 系统配置表数据的输入
INSERT INTO system_config (config_key, config_value, description, last_updated) VALUES ('system_name', '俱乐部管理系统', '系统名称配置', NOW());

INSERT INTO system_config (config_key, config_value, description, last_updated) VALUES ('activity_auto_approve', 'false', '活动是否自动审核通过', NOW());

INSERT INTO system_config (config_key, config_value, description, last_updated) VALUES ('max_club_members', '100', '社团最大成员数限制', NOW());