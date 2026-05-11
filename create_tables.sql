-- 俱乐部管理系统数据库表结构创建脚本

-- 1.1.1 学生表的建立
CREATE TABLE students (
    student_id CHAR(10) COLLATE utf8mb4_unicode_ci NOT NULL,
    name VARCHAR(20) COLLATE utf8mb4_unicode_ci NOT NULL,
    college VARCHAR(30) COLLATE utf8mb4_unicode_ci NULL,
    major_class VARCHAR(30) COLLATE utf8mb4_unicode_ci NULL,
    role VARCHAR(20) COLLATE utf8mb4_unicode_ci NOT NULL,
    password VARCHAR(255) COLLATE utf8mb4_unicode_ci NOT NULL,
    total_points INT DEFAULT 0,
    phone VARCHAR(20) COLLATE utf8mb4_unicode_ci NULL,
    email VARCHAR(50) COLLATE utf8mb4_unicode_ci NULL,
    created_at DATETIME NULL,
    PRIMARY KEY (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 1.1.2 社团表的建立
CREATE TABLE clubs (
    club_id INT AUTO_INCREMENT NOT NULL,
    club_name VARCHAR(50) COLLATE utf8mb4_unicode_ci NOT NULL,
    introduction VARCHAR(500) COLLATE utf8mb4_unicode_ci NULL,
    club_type VARCHAR(20) COLLATE utf8mb4_unicode_ci NULL,
    establish_date DATETIME NULL,
    president_id CHAR(10) COLLATE utf8mb4_unicode_ci NULL,
    created_at DATETIME NULL,
    created_by CHAR(10) COLLATE utf8mb4_unicode_ci NULL,
    status VARCHAR(20) COLLATE utf8mb4_unicode_ci DEFAULT 'ACTIVE',
    logo_url VARCHAR(255) COLLATE utf8mb4_unicode_ci NULL,
    contact_info VARCHAR(100) COLLATE utf8mb4_unicode_ci NULL,
    PRIMARY KEY (club_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 1.1.3 活动表的建立
CREATE TABLE activities (
    activity_id INT AUTO_INCREMENT NOT NULL,
    activity_name VARCHAR(100) COLLATE utf8mb4_unicode_ci NOT NULL,
    club_id INT NULL,
    activity_time DATETIME NULL,
    location VARCHAR(100) COLLATE utf8mb4_unicode_ci NULL,
    status VARCHAR(20) COLLATE utf8mb4_unicode_ci NULL,
    max_participants INT NULL,
    award_points INT NULL,
    description VARCHAR(500) COLLATE utf8mb4_unicode_ci NULL,
    qr_code VARCHAR(255) COLLATE utf8mb4_unicode_ci NULL,
    code VARCHAR(50) COLLATE utf8mb4_unicode_ci NULL,
    check_in_number VARCHAR(6) COLLATE utf8mb4_unicode_ci NULL,
    reject_reason VARCHAR(500) COLLATE utf8mb4_unicode_ci NULL,
    created_at DATETIME NULL,
    activity_type VARCHAR(20) COLLATE utf8mb4_unicode_ci NULL,
    PRIMARY KEY (activity_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 1.1.4 社团成员关系表的建立
CREATE TABLE club_memberships (
    membership_id INT AUTO_INCREMENT NOT NULL,
    student_id CHAR(10) COLLATE utf8mb4_unicode_ci NOT NULL,
    club_id INT NOT NULL,
    role VARCHAR(20) COLLATE utf8mb4_unicode_ci NULL,
    join_date DATETIME NULL,
    status VARCHAR(20) COLLATE utf8mb4_unicode_ci DEFAULT 'ACTIVE',
    application_status VARCHAR(20) COLLATE utf8mb4_unicode_ci DEFAULT 'PENDING',
    removal_reason VARCHAR(255) COLLATE utf8mb4_unicode_ci NULL,
    removal_date DATETIME NULL,
    PRIMARY KEY (membership_id),
    UNIQUE KEY uk_student_club (student_id, club_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 1.1.5 活动报名表的建立
CREATE TABLE activity_registrations (
    registration_id INT AUTO_INCREMENT NOT NULL,
    student_id CHAR(10) COLLATE utf8mb4_unicode_ci NOT NULL,
    activity_id INT NOT NULL,
    registration_time DATETIME NULL,
    check_in_status TINYINT(1) DEFAULT 0,
    check_in_time DATETIME NULL,
    check_in_method VARCHAR(20) COLLATE utf8mb4_unicode_ci NULL,
    PRIMARY KEY (registration_id),
    UNIQUE KEY uk_student_activity (student_id, activity_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 1.1.6 签到表的建立
CREATE TABLE check_ins (
    check_in_id INT AUTO_INCREMENT NOT NULL,
    student_id CHAR(10) COLLATE utf8mb4_unicode_ci NOT NULL,
    activity_id INT NOT NULL,
    check_in_time DATETIME NULL,
    check_in_type VARCHAR(20) COLLATE utf8mb4_unicode_ci NULL,
    created_at DATETIME NULL,
    PRIMARY KEY (check_in_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 1.1.7 积分记录表的建立
CREATE TABLE points_records (
    record_id INT AUTO_INCREMENT NOT NULL,
    student_id CHAR(10) COLLATE utf8mb4_unicode_ci NOT NULL,
    activity_id INT NOT NULL,
    points_earned INT NOT NULL,
    earned_time DATETIME NULL,
    PRIMARY KEY (record_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 1.1.8 积分规则表的建立
CREATE TABLE points_rules (
    rule_id INT AUTO_INCREMENT NOT NULL,
    activity_type VARCHAR(20) COLLATE utf8mb4_unicode_ci NOT NULL,
    base_points_value INT NULL,
    last_updated DATETIME NULL,
    PRIMARY KEY (rule_id),
    UNIQUE KEY uk_activity_type (activity_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 1.1.9 系统配置表的建立
CREATE TABLE system_config (
    config_id INT AUTO_INCREMENT NOT NULL,
    config_key VARCHAR(50) COLLATE utf8mb4_unicode_ci NOT NULL,
    config_value VARCHAR(500) COLLATE utf8mb4_unicode_ci NULL,
    description VARCHAR(200) COLLATE utf8mb4_unicode_ci NULL,
    last_updated DATETIME NULL,
    PRIMARY KEY (config_id),
    UNIQUE KEY uk_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 外键约束的建立

-- 社团表的社长ID外键约束
ALTER TABLE clubs ADD CONSTRAINT 
FK__Club__President__398D8EEE 
FOREIGN KEY (president_id) 
REFERENCES students (student_id) ON DELETE CASCADE ON UPDATE CASCADE;

-- 社团表的创建者ID外键约束
ALTER TABLE clubs ADD CONSTRAINT 
FK__Club__CreatedBy__3A81B327 
FOREIGN KEY (created_by) 
REFERENCES students (student_id) ON DELETE CASCADE ON UPDATE CASCADE;

-- 活动表的社团ID外键约束
ALTER TABLE activities ADD CONSTRAINT 
FK__Activity__Club__3E52440B 
FOREIGN KEY (club_id) 
REFERENCES clubs (club_id) ON DELETE CASCADE ON UPDATE CASCADE;

-- 社团成员关系表的学生ID外键约束
ALTER TABLE club_memberships ADD CONSTRAINT 
FK__Membership__Student__3F466844 
FOREIGN KEY (student_id) 
REFERENCES students (student_id) ON DELETE CASCADE ON UPDATE CASCADE;

-- 社团成员关系表的社团ID外键约束
ALTER TABLE club_memberships ADD CONSTRAINT 
FK__Membership__Club__403A8B7D 
FOREIGN KEY (club_id) 
REFERENCES clubs (club_id) ON DELETE CASCADE ON UPDATE CASCADE;

-- 活动报名表的学生ID外键约束
ALTER TABLE activity_registrations ADD CONSTRAINT 
FK__Registration__Student__4222D4EF 
FOREIGN KEY (student_id) 
REFERENCES students (student_id) ON DELETE CASCADE ON UPDATE CASCADE;

-- 活动报名表的活动ID外键约束
ALTER TABLE activity_registrations ADD CONSTRAINT 
FK__Registration__Activity__4316F928 
FOREIGN KEY (activity_id) 
REFERENCES activities (activity_id) ON DELETE CASCADE ON UPDATE CASCADE;

-- 签到表的学生ID外键约束
ALTER TABLE check_ins ADD CONSTRAINT 
FK__CheckIn__Student__44CA3770 
FOREIGN KEY (student_id) 
REFERENCES students (student_id) ON DELETE CASCADE ON UPDATE CASCADE;

-- 签到表的活动ID外键约束
ALTER TABLE check_ins ADD CONSTRAINT 
FK__CheckIn__Activity__45DD447A 
FOREIGN KEY (activity_id) 
REFERENCES activities (activity_id) ON DELETE CASCADE ON UPDATE CASCADE;

-- 积分记录表的学生ID外键约束
ALTER TABLE points_records ADD CONSTRAINT 
FK__PointsRecord__Student__46D78483 
FOREIGN KEY (student_id) 
REFERENCES students (student_id) ON DELETE CASCADE ON UPDATE CASCADE;

-- 积分记录表的活动ID外键约束
ALTER TABLE points_records ADD CONSTRAINT 
FK__PointsRecord__Activity__47CBA8BC 
FOREIGN KEY (activity_id) 
REFERENCES activities (activity_id) ON DELETE CASCADE ON UPDATE CASCADE;