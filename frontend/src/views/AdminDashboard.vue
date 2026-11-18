<template>
  <div class="dashboard-container">
    <aside class="sidebar">
      <div class="logo">
        <h2>社团管理系统</h2>
        <p>管理员后台</p>
      </div>
      <ul class="nav-menu">
        <li class="nav-item"><a class="nav-link active"><i class="fas fa-tachometer-alt"></i><span>仪表盘</span></a></li>
        <li class="nav-item"><router-link to="/admin-user-management" class="nav-link"><i class="fas fa-users"></i><span>用户管理</span></router-link></li>
        <li class="nav-item"><router-link to="/manage-activities" class="nav-link"><i class="fas fa-calendar-alt"></i><span>活动管理</span></router-link></li>
        <li class="nav-item"><a class="nav-link" @click="logout"><i class="fas fa-sign-out-alt"></i><span>退出登录</span></a></li>
      </ul>
    </aside>

    <main class="main-content">
      <header class="header">
        <h1>管理员仪表盘</h1>
        <div class="user-info">
          <i class="fas fa-user-circle"></i>
          <span>{{ auth.user.name || '系统管理员' }}</span>
        </div>
      </header>

      <section class="stats-grid">
        <div class="stat-card" @click="$router.push('/admin-user-management')">
          <div class="stat-icon total-users"><i class="fas fa-users"></i></div>
          <div class="stat-info">
            <h3>{{ totalUsers }}</h3>
            <p>总用户数</p>
          </div>
        </div>
        <div class="stat-card" @click="$router.push('/manage-activities')">
          <div class="stat-icon total-activities"><i class="fas fa-calendar-alt"></i></div>
          <div class="stat-info">
            <h3>{{ totalActivities }}</h3>
            <p>总活动数</p>
          </div>
        </div>
        <div class="stat-card" @click="$router.push('/admin-user-management?role=student')">
          <div class="stat-icon student-users"><i class="fas fa-user-graduate"></i></div>
          <div class="stat-info">
            <h3>{{ studentUsers }}</h3>
            <p>学生用户数</p>
          </div>
        </div>
        <div class="stat-card" @click="$router.push('/admin-user-management?role=teacher')">
          <div class="stat-icon teacher-users"><i class="fas fa-chalkboard-teacher"></i></div>
          <div class="stat-info">
            <h3>{{ teacherUsers }}</h3>
            <p>教师用户数</p>
          </div>
        </div>
      </section>

      <section class="quick-actions">
        <h2>快捷操作</h2>
        <div class="actions-grid">
          <div class="action-card gradient-1" @click="$router.push('/add-user')">
            <i class="fas fa-user-plus"></i>
            <h3>添加用户</h3>
            <p>创建新的系统用户</p>
          </div>
          <div class="action-card gradient-1" @click="$router.push('/admin-user-management')">
            <i class="fas fa-user-cog"></i>
            <h3>用户管理</h3>
            <p>查看和管理所有用户</p>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import api from '../api'

const router = useRouter()
const auth = useAuthStore()
const totalUsers = ref(0)
const totalActivities = ref(0)
const studentUsers = ref(0)
const teacherUsers = ref(0)

function logout() {
  if (confirm('确定要退出登录吗？')) {
    auth.logout()
    router.push('/login')
  }
}

async function fetchStatistics() {
  try {
    const res = await api.get('/api/statistics/admin-dashboard')
    if (res.data.success) {
      const d = res.data.data
      totalUsers.value = d.totalUserCount || 0
      totalActivities.value = d.totalActivityCount || 0
      studentUsers.value = d.studentCount || 0
      teacherUsers.value = d.teacherCount || 0
    }
  } catch (e) { console.error('获取统计数据失败:', e) }
}

onMounted(() => {
  if (!auth.token || auth.user.role !== 'ADMIN') { router.push('/dashboard'); return }
  fetchStatistics()
})
</script>

<style scoped>
* { margin: 0; padding: 0; box-sizing: border-box; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; }
:root { --primary-gradient: linear-gradient(135deg, #667eea 0%, #764ba2 100%); --secondary-gradient: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); --student-gradient: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); --teacher-gradient: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); }
.dashboard-container { display: flex; min-height: 100vh; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.sidebar { width: 280px; background: rgba(255,255,255,0.97); backdrop-filter: blur(15px); padding: 24px 0; box-shadow: 0 4px 16px rgba(0,0,0,0.12); position: sticky; top: 0; height: 100vh; z-index: 100; }
.logo { text-align: center; padding: 24px; border-bottom: 1px solid #eee; margin-bottom: 24px; }
.logo h2 { color: #667eea; font-size: 24px; font-weight: 700; }
.logo p { color: #666; font-size: 13px; }
.nav-menu { list-style: none; padding: 0 20px; }
.nav-item { margin-bottom: 8px; }
.nav-link { display: flex; align-items: center; padding: 14px 18px; color: #666; text-decoration: none; border-radius: 12px; transition: all 0.3s cubic-bezier(0.4,0,0.2,1); font-weight: 500; cursor: pointer; }
.nav-link:hover { background: linear-gradient(135deg, rgba(102,126,234,0.1), rgba(118,75,162,0.1)); color: #667eea; transform: translateX(4px); }
.nav-link.active { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; box-shadow: 0 2px 8px rgba(0,0,0,0.08); }
.nav-link i { margin-right: 12px; width: 22px; text-align: center; font-size: 18px; }
.main-content { flex: 1; padding: 24px; overflow-y: auto; }
.header { background: white; border-radius: 20px; padding: 24px 32px; margin-bottom: 24px; box-shadow: 0 4px 16px rgba(0,0,0,0.12); display: flex; justify-content: space-between; align-items: center; }
.header h1 { color: #667eea; font-size: 28px; font-weight: 700; }
.user-info { display: flex; align-items: center; gap: 12px; background: rgba(102,126,234,0.05); padding: 10px 16px; border-radius: 12px; }
.user-info i { font-size: 28px; color: #667eea; }
.stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 24px; margin-bottom: 32px; }
.stat-card { background: white; border-radius: 20px; padding: 30px; box-shadow: 0 4px 16px rgba(0,0,0,0.12); display: flex; align-items: center; cursor: pointer; transition: all 0.3s cubic-bezier(0.4,0,0.2,1); position: relative; overflow: hidden; }
.stat-card::before { content: ''; position: absolute; top: 0; left: 0; right: 0; height: 4px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.stat-card:hover { transform: translateY(-8px) scale(1.02); box-shadow: 0 16px 48px rgba(0,0,0,0.2); }
.stat-icon { font-size: 48px; padding: 20px; border-radius: 50%; margin-right: 24px; display: flex; align-items: center; justify-content: center; }
.stat-icon.total-users { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; }
.stat-icon.total-activities { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); color: white; }
.stat-icon.student-users { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); color: white; }
.stat-icon.teacher-users { background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); color: white; }
.stat-info h3 { font-size: 36px; color: #333; margin-bottom: 8px; font-weight: 700; }
.stat-info p { color: #666; font-size: 15px; font-weight: 500; }
.quick-actions { background: white; border-radius: 20px; padding: 30px; box-shadow: 0 4px 16px rgba(0,0,0,0.12); }
.quick-actions h2 { color: #333; margin-bottom: 24px; font-size: 22px; font-weight: 700; display: flex; align-items: center; gap: 10px; }
.quick-actions h2::before { content: ''; display: inline-block; width: 4px; height: 24px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); border-radius: 8px; }
.actions-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 20px; }
.action-card { background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%); border-radius: 16px; padding: 28px 24px; text-align: center; cursor: pointer; transition: all 0.3s cubic-bezier(0.4,0,0.2,1); border: 2px solid transparent; }
.action-card:hover { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; border-color: rgba(255,255,255,0.2); transform: translateY(-6px) scale(1.03); box-shadow: 0 8px 32px rgba(0,0,0,0.16); }
.action-card i { font-size: 36px; margin-bottom: 12px; }
.action-card:hover i { transform: scale(1.2) rotate(15deg); }
.action-card h3 { font-size: 18px; font-weight: 600; margin-bottom: 8px; }
.action-card p { font-size: 13px; opacity: 0.8; }
.action-card.gradient-1 { background: linear-gradient(135deg, rgba(102,126,234,0.1), rgba(118,75,162,0.1)); }
@media (max-width: 768px) { .dashboard-container { flex-direction: column; } .sidebar { width: 100%; height: auto; position: static; } .nav-menu { display: flex; overflow-x: auto; padding: 0 16px; gap: 8px; } .nav-item { margin-bottom: 0; } .nav-link { flex-direction: column; padding: 12px 16px; min-width: 100px; } .nav-link i { margin-right: 0; margin-bottom: 4px; } .main-content { padding: 16px; } .stats-grid { grid-template-columns: 1fr; } .header { flex-direction: column; gap: 16px; } }
</style>
