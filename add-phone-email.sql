-- 使用数据库
USE club_management_system;

-- 查看当前students表中的数据（包含phone和email字段）
SELECT student_id, name, phone, email FROM students;

-- 为初始化的学生添加phone和email值
-- 1. 王社长（计算机协会）
UPDATE students SET phone = '13800138001', email = 'wang_president@club-system.com' WHERE student_id = '2020001001';

-- 2. 刘社长（篮球社）
UPDATE students SET phone = '13800138002', email = 'liu_president@club-system.com' WHERE student_id = '2020002001';

-- 3. 陈社长（音乐社）
UPDATE students SET phone = '13800138003', email = 'chen_president@club-system.com' WHERE student_id = '2020003001';

-- 4. 杨社长（备用，无初始社团）
UPDATE students SET phone = '13800138004', email = 'yang_president@club-system.com' WHERE student_id = '2020004001';

-- 5. 普通学生
UPDATE students SET phone = '13800138005', email = 'zhang_san@club-system.com' WHERE student_id = '2021001001';
UPDATE students SET phone = '13800138006', email = 'li_si@club-system.com' WHERE student_id = '2021001002';

-- 6. 老师（负责创建社团）
UPDATE students SET phone = '13800138007', email = 'zhang_teacher@club-system.com' WHERE student_id = 'T001';

-- 查看更新后的students表数据
SELECT student_id, name, phone, email FROM students;
