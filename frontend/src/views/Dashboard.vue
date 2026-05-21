<template>
  <div class="dashboard-container">
    <!-- 侧边栏 -->
    <div class="sidebar">
      <div class="logo">
        <h2>社团管理系统</h2>
        <p>基于Spring Boot的管理平台</p>
      </div>
      <ul class="nav-menu">
        <li class="nav-item" v-for="item in navMenu" :key="item.to">
          <router-link :to="item.to" :class="['nav-link', { active: item.active }]">
            <i :class="item.icon"></i>
            {{ item.text }}
          </router-link>
        </li>
      </ul>
    </div>

    <!-- 主内容区域 -->
    <div class="main-content">
      <div class="header">
        <div class="user-info">
          <div class="user-avatar">
            <i class="fas fa-user"></i>
          </div>
          <div class="user-details">
            <h2>欢迎回来，{{ currentUser?.name || currentUser?.username }}！</h2>
            <div>
              <p><strong>角色：</strong>{{ getRoleName(currentUser?.role) }}</p>
              <p><strong>用户名：</strong>{{ currentUser?.username }}</p>
              <p v-if="currentUser?.studentId"><strong>学号/工号：</strong>{{ currentUser?.studentId }}</p>
              <p v-if="currentUser?.clubId"><strong>社团ID：</strong>{{ currentUser?.clubId }}</p>
            </div>
          </div>
        </div>
        <button class="logout-btn" @click="logout">
          <i class="fas fa-sign-out-alt"></i>
          退出登录
        </button>
      </div>

      <!-- 快速操作 -->
      <div class="quick-actions">
        <h3><i class="fas fa-bolt"></i> 快速操作</h3>
        <div class="action-buttons">
          <router-link v-for="action in quickActions" :key="action.to" :to="action.to" class="action-btn">
            <i :class="action.icon"></i>
            <span>{{ action.text }}</span>
          </router-link>
        </div>
      </div>

      <!-- 统计卡片 -->
      <div class="stats-grid">
        <div class="stat-card" v-for="(stat, idx) in stats" :key="idx">
          <div class="stat-icon">
            <i :class="stat.icon"></i>
          </div>
          <div class="stat-number">{{ stat.number }}</div>
          <div class="stat-label">{{ stat.label }}</div>
        </div>
      </div>

      <!-- 功能网格 -->
      <div class="features-grid">
        <div class="feature-card" v-for="feature in features" :key="feature.title">
          <h3 v-html="feature.title"></h3>
          <ul class="feature-list">
            <li v-for="item in feature.items" :key="item">
              <i class="fas fa-check"></i>{{ item }}
            </li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import api from '../api'

const router = useRouter()
const auth = useAuthStore()
const currentUser = computed(() => auth.user)

const navMenu = ref([])
const quickActions = ref([])
const stats = ref([])
const features = ref([])

function getRoleName(role) {
  const map = { STUDENT: '学生', PRESIDENT: '社长', TEACHER: '团委老师', ADMIN: '系统管理员' }
  return map[role] || role
}

function generateNavMenu() {
  const role = currentUser.value?.role
  switch (role) {
    case 'STUDENT':
      navMenu.value = [
        { icon: 'fas fa-home', text: '仪表盘', active: true, to: '/dashboard' },
        { icon: 'fas fa-calendar-alt', text: '活动中心', to: '/activity-center' },
        { icon: 'fas fa-tasks', text: '我的活动', to: '/my-activities' },
        { icon: 'fas fa-star', text: '我的积分', to: '/my-points' },
        { icon: 'fas fa-qrcode', text: '扫码签到', to: '/checkin' },
        { icon: 'fas fa-users', text: '社团中心', to: '/club-manage' },
        { icon: 'fas fa-user-plus', text: '我的申请', to: '/my-applications' }
      ]
      break
    case 'PRESIDENT':
      navMenu.value = [
        { icon: 'fas fa-home', text: '仪表盘', active: true, to: '/dashboard' },
        { icon: 'fas fa-plus-circle', text: '创建活动', to: '/create-activity' },
        { icon: 'fas fa-calendar', text: '活动管理', to: '/manage-activities' },
        { icon: 'fas fa-users', text: '社团管理', to: '/club-manage' },
        { icon: 'fas fa-users-cog', text: '成员管理', to: '/member-management' },
        { icon: 'fas fa-qrcode', text: '签到管理', to: '/checkin-manage' }
      ]
      break
    case 'TEACHER':
      navMenu.value = [
        { icon: 'fas fa-home', text: '仪表盘', active: true, to: '/dashboard' },
        { icon: 'fas fa-clipboard-check', text: '活动审核', to: '/activity-review' },
        { icon: 'fas fa-cog', text: '积分规则', to: '/points-rules' },
        { icon: 'fas fa-school', text: '社团管理', to: '/club-manage' },
        { icon: 'fas fa-chart-pie', text: '数据统计', to: '/global-statistics' }
      ]
      break
    case 'ADMIN':
      navMenu.value = [
        { icon: 'fas fa-home', text: '仪表盘', active: true, to: '/admin-dashboard' },
        { icon: 'fas fa-users-cog', text: '用户管理', to: '/admin-user-management' },
        { icon: 'fas fa-plus', text: '添加用户', to: '/add-user' }
      ]
      break
  }
}

function generateQuickActions() {
  const role = currentUser.value?.role
  switch (role) {
    case 'STUDENT':
      quickActions.value = [
        { icon: 'fas fa-calendar-plus', text: '浏览活动', to: '/activity-center' },
        { icon: 'fas fa-tasks', text: '我的报名', to: '/my-activities' },
        { icon: 'fas fa-star', text: '积分查询', to: '/my-points' },
        { icon: 'fas fa-qrcode', text: '扫码签到', to: '/checkin' },
        { icon: 'fas fa-users', text: '社团中心', to: '/club-manage' }
      ]
      break
    case 'PRESIDENT':
      quickActions.value = [
        { icon: 'fas fa-plus-circle', text: '创建活动', to: '/create-activity' },
        { icon: 'fas fa-calendar-check', text: '活动管理', to: '/manage-activities' },
        { icon: 'fas fa-users', text: '管理社团', to: '/club-manage' },
        { icon: 'fas fa-users-cog', text: '成员管理', to: '/member-management' },
        { icon: 'fas fa-qrcode', text: '生成签到码', to: '/checkin-manage' }
      ]
      break
    case 'TEACHER':
      quickActions.value = [
        { icon: 'fas fa-clipboard-check', text: '活动审核', to: '/activity-review' },
        { icon: 'fas fa-cog', text: '积分规则', to: '/points-rules' },
        { icon: 'fas fa-school', text: '社团管理', to: '/club-manage' },
        { icon: 'fas fa-chart-pie', text: '数据统计', to: '/global-statistics' }
      ]
      break
  }
}

function generateStats() {
  const role = currentUser.value?.role
  switch (role) {
    case 'STUDENT':
      stats.value = [
        { icon: 'fas fa-calendar-check', number: '12', label: '参与活动数' },
        { icon: 'fas fa-star', number: '86', label: '当前积分' },
        { icon: 'fas fa-trophy', number: '3', label: '本月排名' },
        { icon: 'fas fa-clock', number: '24', label: '待签到活动' }
      ]
      break
    case 'PRESIDENT':
      stats.value = [
        { icon: 'fas fa-calendar', number: '8', label: '本月活动数' },
        { icon: 'fas fa-users', number: '45', label: '社团成员数' },
        { icon: 'fas fa-chart-line', number: '78%', label: '活动参与率' },
        { icon: 'fas fa-check-circle', number: '5', label: '待审批活动' }
      ]
      break
    case 'TEACHER':
      stats.value = [
        { icon: 'fas fa-school', number: '23', label: '注册社团数' },
        { icon: 'fas fa-calendar', number: '156', label: '年度活动数' },
        { icon: 'fas fa-user-graduate', number: '1,234', label: '活跃学生数' },
        { icon: 'fas fa-clipboard-list', number: '12', label: '待审批申请' }
      ]
      break
  }
}

function generateFeatures() {
  const role = currentUser.value?.role
  switch (role) {
    case 'STUDENT':
      features.value = [
        { title: '<i class="fas fa-calendar-alt"></i>活动管理', items: ['浏览所有活动', '报名参与活动', '查看活动详情', '活动收藏'] },
        { title: '<i class="fas fa-user"></i>个人中心', items: ['查看个人积分', '查询活动记录', '修改个人信息', '积分明细查询'] },
        { title: '<i class="fas fa-users"></i>社团中心', items: ['浏览所有社团', '申请加入社团', '查看社团活动', '社团成员互动'] }
      ]
      break
    case 'PRESIDENT':
      features.value = [
        { title: '<i class="fas fa-calendar-plus"></i>活动管理', items: ['创建新活动', '管理本社活动', '活动审批查询', '活动状态跟踪'] },
        { title: '<i class="fas fa-users"></i>社团管理', items: ['管理社团成员', '查看社团数据', '成员权限设置'] }
      ]
      break
    case 'TEACHER':
      features.value = [
        { title: '<i class="fas fa-clipboard-check"></i>活动审核', items: ['审核活动申请', '查看审核记录', '活动监督管理', '违规处理'] },
        { title: '<i class="fas fa-cog"></i>系统管理', items: ['管理积分规则', '系统参数设置', '用户权限管理', '数据备份'] },
        { title: '<i class="fas fa-school"></i>社团管理', items: ['管理所有社团', '审核社团申请', '监督社团活动', '社团数据统计'] }
      ]
      break
  }
}

async function loadRealTimeData() {
  const role = currentUser.value?.role
  try {
    if (role === 'PRESIDENT') {
      const actRes = await api.get('/api/activity-manage/my-activities')
      if (actRes.data.success) {
        const activities = actRes.data.data
        const pendingCount = activities.filter(a => a.status === 'PENDING').length
        const publishedCount = activities.filter(a => a.status === 'PUBLISHED').length
        const rejectedCount = activities.filter(a => a.status === 'REJECTED').length
        stats.value[0].number = activities.length
        stats.value[1].number = pendingCount
        stats.value[2].number = publishedCount
        stats.value[3].number = rejectedCount
      }
      const clubsRes = await api.get('/api/clubs/my-clubs')
      if (clubsRes.data.success) {
        stats.value[1].number = clubsRes.data.data.length
      }
    }

    if (role === 'STUDENT') {
      const pointsRes = await api.get(`/api/points/student/${currentUser.value.studentId}/total`)
      if (pointsRes.data.success) {
        stats.value[1].number = pointsRes.data.data
      }
      const actRes = await api.get('/api/activities/my-registrations')
      if (actRes.data.success) {
        stats.value[0].number = actRes.data.data.length
      }
    }

    if (role === 'TEACHER') {
      const clubsRes = await api.get('/api/clubs')
      if (clubsRes.data.success) {
        stats.value[0].number = clubsRes.data.data.length
      }
      const pendingRes = await api.get('/api/activity-manage/pending')
      if (pendingRes.data.success) {
        stats.value[3].number = pendingRes.data.data.length
      }
      const overviewRes = await api.get('/api/statistics/overview')
      if (overviewRes.data.success) {
        const data = overviewRes.data.data
        stats.value[1].number = data.activityCount
        stats.value[2].number = data.activeStudentCount
      }
    }
  } catch (e) {
    console.error('加载实时数据失败:', e)
  }
}

function logout() {
  if (confirm('确定要退出登录吗？')) {
    auth.logout()
    router.push('/login')
  }
}

onMounted(() => {
  if (!auth.token) {
    router.push('/login')
    return
  }
  generateNavMenu()
  generateQuickActions()
  generateStats()
  generateFeatures()
  loadRealTimeData()
})
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.dashboard-container {
  display: flex;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #333;
}

/* 侧边栏样式 */
.sidebar {
  width: 260px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  padding: 20px 0;
  box-shadow: 2px 0 10px rgba(0,0,0,0.1);
}

.logo {
  text-align: center;
  padding: 20px;
  border-bottom: 1px solid #eee;
  margin-bottom: 20px;
}

.logo h2 {
  color: #667eea;
  font-size: 22px;
}

.logo p {
  color: #666;
  font-size: 12px;
}

.nav-menu {
  list-style: none;
  padding: 0 20px;
}

.nav-item {
  margin-bottom: 5px;
}

.nav-link {
  display: flex;
  align-items: center;
  padding: 12px 15px;
  color: #555;
  text-decoration: none;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.nav-link:hover {
  background: #667eea;
  color: white;
}

.nav-link.active {
  background: #667eea;
  color: white;
}

.nav-link i {
  margin-right: 10px;
  width: 20px;
  text-align: center;
}

/* 主内容区域 */
.main-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

.header {
  background: white;
  border-radius: 15px;
  padding: 20px 30px;
  margin-bottom: 20px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
}

.user-avatar {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
  margin-right: 15px;
}

.user-details h2 {
  color: #333;
  margin-bottom: 5px;
}

.user-details p {
  color: #666;
  font-size: 14px;
}

.logout-btn {
  background: #f44336;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.3s;
  display: flex;
  align-items: center;
  gap: 8px;
}

.logout-btn:hover {
  background: #d32f2f;
}

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  background: white;
  border-radius: 15px;
  padding: 25px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.1);
  text-align: center;
  transition: transform 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-icon {
  width: 60px;
  height: 60px;
  margin: 0 auto 15px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
}

.stat-card:nth-child(1) .stat-icon { background: #4CAF50; }
.stat-card:nth-child(2) .stat-icon { background: #2196F3; }
.stat-card:nth-child(3) .stat-icon { background: #FF9800; }
.stat-card:nth-child(4) .stat-icon { background: #9C27B0; }

.stat-number {
  font-size: 32px;
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}

.stat-label {
  color: #666;
  font-size: 14px;
}

/* 功能网格 */
.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.feature-card {
  background: white;
  border-radius: 15px;
  padding: 25px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.1);
  border-left: 4px solid #667eea;
  transition: all 0.3s ease;
}

.feature-card:hover {
  box-shadow: 0 8px 25px rgba(0,0,0,0.15);
  transform: translateY(-2px);
}

.feature-card h3 {
  color: #333;
  margin-bottom: 15px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.feature-card h3 :deep(i) {
  color: #667eea;
}

.feature-list {
  list-style: none;
}

.feature-list li {
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #555;
}

.feature-list li:last-child {
  border-bottom: none;
}

.feature-list li i {
  color: #4CAF50;
  font-size: 12px;
}

/* 快速操作卡片 */
.quick-actions {
  background: white;
  border-radius: 15px;
  padding: 25px;
  margin-bottom: 20px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.1);
}

.quick-actions h3 {
  color: #333;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.action-buttons {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 15px;
}

.action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: #f8f9fa;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  text-decoration: none;
  color: #333;
}

.action-btn:hover {
  background: #667eea;
  color: white;
  transform: translateY(-3px);
}

.action-btn i {
  font-size: 24px;
  margin-bottom: 10px;
}

.action-btn span {
  font-size: 14px;
  font-weight: 500;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .dashboard-container {
    flex-direction: column;
  }

  .sidebar {
    width: 100%;
    order: 2;
  }

  .main-content {
    order: 1;
  }

  .header {
    flex-direction: column;
    gap: 15px;
    text-align: center;
  }

  .user-info {
    flex-direction: column;
    text-align: center;
  }

  .user-avatar {
    margin-right: 0;
    margin-bottom: 10px;
  }

  .action-buttons {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 480px) {
  .action-buttons {
    grid-template-columns: 1fr;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .features-grid {
    grid-template-columns: 1fr;
  }
}
</style>
