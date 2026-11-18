<template>
  <div class="participants-page">
    <div class="container">
      <div class="header">
        <h1><i class="fas fa-users"></i> 活动报名详情</h1>
        <button class="btn btn-primary" @click="$router.push('/manage-activities')">
          <i class="fas fa-arrow-left"></i> 返回活动管理
        </button>
      </div>

      <!-- 活动信息 -->
      <div class="activity-info" v-if="activity">
        <div class="activity-title"><i class="fas fa-calendar-alt"></i> {{ activity.activityName }}</div>
        <div class="activity-details">
          <div class="activity-detail"><i class="fas fa-clock"></i> {{ formatDateTime(activity.activityTime) }}</div>
          <div class="activity-detail"><i class="fas fa-map-marker-alt"></i> {{ activity.location }}</div>
          <div class="activity-detail"><i class="fas fa-users"></i> {{ clubName }}</div>
        </div>
      </div>

      <!-- 统计信息 -->
      <div class="stats-container">
        <div class="stat-card primary">
          <div class="stat-number primary">{{ participants.length }}</div>
          <div class="stat-label">总报名人数</div>
        </div>
        <div class="stat-card success">
          <div class="stat-number success">{{ checkedInCount }}</div>
          <div class="stat-label">已签到人数</div>
        </div>
        <div class="stat-card warning">
          <div class="stat-number warning">{{ checkInRate }}%</div>
          <div class="stat-label">签到率</div>
        </div>
      </div>

      <!-- 统计按钮 -->
      <div style="text-align: center; margin: 20px 0;">
        <button class="btn btn-primary" @click="showRegistrationStats">
          <i class="fas fa-chart-bar"></i> 查看报名情况统计
        </button>
      </div>

      <!-- 搜索和筛选 -->
      <div class="search-box">
        <div class="search-input">
          <i class="fas fa-search"></i>
          <input type="text" v-model="searchTerm" placeholder="搜索学生姓名或学号..." @input="filterParticipants">
        </div>
        <div class="filter-select">
          <select v-model="statusFilter" @change="filterParticipants">
            <option value="all">全部状态</option>
            <option value="checkedin">已签到</option>
            <option value="notcheckedin">未签到</option>
          </select>
        </div>
      </div>

      <!-- 参与者列表 -->
      <div class="participants-grid" v-if="filteredParticipants.length > 0">
        <div :class="['participant-card', { 'checked-in': p.checkInStatus }]" v-for="p in filteredParticipants" :key="p.studentId">
          <div class="participant-header">
            <div class="participant-avatar">
              <img :src="`https://picsum.photos/seed/${p.studentId}/100/100.jpg`" @error="$event.target.src='https://picsum.photos/seed/default/100/100.jpg'">
            </div>
            <div class="participant-info-header">
              <div class="participant-name"><i class="fas fa-user-circle"></i> {{ p.name || '未知姓名' }}</div>
              <div class="participant-id"><i class="fas fa-id-badge"></i> {{ p.studentId || '未知学号' }}</div>
            </div>
            <div :class="['participant-status', p.checkInStatus ? 'status-checkedin' : 'status-notcheckedin']">
              <i :class="p.checkInStatus ? 'fas fa-check-circle' : 'far fa-clock'"></i>
              {{ p.checkInStatus ? '已签到' : '未签到' }}
            </div>
          </div>
          <div class="participant-details">
            <div class="detail-section">
              <h4><i class="fas fa-info-circle"></i> 基本信息</h4>
              <div class="detail-row"><span class="detail-label">学院:</span><span class="detail-value">{{ p.college || '未填写' }}</span></div>
              <div class="detail-row"><span class="detail-label">班级:</span><span class="detail-value">{{ p.majorClass || '未填写' }}</span></div>
            </div>
            <div class="detail-section">
              <h4><i class="fas fa-clock"></i> 时间信息</h4>
              <div class="detail-row"><span class="detail-label">报名时间:</span><span class="detail-value">{{ formatDateTime(p.registrationTime) }}</span></div>
              <div v-if="p.checkInStatus" class="detail-row checked-in"><span class="detail-label">签到时间:</span><span class="detail-value">{{ formatDateTime(p.checkInTime) }}</span></div>
              <div v-else class="detail-row not-checked-in"><span class="detail-label">签到状态:</span><span class="detail-value">尚未签到</span></div>
            </div>
          </div>
          <div class="participant-actions">
            <button v-if="!p.checkInStatus" class="btn btn-sm btn-primary" @click="checkInStudent(p.studentId)">
              <i class="fas fa-check"></i> 手动签到
            </button>
            <button v-else class="btn btn-sm btn-success" disabled><i class="fas fa-check-circle"></i> 已签到</button>
            <button class="btn btn-sm btn-info" @click="showStudentDetails(p.studentId)"><i class="fas fa-user"></i> 查看详情</button>
          </div>
        </div>
      </div>
      <div v-else class="empty-state">
        <i class="fas fa-users-slash"></i>
        <p>{{ participants.length === 0 ? '暂无报名学生' : '没有找到匹配的学生' }}</p>
        <p class="subtext">{{ participants.length === 0 ? '该活动还没有学生报名' : '请尝试调整搜索条件' }}</p>
      </div>
    </div>

    <!-- 统计弹窗 -->
    <div v-if="showStatsModal" class="modal-overlay" @click.self="showStatsModal = false">
      <div class="modal-content stats-modal">
        <div class="modal-header">
          <h3><i class="fas fa-chart-bar"></i> 报名情况统计</h3>
          <button class="modal-close" @click="showStatsModal = false">&times;</button>
        </div>
        <div class="modal-body">
          <div class="stats-summary">
            <div class="summary-item"><div class="summary-label">总报名人数</div><div class="summary-value">{{ participants.length }}人</div></div>
            <div class="summary-item"><div class="summary-label">已签到人数</div><div class="summary-value">{{ checkedInCount }}人</div></div>
            <div class="summary-item"><div class="summary-label">签到率</div><div class="summary-value">{{ checkInRate }}%</div></div>
          </div>
          <div class="stats-details">
            <h4><i class="fas fa-university"></i> 各学院报名情况</h4>
            <div class="stat-item" v-for="(count, college) in collegeStats" :key="college">
              <div class="stat-label">{{ college }}</div>
              <div class="stat-bar-container"><div class="stat-bar" :style="{ width: Math.round(count / participants.length * 100) + '%' }"></div></div>
              <div class="stat-value">{{ count }}人 ({{ Math.round(count / participants.length * 100) }}%)</div>
            </div>
          </div>
        </div>
        <div class="modal-footer"><button class="btn btn-primary" @click="showStatsModal = false">关闭</button></div>
      </div>
    </div>

    <!-- 学生详情弹窗 -->
    <div v-if="showStudentModal" class="modal-overlay" @click.self="showStudentModal = false">
      <div class="modal-content">
        <div class="modal-header">
          <h3><i class="fas fa-user-circle"></i> 学生详细信息</h3>
          <button class="modal-close" @click="showStudentModal = false">&times;</button>
        </div>
        <div class="modal-body student-detail-body" v-if="selectedStudent">
          <div class="student-detail-avatar">
            <img :src="`https://picsum.photos/seed/${selectedStudent.studentId}/150/150.jpg`">
          </div>
          <div class="student-detail-info">
            <h4>{{ selectedStudent.name }}</h4>
            <div class="detail-item"><span class="detail-label">学号:</span><span class="detail-value">{{ selectedStudent.studentId }}</span></div>
            <div class="detail-item"><span class="detail-label">学院:</span><span class="detail-value">{{ selectedStudent.college || '未填写' }}</span></div>
            <div class="detail-item"><span class="detail-label">班级:</span><span class="detail-value">{{ selectedStudent.majorClass || '未填写' }}</span></div>
            <div class="detail-item"><span class="detail-label">联系电话:</span><span class="detail-value">{{ selectedStudent.phone || '未填写' }}</span></div>
            <div class="detail-item"><span class="detail-label">邮箱:</span><span class="detail-value">{{ selectedStudent.email || '未填写' }}</span></div>
            <div class="detail-item"><span class="detail-label">积分:</span><span class="detail-value">{{ selectedStudent.totalPoints || 0 }}</span></div>
          </div>
        </div>
        <div class="modal-footer"><button class="btn btn-primary" @click="showStudentModal = false">关闭</button></div>
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
const activity = ref(null)
const clubName = ref('')
const participants = ref([])
const searchTerm = ref('')
const statusFilter = ref('all')
const filteredParticipants = ref([])
const showStatsModal = ref(false)
const showStudentModal = ref(false)
const selectedStudent = ref(null)

const checkedInCount = computed(() => participants.value.filter(p => p.checkInStatus).length)
const checkInRate = computed(() => participants.value.length > 0 ? Math.round(checkedInCount.value / participants.value.length * 100) : 0)
const collegeStats = computed(() => {
  const stats = {}
  participants.value.forEach(p => { const c = p.college || '未填写'; stats[c] = (stats[c] || 0) + 1 })
  return stats
})

function formatDateTime(d) {
  if (!d) return '未设置'
  return new Date(d).toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

function filterParticipants() {
  filteredParticipants.value = participants.value.filter(p => {
    const matchSearch = !searchTerm.value || (p.name || '').toLowerCase().includes(searchTerm.value.toLowerCase()) || (p.studentId || '').toLowerCase().includes(searchTerm.value.toLowerCase())
    const matchStatus = statusFilter.value === 'all' || (statusFilter.value === 'checkedin' && p.checkInStatus) || (statusFilter.value === 'notcheckedin' && !p.checkInStatus)
    return matchSearch && matchStatus
  })
}

async function loadActivity() {
  const id = route.params.id || route.query.activityId
  if (!id) return
  try {
    const [actRes, partRes] = await Promise.all([
      api.get(`/api/activities/${id}`),
      api.get(`/api/activities/${id}/participants`)
    ])
    if (actRes.data.success) {
      activity.value = actRes.data.data
      try {
        const clubRes = await api.get(`/api/clubs/${activity.value.clubId}`)
        clubName.value = clubRes.data.success ? clubRes.data.data.clubName : `社团#${activity.value.clubId}`
      } catch (e) { clubName.value = `社团#${activity.value.clubId}` }
    }
    if (partRes.data.success) {
      participants.value = partRes.data.data || []
      filterParticipants()
    }
  } catch (e) { console.error('加载数据错误:', e) }
}

async function checkInStudent(studentId) {
  if (!confirm(`确定要为学号 ${studentId} 的学生进行手动签到吗？`)) return
  const id = route.params.id || route.query.activityId
  try {
    const res = await api.post(`/api/checkin/activity/${id}/manual-checkin`, { studentId })
    if (res.data.success) { toast.success('签到成功！'); loadActivity() }
    else toast.error('签到失败: ' + res.data.message)
  } catch (e) { toast.error('签到时发生错误') }
}

async function showStudentDetails(studentId) {
  try {
    const res = await api.get(`/api/students/${studentId}`)
    if (res.data.success) { selectedStudent.value = res.data.data; showStudentModal.value = true }
    else toast.error('获取学生信息失败')
  } catch (e) { toast.error('获取学生信息时发生错误') }
}

function showRegistrationStats() { showStatsModal.value = true }

onMounted(() => {
  if (!auth.token) { router.push('/login'); return }
  loadActivity()
})
</script>

<style scoped>
* { margin: 0; padding: 0; box-sizing: border-box; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; }
.participants-page { background: #f5f7fa; color: #333; min-height: 100vh; }
.container { max-width: 1200px; margin: 0 auto; padding: 20px; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 30px; background: white; padding: 25px; border-radius: 15px; box-shadow: 0 4px 20px rgba(0,0,0,0.08); border-left: 4px solid #667eea; }
.header h1 { color: #333; font-size: 28px; display: flex; align-items: center; gap: 12px; }
.header h1 i { color: #667eea; }
.btn { padding: 12px 24px; border: none; border-radius: 8px; cursor: pointer; font-size: 14px; font-weight: 600; transition: all 0.3s ease; display: inline-flex; align-items: center; gap: 8px; }
.btn-primary { background: #667eea; color: white; }
.btn-primary:hover { background: #5a6fd8; transform: translateY(-1px); box-shadow: 0 4px 12px rgba(102,126,234,0.3); }
.btn-success { background: #28a745; color: white; }
.btn-info { background: #17a2b8; color: white; }
.btn-sm { padding: 8px 15px; font-size: 13px; }
.btn:disabled { opacity: 0.7; cursor: not-allowed; }
.activity-info { background: white; border-radius: 15px; padding: 25px; box-shadow: 0 4px 20px rgba(0,0,0,0.08); margin-bottom: 30px; }
.activity-title { font-size: 24px; font-weight: 600; color: #333; margin-bottom: 15px; display: flex; align-items: center; gap: 10px; }
.activity-title i { color: #667eea; }
.activity-details { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 15px; }
.activity-detail { display: flex; align-items: center; gap: 10px; color: #666; font-size: 15px; }
.activity-detail i { color: #667eea; width: 20px; text-align: center; }
.stats-container { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 20px; margin-bottom: 30px; }
.stat-card { background: white; border-radius: 15px; padding: 25px; box-shadow: 0 4px 20px rgba(0,0,0,0.08); text-align: center; transition: all 0.3s ease; }
.stat-card:hover { transform: translateY(-5px); box-shadow: 0 8px 30px rgba(0,0,0,0.12); }
.stat-card.primary { border-left: 4px solid #667eea; }
.stat-card.success { border-left: 4px solid #28a745; }
.stat-card.warning { border-left: 4px solid #ffc107; }
.stat-number { font-size: 36px; font-weight: bold; margin-bottom: 10px; }
.stat-number.primary { color: #667eea; }
.stat-number.success { color: #28a745; }
.stat-number.warning { color: #ffc107; }
.stat-label { color: #666; font-size: 16px; }
.search-box { background: white; padding: 20px; border-radius: 15px; box-shadow: 0 4px 20px rgba(0,0,0,0.08); margin-bottom: 25px; display: flex; gap: 15px; flex-wrap: wrap; }
.search-input { flex: 1; min-width: 250px; position: relative; }
.search-input i { position: absolute; left: 15px; top: 50%; transform: translateY(-50%); color: #667eea; }
.search-input input { width: 100%; padding: 12px 15px 12px 40px; border: 2px solid #e9ecef; border-radius: 8px; font-size: 14px; transition: all 0.3s ease; }
.search-input input:focus { outline: none; border-color: #667eea; box-shadow: 0 0 0 3px rgba(102,126,234,0.1); }
.filter-select select { padding: 12px 15px; border: 2px solid #e9ecef; border-radius: 8px; font-size: 14px; background: white; cursor: pointer; width: 100%; }
.filter-select select:focus { outline: none; border-color: #667eea; }
.participants-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(350px, 1fr)); gap: 25px; }
.participant-card { background: white; border-radius: 15px; padding: 25px; box-shadow: 0 4px 20px rgba(0,0,0,0.08); transition: all 0.3s ease; position: relative; overflow: hidden; }
.participant-card:hover { transform: translateY(-5px); box-shadow: 0 8px 30px rgba(0,0,0,0.12); }
.participant-card::before { content: ''; position: absolute; top: 0; left: 0; width: 4px; height: 100%; background: #667eea; }
.participant-card.checked-in::before { background: #28a745; }
.participant-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.participant-avatar { width: 60px; height: 60px; border-radius: 50%; overflow: hidden; margin-right: 15px; flex-shrink: 0; }
.participant-avatar img { width: 100%; height: 100%; object-fit: cover; }
.participant-info-header { flex: 1; }
.participant-name { font-size: 20px; font-weight: 600; color: #333; display: flex; align-items: center; gap: 10px; }
.participant-name i { color: #667eea; font-size: 24px; }
.participant-id { font-size: 14px; color: #666; display: flex; align-items: center; gap: 8px; margin-top: 5px; }
.participant-id i { color: #667eea; }
.participant-status { padding: 6px 12px; border-radius: 20px; font-size: 13px; font-weight: 600; display: flex; align-items: center; gap: 5px; }
.status-checkedin { background: #d4edda; color: #155724; border: 1px solid #c3e6cb; }
.status-notcheckedin { background: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; }
.participant-details { margin: 15px 0; padding: 15px; background: #f8f9fa; border-radius: 10px; display: grid; grid-template-columns: 1fr 1fr; gap: 15px; }
.detail-section h4 { font-size: 16px; margin-bottom: 10px; color: #333; display: flex; align-items: center; gap: 8px; }
.detail-section h4 i { color: #667eea; }
.detail-row { display: flex; margin-bottom: 8px; }
.detail-label { font-weight: 500; color: #555; width: 80px; flex-shrink: 0; }
.detail-value { color: #333; flex: 1; }
.detail-row.checked-in { color: #28a745; }
.detail-row.not-checked-in { color: #dc3545; }
.participant-actions { display: flex; gap: 10px; margin-top: 15px; flex-wrap: wrap; }
.empty-state { text-align: center; padding: 60px 20px; color: #666; }
.empty-state i { font-size: 64px; margin-bottom: 20px; color: #dee2e6; display: block; }
.empty-state p { font-size: 16px; margin-bottom: 10px; }
.subtext { font-size: 14px; color: #999; }
.modal-overlay { display: flex; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 1000; justify-content: center; align-items: center; }
.modal-content { background: white; border-radius: 15px; width: 90%; max-width: 700px; max-height: 90vh; overflow-y: auto; box-shadow: 0 5px 15px rgba(0,0,0,0.3); }
.modal-header { padding: 20px; border-bottom: 1px solid #eee; display: flex; justify-content: space-between; align-items: center; }
.modal-header h3 { margin: 0; color: #333; display: flex; align-items: center; gap: 10px; }
.modal-header h3 i { color: #667eea; }
.modal-close { font-size: 24px; cursor: pointer; color: #999; background: none; border: none; }
.modal-close:hover { color: #333; }
.modal-body { padding: 20px; }
.modal-footer { padding: 15px 20px; border-top: 1px solid #eee; text-align: right; }
.stats-summary { display: flex; justify-content: space-around; margin-bottom: 25px; padding: 15px; background: #f8f9fa; border-radius: 10px; }
.summary-item { text-align: center; }
.summary-label { font-size: 14px; color: #666; margin-bottom: 5px; }
.summary-value { font-size: 24px; font-weight: bold; color: #667eea; }
.stats-details h4 { margin-bottom: 15px; color: #333; display: flex; align-items: center; gap: 8px; }
.stats-details h4 i { color: #667eea; }
.stat-item { display: flex; align-items: center; margin-bottom: 15px; }
.stat-item .stat-label { width: 120px; font-weight: 500; }
.stat-bar-container { flex: 1; height: 20px; background: #f0f0f0; border-radius: 10px; margin: 0 15px; overflow: hidden; }
.stat-bar { height: 100%; background: #667eea; border-radius: 10px; }
.stat-item .stat-value { width: 100px; text-align: right; font-weight: 500; }
.student-detail-body { display: flex; gap: 20px; }
.student-detail-avatar { flex-shrink: 0; }
.student-detail-avatar img { width: 120px; height: 120px; border-radius: 50%; object-fit: cover; }
.student-detail-info { flex: 1; }
.student-detail-info h4 { margin-top: 0; margin-bottom: 15px; font-size: 22px; color: #333; }
.detail-item { display: flex; margin-bottom: 10px; }
.student-detail-info .detail-label { font-weight: 600; width: 80px; color: #555; }
.student-detail-info .detail-value { color: #333; flex: 1; }
@media (max-width: 768px) { .header { flex-direction: column; gap: 15px; text-align: center; } .stats-container { grid-template-columns: 1fr; } .participants-grid { grid-template-columns: 1fr; } .search-box { flex-direction: column; } .participant-details { grid-template-columns: 1fr; } .student-detail-body { flex-direction: column; align-items: center; } }
</style>
