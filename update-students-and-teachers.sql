-- 更新普通学生和老师的手机号和邮箱
USE club_management_system;

-- 更新普通学生（张三）
UPDATE students 
SET phone = '13800138005', email = 'zhang_san@club-system.com'
WHERE student_id = '2021001001' AND role = 'STUDENT';

-- 更新普通学生（李四）
UPDATE students 
SET phone = '13800138006', email = 'li_si@club-system.com'
WHERE student_id = '2021001002' AND role = 'STUDENT';

-- 更新老师（张老师）
UPDATE students 
SET phone = '13800138007', email = 'zhang_teacher@club-system.com'
WHERE student_id = 'T001' AND role = 'TEACHER';

-- 验证更新结果
SELECT student_id, name, role, phone, email FROM students WHERE role IN ('STUDENT', 'TEACHER');
