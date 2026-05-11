-- 直接运行此脚本更新学生和老师的手机号和邮箱
-- 请确保MySQL服务正在运行，并且您有root权限

-- 如果您的MySQL密码不是123456，请修改下面的密码部分

-- 使用以下命令运行此脚本：
-- mysql -u root -p123456 < update-student-data-direct.sql

-- 连接到club_management_system数据库
USE club_management_system;

-- 查看当前学生数据（用于验证）
SELECT student_id, name, phone, email FROM students;

-- 更新学生数据
UPDATE students SET phone = '13800138001', email = 'wang_president@club-system.com' WHERE student_id = '2020001001';
UPDATE students SET phone = '13800138002', email = 'liu_president@club-system.com' WHERE student_id = '2020002001';
UPDATE students SET phone = '13800138003', email = 'chen_president@club-system.com' WHERE student_id = '2020003001';
UPDATE students SET phone = '13800138004', email = 'yang_president@club-system.com' WHERE student_id = '2020004001';
UPDATE students SET phone = '13800138005', email = 'zhang_san@club-system.com' WHERE student_id = '2021001001';
UPDATE students SET phone = '13800138006', email = 'li_si@club-system.com' WHERE student_id = '2021001002';
UPDATE students SET phone = '13800138007', email = 'zhang_teacher@club-system.com' WHERE student_id = 'T001';

-- 查看更新后的学生数据（用于验证）
SELECT student_id, name, phone, email FROM students;

-- 如果您看到每个学生都有了手机号和邮箱，说明更新成功！
