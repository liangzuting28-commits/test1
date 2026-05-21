<template>
  <header class="header">
    <div class="header-left">
      <h2>{{ $route.name }}</h2>
    </div>
    <div class="header-right">
      <span class="user-info">
        <i class="fas fa-user-circle"></i>
        {{ user?.name || '未登录' }}
        <span class="role-tag">{{ roleLabel }}</span>
      </span>
      <button class="btn btn-outline" @click="handleLogout">
        <i class="fas fa-sign-out-alt"></i> 退出
      </button>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const router = useRouter()
const user = computed(() => auth.user)

const roleLabels = { STUDENT: '学生', PRESIDENT: '社长', TEACHER: '教师', ADMIN: '管理员' }
const roleLabel = computed(() => roleLabels[auth.userRole] || '')

async function handleLogout() {
  await auth.logout()
  router.push('/login')
}
</script>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: white;
  border-bottom: 1px solid var(--border-color);
}
.header-left h2 {
  font-size: 18px;
  color: var(--text-primary);
}
.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-secondary);
}
.role-tag {
  background: var(--primary-color);
  color: white;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
}
</style>
