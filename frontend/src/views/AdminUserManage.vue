<template>
  <div class="page-container">
    <div class="container">
      <!-- Header -->
      <div class="header">
        <h1><i class="fas fa-users-cog"></i> 用户管理</h1>
        <div>
          <router-link to="/admin-dashboard" class="btn btn-secondary">
            <i class="fas fa-arrow-left"></i> 返回仪表盘
          </router-link>
        </div>
      </div>

      <div class="card">
        <!-- Statistics Bar -->
        <div class="statistics-bar">
          <div class="stat-item">
            <h4>总用户数</h4>
            <p>{{ stats.total }}</p>
          </div>
          <div class="stat-item">
            <h4>学生用户</h4>
            <p>{{ stats.student }}</p>
          </div>
          <div class="stat-item">
            <h4>教师用户</h4>
            <p>{{ stats.teacher }}</p>
          </div>
          <div class="stat-item">
            <h4>社团社长</h4>
            <p>{{ stats.president }}</p>
          </div>
        </div>

        <!-- Filters -->
        <div class="filter-section">
          <div class="form-group">
            <label for="roleFilter">用户角色</label>
            <select id="roleFilter" v-model="roleFilter" @change="applyFilters">
              <option value="">所有角色</option>
              <option value="student">学生</option>
              <option value="teacher">教师</option>
              <option value="president">社团社长</option>
              <option value="admin">系统管理员</option>
            </select>
          </div>
          <div class="form-group search-box">
            <label for="searchInput">搜索</label>
            <i class="fas fa-search"></i>
            <input
              id="searchInput"
              v-model="searchText"
              type="text"
              placeholder="搜索用户名或姓名"
              @keyup.enter="applyFilters"
            />
            <button class="search-btn" @click="applyFilters"><i class="fas fa-search"></i> 搜索</button>
          </div>
          <div class="form-group">
            <label for="collegeFilter">学院</label>
            <select id="collegeFilter" v-model="collegeFilter" @change="applyFilters">
              <option value="">所有学院</option>
              <option v-for="c in colleges" :key="c" :value="c">{{ c }}</option>
            </select>
          </div>
        </div>

        <!-- User Table -->
        <div class="table-container">
          <table>
            <thead>
              <tr>
                <th>学号</th>
                <th>姓名</th>
                <th>学院</th>
                <th>班级</th>
                <th>角色</th>
                <th>总积分</th>
                <th>注册时间</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="loading">
                <td colspan="8">
                  <div class="loading-state">
                    <i class="fas fa-spinner fa-spin"></i> 加载中...
                  </div>
                </td>
              </tr>
              <tr v-else-if="paginatedUsers.length === 0">
                <td colspan="8">
                  <div class="empty-state">
                    <i class="fas fa-users-slash"></i>
                    <p>暂无匹配的用户</p>
                  </div>
                </td>
              </tr>
              <tr v-for="user in paginatedUsers" :key="user.studentId">
                <td>{{ user.studentId }}</td>
                <td>{{ user.name }}</td>
                <td>{{ user.college }}</td>
                <td>{{ user.majorClass || '-' }}</td>
                <td>
                  <span :class="['role-badge', user.role ? user.role.toLowerCase() : '']">
                    {{ roleLabel(user.role) }}
                  </span>
                </td>
                <td>{{ user.totalPoints || 0 }}</td>
                <td>{{ formatDateTime(user.createdAt) }}</td>
                <td>
                  <div class="actions">
                    <button class="btn btn-primary btn-sm" @click="viewUserDetails(user)">
                      <i class="fas fa-eye"></i> 查看
                    </button>
                    <button class="btn btn-danger btn-sm" @click="deleteUser(user)">
                      <i class="fas fa-trash"></i> 删除
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Pagination -->
        <div v-if="totalPages > 1" class="pagination">
          <button :disabled="currentPage === 1" @click="changePage(currentPage - 1)">
            <i class="fas fa-chevron-left"></i>
          </button>
          <button
            v-for="page in totalPages"
            :key="page"
            :class="{ active: page === currentPage }"
            @click="changePage(page)"
          >
            {{ page }}
          </button>
          <button :disabled="currentPage === totalPages" @click="changePage(currentPage + 1)">
            <i class="fas fa-chevron-right"></i>
          </button>
        </div>
      </div>
    </div>

    <!-- User Detail Modal -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3><i class="fas fa-user-circle"></i> 用户详情</h3>
          <button class="close-btn" @click="closeModal">&times;</button>
        </div>
        <div v-if="selectedUser" class="user-details">
          <div class="detail-item">
            <span class="detail-label">学号</span>
            <div class="detail-value">{{ selectedUser.studentId }}</div>
          </div>
          <div class="detail-item">
            <span class="detail-label">姓名</span>
            <div class="detail-value">{{ selectedUser.name }}</div>
          </div>
          <div class="detail-item">
            <span class="detail-label">学院</span>
            <div class="detail-value">{{ selectedUser.college }}</div>
          </div>
          <div class="detail-item">
            <span class="detail-label">班级</span>
            <div class="detail-value">{{ selectedUser.majorClass || '未填写' }}</div>
          </div>
          <div class="detail-item">
            <span class="detail-label">手机号</span>
            <div class="detail-value">{{ selectedUser.phone || '未填写' }}</div>
          </div>
          <div class="detail-item">
            <span class="detail-label">邮箱</span>
            <div class="detail-value">{{ selectedUser.email || '未填写' }}</div>
          </div>
          <div class="detail-item">
            <span class="detail-label">角色</span>
            <div class="detail-value">
              <span :class="['role-badge', selectedUser.role ? selectedUser.role.toLowerCase() : '']">
                {{ roleLabel(selectedUser.role) }}
              </span>
            </div>
          </div>
          <div class="detail-item">
            <span class="detail-label">总积分</span>
            <div class="detail-value">{{ selectedUser.totalPoints || 0 }}</div>
          </div>
          <div class="detail-item full-width">
            <span class="detail-label">注册时间</span>
            <div class="detail-value">{{ formatDateTime(selectedUser.createdAt) }}</div>
          </div>
        </div>

        <!-- Activities Section -->
        <div class="activities-section">
          <h4 class="activities-title">
            <i class="fas fa-calendar-alt"></i> 活动信息
          </h4>
          <div v-if="activitiesLoading" class="activities-loading">
            <i class="fas fa-spinner fa-spin"></i> 加载中...
          </div>
          <div v-else-if="userActivities.length === 0" class="activities-empty">
            暂无相关活动
          </div>
          <div v-else class="activities-list">
            <h5>{{ activitiesTitle }} ({{ userActivities.length }}个)</h5>
            <div class="activities-scroll">
              <div v-for="act in userActivities" :key="act.activityId" class="activity-card">
                <div class="activity-card-header">
                  <strong>{{ act.activityName }}</strong>
                  <span :class="['status-badge', statusClass(act.status)]">{{ statusLabel(act.status) }}</span>
                </div>
                <div class="activity-card-info">
                  <span><i class="fas fa-calendar-alt"></i> {{ formatDateTime(act.activityTime) }}</span>
                  <span><i class="fas fa-map-marker-alt"></i> {{ act.location }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="modal-footer">
          <button class="btn btn-secondary" @click="closeModal">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useToastStore } from '../stores/toast'
import api from '../api'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const toast = useToastStore()

const allUsers = ref([])
const filteredUsers = ref([])
const colleges = ref([])
const loading = ref(true)
const currentPage = ref(1)
const usersPerPage = 10

const roleFilter = ref('')
const searchText = ref('')
const collegeFilter = ref('')

const showModal = ref(false)
const selectedUser = ref(null)
const userActivities = ref([])
const activitiesLoading = ref(false)

const stats = computed(() => {
  const users = allUsers.value
  return {
    total: users.length,
    student: users.filter(u => u.role === 'STUDENT').length,
    teacher: users.filter(u => u.role === 'TEACHER').length,
    president: users.filter(u => u.role === 'PRESIDENT').length
  }
})

const totalPages = computed(() => Math.ceil(filteredUsers.value.length / usersPerPage))

const paginatedUsers = computed(() => {
  const start = (currentPage.value - 1) * usersPerPage
  return filteredUsers.value.slice(start, start + usersPerPage)
})

const activitiesTitle = computed(() => {
  if (!selectedUser.value) return ''
  const map = { STUDENT: '报名的活动', PRESIDENT: '创办的活动', TEACHER: '审核的活动' }
  return map[selectedUser.value.role] || '活动'
})

function roleLabel(role) {
  const map = { STUDENT: '学生', TEACHER: '教师', PRESIDENT: '社团社长', ADMIN: '系统管理员' }
  return map[role] || role
}

function statusClass(s) {
  const map = { PENDING: 'status-pending', APPROVED: 'status-approved', REJECTED: 'status-rejected', ACTIVE: 'status-active', ENDED: 'status-ended' }
  return map[s] || ''
}

function statusLabel(s) {
  const map = { PENDING: '待审核', APPROVED: '已通过', REJECTED: '已驳回', ACTIVE: '进行中', ENDED: '已结束' }
  return map[s] || s
}

function formatDateTime(dt) {
  if (!dt) return '未知时间'
  const d = new Date(dt)
  return d.toLocaleString('zh-CN', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit'
  })
}

async function loadUsers() {
  loading.value = true
  try {
    const res = await api.get('/api/students')
    if (res.data.success && res.data.data) {
      allUsers.value = res.data.data
      const collegeSet = [...new Set(allUsers.value.map(u => u.college))].sort()
      colleges.value = collegeSet
      applyFilters()
    }
  } catch (e) {
    console.error('加载用户列表失败:', e)
  }
  loading.value = false
}

function applyFilters() {
  const role = roleFilter.value.toLowerCase()
  const search = searchText.value.toLowerCase()
  const college = collegeFilter.value

  filteredUsers.value = allUsers.value.filter(user => {
    const roleMatch = !role || (user.role && user.role.toLowerCase() === role)
    const searchMatch = !search ||
      (user.studentId && user.studentId.toLowerCase().includes(search)) ||
      (user.name && user.name.toLowerCase().includes(search))
    const collegeMatch = !college || user.college === college
    return roleMatch && searchMatch && collegeMatch
  })

  currentPage.value = 1
}

function changePage(page) {
  if (page < 1 || page > totalPages.value) return
  currentPage.value = page
}

async function viewUserDetails(user) {
  selectedUser.value = user
  showModal.value = true
  await loadUserActivities(user)
}

async function loadUserActivities(user) {
  activitiesLoading.value = true
  userActivities.value = []

  try {
    let url = ''
    switch (user.role) {
      case 'STUDENT':
        url = `/api/activity-manage/student/${user.studentId}/activities`
        break
      case 'PRESIDENT':
        url = `/api/activity-manage/president/${user.studentId}/activities`
        break
      case 'TEACHER':
        url = `/api/activity-manage/teacher/${user.studentId}/activities`
        break
      default:
        activitiesLoading.value = false
        return
    }

    const res = await api.get(url)
    if (res.data.success && res.data.data) {
      userActivities.value = res.data.data
    }
  } catch (e) {
    console.error('加载活动信息失败:', e)
  }

  activitiesLoading.value = false
}

function closeModal() {
  showModal.value = false
  selectedUser.value = null
  userActivities.value = []
}

async function deleteUser(user) {
  if (!confirm(`确定要删除用户 ${user.name} (${user.studentId}) 吗？此操作不可撤销。`)) return

  try {
    const res = await api.delete(`/api/students/${user.studentId}`)
    if (res.data.success) {
      toast.success(`用户 ${user.name} (${user.studentId}) 已删除`)
      await loadUsers()
    } else {
      toast.error(`删除失败：${res.data.message}`)
    }
  } catch (e) {
    console.error('删除用户失败:', e)
    toast.error('删除用户失败：网络错误')
  }
}

onMounted(() => {
  if (!auth.token || auth.user?.role !== 'ADMIN') {
    router.push('/dashboard')
    return
  }

  // Handle URL query param for role pre-filter
  if (route.query.role) {
    roleFilter.value = route.query.role
  }

  loadUsers()
})
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.page-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #333;
}

.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}

/* Header */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  background: white;
  padding: 25px;
  border-radius: 15px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border-left: 4px solid #667eea;
}

.header h1 {
  color: #333;
  font-size: 28px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.header h1 i {
  color: #667eea;
}

/* Card */
.card {
  background: white;
  border-radius: 15px;
  padding: 30px;
  margin-bottom: 25px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

/* Buttons */
.btn {
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
}

.btn-primary {
  background: linear-gradient(45deg, #667eea, #764ba2);
  color: white;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
  position: relative;
  overflow: hidden;
}

.btn-primary::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s;
}

.btn-primary:hover::before {
  left: 100%;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.btn-danger {
  background: linear-gradient(45deg, #f44336, #d32f2f);
  color: white;
  box-shadow: 0 4px 15px rgba(244, 67, 54, 0.3);
  position: relative;
  overflow: hidden;
}

.btn-danger::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s;
}

.btn-danger:hover::before {
  left: 100%;
}

.btn-danger:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(244, 67, 54, 0.4);
}

.btn-secondary {
  background: linear-gradient(45deg, #6c757d, #5a6268);
  color: white;
  box-shadow: 0 4px 15px rgba(108, 117, 125, 0.3);
  position: relative;
  overflow: hidden;
}

.btn-secondary::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s;
}

.btn-secondary:hover::before {
  left: 100%;
}

.btn-secondary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(108, 117, 125, 0.4);
}

.btn-sm {
  padding: 6px 12px;
  font-size: 12px;
}

/* Statistics Bar */
.statistics-bar {
  display: flex;
  justify-content: space-around;
  margin-bottom: 30px;
  gap: 20px;
}

.stat-item {
  flex: 1;
  background: linear-gradient(45deg, #667eea, #764ba2);
  color: white;
  padding: 20px;
  border-radius: 10px;
  text-align: center;
}

.stat-item h4 {
  font-size: 14px;
  margin-bottom: 8px;
  opacity: 0.9;
}

.stat-item p {
  font-size: 24px;
  font-weight: 700;
}

/* Filter Section */
.filter-section {
  display: flex;
  gap: 20px;
  margin-bottom: 30px;
  flex-wrap: wrap;
}

.form-group {
  flex: 1;
  min-width: 200px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 600;
  color: #555;
  font-size: 14px;
}

.form-group select,
.form-group input {
  width: 100%;
  padding: 12px 16px;
  border: 2px solid #e9ecef;
  border-radius: 8px;
  font-size: 14px;
  transition: all 0.3s ease;
  background: #fafbfc;
}

.form-group select:focus,
.form-group input:focus {
  outline: none;
  border-color: #667eea;
  background: white;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.search-box {
  position: relative;
}

.search-box i {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #6c757d;
  pointer-events: none;
}

.search-box input {
  padding-left: 40px;
  padding-right: 90px;
}

.search-btn {
  position: absolute;
  right: 4px;
  top: 50%;
  transform: translateY(-50%);
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 10px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.3s ease;
  z-index: 1;
}

.search-btn:hover {
  transform: translateY(-50%) scale(1.05);
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

/* Table */
.table-container {
  overflow-x: auto;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

table {
  width: 100%;
  border-collapse: collapse;
  background: white;
}

thead {
  background: linear-gradient(45deg, #667eea, #764ba2);
  color: white;
}

th {
  padding: 15px;
  text-align: left;
  font-weight: 600;
  font-size: 14px;
}

td {
  padding: 15px;
  border-bottom: 1px solid #e9ecef;
}

tbody tr {
  transition: background-color 0.3s ease;
}

tbody tr:hover {
  background-color: #f8f9fa;
}

.loading-state {
  text-align: center;
  padding: 40px 20px;
  color: #666;
  font-size: 16px;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #6c757d;
}

.empty-state i {
  font-size: 60px;
  margin-bottom: 20px;
  opacity: 0.3;
  display: block;
}

.empty-state p {
  font-size: 16px;
  margin-bottom: 10px;
}

/* Role Badges */
.role-badge {
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  display: inline-block;
}

.role-badge.student {
  background-color: #d1ecf1;
  color: #0c5460;
}

.role-badge.teacher {
  background-color: #d4edda;
  color: #155724;
}

.role-badge.president {
  background-color: #fff3cd;
  color: #856404;
}

.role-badge.admin {
  background-color: #f8d7da;
  color: #721c24;
}

/* Actions */
.actions {
  display: flex;
  gap: 8px;
}

/* Pagination */
.pagination {
  display: flex;
  justify-content: center;
  margin-top: 30px;
  gap: 8px;
}

.pagination button {
  padding: 8px 16px;
  border: 1px solid #dee2e6;
  background: white;
  color: #6c757d;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 14px;
}

.pagination button:hover:not(:disabled) {
  background: #e9ecef;
}

.pagination button.active {
  background: linear-gradient(45deg, #667eea, #764ba2);
  color: white;
  border-color: #667eea;
}

.pagination button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Modal */
.modal-overlay {
  position: fixed;
  z-index: 1000;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  overflow: auto;
  background-color: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(5px);
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding-top: 5vh;
}

.modal-content {
  background-color: white;
  padding: 30px;
  border-radius: 15px;
  width: 90%;
  max-width: 600px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  animation: modalSlideIn 0.3s ease;
  margin-bottom: 5vh;
}

@keyframes modalSlideIn {
  from {
    opacity: 0;
    transform: translateY(-50px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  padding-bottom: 15px;
  border-bottom: 2px solid #f0f0f0;
}

.modal-header h3 {
  color: #333;
  font-size: 24px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.modal-header h3 i {
  color: #667eea;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #999;
  transition: color 0.3s ease;
}

.close-btn:hover {
  color: #333;
}

/* User Details Grid */
.user-details {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.detail-item {
  margin-bottom: 15px;
}

.detail-label {
  display: block;
  font-weight: 600;
  color: #666;
  font-size: 14px;
  margin-bottom: 5px;
}

.detail-value {
  font-size: 16px;
  color: #333;
  padding: 8px 12px;
  background-color: #f8f9fa;
  border-radius: 6px;
  min-height: 38px;
  display: flex;
  align-items: center;
}

.detail-item.full-width {
  grid-column: 1 / -1;
}

/* Activities Section */
.activities-section {
  margin-top: 25px;
  padding-top: 20px;
  border-top: 2px solid #f0f0f0;
}

.activities-title {
  margin-bottom: 15px;
  color: #667eea;
  font-size: 18px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.activities-loading,
.activities-empty {
  text-align: center;
  color: #666;
  padding: 20px;
}

.activities-list h5 {
  margin-bottom: 10px;
  color: #555;
}

.activities-scroll {
  max-height: 300px;
  overflow-y: auto;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 10px;
}

.activity-card {
  background: #f8f9fa;
  padding: 12px;
  margin-bottom: 8px;
  border-radius: 6px;
  border-left: 4px solid #667eea;
}

.activity-card:last-child {
  margin-bottom: 0;
}

.activity-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 5px;
}

.activity-card-header strong {
  color: #333;
}

.activity-card-info {
  font-size: 13px;
  color: #666;
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.activity-card-info i {
  margin-right: 4px;
}

/* Status Badges */
.status-badge {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 600;
}

.status-pending {
  background-color: #fff3cd;
  color: #856404;
}

.status-approved {
  background-color: #d4edda;
  color: #155724;
}

.status-rejected {
  background-color: #f8d7da;
  color: #721c24;
}

.status-active {
  background-color: #d1ecf1;
  color: #0c5460;
}

.status-ended {
  background-color: #e2e3e5;
  color: #383d41;
}

/* Modal Footer */
.modal-footer {
  text-align: right;
  margin-top: 30px;
  padding-top: 15px;
  border-top: 2px solid #f0f0f0;
}

/* Responsive */
@media (max-width: 768px) {
  .filter-section {
    flex-direction: column;
  }

  .statistics-bar {
    flex-direction: column;
  }

  .header {
    flex-direction: column;
    gap: 20px;
    align-items: flex-start;
  }

  .user-details {
    grid-template-columns: 1fr;
  }

  .actions {
    flex-direction: column;
    gap: 4px;
  }
}
</style>
