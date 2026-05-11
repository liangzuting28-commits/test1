-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS club_management_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE club_management_system;

-- 在students表中插入管理员用户
INSERT INTO students (student_id, name, college, major_class, role, password, created_at)
VALUES ('admin', '系统管理员', '系统管理', '管理员', 'ADMIN', 'admin123', NOW());

-- 查询插入的管理员用户
SELECT * FROM students WHERE role = 'ADMIN';