-- 完整更新所有用户的手机号和邮箱
-- 确保所有角色的用户都有完整的联系方式
USE club_management_system;

-- 1. 更新普通学生
UPDATE students 
SET phone = '13800138005', email = 'zhang_san@club-system.com'
WHERE student_id = '2021001001';

UPDATE students 
SET phone = '13800138006', email = 'li_si@club-system.com'
WHERE student_id = '2021001002';

-- 2. 更新老师
UPDATE students 
SET phone = '13800138007', email = 'zhang_teacher@club-system.com'
WHERE student_id = 'T001';

-- 3. 更新管理员用户
UPDATE students 
SET phone = '13800138000', email = 'admin@club-system.com'
WHERE student_id = 'ADMIN001';

UPDATE students 
SET phone = '13900139000', email = 'superadmin@club-system.com'
WHERE student_id = 'ADMIN002';

-- 4. 更新所有没有手机号的用户（兜底更新）
UPDATE students 
SET phone = CONCAT('13800138', LPAD(student_id, 3, '0')), 
    email = CONCAT(REPLACE(name, ' ', '_'), '@club-system.com')
WHERE phone IS NULL OR phone = '';

-- 5. 更新所有没有邮箱的用户（兜底更新）
UPDATE students 
SET email = CONCAT(REPLACE(name, ' ', '_'), '@club-system.com')
WHERE email IS NULL OR email = '';

-- 验证所有用户的手机号和邮箱
SELECT student_id, name, role, phone, email 
FROM students 
ORDER BY role, student_id;
