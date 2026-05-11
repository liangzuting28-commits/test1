-- 更新周八同学的手机号和邮箱
USE club_management_system;

-- 查看周八同学当前信息
SELECT student_id, name, role, phone, email FROM students WHERE student_id = '2021002002';

-- 更新周八同学的信息
UPDATE students 
SET phone = '13800138104', 
    email = 'zhou_ba@club-system.com'
WHERE student_id = '2021002002';

-- 验证更新结果
SELECT student_id, name, role, phone, email FROM students WHERE student_id = '2021002002';
