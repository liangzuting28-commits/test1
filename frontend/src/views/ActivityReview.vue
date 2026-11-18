<template>
  <div class="activity-review-page">
    <div class="container">
      <!-- Header -->
      <div class="header">
        <h1><i class="fas fa-clipboard-check"></i> 活动审核</h1>
        <button class="btn btn-primary" @click="$router.push('/dashboard')">
          <i class="fas fa-arrow-left"></i> 返回仪表盘
        </button>
      </div>

      <!-- Main card -->
      <div class="card">
        <h3><i class="fas fa-list"></i> 活动列表</h3>

        <!-- Tabs -->
        <div class="tabs">
          <div :class="['tab', { active: activeTab === 'pending' }]" @click="switchTab('pending')">
            <i class="fas fa-clock"></i> 待审核
          </div>
          <div :class="['tab', { active: activeTab === 'approved' }]" @click="switchTab('approved')">
            <i class="fas fa-check-circle"></i> 已通过
          </div>
          <div :class="['tab', { active: activeTab === 'rejected' }]" @click="switchTab('rejected')">
            <i class="fas fa-times-circle"></i> 已驳回
          </div>
        </div>

        <!-- Search and filter -->
        <div class="search-filter-container">
          <div class="search-box">
            <i class="fas fa-search"></i>
            <input type="text" v-model="searchText" placeholder="搜索活动名称或社团名称..." @keyup.enter="applyFilters">
            <button class="search-btn" @click="applyFilters"><i class="fas fa-search"></i> 搜索</button>
          </div>
          <div class="filter-box">
            <select v-model="clubFilter" @change="applyFilters">
              <option value="">所有社团</option>
              <option v-for="club in clubs" :key="club.clubId" :value="club.clubId">{{ club.clubName }}</option>
            </select>
            <select v-model="dateFilter" @change="applyFilters">
              <option value="">所有时间</option>
              <option value="today">今天</option>
              <option value="week">本周</option>
              <option value="month">本月</option>
            </select>
          </div>
        </div>

        <!-- Activity content -->
        <div class="tab-content active">
          <div v-if="loading" class="loading-spinner">
            <i class="fas fa-spinner fa-spin"></i> 加载中...
          </div>

          <template v-else>
            <div v-if="filteredActivities.length > 0" class="activity-grid">
              <div class="activity-card" v-for="activity in filteredActivities" :key="activity.activityId">
                <div class="activity-header">
                  <div>
                    <div class="activity-title">{{ activity.activityName }}</div>
                    <div class="activity-club"><i class="fas fa-university"></i> {{ activity.clubName || '未知社团' }}</div>
                  </div>
                  <!-- Status badge -->
                  <div v-if="activeTab === 'pending'" class="activity-status status-pending">
                    <i class="fas fa-clock"></i> 待审核
                  </div>
                  <div v-else-if="activeTab === 'approved'" class="activity-status status-published">
                    <i class="fas fa-check-circle"></i> 已通过
                  </div>
                  <div v-else class="activity-status status-rejected">
                    <i class="fas fa-times-circle"></i> 已驳回
                  </div>
                </div>

                <div class="activity-info">
                  <div class="info-item">
                    <i class="fas fa-clock"></i>
                    <span>{{ formatDateTime(activity.activityTime) }}</span>
                  </div>
                  <div class="info-item">
                    <i class="fas fa-map-marker-alt"></i>
                    <span>{{ activity.location }}</span>
                  </div>
                  <div class="info-item">
                    <i class="fas fa-users"></i>
                    <span>{{ activity.maxParticipants === 0 ? '无限制' : activity.maxParticipants + '人' }}</span>
                  </div>
                  <div class="info-item">
                    <i class="fas fa-star"></i>
                    <span>{{ activity.awardPoints }} 积分</span>
                  </div>
                </div>

                <div class="activity-description">
                  {{ activity.description || '暂无详细描述' }}
                </div>

                <div v-if="activity.posterFileId || activity.planFileId" class="activity-files-section">
                  <div class="files-section-title"><i class="fas fa-paperclip"></i> 活动附件</div>
                  <div class="files-list">
                    <div v-if="activity.posterFileId" class="file-row">
                      <div class="file-icon-sm file-icon-image"><i class="fas fa-image"></i></div>
                      <span class="file-label">活动宣传图</span>
                      <a :href="'/api/files/download/' + activity.posterFileId" target="_blank" class="file-link file-link-preview"><i class="fas fa-eye"></i> 预览</a>
                      <a :href="'/api/files/download/' + activity.posterFileId" download class="file-link file-link-download"><i class="fas fa-download"></i> 下载</a>
                    </div>
                    <div v-if="activity.planFileId" class="file-row">
                      <div class="file-icon-sm file-icon-doc"><i class="fas fa-file-pdf"></i></div>
                      <span class="file-label">活动策划书</span>
                      <a :href="'/api/files/download/' + activity.planFileId" target="_blank" class="file-link file-link-preview"><i class="fas fa-eye"></i> 预览</a>
                      <a :href="'/api/files/download/' + activity.planFileId" download class="file-link file-link-download"><i class="fas fa-download"></i> 下载</a>
                    </div>
                  </div>
                </div>

                <!-- Reject reason for rejected tab -->
                <div v-if="activeTab === 'rejected' && activity.rejectReason" class="reject-reason">
                  <strong><i class="fas fa-exclamation-triangle"></i> 驳回原因：</strong>{{ activity.rejectReason }}
                </div>

                <div class="activity-actions">
                  <!-- Pending: approve + reject -->
                  <template v-if="activeTab === 'pending'">
                    <button class="btn btn-success" @click="approveActivity(activity.activityId)">
                      <i class="fas fa-check"></i> 通过
                    </button>
                    <button class="btn btn-danger" @click="showRejectModal(activity.activityId)">
                      <i class="fas fa-times"></i> 驳回
                    </button>
                  </template>
                  <!-- Approved / Rejected: view details -->
                  <template v-else>
                    <button class="btn btn-info" @click="viewDetails(activity.activityId)">
                      <i class="fas fa-info-circle"></i> 查看详情
                    </button>
                  </template>
                </div>
              </div>
            </div>

            <!-- Empty state -->
            <div v-else class="empty-state">
              <i :class="emptyIcon"></i>
              <p>{{ emptyTitle }}</p>
              <p class="subtext">{{ emptySubtitle }}</p>
            </div>
          </template>
        </div>
      </div>
    </div>

    <!-- Reject modal -->
    <div v-if="showReject" class="reject-modal" @click.self="closeRejectModal">
      <div class="reject-modal-content">
        <div class="reject-modal-header">
          <h3><i class="fas fa-exclamation-triangle"></i> 驳回活动申请</h3>
          <span class="close-modal" @click="closeRejectModal">&times;</span>
        </div>
        <div class="reject-modal-body">
          <label for="rejectReason">驳回原因：</label>
          <textarea id="rejectReason" v-model="rejectReason" placeholder="请输入驳回该活动的原因..."></textarea>
        </div>
        <div class="reject-modal-footer">
          <button class="btn btn-primary" @click="closeRejectModal">取消</button>
          <button class="btn btn-danger" @click="confirmReject">确认驳回</button>
        </div>
      </div>
    </div>

    <!-- Details modal -->
    <div v-if="showDetails" class="details-modal" @click.self="closeDetailsModal">
      <div class="details-modal-content">
        <div class="details-modal-header">
          <h3><i class="fas fa-info-circle"></i> 活动详情</h3>
          <span class="close-modal" @click="closeDetailsModal">&times;</span>
        </div>
        <div class="details-modal-body" v-if="detailActivity">
          <div class="details-info-grid">
            <div class="details-row">
              <div class="details-label">活动名称</div>
              <div class="details-value">{{ detailActivity.activityName }}</div>
            </div>
            <div class="details-row">
              <div class="details-label">所属社团</div>
              <div class="details-value">{{ detailActivity.clubName || '未知社团' }}</div>
            </div>
            <div class="details-row">
              <div class="details-label">活动类型</div>
              <div class="details-value">{{ detailActivity.activityType || '未设置' }}</div>
            </div>
            <div class="details-row">
              <div class="details-label">活动时间</div>
              <div class="details-value">{{ formatDateTime(detailActivity.activityTime) }}</div>
            </div>
            <div class="details-row">
              <div class="details-label">活动地点</div>
              <div class="details-value">{{ detailActivity.location }}</div>
            </div>
            <div class="details-row">
              <div class="details-label">人数限制</div>
              <div class="details-value">{{ detailActivity.maxParticipants === 0 ? '无限制' : detailActivity.maxParticipants + '人' }}</div>
            </div>
            <div class="details-row">
              <div class="details-label">积分奖励</div>
              <div class="details-value">{{ detailActivity.awardPoints }} 积分</div>
            </div>
            <div class="details-row">
              <div class="details-label">活动状态</div>
              <div class="details-value">
                <span :class="'status-text status-text-' + (detailActivity.status || '').toLowerCase()">
                  {{ statusTextMap[detailActivity.status] || detailActivity.status }}
                </span>
              </div>
            </div>
            <div v-if="detailActivity.rejectReason" class="details-row">
              <div class="details-label">驳回原因</div>
              <div class="details-value reject-value">{{ detailActivity.rejectReason }}</div>
            </div>
          </div>
          <div class="details-description">
            <h4>活动描述：</h4>
            <p>{{ detailActivity.description || '暂无详细描述' }}</p>
          </div>

          <!-- 活动附件 -->
          <div class="details-files">
            <h4><i class="fas fa-paperclip"></i> 活动附件</h4>
            <div class="files-grid">
              <div v-if="detailActivity.posterFileId" class="file-item">
                <div class="file-icon file-icon-image"><i class="fas fa-image"></i></div>
                <div class="file-info">
                  <div class="file-name">活动宣传图</div>
                  <div class="file-actions">
                    <a :href="'/api/files/download/' + detailActivity.posterFileId" target="_blank" class="file-btn file-btn-preview">
                      <i class="fas fa-eye"></i> 预览
                    </a>
                    <a :href="'/api/files/download/' + detailActivity.posterFileId" download class="file-btn file-btn-download">
                      <i class="fas fa-download"></i> 下载
                    </a>
                  </div>
                </div>
              </div>
              <div v-if="detailActivity.planFileId" class="file-item">
                <div class="file-icon file-icon-doc"><i class="fas fa-file-pdf"></i></div>
                <div class="file-info">
                  <div class="file-name">活动策划书</div>
                  <div class="file-actions">
                    <a :href="'/api/files/download/' + detailActivity.planFileId" target="_blank" class="file-btn file-btn-preview">
                      <i class="fas fa-eye"></i> 预览
                    </a>
                    <a :href="'/api/files/download/' + detailActivity.planFileId" download class="file-btn file-btn-download">
                      <i class="fas fa-download"></i> 下载
                    </a>
                  </div>
                </div>
              </div>
              <div v-if="!detailActivity.posterFileId && !detailActivity.planFileId" class="no-files">
                <i class="fas fa-folder-open"></i>
                <p>暂无附件</p>
              </div>
            </div>
          </div>
        </div>
        <div class="details-modal-footer">
          <button class="btn btn-primary" @click="closeDetailsModal">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useToastStore } from '../stores/toast'
import api from '../api'

const router = useRouter()
const auth = useAuthStore()
const toast = useToastStore()

// State
const activeTab = ref('pending')
const loading = ref(true)
const allActivities = ref([])
const filteredActivities = ref([])
const clubs = ref([])

// Filters
const searchText = ref('')
const clubFilter = ref('')
const dateFilter = ref('')

// Reject modal
const showReject = ref(false)
const rejectTargetId = ref(null)
const rejectReason = ref('')

// Details modal
const showDetails = ref(false)
const detailActivity = ref(null)

// Status text map
const statusTextMap = {
  PENDING: '待审核',
  PUBLISHED: '已发布',
  REJECTED: '已驳回',
  ENDED: '已结束'
}

// Computed empty state text
const emptyIcon = computed(() => {
  const icons = {
    pending: 'fas fa-clipboard-check',
    approved: 'fas fa-check-circle',
    rejected: 'fas fa-times-circle'
  }
  return icons[activeTab.value] || 'fas fa-inbox'
})

const emptyTitle = computed(() => {
  const titles = {
    pending: '暂无待审核活动',
    approved: '暂无已通过活动',
    rejected: '暂无已驳回活动'
  }
  return titles[activeTab.value] || '暂无数据'
})

const emptySubtitle = computed(() => {
  const subs = {
    pending: '所有活动都已审核完毕',
    approved: '还没有活动通过审核',
    rejected: '还没有活动被驳回'
  }
  return subs[activeTab.value] || ''
})

// Tab endpoints
const tabEndpoints = {
  pending: '/api/activity-manage/pending',
  approved: '/api/activity-manage/approved',
  rejected: '/api/activity-manage/rejected'
}

// Format date time
function formatDateTime(dateTimeStr) {
  if (!dateTimeStr) return '未设置'
  const date = new Date(dateTimeStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// Switch tab
function switchTab(tab) {
  activeTab.value = tab
  loadActivities()
}

// Load activities for current tab
async function loadActivities() {
  loading.value = true
  try {
    const res = await api.get(tabEndpoints[activeTab.value])
    allActivities.value = res.data.data || []
    applyFilters()
  } catch (e) {
    console.error('加载活动列表失败:', e)
    allActivities.value = []
    filteredActivities.value = []
  }
  loading.value = false
}

// Apply search / club / date filters
function applyFilters() {
  let result = [...allActivities.value]

  // Search filter
  if (searchText.value) {
    const term = searchText.value.toLowerCase()
    result = result.filter(a => {
      const name = (a.activityName || '').toLowerCase()
      const club = (a.clubName || '').toLowerCase()
      return name.includes(term) || club.includes(term)
    })
  }

  // Club filter
  if (clubFilter.value) {
    result = result.filter(a => String(a.clubId) === String(clubFilter.value))
  }

  // Date filter
  if (dateFilter.value) {
    const now = new Date()
    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
    result = result.filter(a => {
      if (!a.activityTime) return false
      const d = new Date(a.activityTime)
      switch (dateFilter.value) {
        case 'today':
          return d >= today && d < new Date(today.getTime() + 86400000)
        case 'week': {
          const weekStart = new Date(today)
          weekStart.setDate(today.getDate() - today.getDay())
          return d >= weekStart
        }
        case 'month': {
          const monthStart = new Date(now.getFullYear(), now.getMonth(), 1)
          return d >= monthStart
        }
        default:
          return true
      }
    })
  }

  filteredActivities.value = result
}

// Load clubs for filter dropdown
async function loadClubs() {
  try {
    const res = await api.get('/api/clubs')
    clubs.value = res.data.data || []
  } catch (e) {
    console.error('加载社团列表失败:', e)
  }
}

// Approve activity
async function approveActivity(id) {
  if (!confirm('确定要通过这个活动申请吗？')) return
  try {
    const res = await api.put(`/api/activity-manage/${id}/review`, {
      status: 'PUBLISHED',
      reason: ''
    })
    if (res.data.success) {
      toast.success('活动审核通过！')
      loadActivities()
    } else {
      toast.error('审核失败: ' + res.data.message)
    }
  } catch (e) {
    console.error('审核活动错误:', e)
    toast.error('审核时发生错误')
  }
}

// Show reject modal
function showRejectModal(id) {
  rejectTargetId.value = id
  rejectReason.value = ''
  showReject.value = true
}

// Close reject modal
function closeRejectModal() {
  showReject.value = false
  rejectTargetId.value = null
  rejectReason.value = ''
}

// Confirm reject
async function confirmReject() {
  if (!rejectReason.value.trim()) {
    toast.warning('请输入驳回原因')
    return
  }
  try {
    const res = await api.put(`/api/activity-manage/${rejectTargetId.value}/review`, {
      status: 'REJECTED',
      reason: rejectReason.value.trim()
    })
    if (res.data.success) {
      toast.success('活动已驳回！')
      closeRejectModal()
      loadActivities()
    } else {
      toast.error('驳回失败: ' + res.data.message)
    }
  } catch (e) {
    console.error('驳回活动错误:', e)
    toast.error('驳回时发生错误')
  }
}

// View details
async function viewDetails(id) {
  try {
    const res = await api.get(`/api/activity-manage/${id}`)
    if (res.data.success) {
      detailActivity.value = res.data.data
      showDetails.value = true
    } else {
      toast.error('获取活动详情失败: ' + res.data.message)
    }
  } catch (e) {
    console.error('查看活动详情错误:', e)
    toast.error('查看活动详情时发生错误')
  }
}

// Close details modal
function closeDetailsModal() {
  showDetails.value = false
  detailActivity.value = null
}

// Init
onMounted(() => {
  if (!auth.token) {
    router.push('/login')
    return
  }
  loadActivities()
  loadClubs()
})
</script>

<style scoped>
/* ===== Reset ===== */
.activity-review-page * {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

/* ===== Page Root ===== */
.activity-review-page {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  min-height: 100vh;
  color: #2d3748;
  line-height: 1.6;
  overflow-x: hidden;
  padding: 20px 0;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* ===== Header ===== */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 35px;
  background: #fff;
  padding: 28px 35px;
  border-radius: 25px;
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.16);
  border-top: 4px solid #667eea;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.header:hover {
  transform: translateY(-2px);
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.2);
}

.header h1 {
  font-size: 30px;
  font-weight: 800;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  display: flex;
  align-items: center;
  gap: 15px;
}

.header h1 i {
  -webkit-text-fill-color: #667eea;
  font-size: 34px;
}

/* ===== Card ===== */
.card {
  background: #fff;
  border-radius: 25px;
  padding: 35px;
  margin-bottom: 30px;
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.12);
  border: 1px solid #f3f4f6;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 4px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  transform: scaleX(0);
  transform-origin: left;
  transition: transform 0.3s ease;
}

.card:hover::before {
  transform: scaleX(1);
}

.card h3 {
  color: #2d3748;
  margin-bottom: 25px;
  font-size: 22px;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 12px;
  padding-bottom: 10px;
  border-bottom: 2px solid #f1f5f9;
}

.card h3 i {
  color: #667eea;
  font-size: 24px;
}

/* ===== Buttons ===== */
.btn {
  padding: 14px 28px;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  font-size: 15px;
  font-weight: 600;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: inline-flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
  position: relative;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.5s ease;
}

.btn:hover::before {
  left: 100%;
}

.btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.16);
}

.btn:active {
  transform: translateY(-1px);
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-success {
  background: linear-gradient(135deg, #4ade80 0%, #22c55e 100%);
  color: white;
}

.btn-danger {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  color: white;
}

.btn-warning {
  background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
  color: white;
}

.btn-info {
  background: linear-gradient(135deg, #06b6d4 0%, #0891b2 100%);
  color: white;
}

/* ===== Tabs ===== */
.tabs {
  display: flex;
  margin-bottom: 30px;
  background: #fff;
  border-radius: 25px;
  padding: 10px;
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.16);
  gap: 8px;
  overflow-x: auto;
  scrollbar-width: none;
}

.tabs::-webkit-scrollbar {
  display: none;
}

.tab {
  flex: 1;
  min-width: 120px;
  padding: 18px 24px;
  text-align: center;
  cursor: pointer;
  border-radius: 12px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  font-weight: 600;
  color: #4a5568;
  border: none;
  background: transparent;
  font-size: 15px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  position: relative;
  overflow: hidden;
}

.tab::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(102, 126, 234, 0.1), transparent);
  transition: left 0.5s ease;
}

.tab:hover::before {
  left: 100%;
}

.tab:hover {
  background: #f1f5f9;
  color: #2d3748;
  transform: translateY(-1px);
}

.tab.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.16);
  transform: translateY(-1px);
}

.tab i {
  font-size: 18px;
}

/* ===== Tab Content ===== */
.tab-content {
  animation: fadeInUp 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* ===== Search and Filter ===== */
.search-filter-container {
  display: flex;
  gap: 20px;
  align-items: center;
  margin-bottom: 25px;
  flex-wrap: wrap;
}

.search-box {
  position: relative;
  flex: 1;
  min-width: 250px;
}

.search-box i {
  position: absolute;
  left: 18px;
  top: 50%;
  transform: translateY(-50%);
  color: #999;
  font-size: 16px;
  pointer-events: none;
}

.search-box input {
  width: 100%;
  padding: 14px 100px 14px 50px;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 500;
  color: #2d3748;
  outline: none;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: #f7fafc;
}

.search-box input:focus {
  border-color: #667eea;
  background: #fff;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
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

.filter-box {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
}

.filter-box select {
  padding: 14px 20px;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 500;
  background: #f7fafc;
  min-width: 150px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  color: #2d3748;
  outline: none;
}

.filter-box select:focus {
  border-color: #667eea;
  background: #fff;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

/* ===== Loading ===== */
.loading-spinner {
  text-align: center;
  padding: 60px 20px;
  color: #667eea;
  font-size: 16px;
}

.loading-spinner i {
  font-size: 40px;
  margin-bottom: 15px;
  display: block;
}

/* ===== Activity Grid ===== */
.activity-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 30px;
  margin-top: 10px;
}

/* ===== Activity Card ===== */
.activity-card {
  background: #fff;
  border-radius: 25px;
  padding: 35px;
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.16);
  border: 1px solid rgba(0, 0, 0, 0.05);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.activity-card:hover {
  transform: translateY(-12px) scale(1.02);
  box-shadow: 0 30px 60px rgba(0, 0, 0, 0.25);
}

.activity-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.activity-card::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.activity-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.activity-title {
  font-size: 24px;
  font-weight: 700;
  color: #2d3748;
  line-height: 1.4;
  margin-bottom: 8px;
}

.activity-card:hover .activity-title {
  color: #667eea;
}

.activity-club {
  font-size: 16px;
  color: #4a5568;
  display: flex;
  align-items: center;
  gap: 5px;
  margin-top: 8px;
}

.activity-club i {
  color: #667eea;
}

/* ===== Status Badges ===== */
.activity-status {
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
}

.status-pending {
  background: #fef3c7;
  color: #92400e;
  border: 1px solid #fde68a;
}

.status-published {
  background: #dcfce7;
  color: #166534;
  border: 1px solid #bbf7d0;
}

.status-rejected {
  background: #f3f4f6;
  color: #4b5563;
  border: 1px solid #e5e7eb;
}

/* ===== Activity Info ===== */
.activity-info {
  display: grid;
  gap: 16px;
  margin-bottom: 25px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #4a5568;
  font-size: 16px;
}

.info-item i {
  color: #667eea;
  width: 18px;
  text-align: center;
  font-size: 16px;
  flex-shrink: 0;
}

/* ===== Activity Description ===== */
.activity-description {
  color: #4a5568;
  margin-bottom: 25px;
  line-height: 1.7;
  font-size: 16px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.activity-files-section {
  padding: 15px;
  background: #f8fafc;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  margin-bottom: 20px;
}

.files-section-title {
  font-size: 13px;
  font-weight: 600;
  color: #4a5568;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.files-section-title i { color: #faad14; }

.files-list { display: flex; flex-direction: column; gap: 8px; }

.file-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  background: white;
  border-radius: 8px;
  border: 1px solid #e8e8e8;
}

.file-icon-sm {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: white;
  flex-shrink: 0;
}

.file-icon-image { background: linear-gradient(135deg, #36cfc9, #13c2c2); }
.file-icon-doc { background: linear-gradient(135deg, #f5222d, #ff4d4f); }

.file-label { flex: 1; font-size: 13px; font-weight: 500; color: #2d3748; }

.file-link {
  padding: 4px 10px;
  border-radius: 5px;
  font-size: 12px;
  font-weight: 500;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  transition: all 0.2s;
}

.file-link-preview { background: #e6f7ff; color: #1890ff; }
.file-link-preview:hover { background: #bae7ff; }
.file-link-download { background: #f6ffed; color: #52c41a; }
.file-link-download:hover { background: #d9f7be; }

/* ===== Reject Reason Display ===== */
.reject-reason {
  background: #fef2f2;
  border: 1px solid #fee2e2;
  border-left: 4px solid #ef4444;
  padding: 20px;
  border-radius: 12px;
  margin: 15px 0;
  font-size: 14px;
}

.reject-reason strong {
  color: #ef4444;
  display: block;
  margin-bottom: 8px;
  font-size: 15px;
}

/* ===== Activity Actions ===== */
.activity-actions {
  display: flex;
  gap: 15px;
  margin-top: 25px;
  flex-wrap: wrap;
}

/* ===== Empty State ===== */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #4a5568;
}

.empty-state i {
  font-size: 64px;
  margin-bottom: 20px;
  color: #e5e7eb;
  display: block;
}

.empty-state p {
  font-size: 16px;
  margin-bottom: 10px;
  color: #4a5568;
}

.empty-state .subtext {
  color: #9ca3af;
  font-size: 14px;
}

/* ===== Reject Modal ===== */
.reject-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.6);
  z-index: 1000;
  display: flex;
  justify-content: center;
  align-items: center;
  backdrop-filter: blur(8px);
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.reject-modal-content {
  background: #fff;
  border-radius: 25px;
  width: 90%;
  max-width: 550px;
  padding: 35px;
  box-shadow: 0 30px 80px rgba(0, 0, 0, 0.3);
  animation: modalSlideIn 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.reject-modal-header {
  margin-bottom: 25px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 20px;
  border-bottom: 2px solid #f7fafc;
}

.reject-modal-header h3 {
  color: #2d3748;
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 10px;
}

.reject-modal-header h3 i {
  color: #ef4444;
}

.close-modal {
  font-size: 24px;
  cursor: pointer;
  color: #4a5568;
  transition: all 0.3s ease;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
}

.close-modal:hover {
  background: #f8fafc;
  color: #2d3748;
}

.reject-modal-body {
  margin-bottom: 25px;
}

.reject-modal-body label {
  display: block;
  margin-bottom: 10px;
  font-weight: 600;
  color: #2d3748;
  font-size: 15px;
}

.reject-modal-body textarea {
  width: 100%;
  padding: 14px 18px;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  font-size: 14px;
  resize: vertical;
  min-height: 120px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: #f7fafc;
  font-family: inherit;
  color: #2d3748;
}

.reject-modal-body textarea:focus {
  outline: none;
  border-color: #667eea;
  background: white;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
}

.reject-modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 20px;
  border-top: 2px solid #f7fafc;
}

/* ===== Details Modal ===== */
.details-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.6);
  z-index: 1000;
  display: flex;
  justify-content: center;
  align-items: center;
  backdrop-filter: blur(8px);
  animation: fadeIn 0.3s ease;
}

.details-modal-content {
  background: #fff;
  border-radius: 25px;
  width: 90%;
  max-width: 700px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 30px 80px rgba(0, 0, 0, 0.3);
  animation: modalSlideIn 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.details-modal-header {
  padding: 25px 35px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 2px solid #f7fafc;
}

.details-modal-header h3 {
  color: #2d3748;
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 10px;
}

.details-modal-header h3 i {
  color: #667eea;
}

.details-modal-body {
  padding: 30px 35px;
}

.details-info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 25px;
}

.details-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 10px 0;
}

.details-label {
  min-width: 100px;
  font-weight: 600;
  color: #4a5568;
  font-size: 15px;
}

.details-value {
  flex: 1;
  color: #2d3748;
  font-size: 15px;
}

.reject-value {
  color: #ef4444;
  font-weight: 500;
}

.status-text {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
}

.status-text-pending {
  background: #fef3c7;
  color: #92400e;
}

.status-text-published {
  background: #dcfce7;
  color: #166534;
}

.status-text-rejected {
  background: #f3f4f6;
  color: #4b5563;
}

.status-text-ended {
  background: #e5e7eb;
  color: #374151;
}

.details-description {
  padding: 20px;
  background: #f8fafc;
  border-radius: 12px;
}

.details-description h4 {
  font-size: 16px;
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 10px;
}

.details-description p {
  color: #4a5568;
  font-size: 15px;
  line-height: 1.8;
}

.details-files {
  padding: 20px;
  background: #f8fafc;
  border-radius: 12px;
}

.details-files h4 {
  font-size: 16px;
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 15px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.details-files h4 i { color: #faad14; }

.files-grid { display: flex; flex-direction: column; gap: 12px; }

.file-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  background: white;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  transition: all 0.3s ease;
}

.file-item:hover { border-color: #667eea; box-shadow: 0 4px 15px rgba(102,126,234,0.1); }

.file-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: white;
  flex-shrink: 0;
}

.file-icon-image { background: linear-gradient(135deg, #36cfc9, #13c2c2); }
.file-icon-doc { background: linear-gradient(135deg, #f5222d, #ff4d4f); }

.file-info { flex: 1; min-width: 0; }
.file-name { font-weight: 600; color: #2d3748; font-size: 14px; margin-bottom: 8px; }

.file-actions { display: flex; gap: 8px; }

.file-btn {
  padding: 6px 14px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 5px;
  transition: all 0.2s;
}

.file-btn-preview { background: #e6f7ff; color: #1890ff; }
.file-btn-preview:hover { background: #bae7ff; }
.file-btn-download { background: #f6ffed; color: #52c41a; }
.file-btn-download:hover { background: #d9f7be; }

.no-files {
  text-align: center;
  padding: 20px;
  color: #bbb;
}

.no-files i { font-size: 32px; margin-bottom: 8px; display: block; }
.no-files p { font-size: 13px; }

.details-modal-footer {
  padding: 20px 35px;
  border-top: 2px solid #f7fafc;
  text-align: right;
}

/* ===== Modal Animation ===== */
@keyframes modalSlideIn {
  from { opacity: 0; transform: scale(0.85); }
  to { opacity: 1; transform: scale(1); }
}

/* ===== Responsive ===== */
@media (max-width: 768px) {
  .container {
    padding: 0 15px;
  }

  .header {
    flex-direction: column;
    gap: 15px;
    text-align: center;
    padding: 20px;
    margin-bottom: 20px;
  }

  .header h1 {
    font-size: 24px;
  }

  .tabs {
    flex-direction: column;
    gap: 5px;
  }

  .tab {
    padding: 12px 15px;
    font-size: 13px;
  }

  .activity-grid {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .activity-card {
    min-height: auto;
  }

  .activity-actions {
    flex-direction: column;
  }

  .activity-actions .btn {
    width: 100%;
    justify-content: center;
  }

  .card {
    padding: 20px;
  }

  .search-filter-container {
    flex-direction: column;
  }

  .filter-box {
    flex-direction: column;
    gap: 10px;
  }

  .filter-box select {
    width: 100%;
    min-width: auto;
  }

  .reject-modal-content,
  .details-modal-content {
    padding: 20px;
    width: 95%;
  }

  .details-modal-header {
    padding: 20px 25px;
  }

  .details-modal-body {
    padding: 20px 25px;
  }

  .details-modal-footer {
    padding: 15px 25px;
  }

  .details-info-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  .activity-review-page {
    padding: 10px 0;
  }

  .header {
    padding: 15px;
  }

  .header h1 {
    font-size: 20px;
  }

  .activity-title {
    font-size: 20px;
  }
}

/* Scrollbar styling for modals */
.details-modal-content::-webkit-scrollbar {
  width: 6px;
}

.details-modal-content::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 10px;
}

.details-modal-content::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 10px;
}

.details-modal-content::-webkit-scrollbar-thumb:hover {
  background: #a1a1a1;
}
</style>
