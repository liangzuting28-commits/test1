<template>
  <div class="my-activities-page">
    <div class="container">
      <div class="header">
        <h1><i class="fas fa-tasks"></i> 我的活动</h1>
        <button class="btn btn-primary" @click="$router.push('/dashboard')">
          <i class="fas fa-arrow-left"></i> 返回仪表盘
        </button>
      </div>

      <div class="tabs">
        <div :class="['tab', { active: currentTab === 'registered' }]" @click="switchTab('registered')">已报名</div>
        <div :class="['tab', { active: currentTab === 'checked-in' }]" @click="switchTab('checked-in')">已签到</div>
        <div :class="['tab', { active: currentTab === 'history' }]" @click="switchTab('history')">历史记录</div>
      </div>

      <div style="display: flex; gap: 15px; margin-bottom: 25px; flex-wrap: wrap;">
        <div style="flex: 1; min-width: 300px;">
          <div style="position: relative;">
            <input type="text" v-model="searchTerm" placeholder="搜索活动名称、地点..."
              style="width: 100%; padding: 12px 40px 12px 16px; border: 2px solid #e2e8f0; border-radius: 12px; font-size: 14px; transition: all 0.3s ease; box-shadow: 0 2px 8px rgba(0,0,0,0.08);"
              @keyup.enter="applyFilters">
            <i class="fas fa-search" style="position: absolute; right: 80px; top: 50%; transform: translateY(-50%); color: #94a3b8; font-size: 16px; pointer-events: none;"></i>
            <button class="search-btn" @click="applyFilters" style="position: absolute; right: 4px; top: 50%; transform: translateY(-50%); background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; border: none; padding: 8px 16px; border-radius: 8px; cursor: pointer; font-size: 13px; font-weight: 600; display: flex; align-items: center; gap: 5px; transition: all 0.3s ease;"><i class="fas fa-search"></i> 搜索</button>
          </div>
        </div>
        <div style="display: flex; gap: 15px; align-items: flex-end;">
          <div>
            <label style="display: block; margin-bottom: 8px; font-weight: 600; color: #343a40; font-size: 14px;">活动状态</label>
            <select v-model="statusFilter" @change="applyFilters"
              style="padding: 12px 16px; border: 2px solid #e2e8f0; border-radius: 12px; font-size: 14px; background: white; color: #333; cursor: pointer; transition: all 0.3s ease; box-shadow: 0 2px 8px rgba(0,0,0,0.08);">
              <option value="">全部状态</option>
              <option value="PENDING">待审核</option>
              <option value="PUBLISHED">已发布</option>
              <option value="REJECTED">已驳回</option>
              <option value="ENDED">已结束</option>
            </select>
          </div>
          <div>
            <button class="btn btn-primary" style="margin-top: 25px;" @click="resetFilters">
              <i class="fas fa-sync-alt"></i> 重置
            </button>
          </div>
        </div>
      </div>

      <div class="activity-grid">
        <template v-if="displayedActivities.length > 0">
          <div class="activity-card" v-for="activity in displayedActivities" :key="activity.activityId">
            <div class="activity-header">
              <div class="activity-title">{{ activity.activityName }}</div>
              <div v-if="activity.status === 'ENDED'" class="activity-status status-ended">已结束</div>
              <div v-else-if="activity.checkInStatus === true" class="activity-status status-checked-in">已签到</div>
              <div v-else-if="activity.registrationStatus === 'PENDING'" class="activity-status status-pending">待审核</div>
              <div v-else-if="activity.registrationStatus === 'APPROVED'" class="activity-status status-approved">已通过</div>
              <div v-else-if="activity.registrationStatus === 'REJECTED'" class="activity-status status-rejected">已拒绝</div>
              <div v-else class="activity-status status-registered">已报名</div>
            </div>
            <div class="activity-info">
              <div class="info-item">
                <i class="fas fa-clock"></i>
                <span>时间: {{ formatDateTime(activity.activityTime) }}</span>
              </div>
              <div class="info-item">
                <i class="fas fa-map-marker-alt"></i>
                <span>地点: {{ activity.location }}</span>
              </div>
              <div class="info-item">
                <i class="fas fa-star"></i>
                <span>积分: {{ activity.awardPoints }}</span>
              </div>
            </div>
            <div v-if="activity.registrationStatus === 'REJECTED' && activity.rejectReason" class="reject-reason">
              <i class="fas fa-exclamation-circle"></i> {{ activity.rejectReason }}
            </div>
            <div class="activity-actions">
              <button v-if="activity.status === 'ENDED' || activity.checkInStatus === true" class="btn" disabled>
                {{ activity.checkInStatus === true ? '已完成' : '已结束' }}
              </button>
              <button v-else-if="activity.registrationStatus === 'REJECTED'" class="btn" disabled>
                已被拒绝
              </button>
              <button v-else class="btn btn-danger" @click="cancelRegistration(activity.activityId)">
                <i class="fas fa-times"></i> 取消报名
              </button>
            </div>
          </div>
        </template>
        <div v-else class="empty-state">
          <i class="fas fa-calendar-times"></i>
          <p>暂无{{ getTabName() }}的活动</p>
        </div>
      </div>
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

const allActivities = ref([])
const currentTab = ref('registered')
const searchTerm = ref('')
const statusFilter = ref('')
const displayedActivities = ref([])

function formatDateTime(dateTimeStr) {
  if (!dateTimeStr) return ''
  return new Date(dateTimeStr).toLocaleString('zh-CN')
}

function getTabName() {
  return { 'registered': '已报名', 'checked-in': '已签到', 'history': '历史' }[currentTab.value] || ''
}

function switchTab(tab) {
  currentTab.value = tab
  applyFilters()
}

function resetFilters() {
  searchTerm.value = ''
  statusFilter.value = ''
  applyFilters()
}

function applyFilters() {
  let filtered = [...allActivities.value]
  if (currentTab.value === 'checked-in') {
    filtered = filtered.filter(a => a.checkInStatus === true)
  } else if (currentTab.value === 'history') {
    filtered = filtered.filter(a => a.status === 'ENDED')
  } else {
    filtered = filtered.filter(a => a.checkInStatus === false && a.status !== 'ENDED')
  }
  if (searchTerm.value) {
    const term = searchTerm.value.toLowerCase()
    filtered = filtered.filter(a =>
      a.activityName.toLowerCase().includes(term) ||
      (a.location && a.location.toLowerCase().includes(term))
    )
  }
  if (statusFilter.value) {
    filtered = filtered.filter(a => a.status === statusFilter.value)
  }
  displayedActivities.value = filtered
}

async function loadMyActivities() {
  try {
    const response = await api.get('/api/activities/my-registrations', {
      headers: { 'X-User-Id': auth.user.studentId }
    })
    if (response.data.success && response.data.data) {
      allActivities.value = response.data.data
    } else {
      allActivities.value = []
    }
    applyFilters()
  } catch (error) {
    console.error('加载活动列表错误:', error)
    allActivities.value = []
    applyFilters()
  }
}

async function cancelRegistration(activityId) {
  if (!confirm('确定要取消报名吗？')) return
  try {
    const response = await api.delete(`/api/activities/${activityId}/register`, {
      headers: { 'X-User-Id': auth.user.studentId }
    })
    if (response.data.success) {
      toast.success('取消报名成功！')
      loadMyActivities()
    } else {
      toast.error('取消报名失败: ' + response.data.message)
    }
  } catch (error) {
    toast.error('取消报名时发生错误')
  }
}

onMounted(() => {
  if (!auth.token) { router.push('/login'); return }
  loadMyActivities()
})
</script>

<style scoped>
* { margin: 0; padding: 0; box-sizing: border-box; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; }
.my-activities-page { background: linear-gradient(135deg, #667eea15 0%, #764ba215 100%); color: #1f2937; line-height: 1.6; min-height: 100vh; }
.container { max-width: 1200px; margin: 0 auto; padding: 20px; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 30px; background: #fff; padding: 24px 30px; border-radius: 16px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); border-top: 4px solid #667eea; }
.header h1 { font-size: 28px; font-weight: 700; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; }
.btn { padding: 10px 20px; border: none; border-radius: 6px; cursor: pointer; font-size: 14px; font-weight: 500; transition: all 0.3s ease; display: flex; align-items: center; gap: 8px; }
.btn-primary { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
.btn-primary:hover { transform: translateY(-2px); box-shadow: 0 4px 6px rgba(0,0,0,0.1); }
.tabs { display: flex; margin-bottom: 30px; background: #fff; border-radius: 10px; padding: 8px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
.tab { flex: 1; padding: 14px; text-align: center; cursor: pointer; border-radius: 6px; transition: all 0.3s ease; font-weight: 500; color: #6b7280; position: relative; overflow: hidden; }
.tab::before { content: ''; position: absolute; bottom: 0; left: 0; right: 0; height: 3px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); transform: scaleX(0); transition: all 0.3s ease; }
.tab.active { color: #667eea; background: linear-gradient(135deg, #667eea05 0%, #764ba205 100%); }
.tab.active::before { transform: scaleX(1); }
.tab:hover:not(.active) { background: rgba(102, 126, 234, 0.05); color: #667eea; }
.activity-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(380px, 1fr)); gap: 25px; }
.activity-card { background: #fff; border-radius: 16px; padding: 24px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); transition: all 0.3s ease; position: relative; overflow: hidden; }
.activity-card::before { content: ''; position: absolute; top: 0; left: 0; right: 0; height: 4px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.activity-card:hover { transform: translateY(-5px); box-shadow: 0 20px 25px rgba(0,0,0,0.1); }
.activity-header { display: flex; justify-content: space-between; align-items: start; margin-bottom: 20px; }
.activity-title { font-size: 20px; font-weight: 700; color: #1f2937; line-height: 1.4; margin-right: 10px; }
.activity-status { padding: 6px 12px; border-radius: 20px; font-size: 12px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px; }
.status-registered { background: linear-gradient(135deg, #dbeafe 0%, #eff6ff 100%); color: #1e40af; }
.status-checked-in { background: linear-gradient(135deg, #dcfce7 0%, #f0fdf4 100%); color: #15803d; }
.status-ended { background: linear-gradient(135deg, #f3f4f6 0%, #f9fafb 100%); color: #4b5563; }
.status-pending { background: linear-gradient(135deg, #fef3c7 0%, #fffbeb 100%); color: #92400e; }
.status-approved { background: linear-gradient(135deg, #d1fae5 0%, #ecfdf5 100%); color: #065f46; }
.status-rejected { background: linear-gradient(135deg, #fee2e2 0%, #fef2f2 100%); color: #991b1b; }
.activity-info { display: grid; gap: 12px; margin-bottom: 20px; }
.info-item { display: flex; align-items: center; gap: 10px; color: #6b7280; font-size: 14px; }
.info-item i { color: #667eea; width: 18px; text-align: center; font-size: 16px; flex-shrink: 0; }
.reject-reason { background: #fef2f2; color: #991b1b; padding: 10px 14px; border-radius: 8px; font-size: 13px; margin-bottom: 16px; display: flex; align-items: center; gap: 8px; border-left: 3px solid #ef4444; }
.reject-reason i { color: #ef4444; font-size: 14px; }
.activity-actions { display: flex; gap: 12px; }
.btn-danger { background: #fee2e2; color: #ef4444; }
.btn-danger:hover { background: #ef4444; color: white; transform: translateY(-2px); box-shadow: 0 4px 6px rgba(0,0,0,0.1); }
.btn:disabled { background: #f3f4f6; color: #9ca3af; cursor: not-allowed; transform: none; }
.empty-state { grid-column: 1 / -1; text-align: center; padding: 80px 20px; background: #fff; border-radius: 16px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
.empty-state i { font-size: 64px; color: #e5e7eb; margin-bottom: 20px; }
.empty-state p { font-size: 18px; color: #6b7280; font-weight: 500; }
@media (max-width: 768px) { .container { padding: 15px; } .header { padding: 20px; margin-bottom: 20px; } .header h1 { font-size: 24px; } .activity-grid { grid-template-columns: 1fr; gap: 20px; } .activity-card { padding: 20px; } .activity-title { font-size: 18px; } }
</style>
