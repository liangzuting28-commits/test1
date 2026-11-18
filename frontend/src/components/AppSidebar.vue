<template>
  <aside class="sidebar">
    <div class="sidebar-header">
      <h3>社团管理系统</h3>
    </div>
    <nav class="sidebar-nav">
      <router-link to="/dashboard" class="nav-item">
        <i class="fas fa-tachometer-alt"></i> 仪表盘
      </router-link>

      <template v-if="role === 'STUDENT' || role === 'PRESIDENT'">
        <router-link to="/activity-center" class="nav-item">
          <i class="fas fa-calendar-alt"></i> 活动中心
        </router-link>
        <router-link to="/my-activities" class="nav-item">
          <i class="fas fa-list"></i> 我的活动
        </router-link>
        <router-link to="/my-points" class="nav-item">
          <i class="fas fa-star"></i> 我的积分
        </router-link>
        <router-link to="/checkin" class="nav-item">
          <i class="fas fa-qrcode"></i> 签到
        </router-link>
      </template>

      <template v-if="role === 'PRESIDENT'">
        <div class="nav-divider">社团管理</div>
        <router-link to="/club-manage" class="nav-item">
          <i class="fas fa-users"></i> 社团管理
        </router-link>
        <router-link to="/create-activity" class="nav-item">
          <i class="fas fa-plus-circle"></i> 创建活动
        </router-link>
        <router-link to="/manage-activities" class="nav-item">
          <i class="fas fa-tasks"></i> 活动管理
        </router-link>
        <router-link to="/checkin-manage" class="nav-item">
          <i class="fas fa-clipboard-check"></i> 签到管理
        </router-link>
        <router-link to="/member-management" class="nav-item">
          <i class="fas fa-user-friends"></i> 成员管理
        </router-link>
      </template>

      <template v-if="role === 'TEACHER'">
        <div class="nav-divider">教师功能</div>
        <router-link to="/activity-review" class="nav-item">
          <i class="fas fa-check-double"></i> 活动审核
        </router-link>
        <router-link to="/points-rules" class="nav-item">
          <i class="fas fa-cogs"></i> 积分规则
        </router-link>
        <router-link to="/points-manage" class="nav-item">
          <i class="fas fa-chart-bar"></i> 积分管理
        </router-link>
        <router-link to="/global-statistics" class="nav-item">
          <i class="fas fa-chart-pie"></i> 统计分析
        </router-link>
      </template>

      <template v-if="role === 'ADMIN'">
        <div class="nav-divider">系统管理</div>
        <router-link to="/admin-dashboard" class="nav-item">
          <i class="fas fa-tachometer-alt"></i> 管理面板
        </router-link>
        <router-link to="/admin-user-management" class="nav-item">
          <i class="fas fa-users-cog"></i> 用户管理
        </router-link>
        <router-link to="/add-user" class="nav-item">
          <i class="fas fa-user-plus"></i> 添加用户
        </router-link>
      </template>

      <router-link to="/my-applications" class="nav-item">
        <i class="fas fa-file-alt"></i> 我的申请
      </router-link>
    </nav>
  </aside>
</template>

<script setup>
import { computed } from 'vue'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const role = computed(() => auth.userRole)
</script>

<style scoped>
.sidebar {
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  width: var(--sidebar-width);
  background: var(--primary-gradient);
  color: white;
  overflow-y: auto;
  z-index: 100;
}
.sidebar-header {
  padding: 20px;
  text-align: center;
  border-bottom: 1px solid rgba(255,255,255,0.2);
}
.sidebar-header h3 {
  font-size: 16px;
  margin: 0;
}
.sidebar-nav {
  padding: 10px 0;
}
.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 20px;
  color: rgba(255,255,255,0.8);
  text-decoration: none;
  transition: all 0.2s;
}
.nav-item:hover,
.nav-item.router-link-active {
  background: rgba(255,255,255,0.15);
  color: white;
}
.nav-item i {
  width: 20px;
  text-align: center;
}
.nav-divider {
  padding: 15px 20px 5px;
  font-size: 12px;
  color: rgba(255,255,255,0.5);
  text-transform: uppercase;
}
</style>
