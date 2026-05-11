-- 更新students表字段长度
ALTER TABLE students
    MODIFY COLUMN student_id VARCHAR(20) COLLATE utf8mb4_unicode_ci NOT NULL,
    MODIFY COLUMN college VARCHAR(50) COLLATE utf8mb4_unicode_ci NULL;

-- 更新clubs表的外键约束以匹配student_id字段长度变化
ALTER TABLE clubs
    DROP FOREIGN KEY FK__Club__President__398D8EEE,
    DROP FOREIGN KEY FK__Club__CreatedBy__3A81B327;

ALTER TABLE clubs
    MODIFY COLUMN president_id VARCHAR(20) COLLATE utf8mb4_unicode_ci NULL,
    MODIFY COLUMN created_by VARCHAR(20) COLLATE utf8mb4_unicode_ci NULL;

ALTER TABLE clubs
    ADD CONSTRAINT FK__Club__President__398D8EEE
    FOREIGN KEY (president_id) REFERENCES students (student_id) ON DELETE CASCADE ON UPDATE CASCADE,
    ADD CONSTRAINT FK__Club__CreatedBy__3A81B327
    FOREIGN KEY (created_by) REFERENCES students (student_id) ON DELETE CASCADE ON UPDATE CASCADE;

-- 更新club_memberships表的外键约束
ALTER TABLE club_memberships
    DROP FOREIGN KEY FK__Membership__Student__3F466844;

ALTER TABLE club_memberships
    MODIFY COLUMN student_id VARCHAR(20) COLLATE utf8mb4_unicode_ci NOT NULL;

ALTER TABLE club_memberships
    ADD CONSTRAINT FK__Membership__Student__3F466844
    FOREIGN KEY (student_id) REFERENCES students (student_id) ON DELETE CASCADE ON UPDATE CASCADE;

-- 更新activity_registrations表的外键约束
ALTER TABLE activity_registrations
    DROP FOREIGN KEY FK__Registration__Student__4222D4EF;

ALTER TABLE activity_registrations
    MODIFY COLUMN student_id VARCHAR(20) COLLATE utf8mb4_unicode_ci NOT NULL;

ALTER TABLE activity_registrations
    ADD CONSTRAINT FK__Registration__Student__4222D4EF
    FOREIGN KEY (student_id) REFERENCES students (student_id) ON DELETE CASCADE ON UPDATE CASCADE;

-- 更新check_ins表的外键约束
ALTER TABLE check_ins
    DROP FOREIGN KEY FK__CheckIn__Student__44CA3770;

ALTER TABLE check_ins
    MODIFY COLUMN student_id VARCHAR(20) COLLATE utf8mb4_unicode_ci NOT NULL;

ALTER TABLE check_ins
    ADD CONSTRAINT FK__CheckIn__Student__44CA3770
    FOREIGN KEY (student_id) REFERENCES students (student_id) ON DELETE CASCADE ON UPDATE CASCADE;

-- 更新points_records表的外键约束
ALTER TABLE points_records
    DROP FOREIGN KEY FK__PointsRecord__Student__46D78483;

ALTER TABLE points_records
    MODIFY COLUMN student_id VARCHAR(20) COLLATE utf8mb4_unicode_ci NOT NULL;

ALTER TABLE points_records
    ADD CONSTRAINT FK__PointsRecord__Student__46D78483
    FOREIGN KEY (student_id) REFERENCES students (student_id) ON DELETE CASCADE ON UPDATE CASCADE;