-- 更新所有缺少手机号和邮箱的用户
USE club_management_system;

-- 查看当前所有用户的完整信息
SELECT student_id, name, role, phone, email FROM students ORDER BY role, student_id;

-- 1. 更新普通学生（根据数据库中实际存在的学生）
UPDATE students SET phone = '13800138101', email = 'wu_jiu@club-system.com' WHERE student_id = '2021003002';
UPDATE students SET phone = '13800138102', email = 'zheng_shi@club-system.com' WHERE student_id = '2021004002';
UPDATE students SET phone = '13800138103', email = 'qian_yi@club-system.com' WHERE student_id = '2021005001';

-- 2. 更新老师用户（T002, T003, T004）
UPDATE students SET phone = '13800138201', email = 'li_teacher@club-system.com' WHERE student_id = 'T002';
UPDATE students SET phone = '13800138202', email = 'wang_teacher@club-system.com' WHERE student_id = 'T003';
UPDATE students SET phone = '13800138203', email = 'zhao_teacher@club-system.com' WHERE student_id = 'T004';

-- 3. 更新所有以2021开头的学生（兜底，确保所有学生都有信息）
UPDATE students 
SET phone = CONCAT('13800138', SUBSTRING(student_id, 5, 3)), 
    email = CONCAT(REPLACE(name, ' ', '_'), '@club-system.com')
WHERE student_id LIKE '2021%' AND (phone IS NULL OR phone = '' OR email IS NULL OR email = '');

-- 4. 更新所有以T开头的老师（兜底，确保所有老师都有信息）
UPDATE students 
SET phone = CONCAT('138001382', SUBSTRING(student_id, 2, 2)), 
    email = CONCAT(REPLACE(name, ' ', '_'), '_teacher@club-system.com')
WHERE student_id LIKE 'T%' AND (phone IS NULL OR phone = '' OR email IS NULL OR email = '');

-- 查看更新后的所有用户信息
SELECT student_id, name, role, phone, email FROM students ORDER BY role, student_id;
