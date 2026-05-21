<template>
  <div class="manage-activities-page">
    <div class="container">
      <!-- Header -->
      <div class="header">
        <h1><i class="fas fa-list"></i> 活动管理</h1>
        <button class="btn btn-primary" @click="goBack">
          <i class="fas fa-arrow-left"></i> 返回仪表盘
        </button>
      </div>

      <!-- Main Card -->
      <div class="card">
        <h3><i class="fas fa-list"></i> 所有活动列表</h3>

        <!-- Search and Filter -->
        <div class="filter-bar">
          <div class="search-box">
            <div class="search-input-wrap">
              <input
                v-model="searchText"
                type="text"
                placeholder="搜索活动名称、社团名称或地点..."
                class="search-input"
                @input="debouncedApplyFilters"
              />
              <i class="fas fa-search search-icon"></i>
            </div>
          </div>
          <div class="filter-controls">
            <div class="filter-group">
              <label class="filter-label">活动状态</label>
              <select v-model="statusFilter" class="filter-select" @change="applyFilters">
                <option value="">全部状态</option>
                <option value="PENDING">待审核</option>
                <option value="PUBLISHED">已发布</option>
                <option value="REJECTED">已驳回</option>
                <option value="ENDED">已结束</option>
              </select>
            </div>
            <div v-if="isAdmin" class="filter-group">
              <label class="filter-label">社团</label>
              <select v-model="clubFilter" class="filter-select" @change="applyFilters">
                <option value="">全部社团</option>
                <option v-for="club in clubs" :key="club.clubId" :value="String(club.clubId)">
                  {{ club.clubName }}
                </option>
              </select>
            </div>
            <div class="filter-group filter-reset">
              <button class="btn btn-info" @click="resetFilters">
                <i class="fas fa-sync-alt"></i> 重置
              </button>
            </div>
          </div>
        </div>

        <!-- Loading -->
        <div v-if="loading" class="loading-state">加载中...</div>

        <!-- Activity Grid -->
        <div v-else-if="filteredActivities.length > 0" class="activity-grid">
          <div
            v-for="activity in filteredActivities"
            :key="activity.activityId"
            class="activity-card"
          >
            <div class="activity-header">
              <div>
                <div class="activity-title">{{ activity.activityName }}</div>
                <div class="activity-club">{{ activity.clubName || '未知社团' }}</div>
              </div>
              <div :class="['activity-status', statusClass(activity.status)]">
                {{ statusLabel(activity.status) }}
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

            <!-- Reject Reason -->
            <div v-if="activity.status === 'REJECTED' && activity.rejectReason" class="reject-reason">
              <strong><i class="fas fa-exclamation-triangle"></i> 驳回原因：</strong>{{ activity.rejectReason }}
            </div>

            <!-- Actions -->
            <div class="activity-actions">
              <!-- PENDING -->
              <template v-if="activity.status === 'PENDING'">
                <button
                  v-if="isAdmin"
                  class="btn btn-info"
                  @click="viewParticipants(activity.activityId)"
                >
                  <i class="fas fa-users"></i> 报名情况
                </button>
                <button class="btn btn-danger" @click="deleteActivity(activity.activityId)">
                  <i class="fas fa-trash"></i> {{ isAdmin ? '删除活动' : '删除' }}
                </button>
              </template>

              <!-- PUBLISHED -->
              <template v-if="activity.status === 'PUBLISHED'">
                <button class="btn btn-success" @click="generateQRCode(activity.activityId)">
                  <i class="fas fa-qrcode"></i> 签到码
                </button>
                <button class="btn btn-info" @click="viewParticipants(activity.activityId)">
                  <i class="fas fa-users"></i> 报名情况
                </button>
                <button class="btn btn-primary" @click="openReviewModal(activity.activityId)">
                  <i class="fas fa-user-check"></i> 报名审核
                </button>
                <button class="btn btn-warning" @click="endActivity(activity.activityId)">
                  <i class="fas fa-stop"></i> 结束活动
                </button>
                <button class="btn btn-danger" @click="deleteActivity(activity.activityId)">
                  <i class="fas fa-trash"></i> 删除活动
                </button>
              </template>

              <!-- REJECTED -->
              <template v-if="activity.status === 'REJECTED'">
                <button
                  v-if="isAdmin"
                  class="btn btn-info"
                  @click="viewParticipants(activity.activityId)"
                >
                  <i class="fas fa-users"></i> 报名情况
                </button>
                <button class="btn btn-danger" @click="deleteActivity(activity.activityId)">
                  <i class="fas fa-trash"></i> {{ isAdmin ? '删除活动' : '删除' }}
                </button>
              </template>

              <!-- ENDED -->
              <template v-if="activity.status === 'ENDED'">
                <button class="btn btn-info" @click="viewParticipants(activity.activityId)">
                  <i class="fas fa-users"></i> 查看参与情况
                </button>
                <button class="btn btn-primary" @click="exportPDF(activity.activityId)">
                  <i class="fas fa-file-download"></i> 导出PDF
                </button>
                <button class="btn btn-danger" @click="deleteActivity(activity.activityId)">
                  <i class="fas fa-trash"></i> 删除记录
                </button>
              </template>
            </div>
          </div>
        </div>

        <!-- Empty State -->
        <div v-else class="empty-state">
          <i class="fas fa-calendar-plus"></i>
          <p>暂无活动记录</p>
          <p class="subtext">{{ isAdmin ? '目前系统中没有任何活动' : '创建第一个活动，开始您的社团管理之旅' }}</p>
        </div>
      </div>
    </div>

    <!-- Participant Statistics Modal -->
    <div v-if="showParticipantModal" class="modal-overlay" @click.self="closeParticipantModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3><i class="fas fa-chart-bar"></i> 活动报名情况统计</h3>
          <span class="close-btn" @click="closeParticipantModal">&times;</span>
        </div>
        <div class="modal-body" ref="modalBodyRef">
          <!-- Activity Info -->
          <div class="modal-activity-info">
            <div class="modal-info-row">
              <span class="modal-info-label">活动名称:</span>
              <span class="modal-info-value">{{ modalActivity.activityName }}</span>
            </div>
            <div class="modal-info-row">
              <span class="modal-info-label">活动时间:</span>
              <span class="modal-info-value">{{ formatDateTime(modalActivity.activityTime) }}</span>
            </div>
            <div class="modal-info-row">
              <span class="modal-info-label">活动地点:</span>
              <span class="modal-info-value">{{ modalActivity.location }}</span>
            </div>
          </div>

          <!-- Stats Summary -->
          <div class="stats-summary">
            <div class="summary-item">
              <div class="summary-label">总报名人数</div>
              <div class="summary-value">{{ modalParticipants.length }}人</div>
            </div>
            <div class="summary-item">
              <div class="summary-label">已签到人数</div>
              <div class="summary-value">{{ checkedInCount }}人</div>
            </div>
            <div class="summary-item">
              <div class="summary-label">签到率</div>
              <div class="summary-value">{{ checkInRate }}%</div>
            </div>
          </div>

          <!-- College Breakdown -->
          <div class="stats-details">
            <h4><i class="fas fa-university"></i> 各学院报名情况</h4>
            <div v-for="(stat, college) in collegeStats" :key="college" class="stat-item">
              <div class="stat-label">{{ college }}</div>
              <div class="stat-bar-container">
                <div class="stat-bar" :style="{ width: stat.percentage + '%' }"></div>
              </div>
              <div class="stat-value">{{ stat.count }}人 ({{ stat.percentage }}%)</div>
            </div>
          </div>

          <!-- Participant List -->
          <div class="participants-section">
            <h4><i class="fas fa-users"></i> 报名学生名单 ({{ modalParticipants.length }}人)</h4>
            <div class="participants-grid">
              <div
                v-for="p in modalParticipants"
                :key="p.studentId"
                class="participant-card"
              >
                <div class="participant-info">
                  <div class="participant-name">{{ p.name }}</div>
                  <div class="participant-detail">学号: {{ p.studentId }}</div>
                  <div class="participant-detail">学院: {{ p.college || '未填写' }}</div>
                </div>
                <div :class="['participant-status', p.checkInStatus ? 'status-checked' : 'status-not-checked']">
                  <i :class="p.checkInStatus ? 'fas fa-check' : 'fas fa-times'"></i>
                  {{ p.checkInStatus ? '已签到' : '未签到' }}
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-primary" @click="closeParticipantModal">关闭</button>
        </div>
      </div>
    </div>

    <!-- QR Code Modal -->
    <div v-if="showQRModal" class="modal-overlay" @click.self="closeQRModal">
      <div class="modal-content qr-modal">
        <div class="modal-header">
          <h3><i class="fas fa-qrcode"></i> 签到二维码</h3>
          <span class="close-btn" @click="closeQRModal">&times;</span>
        </div>
        <div class="modal-body qr-body">
          <div v-if="qrLoading" class="loading-state">正在生成二维码...</div>
          <div v-else-if="qrCodeUrl" class="qr-code-wrap">
            <img :src="qrCodeUrl" alt="签到二维码" class="qr-code-img" />
            <p class="qr-hint">请使用手机扫描二维码进行签到</p>
          </div>
          <div v-else class="empty-state">
            <p>二维码生成失败</p>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-primary" @click="closeQRModal">关闭</button>
        </div>
      </div>
    </div>

    <!-- Registration Review Modal -->
    <div v-if="showReviewModal" class="modal-overlay" @click.self="closeReviewModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3><i class="fas fa-user-check"></i> 报名审核</h3>
          <span class="close-btn" @click="closeReviewModal">&times;</span>
        </div>
        <div class="modal-body">
          <div v-if="reviewLoading" class="loading-state">加载中...</div>
          <template v-else>
            <!-- Pending Registrations -->
            <div v-if="pendingRegistrations.length > 0" class="review-section">
              <h4><i class="fas fa-clock"></i> 待审核 ({{ pendingRegistrations.length }}人)</h4>
              <div class="review-list">
                <div v-for="p in pendingRegistrations" :key="p.registrationId" class="review-card">
                  <div class="review-info">
                    <div class="review-name">{{ p.name }}</div>
                    <div class="review-detail">学号: {{ p.studentId }}</div>
                    <div class="review-detail">学院: {{ p.college || '未填写' }}</div>
                    <div class="review-detail">报名时间: {{ formatDateTime(p.registrationTime) }}</div>
                  </div>
                  <div class="review-actions">
                    <button class="btn btn-approve" @click="approveRegistration(p.registrationId)">
                      <i class="fas fa-check"></i> 通过
                    </button>
                    <button class="btn btn-reject" @click="showRejectInput(p.registrationId)">
                      <i class="fas fa-times"></i> 拒绝
                    </button>
                  </div>
                  <!-- Reject Reason Input -->
                  <div v-if="rejectingId === p.registrationId" class="reject-input-area">
                    <textarea
                      v-model="rejectReason"
                      placeholder="请输入拒绝理由..."
                      rows="2"
                      class="reject-textarea"
                    ></textarea>
                    <div class="reject-input-actions">
                      <button class="btn btn-reject btn-sm" @click="confirmReject(p.registrationId)">
                        <i class="fas fa-check"></i> 确认拒绝
                      </button>
                      <button class="btn btn-secondary btn-sm" @click="cancelReject">
                        <i class="fas fa-times"></i> 取消
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <div v-else class="empty-review">
              <i class="fas fa-check-circle"></i>
              <p>暂无待审核的报名</p>
            </div>

            <!-- Approved Registrations -->
            <div v-if="approvedRegistrations.length > 0" class="review-section">
              <h4><i class="fas fa-check-circle"></i> 已通过 ({{ approvedRegistrations.length }}人)</h4>
              <div class="review-list">
                <div v-for="p in approvedRegistrations" :key="p.registrationId" class="review-card approved-card">
                  <div class="review-info">
                    <div class="review-name">{{ p.name }}</div>
                    <div class="review-detail">学号: {{ p.studentId }}</div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Rejected Registrations -->
            <div v-if="rejectedRegistrations.length > 0" class="review-section">
              <h4><i class="fas fa-times-circle"></i> 已拒绝 ({{ rejectedRegistrations.length }}人)</h4>
              <div class="review-list">
                <div v-for="p in rejectedRegistrations" :key="p.registrationId" class="review-card rejected-card">
                  <div class="review-info">
                    <div class="review-name">{{ p.name }}</div>
                    <div class="review-detail">学号: {{ p.studentId }}</div>
                    <div v-if="p.rejectReason" class="review-reject-reason">
                      <i class="fas fa-exclamation-circle"></i> {{ p.rejectReason }}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </template>
        </div>
        <div class="modal-footer">
          <button class="btn btn-primary" @click="closeReviewModal">关闭</button>
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
const authStore = useAuthStore()
const toast = useToastStore()

// State
const loading = ref(true)
const allActivities = ref([])
const clubs = ref([])
const searchText = ref('')
const statusFilter = ref('')
const clubFilter = ref('')

// Participant modal state
const showParticipantModal = ref(false)
const modalActivity = ref({})
const modalParticipants = ref([])
const modalBodyRef = ref(null)

// QR modal state
const showQRModal = ref(false)
const qrLoading = ref(false)
const qrCodeUrl = ref('')

// Review modal state
const showReviewModal = ref(false)
const reviewLoading = ref(false)
const reviewRegistrations = ref([])
const reviewActivityId = ref(null)
const rejectingId = ref(null)
const rejectReason = ref('')

// Computed
const isAdmin = computed(() => authStore.userRole === 'ADMIN')

const filteredActivities = computed(() => {
  let list = [...allActivities.value]

  if (searchText.value) {
    const term = searchText.value.toLowerCase()
    list = list.filter(a =>
      (a.activityName && a.activityName.toLowerCase().includes(term)) ||
      (a.clubName && a.clubName.toLowerCase().includes(term)) ||
      (a.location && a.location.toLowerCase().includes(term))
    )
  }

  if (statusFilter.value) {
    list = list.filter(a => a.status === statusFilter.value)
  }

  if (clubFilter.value) {
    list = list.filter(a => String(a.clubId) === clubFilter.value)
  }

  return list
})

const checkedInCount = computed(() => {
  return modalParticipants.value.filter(p => p.checkInStatus).length
})

const checkInRate = computed(() => {
  if (modalParticipants.value.length === 0) return 0
  return Math.round((checkedInCount.value / modalParticipants.value.length) * 100)
})

const collegeStats = computed(() => {
  const stats = {}
  modalParticipants.value.forEach(p => {
    const college = p.college || '未填写'
    stats[college] = stats[college] || { count: 0 }
    stats[college].count++
  })
  const total = modalParticipants.value.length || 1
  Object.keys(stats).forEach(key => {
    stats[key].percentage = Math.round((stats[key].count / total) * 100)
  })
  return stats
})

const pendingRegistrations = computed(() => {
  return reviewRegistrations.value.filter(r => r.status === 'PENDING')
})

const approvedRegistrations = computed(() => {
  return reviewRegistrations.value.filter(r => r.status === 'APPROVED')
})

const rejectedRegistrations = computed(() => {
  return reviewRegistrations.value.filter(r => r.status === 'REJECTED')
})

// Debounce helper
let searchTimeout = null
function debouncedApplyFilters() {
  clearTimeout(searchTimeout)
  searchTimeout = setTimeout(applyFilters, 300)
}

function applyFilters() {
  // filteredActivities is a computed, auto-updates
}

function resetFilters() {
  searchText.value = ''
  statusFilter.value = ''
  clubFilter.value = ''
}

// Navigation
function goBack() {
  if (isAdmin.value) {
    router.push('/admin-dashboard')
  } else {
    router.push('/dashboard')
  }
}

// Status helpers
function statusClass(status) {
  const map = {
    PENDING: 'status-pending',
    PUBLISHED: 'status-published',
    REJECTED: 'status-rejected',
    ENDED: 'status-ended'
  }
  return map[status] || ''
}

function statusLabel(status) {
  const map = {
    PENDING: '待审核',
    PUBLISHED: '已发布',
    REJECTED: '已驳回',
    ENDED: '已结束'
  }
  return map[status] || status
}

function formatDateTime(dateStr) {
  if (!dateStr) return '未设置'
  const d = new Date(dateStr)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  return `${year}/${month}/${day} ${hours}:${minutes}`
}

// Load activities
async function loadActivities() {
  loading.value = true
  try {
    const url = isAdmin.value
      ? '/api/activity-manage/all-activities'
      : '/api/activity-manage/my-activities'
    const res = await api.get(url)
    allActivities.value = res.data.data || []
  } catch (e) {
    console.error('加载活动列表错误:', e)
    allActivities.value = []
  }
  loading.value = false
}

// Load clubs (admin only)
async function loadClubs() {
  if (!isAdmin.value) return
  try {
    const res = await api.get('/api/clubs')
    if (res.data.success && res.data.data) {
      clubs.value = res.data.data
    }
  } catch (e) {
    console.error('加载社团数据失败:', e)
  }
}

// End activity
async function endActivity(activityId) {
  if (!confirm('确定要结束这个活动吗？结束后将无法再报名和签到，系统会为已签到的学生发放积分。')) {
    return
  }
  try {
    const res = await api.put(`/api/activity-manage/${activityId}/end`)
    if (res.data.success) {
      toast.success('活动结束成功！已为签到学生发放积分。')
      loadActivities()
    } else {
      toast.error('结束活动失败: ' + res.data.message)
    }
  } catch (e) {
    console.error('结束活动错误:', e)
    toast.error('结束活动时发生错误')
  }
}

// Delete activity
async function deleteActivity(activityId) {
  if (!confirm('警告：确定要强制删除这个活动吗？此操作不可恢复，将删除所有相关数据（包括报名记录和签到信息）。')) {
    return
  }
  try {
    const res = await api.delete(`/api/activity-manage/${activityId}`)
    if (res.data.success) {
      toast.success('活动删除成功')
      loadActivities()
    } else {
      toast.error('删除失败: ' + res.data.message)
    }
  } catch (e) {
    console.error('删除活动错误:', e)
    toast.error('删除时发生错误，请稍后重试')
  }
}

// Generate QR code
async function generateQRCode(activityId) {
  showQRModal.value = true
  qrLoading.value = true
  qrCodeUrl.value = ''
  try {
    const res = await api.get(`/api/checkin/activity/${activityId}/qrcode`)
    if (res.data.success) {
      qrCodeUrl.value = res.data.data || res.data.qrcode || ''
    } else {
      toast.error('生成二维码失败: ' + res.data.message)
    }
  } catch (e) {
    console.error('生成二维码错误:', e)
    toast.error('生成二维码时发生错误')
  }
  qrLoading.value = false
}

function closeQRModal() {
  showQRModal.value = false
  qrCodeUrl.value = ''
}

// View participants
async function viewParticipants(activityId) {
  try {
    const activityRes = await api.get(`/api/activities/${activityId}`)
    if (!activityRes.data.success) {
      toast.error('获取活动信息失败: ' + activityRes.data.message)
      return
    }
    modalActivity.value = activityRes.data.data

    const participantsRes = await api.get(`/api/activities/${activityId}/participants`)
    if (!participantsRes.data.success) {
      toast.error('获取报名学生失败: ' + participantsRes.data.message)
      return
    }
    modalParticipants.value = participantsRes.data.data || []
    showParticipantModal.value = true
  } catch (e) {
    console.error('查看报名情况错误:', e)
    toast.error('查看报名情况时发生错误，请稍后重试')
  }
}

function closeParticipantModal() {
  showParticipantModal.value = false
  modalActivity.value = {}
  modalParticipants.value = []
}

// Open review modal
async function openReviewModal(activityId) {
  showReviewModal.value = true
  reviewLoading.value = true
  reviewActivityId.value = activityId
  rejectingId.value = null
  rejectReason.value = ''
  try {
    const res = await api.get(`/api/activity-manage/activity/${activityId}/registrations`)
    if (res.data.success) {
      reviewRegistrations.value = res.data.data || []
    } else {
      toast.error('获取报名数据失败: ' + res.data.message)
    }
  } catch (e) {
    console.error('获取报名数据错误:', e)
    toast.error('获取报名数据时发生错误')
  }
  reviewLoading.value = false
}

function closeReviewModal() {
  showReviewModal.value = false
  reviewRegistrations.value = []
  reviewActivityId.value = null
  rejectingId.value = null
  rejectReason.value = ''
}

// Approve registration
async function approveRegistration(registrationId) {
  try {
    const res = await api.put(`/api/activity-manage/registrations/${registrationId}/approve`)
    if (res.data.success) {
      toast.success('已通过报名')
      // Refresh review list
      const reviewRes = await api.get(`/api/activity-manage/activity/${reviewActivityId.value}/registrations`)
      if (reviewRes.data.success) {
        reviewRegistrations.value = reviewRes.data.data || []
      }
    } else {
      toast.error('操作失败: ' + res.data.message)
    }
  } catch (e) {
    console.error('通过报名错误:', e)
    toast.error('操作时发生错误')
  }
}

// Show reject input
function showRejectInput(registrationId) {
  rejectingId.value = registrationId
  rejectReason.value = ''
}

// Cancel reject
function cancelReject() {
  rejectingId.value = null
  rejectReason.value = ''
}

// Confirm reject with reason
async function confirmReject(registrationId) {
  if (!rejectReason.value.trim()) {
    toast.warning('请输入拒绝理由')
    return
  }
  try {
    const res = await api.put(`/api/activity-manage/registrations/${registrationId}/reject`, {
      reason: rejectReason.value.trim()
    })
    if (res.data.success) {
      toast.success('已拒绝报名')
      rejectingId.value = null
      rejectReason.value = ''
      // Refresh review list
      const reviewRes = await api.get(`/api/activity-manage/activity/${reviewActivityId.value}/registrations`)
      if (reviewRes.data.success) {
        reviewRegistrations.value = reviewRes.data.data || []
      }
    } else {
      toast.error('操作失败: ' + res.data.message)
    }
  } catch (e) {
    console.error('拒绝报名错误:', e)
    toast.error('操作时发生错误')
  }
}

// Export PDF
async function exportPDF(activityId) {
  const loadingEl = document.createElement('div')
  loadingEl.className = 'pdf-loading-toast'
  loadingEl.textContent = '正在生成PDF...'
  loadingEl.style.cssText = 'position:fixed;top:20px;right:20px;background:#667eea;color:white;padding:10px 20px;border-radius:5px;z-index:9999;font-size:14px;'
  document.body.appendChild(loadingEl)

  try {
    const { jsPDF } = await import('jspdf')
    const { default: html2canvas } = await import('html2canvas')

    // Get activity detail
    const activityRes = await api.get(`/api/activities/${activityId}`)
    if (!activityRes.data.success) throw new Error('获取活动详情失败')
    const activity = activityRes.data.data

    // Get registrations
    const regRes = await api.get(`/api/activity-manage/activity/${activityId}/registrations`)
    const participants = regRes.data.success ? regRes.data.data : []

    // Build hidden HTML element for PDF rendering
    const pdfContent = document.createElement('div')
    pdfContent.style.cssText = 'position:absolute;top:-10000px;left:-10000px;width:800px;padding:20px;background:white;font-family:Arial,sans-serif;font-size:12px;'

    let htmlContent = `
      <div style="text-align:center;background:#667eea;color:white;padding:20px;margin-bottom:30px;">
        <h1 style="margin:0;font-size:24px;">活动详情报告</h1>
      </div>
      <div style="margin-bottom:30px;">
        <h2 style="font-size:18px;margin-bottom:10px;border-bottom:2px solid #ddd;padding-bottom:5px;">活动基本信息</h2>
        <table style="width:100%;border-collapse:collapse;">
          <tr><td style="width:150px;font-weight:bold;">活动名称：</td><td>${activity.activityName || '未设置'}</td></tr>
          <tr><td style="font-weight:bold;">活动类型：</td><td>${activity.activityType || '未设置'}</td></tr>
          <tr><td style="font-weight:bold;">主办社团：</td><td>${activity.clubName || '未知社团'}</td></tr>
          <tr><td style="font-weight:bold;">活动时间：</td><td>${formatDateTime(activity.activityTime)}</td></tr>
          <tr><td style="font-weight:bold;">活动地点：</td><td>${activity.location || '未设置'}</td></tr>
          <tr><td style="font-weight:bold;">人数限制：</td><td>${activity.maxParticipants === 0 ? '无限制' : activity.maxParticipants + '人'}</td></tr>
          <tr><td style="font-weight:bold;">积分奖励：</td><td>${activity.awardPoints} 积分</td></tr>
          <tr><td style="font-weight:bold;">活动状态：</td><td>已结束</td></tr>
          <tr><td style="font-weight:bold;">创建时间：</td><td>${formatDateTime(activity.createdAt)}</td></tr>
        </table>
      </div>
      <div style="margin-bottom:30px;">
        <h2 style="font-size:18px;margin-bottom:10px;border-bottom:2px solid #ddd;padding-bottom:5px;">活动描述</h2>
        <div style="white-space:pre-wrap;">${activity.description || '暂无详细描述'}</div>
      </div>
      <div>
        <h2 style="font-size:18px;margin-bottom:10px;border-bottom:2px solid #ddd;padding-bottom:5px;">参与情况</h2>
        <p><strong>总参与人数：</strong>${participants.length}人</p>
        <table style="width:100%;border-collapse:collapse;margin-top:15px;">
          <thead>
            <tr style="background:#f5f5f5;">
              <th style="border:1px solid #ddd;padding:8px;text-align:left;width:50px;">序号</th>
              <th style="border:1px solid #ddd;padding:8px;text-align:left;width:120px;">学号</th>
              <th style="border:1px solid #ddd;padding:8px;text-align:left;width:100px;">姓名</th>
              <th style="border:1px solid #ddd;padding:8px;text-align:left;width:150px;">学院</th>
              <th style="border:1px solid #ddd;padding:8px;text-align:left;">签到状态</th>
            </tr>
          </thead>
          <tbody>`

    participants.forEach((p, i) => {
      htmlContent += `
            <tr>
              <td style="border:1px solid #ddd;padding:8px;">${i + 1}</td>
              <td style="border:1px solid #ddd;padding:8px;">${p.studentId}</td>
              <td style="border:1px solid #ddd;padding:8px;">${p.name || '未知姓名'}</td>
              <td style="border:1px solid #ddd;padding:8px;">${p.college || '未知学院'}</td>
              <td style="border:1px solid #ddd;padding:8px;">${p.checkInStatus ? '已签到' : '未签到'}</td>
            </tr>`
    })

    htmlContent += `
          </tbody>
        </table>
      </div>
      <div style="margin-top:50px;text-align:center;color:#999;font-size:10px;">
        社团管理系统 - 活动详情报告
      </div>`

    pdfContent.innerHTML = htmlContent
    document.body.appendChild(pdfContent)

    const canvas = await html2canvas(pdfContent, { scale: 2, useCORS: true })

    const pdf = new jsPDF({ orientation: 'portrait', unit: 'mm', format: 'a4' })
    const imgWidth = 210
    const imgHeight = (canvas.height * imgWidth) / canvas.width

    pdf.addImage(canvas.toDataURL('image/png'), 'PNG', 0, 0, imgWidth, imgHeight)

    const fileName = `活动详情_${activity.activityName || activity.activityId}_${new Date().toLocaleDateString('zh-CN').replace(/\//g, '-')}.pdf`
    pdf.save(fileName)

    document.body.removeChild(pdfContent)
    document.body.removeChild(loadingEl)
    toast.success('PDF生成成功！')
  } catch (e) {
    console.error('生成PDF错误:', e)
    toast.error('生成PDF时发生错误，请稍后重试')
    if (loadingEl.parentNode) {
      document.body.removeChild(loadingEl)
    }
  }
}

// Init
onMounted(async () => {
  if (!authStore.token) {
    router.push('/login')
    return
  }
  await loadActivities()
  await loadClubs()
})
</script>

<style scoped>
/* CSS Variables - unified design system */
.manage-activities-page {
  --primary-color: #667eea;
  --secondary-color: #764ba2;
  --success-color: #4ade80;
  --warning-color: #fbbf24;
  --danger-color: #ef4444;
  --info-color: #06b6d4;
  --light-color: #f8fafc;
  --dark-color: #1e293b;
  --text-color: #334155;
  --text-light: #64748b;
  --card-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
  --card-shadow-hover: 0 20px 40px rgba(0, 0, 0, 0.15);
  --transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  --border-radius: 20px;
  --border-radius-sm: 12px;
  --border-radius-lg: 25px;

  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  min-height: 100vh;
  color: var(--text-color);
  overflow-x: hidden;
  line-height: 1.6;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

/* Scrollbar */
.manage-activities-page::-webkit-scrollbar {
  width: 8px;
}

.manage-activities-page::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.manage-activities-page::-webkit-scrollbar-thumb {
  background: var(--primary-color);
  border-radius: 4px;
}

.manage-activities-page::-webkit-scrollbar-thumb:hover {
  background: var(--secondary-color);
}

/* Container */
.container {
  max-width: 1200px;
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
  padding: 25px 30px;
  border-radius: var(--border-radius);
  box-shadow: var(--card-shadow);
  border-top: 4px solid var(--primary-color);
  transition: var(--transition);
}

.header:hover {
  transform: translateY(-3px);
  box-shadow: var(--card-shadow-hover);
}

.header h1 {
  color: var(--dark-color);
  font-size: 28px;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 12px;
}

.header h1 i {
  color: var(--primary-color);
  font-size: 32px;
}

/* Card */
.card {
  background: white;
  border-radius: var(--border-radius);
  padding: 30px;
  margin-bottom: 25px;
  box-shadow: var(--card-shadow);
  border: 1px solid #f0f0f0;
  transition: var(--transition);
  position: relative;
  overflow: hidden;
}

.card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 3px;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
}

.card:hover {
  transform: translateY(-5px);
  box-shadow: var(--card-shadow-hover);
}

.card h3 {
  color: var(--dark-color);
  margin-bottom: 20px;
  font-size: 22px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 10px;
}

.card h3 i {
  color: var(--primary-color);
  font-size: 24px;
}

/* Filter Bar */
.filter-bar {
  display: flex;
  gap: 15px;
  margin-bottom: 25px;
  flex-wrap: wrap;
}

.search-box {
  flex: 1;
  min-width: 300px;
}

.search-input-wrap {
  position: relative;
}

.search-input {
  width: 100%;
  padding: 12px 40px 12px 16px;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  font-size: 14px;
  transition: var(--transition);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  outline: none;
}

.search-input:focus {
  border-color: var(--primary-color);
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.2);
}

.search-icon {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  color: #94a3b8;
  font-size: 16px;
}

.filter-controls {
  display: flex;
  gap: 15px;
  align-items: flex-end;
  flex-wrap: wrap;
}

.filter-group {
  display: flex;
  flex-direction: column;
}

.filter-label {
  display: block;
  margin-bottom: 8px;
  font-weight: 600;
  color: var(--dark-color);
  font-size: 14px;
}

.filter-select {
  padding: 12px 16px;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  font-size: 14px;
  background: white;
  color: var(--text-color);
  cursor: pointer;
  transition: var(--transition);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  outline: none;
}

.filter-select:focus {
  border-color: var(--primary-color);
}

.filter-reset {
  justify-content: flex-end;
}

/* Buttons */
.btn {
  padding: 12px 24px;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: var(--transition);
  display: inline-flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
  position: relative;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s ease;
}

.btn:hover::before {
  left: 100%;
}

.btn:hover {
  transform: translateY(-2px);
}

.btn-primary {
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
  color: white;
}

.btn-primary:hover {
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
}

.btn-success {
  background: linear-gradient(135deg, var(--success-color), #22c55e);
  color: white;
}

.btn-success:hover {
  box-shadow: 0 8px 20px rgba(74, 222, 128, 0.4);
}

.btn-danger {
  background: linear-gradient(135deg, var(--danger-color), #dc2626);
  color: white;
}

.btn-danger:hover {
  box-shadow: 0 8px 20px rgba(239, 68, 68, 0.4);
}

.btn-warning {
  background: linear-gradient(135deg, var(--warning-color), #f59e0b);
  color: white;
}

.btn-warning:hover {
  box-shadow: 0 8px 20px rgba(251, 191, 36, 0.4);
}

.btn-info {
  background: linear-gradient(135deg, var(--info-color), #0891b2);
  color: white;
}

.btn-info:hover {
  box-shadow: 0 8px 20px rgba(6, 182, 212, 0.4);
}

/* Loading */
.loading-state {
  text-align: center;
  padding: 60px 20px;
  color: var(--text-light);
  font-size: 16px;
}

/* Activity Grid */
.activity-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
  gap: 25px;
}

/* Activity Card */
.activity-card {
  background: white;
  border-radius: var(--border-radius);
  padding: 25px;
  box-shadow: var(--card-shadow);
  border: 1px solid #f0f0f0;
  transition: var(--transition);
  position: relative;
  overflow: hidden;
  animation: fadeInUp 0.5s ease;
}

.activity-card:hover {
  transform: translateY(-5px) scale(1.02);
  box-shadow: var(--card-shadow-hover);
}

.activity-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
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

/* Activity Header */
.activity-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.activity-title {
  font-size: 19px;
  font-weight: 700;
  color: var(--dark-color);
  line-height: 1.4;
  margin-bottom: 5px;
}

.activity-club {
  font-size: 14px;
  color: var(--text-light);
  font-weight: 500;
}

/* Status Badge */
.activity-status {
  padding: 8px 16px;
  border-radius: 25px;
  font-size: 13px;
  font-weight: 600;
  white-space: nowrap;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  border: 2px solid transparent;
}

.status-pending {
  background: #fff3cd;
  color: #856404;
  border-color: #ffeaa7;
}

.status-published {
  background: #d4edda;
  color: #155724;
  border-color: #c3e6cb;
}

.status-rejected {
  background: #f8d7da;
  color: #721c24;
  border-color: #f5c6cb;
}

.status-ended {
  background: #e9ecef;
  color: #495057;
  border-color: #dee2e6;
}

/* Activity Info */
.activity-info {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 15px;
  margin-bottom: 20px;
  padding: 20px;
  background: var(--light-color);
  border-radius: var(--border-radius-sm);
}

.info-item {
  display: flex;
  align-items: center;
  gap: 12px;
  color: var(--text-light);
  font-size: 14px;
  font-weight: 500;
}

.info-item i {
  color: var(--primary-color);
  width: 20px;
  text-align: center;
  font-size: 16px;
}

/* Activity Description */
.activity-description {
  color: var(--text-light);
  margin-bottom: 20px;
  line-height: 1.6;
  font-size: 14px;
  padding: 15px;
  background: #f0f4f8;
  border-radius: var(--border-radius-sm);
  border-left: 3px solid var(--primary-color);
}

/* Reject Reason */
.reject-reason {
  background: #fef2f2;
  border: 1px solid #fee2e2;
  border-left: 4px solid var(--danger-color);
  padding: 18px;
  border-radius: var(--border-radius-sm);
  margin: 15px 0;
  font-size: 14px;
}

.reject-reason strong {
  color: var(--danger-color);
  display: block;
  margin-bottom: 8px;
}

/* Activity Actions */
.activity-actions {
  display: flex;
  gap: 12px;
  margin-top: 25px;
  flex-wrap: wrap;
}

/* Empty State */
.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: var(--text-light);
  background: var(--light-color);
  border-radius: var(--border-radius);
  border: 2px dashed #cbd5e1;
}

.empty-state i {
  font-size: 72px;
  margin-bottom: 25px;
  color: #cbd5e1;
  animation: pulse 2s infinite ease-in-out;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

.empty-state p {
  font-size: 18px;
  margin-bottom: 10px;
  font-weight: 600;
  color: var(--text-color);
}

.empty-state .subtext {
  font-size: 15px;
  color: var(--text-light);
}

/* Modal */
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
}

.modal-content {
  background-color: white;
  border-radius: 15px;
  width: 90%;
  max-width: 700px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.3);
}

.modal-header {
  padding: 20px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  margin: 0;
  color: #333;
  font-size: 20px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
}

.close-btn {
  font-size: 28px;
  cursor: pointer;
  color: #999;
  line-height: 1;
  transition: color 0.2s;
}

.close-btn:hover {
  color: #333;
}

.modal-body {
  padding: 20px;
}

.modal-footer {
  padding: 15px 20px;
  border-top: 1px solid #eee;
  text-align: right;
}

/* Modal Activity Info */
.modal-activity-info {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 10px;
}

.modal-info-row {
  display: flex;
  margin-bottom: 10px;
}

.modal-info-row:last-child {
  margin-bottom: 0;
}

.modal-info-label {
  width: 100px;
  font-weight: 500;
  flex-shrink: 0;
}

.modal-info-value {
  flex: 1;
}

/* Stats Summary */
.stats-summary {
  display: flex;
  justify-content: space-around;
  margin-bottom: 25px;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 10px;
}

.summary-item {
  text-align: center;
}

.summary-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 5px;
}

.summary-value {
  font-size: 24px;
  font-weight: bold;
  color: #667eea;
}

/* Stats Details (College Breakdown) */
.stats-details {
  margin-bottom: 25px;
}

.stats-details h4 {
  margin-bottom: 15px;
  color: #333;
  display: flex;
  align-items: center;
  gap: 8px;
}

.stat-item {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.stat-label {
  width: 120px;
  font-weight: 500;
  font-size: 14px;
  flex-shrink: 0;
}

.stat-bar-container {
  flex: 1;
  height: 20px;
  background-color: #f0f0f0;
  border-radius: 10px;
  margin: 0 15px;
  overflow: hidden;
}

.stat-bar {
  height: 100%;
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
  border-radius: 10px;
  transition: width 0.6s ease;
}

.stat-value {
  width: 100px;
  text-align: right;
  font-weight: 500;
  font-size: 14px;
  flex-shrink: 0;
}

/* Participants Section */
.participants-section {
  margin-top: 25px;
}

.participants-section h4 {
  margin-bottom: 15px;
  color: #333;
  display: flex;
  align-items: center;
  gap: 8px;
}

.participants-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 15px;
  max-height: 300px;
  overflow-y: auto;
  padding: 10px;
  background-color: #f8f9fa;
  border-radius: 10px;
}

.participant-card {
  background-color: white;
  padding: 15px;
  border-radius: 10px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.05);
  border-left: 4px solid var(--primary-color);
  transition: all 0.3s ease;
}

.participant-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.participant-name {
  font-weight: bold;
  font-size: 16px;
  margin-bottom: 5px;
  color: #333;
}

.participant-detail {
  font-size: 14px;
  color: #666;
  margin-bottom: 3px;
}

.participant-status {
  margin-top: 10px;
  padding: 5px 10px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
  display: inline-block;
}

.status-checked {
  background-color: #d4edda;
  color: #155724;
}

.status-not-checked {
  background-color: #f8d7da;
  color: #721c24;
}

/* Registration Status Badges */
.participant-badges {
  display: flex;
  gap: 8px;
  margin-top: 10px;
  flex-wrap: wrap;
  align-items: center;
}

.registration-status-badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  display: inline-block;
}

.badge-pending {
  background-color: #fff3cd;
  color: #856404;
}

.badge-approved {
  background-color: #d4edda;
  color: #155724;
}

.badge-rejected {
  background-color: #f8d7da;
  color: #721c24;
}

/* Participant Actions */
.participant-actions {
  display: flex;
  gap: 8px;
  margin-top: 10px;
}

.btn-approve {
  background: #28a745;
  color: white;
  padding: 6px 14px;
  font-size: 12px;
  border-radius: 6px;
  flex: 1;
}

.btn-approve:hover {
  background: #218838;
}

.btn-reject {
  background: #dc3545;
  color: white;
  padding: 6px 14px;
  font-size: 12px;
  border-radius: 6px;
  flex: 1;
}

.btn-reject:hover {
  background: #c82333;
}

/* QR Modal */
.qr-modal {
  max-width: 400px;
}

.qr-body {
  text-align: center;
}

.qr-code-wrap {
  padding: 20px;
}

.qr-code-img {
  max-width: 250px;
  border: 2px solid #eee;
  border-radius: 8px;
}

.qr-hint {
  margin-top: 15px;
  color: #666;
  font-size: 14px;
}

/* Review Modal */
.review-section {
  margin-bottom: 20px;
}

.review-section h4 {
  color: var(--dark-color);
  margin-bottom: 12px;
  font-size: 16px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
}

.review-section h4 i {
  color: var(--primary-color);
}

.review-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.review-card {
  background-color: white;
  padding: 15px;
  border-radius: 10px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.05);
  border-left: 4px solid var(--warning-color);
  transition: all 0.3s ease;
}

.review-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.approved-card {
  border-left-color: var(--success-color);
}

.rejected-card {
  border-left-color: var(--danger-color);
}

.review-info {
  flex: 1;
}

.review-name {
  font-weight: bold;
  font-size: 16px;
  margin-bottom: 5px;
  color: #333;
}

.review-detail {
  font-size: 14px;
  color: #666;
  margin-bottom: 3px;
}

.review-reject-reason {
  font-size: 13px;
  color: #dc3545;
  margin-top: 6px;
  padding: 6px 10px;
  background: #fef2f2;
  border-radius: 6px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.review-actions {
  display: flex;
  gap: 8px;
  margin-top: 12px;
}

.reject-input-area {
  margin-top: 12px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
}

.reject-textarea {
  width: 100%;
  padding: 10px;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 14px;
  resize: vertical;
  transition: all 0.3s ease;
}

.reject-textarea:focus {
  border-color: var(--primary-color);
  outline: none;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.reject-input-actions {
  display: flex;
  gap: 8px;
  margin-top: 10px;
  justify-content: flex-end;
}

.btn-sm {
  padding: 6px 14px;
  font-size: 12px;
}

.btn-secondary {
  background: #6c757d;
  color: white;
}

.btn-secondary:hover {
  background: #5a6268;
}

.empty-review {
  text-align: center;
  padding: 30px;
  color: #999;
}

.empty-review i {
  font-size: 48px;
  margin-bottom: 10px;
  opacity: 0.5;
}

.empty-review p {
  font-size: 16px;
}

/* Responsive */
@media (max-width: 768px) {
  .container {
    padding: 15px;
  }

  .header {
    flex-direction: column;
    gap: 15px;
    text-align: center;
    padding: 20px;
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
    width: 100%;
    justify-content: center;
  }

  .filter-bar {
    flex-direction: column;
  }

  .filter-controls {
    flex-direction: column;
    width: 100%;
  }

  .filter-select {
    width: 100%;
  }

  .stats-summary {
    flex-direction: column;
    gap: 15px;
  }

  .stat-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 5px;
  }

  .stat-bar-container {
    margin: 5px 0;
    width: 100%;
  }

  .stat-value {
    text-align: left;
    width: auto;
  }

  .stat-label {
    width: auto;
  }

  .participants-grid {
    grid-template-columns: 1fr;
  }

  .modal-content {
    width: 95%;
    max-height: 95vh;
  }
}
</style>
