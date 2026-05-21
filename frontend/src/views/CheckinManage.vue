<template>
  <div class="checkin-page">
    <!-- Header -->
    <div class="header">
      <h1><i class="fas fa-qrcode"></i> 签到管理</h1>
      <div>
        <router-link to="/dashboard" class="btn btn-primary">
          <i class="fas fa-arrow-left"></i> 返回仪表盘
        </router-link>
      </div>
    </div>

    <!-- Activity List Card -->
    <div class="card">
      <h3><i class="fas fa-list"></i> 我的活动</h3>

      <!-- Search and Filter -->
      <div class="filter-bar">
        <div class="search-wrap">
          <div style="position: relative;">
            <input
              v-model="searchTerm"
              type="text"
              placeholder="搜索活动名称、地点..."
              class="search-input"
              @keyup.enter="applyFilters"
            />
            <i class="fas fa-search search-icon"></i>
            <button class="search-btn" @click="applyFilters"><i class="fas fa-search"></i></button>
          </div>
        </div>
        <div class="filter-controls">
          <div>
            <label class="filter-label">活动状态</label>
            <select v-model="statusFilter" class="status-select" @change="applyFilters">
              <option value="">全部状态</option>
              <option value="PENDING">待审核</option>
              <option value="PUBLISHED">已发布</option>
              <option value="REJECTED">已驳回</option>
              <option value="ENDED">已结束</option>
            </select>
          </div>
          <div>
            <button class="btn btn-primary reset-btn" @click="resetFilters">
              <i class="fas fa-sync-alt"></i> 重置
            </button>
          </div>
        </div>
      </div>

      <!-- Activity Grid -->
      <div v-if="loading" class="empty-state">
        <i class="fas fa-spinner fa-spin"></i>
        <p>加载中...</p>
      </div>
      <div v-else-if="filteredActivities.length > 0" class="activity-grid">
        <div v-for="activity in filteredActivities" :key="activity.activityId" class="activity-card">
          <div class="activity-header">
            <div class="activity-title">{{ activity.activityName }}</div>
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
          <div class="activity-actions">
            <button class="btn btn-success" @click="generateQRCode(activity.activityId)">
              <i class="fas fa-qrcode"></i> 生成签到码
            </button>
            <button class="btn btn-primary" @click="viewCheckInInfo(activity.activityId)">
              <i class="fas fa-chart-bar"></i> 查看签到情况
            </button>
          </div>
        </div>
      </div>
      <div v-else class="empty-state">
        <i class="fas fa-calendar-times"></i>
        <p>暂无符合条件的活动</p>
      </div>
    </div>

    <!-- QR Code Modal -->
    <div v-if="showQRModal" class="modal-overlay" @click.self="closeQRCodeModal">
      <div class="modal-card qrcode-modal-card">
        <h3 class="modal-title-center">
          <i class="fas fa-qrcode" style="color: var(--primary-color); margin-right: 10px;"></i>
          活动签到二维码
        </h3>
        <div class="qrcode-container">
          <img v-if="qrCodeData.qrCode" :src="qrCodeData.qrCode" alt="签到二维码" class="qrcode-image" />
          <div class="qrcode-number">
            签到数: <span class="qrcode-number-value">{{ qrCodeData.checkInNumber }}</span>
          </div>
          <p>请让学生扫描此二维码或输入签到数进行签到</p>
        </div>
        <div class="modal-footer">
          <button class="btn btn-primary btn-lg" @click="closeQRCodeModal">
            <i class="fas fa-times"></i> 关闭
          </button>
        </div>
      </div>
    </div>

    <!-- Check-in Info Modal -->
    <div v-if="showCheckInModal" class="modal-overlay" @click.self="closeCheckInInfoModal">
      <div class="modal-card checkin-modal-card">
        <div class="modal-header">
          <h3>
            <i class="fas fa-chart-bar" style="color: var(--primary-color); margin-right: 10px;"></i>
            签到情况
          </h3>
          <button class="modal-close" @click="closeCheckInInfoModal">&times;</button>
        </div>
        <div class="modal-body">
          <!-- Summary Stats -->
          <div v-if="checkInStatus" class="checkin-summary">
            <h4 class="summary-title">
              <i class="fas fa-calendar-alt"></i> {{ checkInStatus.activity?.activityName }}
            </h4>
            <div class="stats-grid">
              <div class="checkin-stat-item">
                <div class="stat-value">{{ checkInStatus.totalParticipants }}</div>
                <div class="stat-label">报名总人数</div>
              </div>
              <div class="checkin-stat-item">
                <div class="stat-value" style="color: var(--success-color);">{{ checkInStatus.checkedInParticipants }}</div>
                <div class="stat-label">已签到人数</div>
              </div>
              <div class="checkin-stat-item">
                <div class="stat-value" style="color: var(--warning-color);">{{ checkInStatus.totalParticipants - checkInStatus.checkedInParticipants }}</div>
                <div class="stat-label">未签到人数</div>
              </div>
              <div class="checkin-stat-item">
                <div class="stat-value" style="color: var(--primary-color);">{{ (checkInStatus.checkInRate * 100).toFixed(2) }}%</div>
                <div class="stat-label">签到率</div>
              </div>
            </div>
          </div>

          <!-- Detail Table -->
          <div class="checkin-details">
            <h4 class="detail-title">
              <i class="fas fa-list"></i> 签到明细
            </h4>
            <div v-if="registrations.length > 0" class="checkin-table-container">
              <table class="checkin-table">
                <thead>
                  <tr>
                    <th>学号</th>
                    <th>姓名</th>
                    <th>签到状态</th>
                    <th>签到时间</th>
                    <th>签到方式</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="reg in registrations" :key="reg.studentId">
                    <td>{{ reg.studentId }}</td>
                    <td>{{ reg.studentName }}</td>
                    <td>
                      <span :class="['checkin-status', reg.checkInStatus ? 'checked-in' : 'not-checked']">
                        {{ reg.checkInStatus ? '已签到' : '未签到' }}
                      </span>
                    </td>
                    <td>{{ reg.checkInTime ? formatDateTime(reg.checkInTime) : '-' }}</td>
                    <td>{{ reg.checkInMethod ? reg.checkInMethod : '-' }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
            <div v-else class="empty-state">
              <i class="fas fa-users-slash"></i>
              <p>暂无签到记录</p>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-primary btn-lg" @click="closeCheckInInfoModal">
            <i class="fas fa-times"></i> 关闭
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useToastStore } from '../stores/toast'
import api from '../api'

const router = useRouter()
const authStore = useAuthStore()
const toast = useToastStore()

// Reactive state
const allActivities = ref([])
const loading = ref(true)
const searchTerm = ref('')
const statusFilter = ref('')

// QR code modal
const showQRModal = ref(false)
const qrCodeData = ref({ qrCode: '', checkInNumber: '' })

// Check-in info modal
const showCheckInModal = ref(false)
const currentActivityId = ref(null)
const checkInStatus = ref(null)
const registrations = ref([])
let checkInUpdateInterval = null

// Computed: filtered activities
const filteredActivities = computed(() => {
  let list = allActivities.value

  // Text search
  if (searchTerm.value) {
    const term = searchTerm.value.toLowerCase()
    list = list.filter(a =>
      a.activityName.toLowerCase().includes(term) ||
      (a.location && a.location.toLowerCase().includes(term))
    )
  }

  // Status filter
  if (statusFilter.value) {
    list = list.filter(a => a.status === statusFilter.value)
  } else {
    // Default: only show PUBLISHED
    list = list.filter(a => a.status === 'PUBLISHED')
  }

  return list
})

// Debounce timer for search
let searchTimeout = null

function onSearchInput() {
  clearTimeout(searchTimeout)
  searchTimeout = setTimeout(() => {
    // computed handles the filtering reactively
  }, 300)
}

function applyFilters() {
  // computed handles it
}

function resetFilters() {
  searchTerm.value = ''
  statusFilter.value = ''
}

function formatDateTime(dateTimeStr) {
  if (!dateTimeStr) return ''
  const date = new Date(dateTimeStr)
  return date.toLocaleString('zh-CN')
}

// Load activities
async function loadActivities() {
  loading.value = true
  try {
    const res = await api.get('/api/activity-manage/my-activities')
    if (res.data.success && res.data.data) {
      allActivities.value = res.data.data
    } else {
      allActivities.value = []
    }
  } catch (e) {
    console.error('加载活动列表错误:', e)
    allActivities.value = []
  }
  loading.value = false
}

// Generate QR Code
async function generateQRCode(activityId) {
  try {
    const res = await api.get(`/api/checkin/activity/${activityId}/qrcode`)
    if (res.data.success) {
      qrCodeData.value = {
        qrCode: res.data.data.qrCode || '',
        checkInNumber: res.data.data.checkInNumber || ''
      }
      showQRModal.value = true
    } else {
      toast.error('生成二维码失败: ' + res.data.message)
    }
  } catch (e) {
    console.error('生成二维码错误:', e)
    toast.error('生成二维码时发生错误')
  }
}

function closeQRCodeModal() {
  showQRModal.value = false
}

// View Check-in Info
async function viewCheckInInfo(activityId) {
  try {
    currentActivityId.value = activityId
    await loadCheckInInfo(activityId)
    showCheckInModal.value = true

    // Start auto-refresh every 5 seconds
    checkInUpdateInterval = setInterval(() => {
      loadCheckInInfo(activityId)
    }, 5000)
  } catch (e) {
    console.error('获取签到信息错误:', e)
    toast.error('获取签到信息时发生错误')
  }
}

async function loadCheckInInfo(activityId) {
  try {
    const [statusRes, regRes] = await Promise.all([
      api.get(`/api/checkin/activity/${activityId}/status`),
      api.get(`/api/activity-manage/activity/${activityId}/registrations`)
    ])

    if (statusRes.data.success) {
      checkInStatus.value = statusRes.data.data
    }

    if (regRes.data.success && regRes.data.data) {
      registrations.value = regRes.data.data
    } else {
      registrations.value = []
    }
  } catch (e) {
    console.error('获取签到信息错误:', e)
  }
}

function closeCheckInInfoModal() {
  showCheckInModal.value = false
  if (checkInUpdateInterval) {
    clearInterval(checkInUpdateInterval)
    checkInUpdateInterval = null
  }
  currentActivityId.value = null
  checkInStatus.value = null
  registrations.value = []
}

// Cleanup on unmount
onBeforeUnmount(() => {
  if (checkInUpdateInterval) {
    clearInterval(checkInUpdateInterval)
    checkInUpdateInterval = null
  }
})

onMounted(() => {
  if (!authStore.token) {
    router.push('/login')
    return
  }
  loadActivities()
})
</script>

<style scoped>
.checkin-page {
  /* CSS Variables */
  --primary-color: #667eea;
  --primary-hover: #5a6fd8;
  --success-color: #4CAF50;
  --success-hover: #45a049;
  --warning-color: #ff9800;
  --warning-hover: #f57c00;
  --danger-color: #f44336;
  --danger-hover: #da190b;
  --info-color: #2196F3;
  --info-hover: #0b7dda;
  --light-color: #f8f9fa;
  --dark-color: #343a40;
  --text-color: #333;
  --text-muted: #666;
  --border-color: #e0e0e0;
  --background-color: #f5f7fa;
  --card-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  --card-shadow-hover: 0 8px 25px rgba(0, 0, 0, 0.15);
  --border-radius: 10px;
  --transition: all 0.3s ease;
  --gradient-primary: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  --gradient-success: linear-gradient(135deg, #4CAF50 0%, #45a049 100%);
  --gradient-warning: linear-gradient(135deg, #ff9800 0%, #f57c00 100%);
  --gradient-danger: linear-gradient(135deg, #f44336 0%, #da190b 100%);
  --gradient-info: linear-gradient(135deg, #2196F3 0%, #0b7dda 100%);
  --gradient-light: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  --shimmer: linear-gradient(90deg, rgba(255, 255, 255, 0) 0%, rgba(255, 255, 255, 0.2) 50%, rgba(255, 255, 255, 0) 100%);
  padding: 20px;
  background: var(--background-color);
  background-image: linear-gradient(to bottom right, rgba(102, 126, 234, 0.05), rgba(118, 75, 162, 0.05));
  min-height: 100vh;
  max-width: 1200px;
  margin: 0 auto;
}

/* Header */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  background: white;
  padding: 25px 30px;
  border-radius: var(--border-radius);
  box-shadow: var(--card-shadow);
  transition: var(--transition);
  border-top: 4px solid transparent;
  background: linear-gradient(white, white) padding-box,
    var(--gradient-primary) border-box;
  position: relative;
  overflow: hidden;
}

.header::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: var(--shimmer);
  transition: left 0.6s;
}

.header:hover::before {
  left: 100%;
}

.header:hover {
  box-shadow: var(--card-shadow-hover);
  transform: translateY(-2px);
}

.header h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: var(--dark-color);
  display: flex;
  align-items: center;
  gap: 10px;
}

.header h1 i {
  color: var(--primary-color);
}

/* Card */
.card {
  background: white;
  border-radius: var(--border-radius);
  padding: 25px;
  margin-bottom: 20px;
  box-shadow: var(--card-shadow);
  transition: var(--transition);
  border-top: 4px solid transparent;
  background: linear-gradient(white, white) padding-box,
    var(--gradient-light) border-box;
}

.card:hover {
  box-shadow: var(--card-shadow-hover);
  transform: translateY(-2px);
}

.card h3 {
  margin-top: 0;
  margin-bottom: 20px;
  color: var(--dark-color);
  font-size: 18px;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 8px;
}

.card h3 i {
  color: var(--primary-color);
}

/* Buttons */
.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: var(--transition);
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  position: relative;
  overflow: hidden;
}

.btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: var(--shimmer);
  transition: left 0.3s;
}

.btn:hover::before {
  left: 100%;
}

.btn-primary {
  background: var(--gradient-primary);
  color: white;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.btn-success {
  background: var(--gradient-success);
  color: white;
}

.btn-success:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(76, 175, 80, 0.3);
}

.btn-lg {
  padding: 12px 24px;
  font-size: 16px;
}

/* Filter Bar */
.filter-bar {
  display: flex;
  gap: 15px;
  margin-bottom: 25px;
  flex-wrap: wrap;
}

.search-wrap {
  flex: 1;
  min-width: 300px;
}

.search-input {
  width: 100%;
  padding: 12px 90px 12px 16px;
  border: 2px solid #e0e0e0;
  border-radius: 10px;
  font-size: 14px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
  box-sizing: border-box;
}

.search-input:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 2px 10px rgba(102, 126, 234, 0.2);
}

.search-icon {
  position: absolute;
  right: 55px;
  top: 50%;
  transform: translateY(-50%);
  color: #666;
  font-size: 16px;
  pointer-events: none;
}

.search-btn {
  position: absolute;
  right: 4px;
  top: 50%;
  transform: translateY(-50%);
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 5px;
  transition: all 0.3s ease;
  z-index: 1;
}

.search-btn:hover {
  transform: translateY(-50%) scale(1.05);
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.filter-controls {
  display: flex;
  gap: 15px;
  align-items: flex-end;
}

.filter-label {
  display: block;
  margin-bottom: 8px;
  font-weight: 600;
  color: #343a40;
  font-size: 14px;
}

.status-select {
  padding: 12px 16px;
  border: 2px solid #e0e0e0;
  border-radius: 10px;
  font-size: 14px;
  background: white;
  color: #333;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
}

.status-select:focus {
  outline: none;
  border-color: var(--primary-color);
}

.reset-btn {
  margin-top: 25px;
}

/* Activity Grid */
.activity-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 20px;
}

/* Activity Card */
.activity-card {
  background: white;
  border-radius: var(--border-radius);
  padding: 25px;
  box-shadow: var(--card-shadow);
  border-left: 4px solid var(--primary-color);
  transition: var(--transition);
  border-top: 4px solid transparent;
  background: linear-gradient(white, white) padding-box,
    linear-gradient(to right, var(--primary-color), var(--primary-color)) border-box;
  position: relative;
  overflow: hidden;
}

.activity-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: var(--shimmer);
  transition: left 0.6s;
}

.activity-card:hover::before {
  left: 100%;
}

.activity-card:hover {
  box-shadow: var(--card-shadow-hover);
  transform: translateY(-2px);
  border-left-color: var(--primary-hover);
}

.activity-header {
  display: flex;
  justify-content: space-between;
  align-items: start;
  margin-bottom: 20px;
}

.activity-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--dark-color);
  margin: 0;
  line-height: 1.3;
  flex: 1;
}

.activity-info {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 12px;
  margin-bottom: 20px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--text-muted);
  font-size: 14px;
  padding: 8px 0;
  border-bottom: 1px solid rgba(102, 126, 234, 0.1);
}

.info-item:last-child {
  border-bottom: none;
}

.info-item i {
  color: var(--primary-color);
  width: 16px;
  text-align: center;
  font-size: 16px;
}

.activity-actions {
  display: flex;
  gap: 12px;
  margin-top: 20px;
  flex-wrap: wrap;
}

.activity-actions .btn {
  flex: 1;
  min-width: 120px;
  justify-content: center;
}

/* Empty State */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: var(--text-muted);
  background: rgba(255, 255, 255, 0.5);
  border-radius: var(--border-radius);
  border: 2px dashed var(--border-color);
}

.empty-state i {
  font-size: 64px;
  margin-bottom: 20px;
  color: rgba(102, 126, 234, 0.2);
  transition: var(--transition);
  display: block;
}

.empty-state:hover i {
  color: rgba(102, 126, 234, 0.4);
  transform: scale(1.1);
}

.empty-state p {
  font-size: 18px;
  margin: 0;
  font-weight: 500;
}

/* Modal Overlay */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  backdrop-filter: blur(5px);
  animation: fadeIn 0.3s ease;
}

/* Modal Card */
.modal-card {
  background-color: white;
  border-radius: var(--border-radius);
  box-shadow: var(--card-shadow-hover);
  max-height: 80vh;
  overflow-y: auto;
  position: relative;
  animation: slideIn 0.3s ease;
  border-top: 4px solid var(--primary-color);
}

.qrcode-modal-card {
  max-width: 500px;
  width: 95%;
  padding: 25px;
}

.checkin-modal-card {
  max-width: 800px;
  width: 95%;
}

.modal-title-center {
  margin-top: 0;
  margin-bottom: 20px;
  color: var(--dark-color);
  font-size: 20px;
  font-weight: 700;
  text-align: center;
}

/* QR Code Container */
.qrcode-container {
  text-align: center;
  padding: 30px;
}

.qrcode-image {
  max-width: 300px;
  margin: 0 auto 25px;
  border-radius: 10px;
  box-shadow: var(--card-shadow);
  padding: 15px;
  background: white;
  transition: var(--transition);
  display: block;
}

.qrcode-image:hover {
  box-shadow: var(--card-shadow-hover);
  transform: scale(1.05);
}

.qrcode-number {
  margin: 15px 0;
  font-size: 18px;
  font-weight: bold;
}

.qrcode-number-value {
  color: var(--primary-color);
  font-size: 24px;
}

/* Check-in Info Modal Header */
.modal-header {
  padding: 20px;
  border-bottom: 1px solid var(--border-color);
  background-color: var(--light-color);
  border-radius: var(--border-radius) var(--border-radius) 0 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  margin: 0;
  color: var(--dark-color);
  font-size: 20px;
  font-weight: 700;
  display: flex;
  align-items: center;
}

.modal-close {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: var(--text-color);
  padding: 0;
  line-height: 1;
}

.modal-close:hover {
  color: var(--primary-color);
}

.modal-body {
  padding: 20px;
}

.modal-footer {
  text-align: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid var(--border-color);
}

/* Check-in Summary */
.checkin-summary {
  margin-bottom: 20px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.summary-title {
  margin-top: 0;
  margin-bottom: 15px;
  color: var(--dark-color);
  font-size: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.summary-title i {
  color: var(--primary-color);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 15px;
}

.checkin-stat-item {
  background-color: var(--light-color);
  padding: 15px;
  border-radius: 8px;
  text-align: center;
  box-shadow: var(--card-shadow);
}

.checkin-stat-item .stat-value {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 5px;
  color: var(--dark-color);
}

.checkin-stat-item .stat-label {
  font-size: 14px;
  color: var(--text-color);
}

/* Check-in Details */
.checkin-details {
  margin-top: 20px;
}

.detail-title {
  margin-bottom: 15px;
  color: var(--dark-color);
  font-size: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.detail-title i {
  color: var(--primary-color);
}

.checkin-table-container {
  overflow-x: auto;
}

.checkin-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

.checkin-table th,
.checkin-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid var(--border-color);
}

.checkin-table th {
  background-color: var(--light-color);
  font-weight: bold;
  color: var(--dark-color);
}

.checkin-table tr:hover {
  background-color: rgba(102, 126, 234, 0.05);
}

.checkin-status {
  padding: 5px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: bold;
}

.checkin-status.checked-in {
  background-color: rgba(76, 175, 80, 0.2);
  color: var(--success-color);
}

.checkin-status.not-checked {
  background-color: rgba(255, 152, 0, 0.2);
  color: var(--warning-color);
}

/* Animations */
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideIn {
  from { transform: translateY(-30px); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
}

/* Responsive */
@media (max-width: 768px) {
  .checkin-page {
    padding: 15px;
  }

  .header {
    padding: 20px;
    flex-direction: column;
    gap: 15px;
    text-align: center;
  }

  .header h1 {
    font-size: 20px;
  }

  .activity-grid {
    grid-template-columns: 1fr;
  }

  .activity-info {
    grid-template-columns: 1fr;
  }

  .activity-actions {
    flex-direction: column;
  }

  .activity-actions .btn {
    min-width: auto;
  }

  .qrcode-image {
    max-width: 250px;
  }

  .filter-bar {
    flex-direction: column;
  }

  .search-wrap {
    min-width: auto;
  }

  .filter-controls {
    flex-direction: column;
    align-items: stretch;
  }

  .reset-btn {
    margin-top: 0;
  }
}

/* Scrollbar */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: var(--primary-color);
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: var(--primary-hover);
}
</style>
