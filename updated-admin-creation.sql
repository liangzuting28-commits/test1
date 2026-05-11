-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS club_management_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE club_management_system;

-- 查看students表结构（用于验证）
DESCRIBE students;

-- 在students表中插入管理员用户（完整字段版）
INSERT INTO students (student_id, name, college, major_class, role, password, total_points, phone, email, created_at)
VALUES 
('ADMIN001', '系统管理员', '系统管理', '管理员组', 'ADMIN', '123456', 0, '13800138000', 'admin@club-system.com', NOW()),
('ADMIN002', '超级管理员', '系统管理', '管理员组', 'ADMIN', 'admin123', 0, '13900139000', 'superadmin@club-system.com', NOW());

-- 查询插入的所有管理员用户
SELECT * FROM students WHERE role = 'ADMIN';

-- 查询所有用户（用于确认）
SELECT student_id, name, role, email FROM students;