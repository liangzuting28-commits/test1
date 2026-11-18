<template>
  <div class="my-applications-page">
    <div class="container">
      <div class="header">
        <h1><i class="fas fa-user-plus"></i> 我的社团申请</h1>
        <button class="btn btn-primary" @click="$router.push('/dashboard')">
          <i class="fas fa-arrow-left"></i> 返回仪表盘
        </button>
      </div>

      <div class="applications-grid" v-if="!loading">
        <template v-if="applications.length > 0">
          <div class="application-card" v-for="app in applications" :key="app.membershipId || app.id">
            <div class="application-header">
              <div class="application-title">申请加入社团</div>
              <div :class="['application-status', statusClass(app.applicationStatus)]">
                {{ statusText(app.applicationStatus) }}
              </div>
            </div>
            <div class="application-info">
              <div class="info-item">
                <i class="fas fa-users"></i>
                <span>社团ID: {{ app.clubId }}</span>
              </div>
              <div class="info-item">
                <i class="fas fa-calendar"></i>
                <span>申请时间: {{ formatDateTime(app.joinDate) }}</span>
              </div>
            </div>
            <div v-if="app.applicationStatus === 'REJECTED'" class="application-description">
              <strong>拒绝原因:</strong> {{ app.reason || '未说明原因' }}
            </div>
            <div v-if="app.applicationStatus === 'PENDING'" class="application-actions">
              <button class="btn btn-primary" @click="deleteApplication(app.membershipId || app.id)">
                <i class="fas fa-trash"></i> 删除申请
              </button>
            </div>
          </div>
        </template>
        <div v-else class="empty-state">
          <i class="fas fa-inbox"></i>
          <p>暂无申请记录</p>
          <p>去<span class="link" @click="$router.push('/club-manage')">社团中心</span>申请加入感兴趣的社团吧！</p>
        </div>
      </div>
      <div v-else class="loading"><i class="fas fa-spinner fa-spin"></i></div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useToastStore } from '../stores/toast'
import api from '../api'

const router = useRouter()
const auth = useAuthStore()
const toast = useToastStore()
const applications = ref([])
const loading = ref(true)

function formatDateTime(d) {
  if (!d) return '未知时间'
  return new Date(d).toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

function statusClass(s) {
  return { PENDING: 'status-pending', APPROVED: 'status-approved', REJECTED: 'status-rejected' }[s] || ''
}

function statusText(s) {
  return { PENDING: '待审核', APPROVED: '已通过', REJECTED: '已拒绝' }[s] || s
}

async function loadApplications() {
  loading.value = true
  try {
    const res = await api.get(`/api/member-manage/student/${auth.user.studentId}/clubs`)
    if (res.data.success && res.data.data) {
      applications.value = res.data.data
    } else {
      applications.value = []
    }
  } catch (e) { applications.value = [] }
  loading.value = false
}

async function deleteApplication(membershipId) {
  if (!confirm('确定要删除这条申请记录吗？')) return
  try {
    const res = await api.delete(`/api/clubs/my-applications/${membershipId}`)
    if (res.data.success) {
      toast.success('申请记录删除成功')
      loadApplications()
    } else {
      toast.error('删除失败：' + res.data.message)
    }
  } catch (e) { toast.error('删除失败，请稍后重试') }
}

onMounted(() => {
  if (!auth.token) { router.push('/login'); return }
  loadApplications()
})
</script>

<style scoped>
* { margin: 0; padding: 0; box-sizing: border-box; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; }
.my-applications-page { background: #f5f7fa; color: #333; min-height: 100vh; }
.container { max-width: 1200px; margin: 0 auto; padding: 20px; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 30px; background: white; padding: 25px; border-radius: 15px; box-shadow: 0 4px 20px rgba(0,0,0,0.08); border-left: 4px solid #667eea; }
.header h1 { color: #333; font-size: 28px; display: flex; align-items: center; gap: 12px; }
.header h1 i { color: #667eea; }
.btn { padding: 12px 24px; border: none; border-radius: 8px; cursor: pointer; font-size: 14px; font-weight: 600; transition: all 0.3s ease; display: inline-flex; align-items: center; gap: 8px; }
.btn-primary { background: #667eea; color: white; }
.btn-primary:hover { background: #5a6fd8; transform: translateY(-1px); }
.applications-grid { display: grid; gap: 20px; }
.application-card { background: white; border-radius: 15px; padding: 25px; box-shadow: 0 4px 20px rgba(0,0,0,0.08); border: 1px solid #f0f0f0; transition: all 0.3s ease; position: relative; overflow: hidden; }
.application-card:hover { transform: translateY(-5px); box-shadow: 0 8px 30px rgba(0,0,0,0.12); }
.application-card::before { content: ''; position: absolute; top: 0; left: 0; width: 4px; height: 100%; background: #667eea; }
.application-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 20px; }
.application-title { font-size: 18px; font-weight: 700; color: #333; line-height: 1.4; }
.application-status { padding: 6px 12px; border-radius: 20px; font-size: 12px; font-weight: 600; white-space: nowrap; }
.status-pending { background: #fff3cd; color: #856404; border: 1px solid #ffeaa7; }
.status-approved { background: #d4edda; color: #155724; border: 1px solid #c3e6cb; }
.status-rejected { background: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; }
.application-info { display: grid; gap: 12px; margin-bottom: 20px; }
.info-item { display: flex; align-items: center; gap: 10px; color: #666; font-size: 14px; }
.info-item i { color: #667eea; width: 16px; text-align: center; }
.application-description { color: #666; margin-bottom: 20px; line-height: 1.6; font-size: 14px; }
.empty-state { text-align: center; padding: 60px 20px; color: #666; }
.empty-state i { font-size: 64px; margin-bottom: 20px; color: #dee2e6; display: block; }
.empty-state p { font-size: 16px; margin-bottom: 10px; }
.link { color: #667eea; cursor: pointer; text-decoration: underline; }
.loading { display: flex; justify-content: center; align-items: center; padding: 60px 0; }
.loading i { font-size: 32px; color: #667eea; }
@media (max-width: 768px) { .header { flex-direction: column; gap: 15px; text-align: center; } }
</style>
