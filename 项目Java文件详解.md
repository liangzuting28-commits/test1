# 社团管理系统 — Java 文件详解

项目共 **67 个 Java 文件**，按 Spring Boot 分层架构组织。

---

## 一、启动入口（1 个文件）

### ClubManagementSystemApplication.java
`src/main/java/com/club/managementsystem/ClubManagementSystemApplication.java`

Spring Boot 启动类。`@SpringBootApplication` 标记为自动配置入口，`@ServletComponentScan` 让 Spring 扫描并注册 `@WebListener`（监听器）和 `@WebFilter`（过滤器）注解的组件。`main()` 方法调用 `SpringApplication.run()` 启动整个应用。

---

## 二、entity — 实体层（10 个文件）

每个实体类对应数据库一张表，使用 Lombok `@Data` 自动生成 getter/setter，使用 `@PrePersist` 在插入前自动填充时间字段。所有实体均使用 `jakarta.persistence.*`（Spring Boot 3.x）。

### 1. Student.java → `students` 表
`src/main/java/com/club/managementsystem/entity/Student.java`

系统用户，四种角色共用一张表。

| 字段 | 列名 | 类型 | 说明 |
|------|------|------|------|
| studentId | student_id | String(20) | **主键**，手动指定（非自增），如 "2021001" |
| name | name | String(20) | 姓名，非空 |
| college | college | String(50) | 学院 |
| majorClass | major_class | String(30) | 专业班级 |
| role | role | 枚举 | STUDENT / PRESIDENT / TEACHER / ADMIN |
| password | password | String(255) | BCrypt 加密后的密码 |
| totalPoints | total_points | Integer | 累计积分（默认 0） |
| phone | phone | String(20) | 手机号 |
| email | email | String(50) | 邮箱 |
| createdAt | created_at | LocalDateTime | 创建时间（@PrePersist 自动填充） |

**枚举 `Student.Role`：**
- `STUDENT` — 普通学生
- `PRESIDENT` — 社长
- `TEACHER` — 教师
- `ADMIN` — 管理员

---

### 2. Club.java → `clubs` 表
`src/main/java/com/club/managementsystem/entity/Club.java`

社团信息。

| 字段 | 列名 | 类型 | 说明 |
|------|------|------|------|
| clubId | club_id | Integer | **主键**，自增 |
| clubName | club_name | String(50) | 社团名称（唯一），非空 |
| introduction | introduction | String(500) | 社团简介 |
| clubType | club_type | String(20) | 社团类型（学术/文体/志愿等） |
| establishDate | establish_date | LocalDateTime | 成立日期 |
| presidentId | president_id | String(10) | 社长学号（外键→students） |
| createdBy | created_by | String(10) | 创建者学号 |
| status | status | String | 状态（默认 "ACTIVE"） |
| logoUrl | logo_url | String(255) | 社团 Logo 地址 |
| contactInfo | contact_info | String(100) | 联系方式 |
| createdAt | created_at | LocalDateTime | 创建时间 |

---

### 3. ClubMembership.java → `club_memberships` 表
`src/main/java/com/club/managementsystem/entity/ClubMembership.java`

社团成员关系，`(student_id, club_id)` 有唯一约束防止重复加入。

| 字段 | 列名 | 类型 | 说明 |
|------|------|------|------|
| membershipId | membership_id | Integer | **主键**，自增 |
| studentId | student_id | String(10) | 学生学号 |
| clubId | club_id | Integer | 社团 ID |
| role | role | 枚举 | MEMBER / MANAGER / PRESIDENT |
| joinDate | join_date | LocalDateTime | 加入时间 |
| status | status | String | 账号状态（默认 "ACTIVE"） |
| applicationStatus | application_status | String | 申请状态（见下方说明） |
| removalReason | removal_reason | String(255) | 移除/拒绝原因 |
| removalDate | removal_date | LocalDateTime | 移除时间 |

**枚举 `ClubMembership.Role`：**
- `MEMBER` — 普通成员
- `MANAGER` — 社团管理员
- `PRESIDENT` — 社长

**`applicationStatus` 取值：**
- `PENDING` — 待审批（学生提交加入申请）
- `APPROVED` — 已通过
- `REJECTED` — 已拒绝
- `REMOVED` — 被社长移除
- `PENDING_LEAVE` — 申请退出（待社长审批）
- `LEFT` — 已退出

---

### 4. Activity.java → `activities` 表
`src/main/java/com/club/managementsystem/entity/Activity.java`

活动信息。

| 字段 | 列名 | 类型 | 说明 |
|------|------|------|------|
| activityId | activity_id | Integer | **主键**，自增 |
| activityName | activity_name | String(100) | 活动名称，非空 |
| clubId | club_id | Integer | 主办社团 ID |
| activityTime | activity_time | LocalDateTime | 活动时间 |
| location | location | String(100) | 活动地点 |
| status | status | 枚举 | 活动状态（见下方） |
| maxParticipants | max_participants | Integer | 人数上限（0=无限制） |
| awardPoints | award_points | Integer | 参与可获积分 |
| description | description | String(500) | 活动描述 |
| activityType | activity_type | String(20) | 活动类型（学术讲座/文体活动等） |
| posterFileId | poster_file_id | Long | 海报文件 ID（外键→file_records） |
| planFileId | plan_file_id | Long | 策划书文件 ID |
| qrCode | qr_code | String(255) | 签到二维码（Base64） |
| code | code | String(50) | 活动编码 |
| checkInNumber | check_in_number | String(6) | 6 位签到码 |
| rejectReason | reject_reason | String(500) | 教师驳回原因 |
| createdAt | created_at | LocalDateTime | 创建时间 |

**枚举 `Activity.Status`：**
- `PENDING` — 待审核（社长创建后，等待教师审核）
- `REJECTED` — 已驳回（教师驳回）
- `PUBLISHED` — 已发布（教师通过，学生可报名）
- `REGISTERING` — 报名中（已定义但未使用）
- `ENDED` — 已结束（社长结束活动，触发发积分）
- `CANCELLED` — 已取消（已定义但未使用）

---

### 5. ActivityRegistration.java → `activity_registrations` 表
`src/main/java/com/club/managementsystem/entity/ActivityRegistration.java`

学生报名活动的记录。

| 字段 | 列名 | 类型 | 说明 |
|------|------|------|------|
| registrationId | registration_id | Integer | **主键**，自增 |
| studentId | student_id | String(10) | 学生学号 |
| activityId | activity_id | Integer | 活动 ID |
| registrationTime | registration_time | LocalDateTime | 报名时间 |
| status | status | 枚举 | 报名审核状态（见下方） |
| rejectReason | reject_reason | String | 社长拒绝理由 |
| checkInStatus | check_in_status | Boolean | 是否已签到（默认 false） |
| checkInTime | check_in_time | LocalDateTime | 签到时间 |
| checkInMethod | check_in_method | 枚举 | 签到方式（见下方） |

**枚举 `RegistrationStatus`：**
- `PENDING` — 待审核（学生提交报名后默认状态）
- `APPROVED` — 已通过（社长审批通过，正式报名成功）
- `REJECTED` — 已拒绝（社长拒绝，含拒绝理由）

**枚举 `CheckInMethod`：**
- `QR_CODE` — 扫码签到
- `MANUAL` — 社长手动签到

---

### 6. CheckIn.java → `check_ins` 表
`src/main/java/com/club/managementsystem/entity/CheckIn.java`

签到记录。

| 字段 | 列名 | 类型 | 说明 |
|------|------|------|------|
| checkInId | check_in_id | Integer | **主键**，自增 |
| studentId | student_id | String(10) | 学生学号 |
| activityId | activity_id | Integer | 活动 ID |
| checkInTime | check_in_time | LocalDateTime | 签到时间 |
| checkInType | check_in_type | String(20) | 签到方式：SCAN（扫码）/ MANUAL（手动） |
| createdAt | created_at | LocalDateTime | 记录创建时间 |

---

### 7. PointsRecord.java → `points_records` 表
`src/main/java/com/club/managementsystem/entity/PointsRecord.java`

积分发放记录。

| 字段 | 列名 | 类型 | 说明 |
|------|------|------|------|
| recordId | record_id | Integer | **主键**，自增 |
| studentId | student_id | String(10) | 学生学号 |
| activityId | activity_id | Integer | 关联活动 ID |
| pointsEarned | points_earned | Integer | 获得积分数 |
| earnedTime | earned_time | LocalDateTime | 获得时间 |

---

### 8. PointsRule.java → `points_rules` 表
`src/main/java/com/club/managementsystem/entity/PointsRule.java`

积分规则配置，按活动类型设定基础积分。

| 字段 | 列名 | 类型 | 说明 |
|------|------|------|------|
| ruleId | rule_id | Integer | **主键**，自增 |
| activityType | activity_type | String(20) | 活动类型（唯一约束） |
| basePointsValue | base_points_value | Integer | 该类型活动的基础积分 |
| lastUpdated | last_updated | LocalDateTime | 最后更新时间（@PrePersist + @PreUpdate） |

---

### 9. FileRecord.java → `file_records` 表
`src/main/java/com/club/managementsystem/entity/FileRecord.java`

上传文件的元数据，实际文件存储在磁盘。

| 字段 | 列名 | 类型 | 说明 |
|------|------|------|------|
| fileId | file_id | Long | **主键**，自增（注意是 Long 不是 Integer） |
| originalName | original_name | String | 原始文件名，非空 |
| storedName | stored_name | String | 存储文件名（UUID 防冲突），非空 |
| filePath | file_path | String | 磁盘存储路径，非空 |
| fileSize | file_size | Long | 文件大小（字节） |
| contentType | content_type | String | MIME 类型（如 image/png） |
| uploaderId | uploader_id | String | 上传者学号 |
| uploadTime | upload_time | LocalDateTime | 上传时间 |

---

### 10. SystemConfig.java → `system_config` 表
`src/main/java/com/club/managementsystem/entity/SystemConfig.java`

系统配置键值对。

| 字段 | 列名 | 类型 | 说明 |
|------|------|------|------|
| configId | config_id | Integer | **主键**，自增 |
| configKey | config_key | String(50) | 配置键（唯一约束） |
| configValue | config_value | String(500) | 配置值 |
| description | description | String(200) | 配置说明 |
| lastUpdated | last_updated | LocalDateTime | 最后更新时间 |

---

## 三、repository — 数据访问层（10 个文件）

继承 `JpaRepository<Entity, ID>`，Spring Data JPA 根据方法名自动生成 SQL 查询。

### 1. StudentRepository.java
`src/main/java/com/club/managementsystem/repository/StudentRepository.java`

| 方法 | 说明 |
|------|------|
| `findByStudentId(String)` | 按学号查学生 |
| 继承 `findAll()`, `save()`, `deleteById()` 等 | 标准 CRUD |

### 2. ClubRepository.java
`src/main/java/com/club/managementsystem/repository/ClubRepository.java`

| 方法 | 说明 |
|------|------|
| `findByClubName(String)` | 按名称查社团 |
| `findByPresidentId(String)` | 按社长学号查社团 |
| `existsByClubName(String)` | 检查社团名是否已存在 |

### 3. ClubMembershipRepository.java
`src/main/java/com/club/managementsystem/repository/ClubMembershipRepository.java`

| 方法 | 说明 |
|------|------|
| `findByStudentIdAndClubId(String, Integer)` | 查某学生在某社团的成员关系 |
| `findByClubIdAndApplicationStatus(Integer, String)` | 按社团和申请状态查询 |
| `findByStudentIdAndApplicationStatus(String, String)` | 按学生和申请状态查询 |
| `existsByStudentIdAndClubIdAndApplicationStatus(...)` | 检查是否已加入 |
| `countByClubIdAndApplicationStatus(Integer, String)` | 统计某社团已批准成员数 |

### 4. ActivityRepository.java
`src/main/java/com/club/managementsystem/repository/ActivityRepository.java`

| 方法 | 说明 |
|------|------|
| `findByStatus(Activity.Status)` | 按状态查活动 |
| `findByClubId(Integer)` | 按社团查活动 |
| `findByCode(String)` | 按活动编码查 |
| `findByCheckInNumber(String)` | 按签到码查 |

### 5. ActivityRegistrationRepository.java
`src/main/java/com/club/managementsystem/repository/ActivityRegistrationRepository.java`

| 方法 | 说明 |
|------|------|
| `findByStudentId(String)` | 查某学生的所有报名 |
| `findByActivityId(Integer)` | 查某活动的所有报名 |
| `findByStudentIdAndActivityId(String, Integer)` | 查某学生在某活动的报名 |
| `existsByStudentIdAndActivityId(String, Integer)` | 检查是否已报名 |
| `countByActivityId(Integer)` | 统计活动报名总数 |
| `countByActivityIdAndStatus(Integer, RegistrationStatus)` | 按状态统计报名数（人数限制只算已通过） |
| `deleteByActivityId(Integer)` | 删除活动时批量清除报名 |

### 6. CheckInRepository.java
`src/main/java/com/club/managementsystem/repository/CheckInRepository.java`

| 方法 | 说明 |
|------|------|
| `findByStudentId(String)` | 查某学生的签到记录 |
| `findByActivityId(Integer)` | 查某活动的签到记录 |
| `existsByStudentIdAndActivityId(String, Integer)` | 检查是否已签到 |

### 7. PointsRecordRepository.java
`src/main/java/com/club/managementsystem/repository/PointsRecordRepository.java`

| 方法 | 说明 |
|------|------|
| `findByStudentId(String)` | 查某学生的积分记录 |
| `findByActivityId(Integer)` | 查某活动的积分记录 |
| `existsByStudentIdAndActivityId(String, Integer)` | 检查是否已发放积分 |

### 8. PointsRuleRepository.java
`src/main/java/com/club/managementsystem/repository/PointsRuleRepository.java`

| 方法 | 说明 |
|------|------|
| `findByActivityType(String)` | 按活动类型查积分规则 |

### 9. FileRecordRepository.java
`src/main/java/com/club/managementsystem/repository/FileRecordRepository.java`

继承标准 CRUD 方法（`findById()`, `save()`, `deleteById()` 等）。

### 10. SystemConfigRepository.java
`src/main/java/com/club/managementsystem/repository/SystemConfigRepository.java`

| 方法 | 说明 |
|------|------|
| `findByConfigKey(String)` | 按配置键查值 |

---

## 四、service — 业务逻辑层（8 个文件）

处理核心业务逻辑，被 Controller 调用，调用 Repository 操作数据库。使用 `@Transactional` 管理事务。

### 1. AuthService.java — 认证服务
`src/main/java/com/club/managementsystem/service/AuthService.java`

| 方法 | 说明 |
|------|------|
| `login(LoginRequest)` | 验证学号存在、角色匹配、BCrypt 密码正确后生成 JWT Token |

---

### 2. StudentService.java — 学生服务
`src/main/java/com/club/managementsystem/service/StudentService.java`

| 方法 | 说明 |
|------|------|
| `findAll()` | 获取全部学生 |
| `findByStudentId(String)` | 按学号查询 |
| `save(Student)` | 保存/更新学生 |
| `deleteById(String)` | **级联删除**：删除学生时同时清除其报名、签到、积分、社团成员关系、社长引用 |
| `existsByStudentId(String)` | 检查学号是否存在 |
| `findByRole(Role)` | 按角色筛选 |

---

### 3. ClubService.java — 社团服务
`src/main/java/com/club/managementsystem/service/ClubService.java`

| 方法 | 说明 |
|------|------|
| `createClub(request, creatorId)` | 教师创建社团，自动创建 PRESIDENT 成员记录 |
| `applyForClub(studentId, clubId, message)` | 学生申请加入社团（检查重复/已批准/已拒绝） |
| `reviewApplication(membershipId, status, reason, operatorId)` | 社长审批加入/退出申请 |
| `getClubDetail(clubId)` | 返回社团详情 DTO（含社长信息、成员列表、成员数、活动数） |
| `deleteClub(clubId, operatorId)` | 删除社团（先删所有成员关系） |
| `updateClub(clubId, request, userId)` | 社长更新社团信息（名称唯一性校验） |
| `changePresident(clubId, newPresidentId, operatorId)` | 教师更换社长（旧社长降为 MEMBER） |
| `removePresident(clubId, operatorId)` | 教师移除社长 |
| `getPendingApplications(clubId, presidentId)` | 获取待审批的加入/退出申请 |
| `getStudentClubs(studentId)` | 获取学生已加入的社团 |
| `getStudentApplications(studentId)` | 获取学生的所有社团申请记录 |
| `findAll()` | 获取全部社团 |
| `findById(clubId)` | 按 ID 查社团 |
| `findByPresidentId(presidentId)` | 按社长查社团 |
| `deleteStudentApplication(membershipId, studentId)` | 学生删除自己的待审申请 |

---

### 4. ClubMembershipService.java — 社团成员服务
`src/main/java/com/club/managementsystem/service/ClubMembershipService.java`

| 方法 | 说明 |
|------|------|
| `getApprovedClubMembers(clubId)` | 获取已批准的成员列表 |
| `getPendingApplications(clubId)` | 获取待审批的申请列表 |
| `removeMember(clubId, studentId, reason, operatorId)` | 社长移除成员（记录原因和时间） |
| `requestLeaveClub(clubId, studentId, reason)` | 学生申请退出社团（状态改为 PENDING_LEAVE） |
| `updateMemberRole(membershipId, role, operatorId)` | 社长修改成员角色（不能改 PRESIDENT） |
| `cancelApplication(clubId, studentId, reason)` | 学生取消待审申请 |
| `isPresident(studentId, clubId)` | 判断是否为社长 |
| `getClubMembersByPresident(clubId, presidentId)` | 验证社长权限后获取成员列表 |
| `removeMemberByPresident(clubId, studentId, presidentId)` | 验证社长权限后移除成员 |

---

### 5. ActivityService.java — 活动服务（最核心）
`src/main/java/com/club/managementsystem/service/ActivityService.java`

**活动生命周期：**

| 方法 | 说明 |
|------|------|
| `createActivity(request, presidentId)` | 社长创建活动（状态=PENDING，自动套用积分规则） |
| `updateActivityStatus(activityId, status, reason)` | 教师审核活动（→PUBLISHED 或 →REJECTED+理由） |
| `reviewActivity(activityId, status, reason)` | 教师审核活动（同上，另一个入口） |
| `endActivity(activityId, presidentId)` | 社长结束活动（→ENDED，为已通过+已签到的学生发积分） |
| `deleteById(activityId, userId)` | 删除活动（级联删除积分、签到、报名，需验证权限） |

**报名审核：**

| 方法 | 说明 |
|------|------|
| `registerActivity(studentId, activityId)` | 学生报名（状态=PENDING，检查重复和人数上限只算已通过的） |
| `cancelRegistration(studentId, activityId)` | 学生取消报名（已签到不可取消） |
| `approveRegistration(registrationId)` | 社长通过报名 |
| `rejectRegistration(registrationId, reason)` | 社长拒绝报名（含理由） |

**查询：**

| 方法 | 说明 |
|------|------|
| `getPublishedActivities()` | 学生端看到的已发布活动 |
| `getActivitiesByPresident(presidentId)` | 社长看到的自己社团的活动 |
| `getStudentRegisteredActivitiesWithCheckIn(studentId)` | 学生的报名列表（含签到状态、审核状态、拒绝理由） |
| `getActivityParticipantsWithDetails(activityId)` | 活动参与者详情（含学生姓名、学院、积分） |
| `getPendingActivities()` | 待审核活动列表（教师） |
| `getApprovedActivities()` | 已通过活动列表 |
| `getRejectedActivities()` | 已驳回活动列表 |
| `isPresidentOfActivity(presidentId, activityId)` | 判断是否为活动所属社团的社长 |
| `getActivityByCode(code)` | 按活动编码查 |
| `findByCheckInNumber(checkInNumber)` | 按签到码查 |
| `findAll()` | 获取全部活动 |

---

### 6. CheckInService.java — 签到服务
`src/main/java/com/club/managementsystem/service/CheckInService.java`

| 方法 | 说明 |
|------|------|
| `generateCheckInQRCode(activityId)` | 生成签到二维码（首次生成时创建 6 位签到码） |
| `checkIn(studentId, activityId)` | 扫码签到（校验活动状态、报名状态、防重复签到） |
| `manualCheckIn(presidentId, activityId, studentId)` | 社长手动签到（立即发积分） |
| `getActivityCheckInInfo(activityId)` | 获取签到统计（总数、已签到数、签到率） |
| `getCheckInRecordsByStudentId(studentId, page, size)` | 获取学生签到记录 |
| `getCheckInRecordsByActivityId(activityId, page, size)` | 获取活动签到记录 |

---

### 7. PointsService.java — 积分服务
`src/main/java/com/club/managementsystem/service/PointsService.java`

| 方法 | 说明 |
|------|------|
| `getStudentPoints(studentId)` | 学生积分记录列表（含活动名称、时间、类型） |
| `getStudentTotalPoints(studentId)` | 学生总积分 |
| `getPointsRanking()` | 积分排行榜（按积分降序） |
| `awardPointsToStudent(studentId, activityId, points)` | 发放积分（事务性，防重复发放） |
| `getAllPointsRules()` | 获取全部积分规则 |
| `addPointsRule(pointsRule)` | 新增积分规则（按活动类型去重） |
| `updatePointsRule(ruleId, pointsRule)` | 更新积分规则 |
| `deletePointsRule(ruleId)` | 删除积分规则 |
| `getPointsRuleByActivityType(activityType)` | 按活动类型查积分规则 |

---

### 8. StatisticsService.java — 统计服务
`src/main/java/com/club/managementsystem/service/StatisticsService.java`

| 方法 | 说明 |
|------|------|
| `getOverviewStatistics()` | 系统总览（各角色人数、社团数、活动状态分布、总积分） |
| `getAdminDashboardStatistics()` | 管理员仪表盘（用户总数、活动总数、学生数、教师数） |
| `getActivityStatistics()` | 活动统计（按状态/社团分组、参与人数） |
| `getClubStatistics()` | 社团统计（各社团活动数、社团类型分布） |
| `getPointsStatistics()` | 积分统计（积分区间分布、按活动类型统计） |
| `getActivityTypeDistribution(semester, clubId)` | 活动类型分布（可按学期/社团筛选） |
| `getClubActivityCount(semester, activityType)` | 各社团活动数排名（可筛选） |
| `getPointsTrend(semester, clubId)` | 近 6 个月积分发放趋势（可筛选） |
| `getParticipationRanking(semester, clubId, activityType, limit)` | 活动参与率排名（可筛选） |
| `getAllClubs()` | 社团列表（筛选下拉框用） |

---

## 五、controller — 控制器层（14 个文件）

接收 HTTP 请求，调用 Service，返回 `ApiResponse` 统一 JSON 格式：`{ success: bool, message: string, data: T }`。

### 1. AuthController.java → `/api/auth`
`src/main/java/com/club/managementsystem/controller/AuthController.java`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/login` | 登录：验证身份 → 生成 JWT → 写入 HttpOnly Cookie（7 天有效） |
| POST | `/api/auth/logout` | 登出：清除 JWT Cookie |
| GET | `/api/auth/me` | 获取当前用户信息（从 Cookie 读取 JWT 验证，支持 7 天免登录） |
| GET | `/api/auth/test` | 健康检查，确认后端运行中 |

---

### 2. StudentController.java → `/api/students`
`src/main/java/com/club/managementsystem/controller/StudentController.java`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/students` | 获取全部学生 |
| GET | `/api/students/{studentId}` | 获取单个学生详情 |
| POST | `/api/students` | 新增学生（管理员） |
| DELETE | `/api/students/{studentId}` | 删除学生（管理员，级联删除所有关联数据） |

---

### 3. ClubController.java → `/api/clubs`
`src/main/java/com/club/managementsystem/controller/ClubController.java`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/clubs` | 获取全部社团 |
| GET | `/api/clubs/{clubId}` | 获取社团基本信息 |
| GET | `/api/clubs/{clubId}/detail` | 获取社团详情（含社长、成员列表、活动数） |
| POST | `/api/clubs` | 创建社团（教师） |
| PUT | `/api/clubs/{clubId}` | 更新社团信息（社长） |
| DELETE | `/api/clubs/{clubId}` | 删除社团（教师/社长） |
| POST | `/api/clubs/{clubId}/apply` | 学生申请加入社团 |
| GET | `/api/clubs/{clubId}/pending-applications` | 获取待审批申请（社长） |
| PUT | `/api/clubs/applications/{membershipId}/review` | 审批加入申请（社长） |
| DELETE | `/api/clubs/my-applications/{membershipId}` | 学生删除自己的待审申请 |
| GET | `/api/clubs/my-clubs` | 获取社长管理的社团 |
| GET | `/api/clubs/by-president/{presidentId}` | 按社长查社团 |
| PUT | `/api/clubs/{clubId}/president` | 更换社长（教师） |
| DELETE | `/api/clubs/{clubId}/president` | 移除社长（教师） |

---

### 4. ClubMembershipController.java → `/api/club-memberships`
`src/main/java/com/club/managementsystem/controller/ClubMembershipController.java`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/club-memberships/president/members/{clubId}` | 获取社团成员（社长） |
| DELETE | `/api/club-memberships/president/remove/{clubId}/{studentId}` | 移除成员（社长） |
| DELETE | `/api/club-memberships/club/{clubId}/member/{studentId}` | 学生取消自己的申请 |

---

### 5. MemberManageController.java → `/api/member-manage`
`src/main/java/com/club/managementsystem/controller/MemberManageController.java`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/member-manage/club/{clubId}/members` | 获取已批准成员详情（社长） |
| DELETE | `/api/member-manage/club/{clubId}/member/{studentId}` | 移除成员（含理由，社长） |
| PUT | `/api/member-manage/{membershipId}/role` | 修改成员角色（社长） |
| GET | `/api/member-manage/club/{clubId}/pending-applications` | 获取待审批申请（社长） |
| GET | `/api/member-manage/student/{studentId}/clubs` | 获取学生加入的所有社团 |
| POST | `/api/member-manage/club/{clubId}/member/{studentId}/request-leave` | 学生申请退出社团（需理由） |

---

### 6. ActivityController.java → `/api/activities`
`src/main/java/com/club/managementsystem/controller/ActivityController.java`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/activities` | 获取活动列表（教师/管理员看全部，学生只看已发布） |
| GET | `/api/activities/{activityId}` | 获取单个活动详情 |
| GET | `/api/activities/club/{clubId}` | 获取某社团的所有活动 |
| POST | `/api/activities` | 创建活动（简单版，无文件上传） |

---

### 7. ActivityManageController.java → `/api/activity-manage`
`src/main/java/com/club/managementsystem/controller/ActivityManageController.java`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/activity-manage/create` | 创建活动（含海报/策划书上传，社长） |
| PUT | `/api/activity-manage/{activityId}/end` | 结束活动（社长，触发发积分） |
| DELETE | `/api/activity-manage/{activityId}` | 删除活动（级联删除所有关联数据） |
| GET | `/api/activity-manage/{activityId}` | 获取活动详情 |
| GET | `/api/activity-manage/my-activities` | 获取社长社团的活动 |
| GET | `/api/activity-manage/all-activities` | 获取全部活动（管理员/社长） |
| GET | `/api/activity-manage/pending` | 获取待审核活动（教师） |
| GET | `/api/activity-manage/approved` | 获取已通过活动 |
| GET | `/api/activity-manage/rejected` | 获取已驳回活动 |
| PUT | `/api/activity-manage/{activityId}/review` | 教师审核活动（通过→PUBLISHED / 驳回→REJECTED+理由） |
| GET | `/api/activity-manage/activity/{activityId}/registrations` | 获取活动报名记录（社长） |
| PUT | `/api/activity-manage/registrations/{registrationId}/approve` | 社长通过学生报名 |
| PUT | `/api/activity-manage/registrations/{registrationId}/reject` | 社长拒绝学生报名（含理由） |
| GET | `/api/activity-manage/student/{studentId}/activities` | 获取学生报名的活动 |
| GET | `/api/activity-manage/president/{studentId}/activities` | 获取某社长的活动 |
| GET | `/api/activity-manage/teacher/{studentId}/activities` | 获取教师待审活动 |

---

### 8. ActivityRegistrationController.java → `/api/activities`
`src/main/java/com/club/managementsystem/controller/ActivityRegistrationController.java`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/activities/{activityId}/register` | 学生报名活动（状态=PENDING，需社长审核） |
| DELETE | `/api/activities/{activityId}/register` | 学生取消报名 |
| GET | `/api/activities/my-registrations` | 获取学生报名列表（含签到状态、审核状态） |
| GET | `/api/activities/{activityId}/participants` | 获取活动参与者列表 |

---

### 9. CheckInController.java → `/api/checkin`
`src/main/java/com/club/managementsystem/controller/CheckInController.java`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/checkin/activity/{code}` | 按签到码获取活动信息 |
| GET | `/api/checkin/activity/{activityId}/qrcode` | 生成签到二维码 |
| POST | `/api/checkin/scan` | 扫码签到 |
| POST | `/api/checkin/manual` | 输入签到码手动签到 |
| POST | `/api/checkin/activity/{activityId}/checkin` | 按活动 ID 签到 |
| POST | `/api/checkin/activity/{activityId}/manual-checkin` | 社长手动为学生签到 |
| GET | `/api/checkin/student/{studentId}` | 获取学生签到记录 |
| GET | `/api/checkin/activity/{activityId}` | 获取活动签到记录 |
| GET | `/api/checkin/activity/{activityId}/status` | 获取活动签到统计 |
| GET | `/api/checkin/activity/by-number/{checkInNumber}` | 按签到编号查活动 |

---

### 10. PointsController.java → `/api/points`
`src/main/java/com/club/managementsystem/controller/PointsController.java`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/points/student/{studentId}` | 获取学生积分记录 |
| GET | `/api/points/student/{studentId}/total` | 获取学生总积分 |
| GET | `/api/points/ranking` | 积分排行榜 |
| GET | `/api/points/rules` | 获取全部积分规则 |
| POST | `/api/points/rules` | 新增积分规则 |
| PUT | `/api/points/rules/{ruleId}` | 更新积分规则 |
| DELETE | `/api/points/rules/{ruleId}` | 删除积分规则 |
| GET | `/api/points/rules/type/{activityType}` | 按活动类型查积分规则 |

---

### 11. StatisticsController.java → `/api/statistics`
`src/main/java/com/club/managementsystem/controller/StatisticsController.java`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/statistics/overview` | 系统总览统计 |
| GET | `/api/statistics/admin-dashboard` | 管理员仪表盘数据 |
| GET | `/api/statistics/activities` | 活动统计 |
| GET | `/api/statistics/club-stats` | 社团统计 |
| GET | `/api/statistics/points` | 积分统计 |
| GET | `/api/statistics/activity-types` | 活动类型分布（可按学期/社团筛选） |
| GET | `/api/statistics/club-activities` | 各社团活动数（可筛选） |
| GET | `/api/statistics/points-trend` | 积分发放趋势（可筛选） |
| GET | `/api/statistics/participation-ranking` | 活动参与率排名（可筛选） |
| GET | `/api/statistics/clubs` | 社团列表（筛选下拉框用） |

---

### 12. FileController.java → `/api/files`
`src/main/java/com/club/managementsystem/controller/FileController.java`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/files/upload` | 上传文件（MultipartFile → 存磁盘 + 写 FileRecord） |
| GET | `/api/files/download/{fileId}` | 下载文件（从 FileRecord 读路径 → 返回文件流） |

---

### 13. JspController.java → `/jsp`
`src/main/java/com/club/managementsystem/controller/JspController.java`

课程考核用，与 Vue 前端完全独立，仅用于展示 EL 表达式和 JSTL 标签。

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/jsp/clubs` | JSP 页面：社团列表（展示 EL + JSTL 核心标签） |
| GET | `/jsp/student/{studentId}` | JSP 页面：学生详情（展示 EL 表达式） |
| GET | `/jsp/dashboard` | JSP 页面：统计面板（展示 JSTL 格式化标签） |

---

### 14. VueForwardController.java → `/vue`
`src/main/java/com/club/managementsystem/controller/VueForwardController.java`

解决 Vue Router History 模式刷新 404 问题。将所有 `/vue/xxx` 请求（无文件扩展名的）转发到 `/vue/index.html`，让 Vue Router 在客户端接管路由。正则 `[^.]*` 确保 `.js`、`.css` 等静态资源不受影响。

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/vue/{path}` | 转发 Vue 子路由到 index.html |
| GET | `/vue` | 转发 Vue 根路由到 index.html |

---

## 六、config — 配置类（4 个文件）

### 1. SecurityConfig.java
`src/main/java/com/club/managementsystem/config/SecurityConfig.java`

Spring Security 配置。定义 `SecurityFilterChain`，只匹配 `/api/**` 路径。放行以下接口：
- `/api/auth/login`、`/api/auth/test`（登录和健康检查）
- `/api/files/download/**`（文件下载）
- `GET /api/activities`、`GET /api/activities/{id}`（活动查询）

其余 `/api/**` 接口需要 JWT 认证。静态资源（`/vue/**`、`/jsp/**`）不受 Security 影响。

### 2. WebConfig.java
`src/main/java/com/club/managementsystem/config/WebConfig.java`

Web MVC 配置。
- 注册静态资源映射：`/vue/**` → `classpath:/static/vue/`
- 配置 CORS 跨域：允许所有来源、所有方法、所有 Header

### 3. DataInitializer.java
`src/main/java/com/club/managementsystem/config/DataInitializer.java`

`@Order(2)`，应用启动时执行。检查数据库是否有初始数据，没有则创建：
- 管理员账号（admin/Admin@123）
- 测试教师、测试学生
- 测试社团、示例活动
- 默认积分规则

所有密码使用 BCrypt 加密。

### 4. PasswordMigrationRunner.java
`src/main/java/com/club/managementsystem/config/PasswordMigrationRunner.java`

`@Order(1)`，在 DataInitializer 之前执行。扫描所有学生记录，将明文密码自动迁移为 BCrypt 加密格式。已加密的密码（以 `$2a$` 开头）会跳过。

---

## 七、security — 安全层（1 个文件）

### JwtAuthenticationFilter.java
`src/main/java/com/club/managementsystem/security/JwtAuthenticationFilter.java`

继承 `OncePerRequestFilter`，每个请求执行一次。

**处理流程：**
1. 从 `Authorization: Bearer xxx` Header 提取 JWT
2. 如果 Header 没有，从 `jwt_token` Cookie 提取
3. 调用 `JwtUtil` 验证 Token 有效性
4. 有效则将用户信息设入 Spring SecurityContext，请求继续
5. 无效或无 Token 时，向后兼容：读取 `X-User-Id` Header 作为用户标识
6. 都没有则返回 401 未授权

---

## 八、filter — Servlet 过滤器（2 个文件）

### 1. CharacterEncodingFilter.java
`src/main/java/com/club/managementsystem/filter/CharacterEncodingFilter.java`

`@WebFilter(urlPatterns = "/*")`，拦截所有请求。强制设置请求和响应的字符编码为 UTF-8，防止中文乱码。

### 2. RequestLoggingFilter.java
`src/main/java/com/club/managementsystem/filter/RequestLoggingFilter.java`

`@WebFilter(urlPatterns = "/*")`，记录 API 请求日志。只记录 `/api/` 开头的请求，输出内容：
- HTTP 方法（GET/POST/PUT/DELETE）
- 请求 URI
- 查询参数
- 客户端 IP
- 请求耗时（毫秒）

静态资源（`.js`、`.css`、`.html` 等）不记录。

---

## 九、listener — Servlet 监听器（3 个文件）

### 1. SessionListener.java
`src/main/java/com/club/managementsystem/listener/SessionListener.java`

`@WebListener`，实现 `HttpSessionListener`。
- `sessionCreated()` — Session 创建时在线用户数 +1，打印日志
- `sessionDestroyed()` — Session 销毁时在线用户数 -1，打印日志
- 使用 `AtomicInteger` 维护线程安全的在线计数器

### 2. ApplicationListener.java
`src/main/java/com/club/managementsystem/listener/ApplicationListener.java`

`@WebListener`，实现 `ServletContextListener`。
- `contextInitialized()` — 应用启动时打印上下文路径和 Servlet 版本号
- `contextDestroyed()` — 应用关闭时打印关闭日志

### 3. RequestListener.java
`src/main/java/com/club/managementsystem/listener/RequestListener.java`

`@WebListener`，实现 `ServletRequestListener`。
- `requestInitialized()` — 请求创建时打印 HTTP 方法、URI、SessionID（只记录 `/api/` 请求）
- `requestDestroyed()` — 请求销毁时打印 HTTP 方法、URI

---

## 十、dto — 数据传输对象（15 个文件）

用于 Controller 层接收请求参数和封装响应数据。

### 1. ApiResponse.java
`src/main/java/com/club/managementsystem/dto/ApiResponse.java`

统一 API 响应格式：`{ success: boolean, message: String, data: T }`。提供静态方法 `success(data)` 和 `error(message)` 快速构建响应。

### 2. LoginRequest.java
`src/main/java/com/club/managementsystem/dto/LoginRequest.java`

登录请求体：`studentId` + `password` + `role`

### 3. LoginResponse.java
`src/main/java/com/club/managementsystem/dto/LoginResponse.java`

登录响应体：`token` + 用户信息（studentId、name、role）

### 4. ActivityRequest.java
`src/main/java/com/club/managementsystem/dto/ActivityRequest.java`

创建活动请求（含 `MultipartFile` 海报和策划书文件上传字段）。

### 5. ActivityDTO.java
`src/main/java/com/club/managementsystem/dto/ActivityDTO.java`

活动数据传输对象，用于活动信息的传递。

### 6. ActivityStatusUpdate.java
`src/main/java/com/club/managementsystem/dto/ActivityStatusUpdate.java`

活动状态更新请求：`status` + `rejectReason`

### 7. ClubDTO.java
`src/main/java/com/club/managementsystem/dto/ClubDTO.java`

社团详情 DTO：社团基本信息 + 社长信息 + 成员列表 + 成员数 + 活动数

### 8. CreateClubRequest.java
`src/main/java/com/club/managementsystem/dto/CreateClubRequest.java`

创建社团请求：社团名、简介、类型、社长学号等

### 9. UpdateClubRequest.java
`src/main/java/com/club/managementsystem/dto/UpdateClubRequest.java`

更新社团请求：社团名、简介、类型、联系方式、Logo 等

### 10. ClubApplicationRequest.java
`src/main/java/com/club/managementsystem/dto/ClubApplicationRequest.java`

社团加入申请请求

### 11. ClubApplicationResponse.java
`src/main/java/com/club/managementsystem/dto/ClubApplicationResponse.java`

社团申请响应

### 12. UpdateApplicationStatusRequest.java
`src/main/java/com/club/managementsystem/dto/UpdateApplicationStatusRequest.java`

更新申请状态请求：`status` + `reason`

### 13. MemberDetailDTO.java
`src/main/java/com/club/managementsystem/dto/MemberDetailDTO.java`

成员详情 DTO：学生基本信息 + 社团角色 + 加入时间

### 14. RemoveMemberRequest.java
`src/main/java/com/club/managementsystem/dto/RemoveMemberRequest.java`

移除成员请求：含 `reason` 理由字段

### 15. PresidentChangeRequest.java
`src/main/java/com/club/managementsystem/dto/PresidentChangeRequest.java`

社长变更请求：`newPresidentId` 新社长学号

---

## 十一、util — 工具类（4 个文件）

### 1. JwtUtil.java
`src/main/java/com/club/managementsystem/util/JwtUtil.java`

JWT 工具类。
- `generateToken(studentId, role)` — 生成 JWT Token（HMAC-SHA256 签名）
- `validateToken(token)` — 验证 Token 是否有效
- `getStudentIdFromToken(token)` — 从 Token 中提取学号
- `getRoleFromToken(token)` — 从 Token 中提取角色

密钥和过期时间从 `application.properties` 读取（`jwt.secret`、`jwt.expiration`）。

### 2. QRCodeUtil.java
`src/main/java/com/club/managementsystem/util/QRCodeUtil.java`

二维码生成工具。将签到码字符串编码为 QR Code 图片，返回 Base64 字符串。使用 Google ZXing 库。

### 3. RoleCheckUtil.java
`src/main/java/com/club/managementsystem/util/RoleCheckUtil.java`

角色检查工具类，提供静态方法：
- `isPresident(student)` — 是否为社长
- `isTeacher(student)` — 是否为教师
- `isAdmin(student)` — 是否为管理员
- `isStudent(student)` — 是否为学生

### 4. StudentDataUpdater.java
`src/main/java/com/club/managementsystem/util/StudentDataUpdater.java`

数据批量更新工具。一次性脚本，用于批量更新学生数据（如补全手机号、邮箱等字段）。

---

## 调用链路全景图

```
浏览器请求
  │
  ▼
CharacterEncodingFilter（UTF-8 编码）
  │
  ▼
RequestLoggingFilter（记录请求日志）
  │
  ▼
JwtAuthenticationFilter（JWT 认证 / X-User-Id 兼容）
  │
  ▼
RequestListener.requestInitialized()（请求创建通知）
  │
  ▼
Controller（接收请求，参数校验）
  │
  ▼
Service（业务逻辑，事务管理）
  │
  ▼
Repository（JPA 数据库操作）
  │
  ▼
Entity（映射数据库表）
  │
  ▼
Controller 返回 ApiResponse JSON
  │
  ▼
RequestListener.requestDestroyed()（请求销毁通知）
  │
  ▼
响应返回浏览器
```

**独立运行的组件：**
- `SessionListener` — 监听 Session 生命周期，统计在线用户
- `ApplicationListener` — 监听应用启停
- `DataInitializer` — 启动时初始化默认数据
- `PasswordMigrationRunner` — 启动时迁移明文密码为 BCrypt

---

## 枚举值汇总

| 实体 | 枚举名 | 取值 |
|------|--------|------|
| Student | Role | STUDENT, PRESIDENT, TEACHER, ADMIN |
| ClubMembership | Role | MEMBER, MANAGER, PRESIDENT |
| Activity | Status | PENDING, REJECTED, PUBLISHED, REGISTERING, ENDED, CANCELLED |
| ActivityRegistration | RegistrationStatus | PENDING, APPROVED, REJECTED |
| ActivityRegistration | CheckInMethod | QR_CODE, MANUAL |

---

## 设计特点

1. **无 JPA 关系映射**：所有外键（studentId、clubId、activityId 等）存储为普通字段，不使用 `@ManyToOne`、`@OneToMany`。关联查询在 Service 层手动完成。

2. **级联删除在 Service 层处理**：删除学生/社团/活动时，由 Service 代码逐表清理关联数据，而非依赖 JPA 的 `CascadeType`。

3. **统一响应格式**：所有 Controller 返回 `ApiResponse<T>`，前端统一解析 `success`、`message`、`data` 三个字段。

4. **双认证机制**：JWT Token 优先（从 Header 或 Cookie 读取），无 Token 时回退到 `X-User-Id` Header（向后兼容旧版前端）。

5. **报名两级审核**：活动层面由教师审核（PENDING→PUBLISHED），报名层面由社长审核（PENDING→APPROVED/REJECTED）。

---

# 项目技术栈完整说明

## 整体架构

```
┌─────────────────────────────────────────────────┐
│                  Spring Boot 3.5.7               │
│                  (Java 22, Maven)                │
├───────────────┬──────────────┬──────────────────┤
│  Spring MVC   │   Spring     │   Spring Data    │
│  (Web 层)     │   Security   │   JPA + Hibernate│
│               │  (安全层)    │   (持久层)       │
├───────────────┼──────────────┼──────────────────┤
│  Controller   │  JWT + BCrypt│  Entity +        │
│  @RestController│  Filter     │  Repository      │
│  @Controller  │  Cookie      │  自动建表        │
└───────────────┴──────────────┴──────────────────┘
         │                              │
         ▼                              ▼
┌─────────────────┐          ┌─────────────────┐
│  Vue 3 前端      │          │  MySQL 数据库    │
│  Vite + Router  │          │  club_management │
│  Pinia + Axios  │          │  _system         │
└─────────────────┘          └─────────────────┘
```

## 1. Spring Boot — 核心框架

Spring Boot 是整个项目的启动器和核心容器，自动配置 Spring 的所有组件。

| 项目 | 说明 | 文件 |
|------|------|------|
| 启动类 | `@SpringBootApplication` 注解，自动扫描、自动配置 | `ClubManagementSystemApplication.java` |
| 自动配置 | 内嵌 Tomcat 服务器，无需手动部署 WAR 包 | `pom.xml` 中 `spring-boot-starter-web` |
| 配置管理 | `application.properties` 集中管理数据库、端口、JWT 等所有配置 | `application.properties` |
| 组件扫描 | 自动扫描 `com.club.managementsystem` 下所有 `@Component`、`@Service`、`@Controller` | 启动类所在包及子包 |

Maven 核心依赖：

```
spring-boot-starter-web          — Web + 内嵌 Tomcat + Spring MVC
spring-boot-starter-data-jpa     — Spring Data JPA + Hibernate
spring-boot-starter-security     — Spring Security
spring-boot-starter-validation   — 参数校验
mysql-connector-j                — MySQL 驱动
jjwt-api / jjwt-impl / jjwt-jackson — JWT 生成解析
tomcat-embed-jasper + jakarta.servlet.jsp.jstl — JSP 支持
```

## 2. Spring MVC — Web 层（处理 HTTP 请求）

Spring MVC 负责接收前端请求、调用业务逻辑、返回响应。

| 注解 | 作用 | 使用文件 |
|------|------|------|
| `@RestController` | 标记 REST API 控制器，返回 JSON | AuthController、ActivityManageController、ClubController 等 |
| `@Controller` | 标记页面控制器，可返回视图 | VueForwardController、JspController |
| `@RequestMapping` | 映射 URL 路径到方法 | 所有 Controller |
| `@GetMapping` / `@PostMapping` / `@PutMapping` / `@DeleteMapping` | HTTP 方法映射 | 所有 Controller |
| `@RequestBody` | 接收 JSON 请求体 | 登录、审核等接口 |
| `@RequestPart` | 接收 multipart 表单数据（文件+JSON） | 活动创建接口 |
| `@PathVariable` | 从 URL 路径取参数 `/api/clubs/{id}` | 所有带路径参数的接口 |
| `@RequestParam` | 从查询字符串取参数 `?page=1` | 分页查询接口 |
| `@Value` | 读取 application.properties 配置 | FileController 读取上传目录 |
| `@CrossOrigin` | 跨域配置 | Controller 类级别 |
| `ResponseEntity` | 自定义 HTTP 响应状态码和头 | 文件下载接口 |
| `MultipartFile` | 文件上传对象 | FileController、ActivityManageController |

Controller 层文件清单：

| 文件 | 职责 |
|------|------|
| `AuthController.java` | 登录、登出、获取当前用户、Cookie 设置 |
| `ActivityManageController.java` | 活动 CRUD、审核、文件关联 |
| `ClubController.java` | 社团列表、申请加入 |
| `FileController.java` | 文件上传下载 |
| `CheckinController.java` | 签到、签到码生成 |
| `PointsController.java` | 积分管理 |
| `MemberManageController.java` | 成员审批、移除 |
| `VueForwardController.java` | Vue 路由转发 |
| `JspController.java` | JSP 页面渲染（EL+JSTL 展示） |

## 3. Spring Data JPA + Hibernate — 持久层（数据库操作）

JPA 是 Java 持久化规范，Hibernate 是它的实现。Spring Data JPA 在此基础上提供了更简洁的 Repository 接口。

核心概念：

| 概念 | 作用 | 示例 |
|------|------|------|
| `@Entity` | 标记 Java 类为数据库表映射 | `Student` 实体 → `student` 表 |
| `@Table` | 指定表名 | `@Table(name = "student")` |
| `@Id` | 主键字段 | `@Id @GeneratedValue(strategy = IDENTITY)` |
| `@Column` | 列映射 | `@Column(name = "student_id", unique = true)` |
| `@OneToMany` / `@ManyToOne` | 一对多 / 多对一关系 | 一个社团有多个成员 |
| `@ManyToMany` | 多对多关系 | 学生和社团的多对多 |
| `JpaRepository<Entity, ID>` | 继承后自动获得 CRUD 方法 | `findAll()`、`save()`、`deleteById()` |
| 方法名派生查询 | 按方法名自动生成 SQL | `findByStudentId(String id)` → `SELECT * FROM student WHERE student_id = ?` |
| `@Query` | 自定义 JPQL 或原生 SQL | `@Query("SELECT s FROM Student s WHERE s.college = ?1")` |
| `spring.jpa.hibernate.ddl-auto=update` | 启动时自动根据实体类更新表结构 | 新增字段自动加列 |

实体类文件清单：

| 文件 | 对应表 |
|------|------|
| `Student.java` | student — 学生信息 |
| `Club.java` | club — 社团信息 |
| `Activity.java` | activity — 活动信息 |
| `ClubMembership.java` | club_membership — 社团成员关系 |
| `ActivityRegistration.java` | activity_registration — 活动报名 |
| `PointsRecord.java` | points_record — 积分记录 |
| `PointsRule.java` | points_rule — 积分规则 |
| `CheckinRecord.java` | checkin_record — 签到记录 |
| `FileRecord.java` | file_record — 文件上传记录 |

Repository 文件清单：

| 文件 | 继承接口 | 关键自定义方法 |
|------|------|------|
| `StudentRepository.java` | `JpaRepository<Student, String>` | `findByStudentId()`, `findByRole()` |
| `ClubRepository.java` | `JpaRepository<Club, Long>` | `findByClubName()` |
| `ActivityRepository.java` | `JpaRepository<Activity, Long>` | `findByStatus()`, `findByClubId()` |
| `ClubMembershipRepository.java` | `JpaRepository<ClubMembership, Long>` | `findByClubIdAndStatus()` |
| `ActivityRegistrationRepository.java` | `JpaRepository<ActivityRegistration, Long>` | `findByActivityIdAndStudentId()` |
| `PointsRecordRepository.java` | `JpaRepository<PointsRecord, Long>` | `findByStudentId()` |
| `PointsRuleRepository.java` | `JpaRepository<PointsRule, Long>` | — |
| `CheckinRecordRepository.java` | `JpaRepository<CheckinRecord, Long>` | `findByActivityIdAndStudentId()` |
| `FileRecordRepository.java` | `JpaRepository<FileRecord, Long>` | — |

## 4. Spring Security — 安全层

Spring Security 提供认证（你是谁）和授权（你能做什么）。

| 组件 | 作用 | 文件 |
|------|------|------|
| `SecurityFilterChain` | 定义哪些 URL 需要认证、哪些放行 | `SecurityConfig.java` |
| `BCryptPasswordEncoder` | 密码加密（不可逆哈希） | `SecurityConfig.java` 注册 Bean |
| `OncePerRequestFilter` | 每个请求执行一次的过滤器 | `JwtAuthenticationFilter.java` |
| `http.csrf().disable()` | 禁用 CSRF（前后端分离用 JWT 不需要） | `SecurityConfig.java` |
| `http.addFilterBefore()` | 在过滤器链前插入自定义过滤器 | `SecurityConfig.java` |
| `SecurityContextHolder` | 存储当前请求的认证信息 | `JwtAuthenticationFilter.java` |

安全流程：

```
前端请求 → JwtAuthenticationFilter（解析 JWT/Cookie）
         → SecurityFilterChain（检查 URL 权限）
         → Controller（处理业务）
```

## 5. JWT（JSON Web Token）— 认证令牌

| 类/方法 | 作用 | 文件 |
|------|------|------|
| `generateToken(studentId, role)` | 生成 JWT 字符串 | `JwtUtil.java` |
| `parseToken(token)` | 解析 JWT，提取 userId 和 role | `JwtUtil.java` |
| `validateToken(token)` | 验证 JWT 是否有效（签名 + 过期时间） | `JwtUtil.java` |
| `setTokenInCookie(token, response)` | 将 JWT 写入 HttpOnly Cookie | `AuthController.java` |
| `readTokenFromCookie(request)` | 从 Cookie 读取 JWT | `JwtAuthenticationFilter.java` |

JWT 配置：

```properties
jwt.secret=Y2x1Yi1tYW5hZ2VtZW50LXN5c3RlbS1qd3Qtc2VjcmV0...
jwt.expiration=604800000   # 7天过期
```

## 6. 数据库 — MySQL

| 项目 | 值 |
|------|------|
| 数据库名 | `club_management_system` |
| 连接方式 | JDBC（`mysql-connector-j` 驱动） |
| 连接池 | HikariCP（Spring Boot 默认） |
| ORM 映射 | Hibernate（JPA 实现） |
| 表管理 | `ddl-auto=update` — 实体类变 → 表自动变 |

`application.properties` 数据库配置：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/club_management_system?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=UTF-8
spring.datasource.username=root
spring.datasource.password=123456
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

## 7. 前端 — Vue 3

| 技术 | 作用 | 说明 |
|------|------|------|
| Vue 3 | UI 框架 | Composition API（`<script setup>`） |
| Vite | 构建工具 | 开发热更新 + 生产构建 |
| Vue Router | 路由 | `createWebHistory('/vue/')` — History 模式 |
| Pinia | 状态管理 | auth store（用户信息、token）、toast store |
| Axios | HTTP 请求 | 请求拦截器自动带 JWT，响应拦截器处理 401 |
| Font Awesome | 图标库 | 全站图标 |

Vue 项目结构：

```
frontend/src/
├── main.js              — Vue 应用入口
├── App.vue              — 根组件（Toast + 路由动画）
├── router/index.js      — 20+ 页面路由
├── stores/
│   ├── auth.js          — 登录状态管理
│   └── toast.js         — 通知消息管理
├── api/index.js         — Axios 封装（JWT 注入 + FormData 处理）
├── views/               — 20 个页面组件
└── components/          — Toast 等公共组件
```

## 8. JavaWeb 监听器、过滤器与 JSP

### 监听器（Listener）

| 文件 | 接口 | 作用 |
|------|------|------|
| `SessionListener.java` | `HttpSessionListener` | 监听 Session 创建和销毁，记录在线用户数 |
| `AppLifecycleListener.java` | `ServletContextListener` | 监听应用启动和关闭，记录启动时间和版本号 |
| `SessionAttributeListener.java` | `HttpSessionAttributeListener` | 监听 Session 属性的添加、移除和替换 |

### 过滤器（Filter）

| 文件 | URL 模式 | 作用 |
|------|------|------|
| `JwtAuthenticationFilter.java` | `/api/**` | JWT 认证过滤器，从 Cookie/Header 解析用户身份 |
| `RequestLoggingFilter.java` | `/*` | 请求日志过滤器，记录每个请求的方法、URI、耗时 |
| `CharacterEncodingFilter.java` | `/*` | UTF-8 编码过滤器，防止中文乱码 |
| `JspLoggingFilter.java` | `/jsp/*` | JSP 请求专用日志过滤器 |

### JSP 页面（EL + JSTL）

| 文件 | EL 表达式用法 | JSTL 标签用法 |
|------|------|------|
| `clubs.jsp` | `${title}`、三元运算、`${pageContext.request.contextPath}` | `c:forEach`、`c:if`、`c:choose`、`c:out`、`c:set`、`c:url`、`fmt:formatDate`、`fn:length`、`fn:substring` |
| `activity-list.jsp` | `${activityCount}`、三元运算 | `c:forEach`、`c:if`、`c:choose`、`fmt:parseDate`、`fmt:formatDate`、`fn:contains` |
| `files.jsp` | `${fileCount}`、`${file.fileSize}` | `c:forEach`、`c:choose`、`c:if`、`c:out`、`fmt:formatNumber`、`fn:contains` |
| `dashboard.jsp` | `${studentCount}`、算术运算 `${a + b}` | `c:set`、`c:if`、`fmt:formatDate` |
| `student-detail.jsp` | `${found}`、`${student.studentId}` | `c:if`、`c:choose`、`c:out` |

## 9. 课程考核点完整对照

| 考核点 | 分值 | 实现方式 | 对应文件 |
|------|------|------|------|
| 数据库连接 | 50分 | MySQL + JDBC + HikariCP 连接池 | `application.properties` |
| JPA 实体映射 | — | `@Entity` + `@Table` + `@Column` + `@Id` | entity/ 目录 9 个实体类 |
| Repository 数据访问 | — | JpaRepository 继承 + 方法名派生查询 + `@Query` | repository/ 目录 9 个接口 |
| Cookie 自动登录 | 5分 | 登录写 HttpOnly Cookie → 过滤器从 Cookie 读 JWT → /me 验证 | AuthController + JwtAuthenticationFilter |
| 文件上传 | 5分 | `MultipartFile` + `FileController` + `FileRecord` 实体 | `FileController.java` |
| 文件下载 | 5分 | `InputStreamResource` + `ResponseEntity` + 超链接下载 | `FileController.java` + JSP 下载链接 |
| 监听器 | 5分 | `HttpSessionListener` + `ServletContextListener` + `HttpSessionAttributeListener` | 3 个 Listener 文件 |
| 过滤器 | 5分 | `@WebFilter` 请求日志 + `OncePerRequestFilter` JWT 认证 + 编码过滤器 | 4 个 Filter 文件 |
| EL 表达式 | 5分 | `${student.name}`、三元运算、`empty` 运算符、算术运算、`pageContext` | 5 个 JSP 文件 |
| JSTL 标签 | 5分 | `c:if`、`c:choose`、`c:forEach`、`c:set`、`c:url`、`c:out`、`fmt:formatDate`、`fmt:formatNumber`、`fn:length`、`fn:substring`、`fn:contains` | 5 个 JSP 文件 |
| Spring Security | — | SecurityFilterChain + BCryptPasswordEncoder + JWT 过滤器链 | `SecurityConfig.java` |
| 密码加密 | — | BCrypt 哈希 + 启动时自动迁移明文密码 | `PasswordMigrationRunner.java` |
| SSM 框架加分 | 5-10分 | Spring Boot + Spring MVC + Spring Data JPA（替代传统 SSM） | 全部后端代码 |
| 前端框架加分 | — | Vue 3 + Vite + Vue Router + Pinia | frontend/ 目录 |

## 10. 和传统 SSM 的对应关系

| 传统 SSM | 本项目 | 优势 |
|------|------|------|
| Spring（手动配置 XML） | Spring Boot（自动配置，零 XML） | 约定优于配置，开箱即用 |
| Spring MVC（web.xml 配置 DispatcherServlet） | Spring Boot 内置（spring-boot-starter-web） | 自动注册，无需 web.xml |
| MyBatis（手写 SQL 映射 XML + Mapper 接口） | Spring Data JPA + Hibernate（实体注解 + 方法名派生） | 不用写 SQL，自动建表，实体即表 |
| 手动集成安全框架 | Spring Security（spring-boot-starter-security） | 一行依赖引入完整安全体系 |
| 部署 WAR 到外部 Tomcat | 内嵌 Tomcat，`java -jar` 直接运行 | 部署更简单 |

> **一句话总结：** 本项目用的是 Spring Boot 全家桶，是传统 SSM 的现代升级版，功能完全覆盖且更强。
