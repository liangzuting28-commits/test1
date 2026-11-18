<template>
  <div class="club-manage-page">
    <div class="container">
      <!-- Header -->
      <div class="header">
        <h1><i class="fas fa-users"></i> 社团管理中心</h1>
        <button class="btn btn-primary" @click="router.push('/dashboard')">
          <i class="fas fa-arrow-left"></i> 返回仪表盘
        </button>
      </div>

      <!-- Tabs -->
      <div class="tabs">
        <div
          v-if="userRole !== 'TEACHER'"
          :class="['tab', { active: activeTab === 'my-clubs' }]"
          @click="switchTab('my-clubs')"
        >
          <i class="fas fa-list"></i> 我的社团
        </div>
        <div
          v-if="userRole === 'TEACHER'"
          :class="['tab', { active: activeTab === 'create-club' }]"
          @click="switchTab('create-club')"
        >
          <i class="fas fa-plus-circle"></i> 创建社团
        </div>
        <div
          :class="['tab', { active: activeTab === 'all-clubs' }]"
          @click="switchTab('all-clubs')"
        >
          <i class="fas fa-school"></i> 所有社团
        </div>
      </div>

      <!-- My Clubs Tab -->
      <div v-if="activeTab === 'my-clubs'" class="content-section">
        <div class="card">
          <h3>
            <i :class="userRole === 'PRESIDENT' ? 'fas fa-list' : 'fas fa-users'"></i>
            {{ userRole === 'PRESIDENT' ? '我管理的社团' : '我加入的社团' }}
          </h3>
          <div class="search-box">
            <div class="search-input-wrapper">
              <i class="fas fa-search"></i>
              <input
                type="text"
                v-model="myClubsSearch"
                placeholder="搜索我的社团..."
                @keyup.enter="filterMyClubs"
              />
              <button class="search-btn" @click="filterMyClubs"><i class="fas fa-search"></i> 搜索</button>
            </div>
          </div>
          <div v-if="myClubsLoading" class="loading-state">
            <div class="spinner"></div>
            <p>加载中...</p>
          </div>
          <div v-else-if="filteredMyClubs.length > 0" class="club-grid">
            <div
              v-for="club in filteredMyClubs"
              :key="club.clubId"
              class="club-card"
            >
              <div class="club-card-inner">
                <div class="club-header">
                  <div>
                    <div class="club-title">{{ club.clubName }}</div>
                    <div class="club-type">{{ club.clubType }}</div>
                  </div>
                  <div :class="['club-status', getStatusClass(club)]">
                    {{ getStatusText(club) }}
                  </div>
                </div>
                <div class="club-info">
                  <div class="info-item">
                    <i class="fas fa-user"></i>
                    <span>社长: {{ club.presidentDetail?.studentName || club.presidentName || club.presidentId }}</span>
                  </div>
                  <div class="info-item">
                    <i class="fas fa-calendar"></i>
                    <span>{{ userRole === 'PRESIDENT' ? '创建时间' : '加入时间' }}: {{ formatDateTime(club.createdAt || club.joinDate) }}</span>
                  </div>
                  <div class="info-item">
                    <i class="fas fa-phone"></i>
                    <span>联系方式: {{ club.contactInfo || '暂无' }}</span>
                  </div>
                </div>
                <div class="club-description">
                  {{ club.introduction || '暂无详细介绍' }}
                </div>
                <div class="club-actions">
                  <button class="btn btn-primary btn-sm" @click="viewClubDetail(club.clubId)">
                    <i class="fas fa-info-circle"></i> 详情
                  </button>
                  <template v-if="userRole === 'PRESIDENT'">
                    <button class="btn btn-success btn-sm" @click="goToMemberManage(club.clubId)">
                      <i class="fas fa-users"></i> 成员管理
                    </button>
                    <button class="btn btn-warning btn-sm" @click="editClub(club.clubId)">
                      <i class="fas fa-edit"></i> 编辑
                    </button>
                  </template>
                  <template v-if="userRole === 'STUDENT'">
                    <button
                      v-if="club.applicationStatus === 'APPROVED'"
                      class="btn btn-warning btn-sm"
                      @click="leaveClub(club.clubId)"
                    >
                      <i class="fas fa-sign-out-alt"></i> 申请退出
                    </button>
                    <button
                      v-else-if="club.applicationStatus === 'PENDING_LEAVE'"
                      class="btn btn-secondary btn-sm"
                      disabled
                    >
                      <i class="fas fa-clock"></i> 退出申请中
                    </button>
                    <button
                      v-else
                      class="btn btn-danger btn-sm"
                      @click="cancelApplication(club.clubId)"
                    >
                      <i class="fas fa-times"></i> 取消申请
                    </button>
                  </template>
                </div>
              </div>
            </div>
          </div>
          <div v-else class="empty-state">
            <i class="fas fa-users-slash"></i>
            <p v-if="userRole === 'PRESIDENT'">暂无管理的社团</p>
            <p v-else>您还未加入任何社团</p>
            <p class="empty-sub">{{ userRole === 'PRESIDENT' ? '创建第一个社团，开始您的社团管理之旅' : '快去"所有社团"页面申请加入感兴趣的社团吧！' }}</p>
          </div>
        </div>
      </div>

      <!-- All Clubs Tab -->
      <div v-if="activeTab === 'all-clubs'" class="content-section">
        <div class="card">
          <h3><i class="fas fa-school"></i> 所有社团</h3>
          <div class="search-box">
            <div class="search-input-wrapper">
              <i class="fas fa-search"></i>
              <input
                type="text"
                v-model="allClubsSearch"
                placeholder="搜索所有社团..."
                @keyup.enter="filterAllClubs"
              />
              <button class="search-btn" @click="filterAllClubs"><i class="fas fa-search"></i> 搜索</button>
            </div>
          </div>
          <div v-if="allClubsLoading" class="loading-state">
            <div class="spinner"></div>
            <p>加载中...</p>
          </div>
          <div v-else-if="filteredAllClubs.length > 0" class="club-grid">
            <div
              v-for="club in filteredAllClubs"
              :key="club.clubId"
              class="club-card"
            >
              <div class="club-card-inner">
                <div class="club-header">
                  <div>
                    <div class="club-title">{{ club.clubName }}</div>
                    <div class="club-type">{{ club.clubType }}</div>
                  </div>
                  <div class="club-status status-active">{{ club.status || 'ACTIVE' }}</div>
                </div>
                <div class="club-info">
                  <div class="info-item">
                    <i class="fas fa-user"></i>
                    <span>社长: {{ club.presidentDetail?.studentName || club.presidentId }}</span>
                  </div>
                  <div class="info-item">
                    <i class="fas fa-calendar"></i>
                    <span>创建时间: {{ formatDateTime(club.createdAt) }}</span>
                  </div>
                  <div class="info-item">
                    <i class="fas fa-phone"></i>
                    <span>联系方式: {{ club.contactInfo || '暂无' }}</span>
                  </div>
                </div>
                <div class="club-description">
                  {{ club.introduction || '暂无详细介绍' }}
                </div>
                <div class="club-actions">
                  <button class="btn btn-primary btn-sm" @click="viewClubDetail(club.clubId)">
                    <i class="fas fa-info-circle"></i> 详情
                  </button>
                  <button
                    v-if="userRole === 'STUDENT'"
                    class="btn btn-success btn-sm"
                    @click="applyForClub(club.clubId)"
                  >
                    <i class="fas fa-user-plus"></i> 申请加入
                  </button>
                  <button
                    v-if="userRole === 'TEACHER'"
                    class="btn btn-warning btn-sm"
                    @click="openManagePresident(club.clubId, club.presidentId, club.presidentDetail?.studentName || club.presidentId)"
                  >
                    <i class="fas fa-user-cog"></i> 管理社长
                  </button>
                  <button
                    v-if="userRole === 'TEACHER'"
                    class="btn btn-info btn-sm"
                    @click="exportClubToPDF(club.clubId)"
                  >
                    <i class="fas fa-file-pdf"></i> 导出PDF
                  </button>
                </div>
              </div>
            </div>
          </div>
          <div v-else class="empty-state">
            <i class="fas fa-school"></i>
            <p>暂无社团</p>
          </div>
        </div>
      </div>

      <!-- Create Club Tab -->
      <div v-if="activeTab === 'create-club'" class="content-section">
        <div class="card">
          <h3><i class="fas fa-plus-circle"></i> 创建新社团</h3>
          <form @submit.prevent="submitCreateClub">
            <div class="form-group">
              <label><i class="fas fa-heading"></i> 社团名称</label>
              <input
                type="text"
                v-model="createForm.clubName"
                placeholder="请输入社团名称"
                required
              />
            </div>
            <div class="form-group">
              <label><i class="fas fa-tag"></i> 社团类型</label>
              <select v-model="createForm.clubType" required>
                <option value="">请选择社团类型</option>
                <option value="学术科技">学术科技</option>
                <option value="文化体育">文化体育</option>
                <option value="艺术表演">艺术表演</option>
                <option value="志愿公益">志愿公益</option>
                <option value="其他">其他</option>
              </select>
            </div>
            <div class="form-group">
              <label><i class="fas fa-file-alt"></i> 社团介绍</label>
              <textarea
                v-model="createForm.introduction"
                placeholder="请简要介绍社团的宗旨、活动内容等..."
                rows="5"
              ></textarea>
            </div>
            <div class="form-group">
              <label><i class="fas fa-phone"></i> 联系方式</label>
              <input
                type="text"
                v-model="createForm.contactInfo"
                placeholder="请输入联系电话或微信等"
              />
            </div>
            <div class="form-group">
              <label><i class="fas fa-image"></i> 社团Logo URL</label>
              <input
                type="text"
                v-model="createForm.logoUrl"
                placeholder="请输入社团Logo的URL地址（可选）"
              />
            </div>
            <div class="form-group">
              <label><i class="fas fa-user"></i> 指定社长</label>
              <input
                type="text"
                v-model="createForm.presidentId"
                placeholder="请输入社长学号"
                required
              />
            </div>
            <button type="submit" class="btn btn-primary btn-lg" :disabled="createSubmitting">
              <i class="fas fa-save"></i> {{ createSubmitting ? '创建中...' : '创建社团' }}
            </button>
          </form>
        </div>
      </div>
    </div>

    <!-- Club Detail Modal -->
    <div v-if="showDetailModal" class="modal-overlay" @click.self="closeDetailModal">
      <div class="modal-content modal-lg">
        <div class="modal-header">
          <h3><i class="fas fa-info-circle"></i> 社团详情</h3>
          <button class="modal-close" @click="closeDetailModal">&times;</button>
        </div>
        <div v-if="detailLoading" class="modal-body">
          <div class="loading-state">
            <div class="spinner"></div>
            <p>加载中...</p>
          </div>
        </div>
        <div v-else-if="clubDetail" class="modal-body detail-body">
          <!-- Basic Info -->
          <div class="detail-section">
            <div class="club-info detail-info">
              <div class="info-item">
                <i class="fas fa-heading"></i>
                <span><strong>社团名称:</strong> {{ clubDetail.clubName }}</span>
              </div>
              <div class="info-item">
                <i class="fas fa-tag"></i>
                <span><strong>社团类型:</strong> {{ clubDetail.clubType }}</span>
              </div>
              <div class="info-item">
                <i class="fas fa-users"></i>
                <span><strong>成员数量:</strong> {{ clubDetail.memberCount || 0 }} 人</span>
              </div>
              <div class="info-item">
                <i class="fas fa-calendar"></i>
                <span><strong>创建时间:</strong> {{ formatDateTime(clubDetail.createdAt) }}</span>
              </div>
              <div class="info-item">
                <i class="fas fa-phone"></i>
                <span><strong>联系方式:</strong> {{ clubDetail.contactInfo || '暂无' }}</span>
              </div>
            </div>
          </div>

          <!-- President Info -->
          <div class="detail-section president-section">
            <h4><i class="fas fa-user-tie"></i> 社长信息</h4>
            <div class="president-details">
              <div class="info-item">
                <i class="fas fa-id-card"></i>
                <span><strong>学号:</strong> {{ clubDetail.presidentDetail?.studentId || '暂无' }}</span>
              </div>
              <div class="info-item">
                <i class="fas fa-user"></i>
                <span><strong>姓名:</strong> {{ clubDetail.presidentDetail?.studentName || '暂无' }}</span>
              </div>
              <div class="info-item">
                <i class="fas fa-university"></i>
                <span><strong>学院:</strong> {{ clubDetail.presidentDetail?.college || '暂无' }}</span>
              </div>
              <div class="info-item">
                <i class="fas fa-graduation-cap"></i>
                <span><strong>专业班级:</strong> {{ clubDetail.presidentDetail?.majorClass || '暂无' }}</span>
              </div>
              <div class="info-item">
                <i class="fas fa-phone"></i>
                <span><strong>联系电话:</strong> {{ clubDetail.presidentDetail?.phone || '暂无' }}</span>
              </div>
              <div class="info-item">
                <i class="fas fa-envelope"></i>
                <span><strong>邮箱:</strong> {{ clubDetail.presidentDetail?.email || '暂无' }}</span>
              </div>
              <div class="info-item">
                <i class="fas fa-calendar-plus"></i>
                <span><strong>加入时间:</strong> {{ formatDateTime(clubDetail.presidentDetail?.joinDate) }}</span>
              </div>
            </div>
          </div>

          <!-- Members -->
          <div class="detail-section members-section">
            <h4><i class="fas fa-users"></i> 成员列表</h4>
            <div v-if="membersLoading" class="loading-state">
              <div class="spinner"></div>
              <p>加载成员中...</p>
            </div>
            <div v-else-if="clubMembers.length > 0" class="members-grid">
              <div
                v-for="member in clubMembers"
                :key="member.studentId"
                :class="['member-card', 'role-' + member.role]"
              >
                <div class="member-avatar">
                  {{ member.name ? member.name.charAt(0) : '?' }}
                </div>
                <div class="member-info">
                  <h5 class="member-name">{{ member.name }}</h5>
                  <p class="member-detail">学号: {{ member.studentId }}</p>
                  <p class="member-detail">学院: {{ member.college || '暂无' }}</p>
                  <p class="member-detail">专业班级: {{ member.majorClass || '暂无' }}</p>
                  <p class="member-detail">电话: {{ member.phone || '暂无' }}</p>
                  <p class="member-detail">加入时间: {{ formatDateTime(member.joinDate) }}</p>
                </div>
                <div class="member-role">
                  <span :class="['role-badge', 'badge-' + member.role]">
                    {{ getRoleText(member.role) }}
                  </span>
                </div>
              </div>
            </div>
            <div v-else class="no-members">
              <p>暂无成员信息</p>
            </div>
          </div>

          <!-- Description -->
          <div class="detail-section">
            <div class="club-description detail-description">
              <strong>社团介绍:</strong><br />
              {{ clubDetail.introduction || '暂无详细介绍' }}
            </div>
          </div>

          <div class="modal-footer-center">
            <button class="btn btn-primary" @click="closeDetailModal">
              <i class="fas fa-times"></i> 关闭
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Manage President Modal -->
    <div v-if="showPresidentModal" class="modal-overlay" @click.self="closePresidentModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3><i class="fas fa-user-cog"></i> 管理社长</h3>
          <button class="modal-close" @click="closePresidentModal">&times;</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>当前社长</label>
            <div class="input-with-icon">
              <i class="fas fa-user"></i>
              <input type="text" :value="presidentModal.currentDisplay" readonly />
            </div>
          </div>
          <div class="form-group">
            <label>新社长学号</label>
            <div class="input-with-icon">
              <i class="fas fa-id-card"></i>
              <input
                type="text"
                v-model="presidentModal.newPresidentId"
                placeholder="输入新社长学号"
                @input="onNewPresidentIdChange"
              />
            </div>
          </div>
          <div class="form-group">
            <label>新社长姓名</label>
            <div class="input-with-icon">
              <i class="fas fa-user"></i>
              <input
                type="text"
                :value="presidentModal.newPresidentName"
                readonly
                placeholder="系统自动填充"
              />
            </div>
          </div>
          <div class="modal-actions">
            <button class="btn btn-primary" @click="changePresident" :disabled="presidentChanging">
              <i class="fas fa-exchange-alt"></i> {{ presidentChanging ? '更换中...' : '更换社长' }}
            </button>
            <button class="btn btn-danger" @click="removePresident" :disabled="presidentRemoving">
              <i class="fas fa-user-minus"></i> {{ presidentRemoving ? '移除中...' : '移除社长' }}
            </button>
            <button class="btn btn-outline" @click="closePresidentModal">
              <i class="fas fa-times"></i> 取消
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- PDF Export Loading Overlay -->
    <div v-if="pdfExporting" class="modal-overlay pdf-overlay">
      <div class="pdf-loading">
        <div class="spinner"></div>
        <p>正在生成PDF...</p>
      </div>
    </div>

    <!-- Edit Club Modal -->
    <div v-if="showEditModal" class="modal-overlay" @click.self="closeEditModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3><i class="fas fa-edit"></i> 编辑社团信息</h3>
          <button class="modal-close" @click="closeEditModal">&times;</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="submitEditClub">
            <div class="form-group">
              <label>社团名称</label>
              <input type="text" v-model="editForm.clubName" required />
            </div>
            <div class="form-group">
              <label>社团类型</label>
              <input type="text" v-model="editForm.clubType" required />
            </div>
            <div class="form-group">
              <label>联系方式</label>
              <input type="text" v-model="editForm.contactInfo" />
            </div>
            <div class="form-group">
              <label>社团介绍</label>
              <textarea v-model="editForm.introduction" rows="4"></textarea>
            </div>
            <div class="modal-actions" style="justify-content: center;">
              <button type="button" class="btn btn-secondary" @click="closeEditModal">
                <i class="fas fa-times"></i> 取消
              </button>
              <button type="submit" class="btn btn-primary" :disabled="editSubmitting">
                <i class="fas fa-save"></i> {{ editSubmitting ? '保存中...' : '保存修改' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useToastStore } from '../stores/toast'
import api from '../api'
import { jsPDF } from 'jspdf'
import html2canvas from 'html2canvas'

const router = useRouter()
const auth = useAuthStore()
const toast = useToastStore()

const userRole = computed(() => auth.user?.role || '')
const userId = computed(() => auth.user?.studentId || '')

// --- Tab state ---
const activeTab = ref('my-clubs')

// --- My Clubs ---
const myClubs = ref([])
const filteredMyClubs = ref([])
const myClubsSearch = ref('')
const myClubsLoading = ref(false)

// --- All Clubs ---
const allClubs = ref([])
const filteredAllClubs = ref([])
const allClubsSearch = ref('')
const allClubsLoading = ref(false)

// --- Create Club ---
const createForm = reactive({
  clubName: '',
  clubType: '',
  introduction: '',
  contactInfo: '',
  logoUrl: '',
  presidentId: ''
})
const createSubmitting = ref(false)

// --- Detail Modal ---
const showDetailModal = ref(false)
const detailLoading = ref(false)
const clubDetail = ref(null)
const membersLoading = ref(false)
const clubMembers = ref([])

// --- President Modal ---
const showPresidentModal = ref(false)
const presidentModal = reactive({
  clubId: null,
  currentPresidentId: '',
  currentDisplay: '',
  newPresidentId: '',
  newPresidentName: ''
})
const presidentChanging = ref(false)
const presidentRemoving = ref(false)

// --- PDF ---
const pdfExporting = ref(false)

// --- Edit Club ---
const showEditModal = ref(false)
const editSubmitting = ref(false)
const editForm = reactive({
  clubId: null,
  clubName: '',
  clubType: '',
  contactInfo: '',
  introduction: ''
})

// --- Format date ---
function formatDateTime(dateTimeStr) {
  if (!dateTimeStr) return '未知时间'
  const date = new Date(dateTimeStr)
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  const h = String(date.getHours()).padStart(2, '0')
  const min = String(date.getMinutes()).padStart(2, '0')
  return `${y}-${m}-${d} ${h}:${min}`
}

// --- Status helpers (for my clubs of students) ---
function getStatusClass(club) {
  if (club.applicationStatus === 'APPROVED' || club.status === 'ACTIVE') return 'status-approved'
  if (club.applicationStatus === 'PENDING' || club.status === 'PENDING') return 'status-pending'
  return 'status-inactive'
}

function getStatusText(club) {
  if (club.applicationStatus === 'APPROVED') return '已通过'
  if (club.applicationStatus === 'PENDING') return '待审核'
  if (club.applicationStatus === 'PENDING_LEAVE') return '退出申请中'
  return club.status || 'ACTIVE'
}

function getRoleText(role) {
  const map = { president: '社长', manager: '管理员', member: '成员' }
  return map[role] || role || '成员'
}

// --- Tab switching ---
function switchTab(tab) {
  activeTab.value = tab
  if (tab === 'my-clubs') loadMyClubs()
  else if (tab === 'all-clubs') loadAllClubs()
}

// --- Load My Clubs ---
async function loadMyClubs() {
  myClubsLoading.value = true
  try {
    if (userRole.value === 'PRESIDENT') {
      const res = await api.get('/api/clubs/my-clubs')
      myClubs.value = res.data.data || []
    } else if (userRole.value === 'STUDENT') {
      const res = await api.get(`/api/member-manage/student/${userId.value}/clubs`)
      myClubs.value = res.data.data || []
    }
  } catch (e) {
    console.error('加载我的社团错误:', e)
    myClubs.value = []
  }
  myClubsLoading.value = false
  filterMyClubs()
}

function filterMyClubs() {
  const term = myClubsSearch.value.toLowerCase()
  if (!term) {
    filteredMyClubs.value = myClubs.value
    return
  }
  filteredMyClubs.value = myClubs.value.filter(club =>
    (club.clubName && club.clubName.toLowerCase().includes(term)) ||
    (club.clubType && club.clubType.toLowerCase().includes(term)) ||
    (club.introduction && club.introduction.toLowerCase().includes(term))
  )
}

// --- Load All Clubs ---
async function loadAllClubs() {
  allClubsLoading.value = true
  try {
    const res = await api.get('/api/clubs')
    allClubs.value = res.data.data || []
  } catch (e) {
    console.error('加载所有社团错误:', e)
    allClubs.value = []
  }
  allClubsLoading.value = false
  filterAllClubs()
}

function filterAllClubs() {
  const term = allClubsSearch.value.toLowerCase()
  if (!term) {
    filteredAllClubs.value = allClubs.value
    return
  }
  filteredAllClubs.value = allClubs.value.filter(club =>
    (club.clubName && club.clubName.toLowerCase().includes(term)) ||
    (club.clubType && club.clubType.toLowerCase().includes(term)) ||
    (club.introduction && club.introduction.toLowerCase().includes(term))
  )
}

// --- View Club Detail ---
async function viewClubDetail(clubId) {
  showDetailModal.value = true
  detailLoading.value = true
  clubDetail.value = null
  clubMembers.value = []
  try {
    const res = await api.get(`/api/clubs/${clubId}/detail`)
    if (res.data.success) {
      clubDetail.value = res.data.data
      // Load members
      membersLoading.value = true
      try {
        const memRes = await api.get(`/api/clubs/${clubId}/members`)
        clubMembers.value = memRes.data.data || []
      } catch (e) {
        console.error('加载成员错误:', e)
        // Use members from detail if available
        clubMembers.value = res.data.data.members || []
      }
      membersLoading.value = false
    } else {
      toast.error('获取社团详情失败: ' + res.data.message)
      showDetailModal.value = false
    }
  } catch (e) {
    console.error('获取社团详情错误:', e)
    toast.error('获取社团详情时发生错误')
    showDetailModal.value = false
  }
  detailLoading.value = false
}

function closeDetailModal() {
  showDetailModal.value = false
  clubDetail.value = null
  clubMembers.value = []
}

// --- Apply for club (student) ---
async function applyForClub(clubId) {
  if (!confirm('确定要申请加入这个社团吗？')) return
  try {
    const res = await api.post(`/api/clubs/${clubId}/apply`)
    if (res.data.success) {
      toast.success('申请提交成功，等待社长审核！')
      loadAllClubs()
    } else {
      toast.error('申请失败: ' + res.data.message)
    }
  } catch (e) {
    console.error('申请加入社团错误:', e)
    toast.error('申请时发生错误')
  }
}

// --- Leave club (student) ---
async function leaveClub(clubId) {
  const reason = prompt('请输入退出社团的原因（必填）:')
  if (!reason || reason.trim() === '') {
    toast.warning('请输入退出原因')
    return
  }
  try {
    const res = await api.post(
      `/api/member-manage/club/${clubId}/member/${userId.value}/request-leave`,
      { reason: reason.trim() }
    )
    if (res.data.success) {
      toast.success('退出申请已提交，等待社长审批')
      loadMyClubs()
    } else {
      toast.error('申请失败: ' + res.data.message)
    }
  } catch (e) {
    console.error('申请退出社团错误:', e)
    toast.error('申请退出社团时发生错误')
  }
}

// --- Cancel application (student) ---
async function cancelApplication(clubId) {
  if (!confirm('确定要取消申请吗？')) return
  try {
    const res = await api.delete(
      `/api/club-memberships/club/${clubId}/member/${userId.value}`,
      { data: { reason: '学生取消申请' } }
    )
    if (res.data.success) {
      toast.success('取消申请成功')
      loadMyClubs()
    } else {
      toast.error('取消失败: ' + res.data.message)
    }
  } catch (e) {
    console.error('取消申请错误:', e)
    toast.error('取消申请时发生错误')
  }
}

// --- Go to member management (president) ---
function goToMemberManage(clubId) {
  router.push({ path: '/member-management', query: { clubId } })
}

// --- Edit club (president) ---
async function editClub(clubId) {
  try {
    const res = await api.get(`/api/clubs/${clubId}`)
    if (res.data.success) {
      const club = res.data.data
      editForm.clubId = clubId
      editForm.clubName = club.clubName || ''
      editForm.clubType = club.clubType || ''
      editForm.contactInfo = club.contactInfo || ''
      editForm.introduction = club.introduction || ''
      showEditModal.value = true
    } else {
      toast.error('获取社团信息失败: ' + res.data.message)
    }
  } catch (e) {
    console.error('获取社团信息错误:', e)
    toast.error('获取社团信息时发生错误')
  }
}

function closeEditModal() {
  showEditModal.value = false
  editForm.clubId = null
  editForm.clubName = ''
  editForm.clubType = ''
  editForm.contactInfo = ''
  editForm.introduction = ''
}

async function submitEditClub() {
  editSubmitting.value = true
  try {
    const res = await api.put(`/api/clubs/${editForm.clubId}`, {
      clubName: editForm.clubName.trim(),
      clubType: editForm.clubType.trim(),
      contactInfo: editForm.contactInfo.trim(),
      introduction: editForm.introduction.trim()
    })
    if (res.data.success) {
      toast.success('社团信息更新成功！')
      closeEditModal()
      loadMyClubs()
    } else {
      toast.error('更新失败: ' + res.data.message)
    }
  } catch (e) {
    console.error('更新社团信息错误:', e)
    toast.error('更新社团信息时发生错误')
  }
  editSubmitting.value = false
}

// --- Create club ---
async function submitCreateClub() {
  if (!createForm.clubName.trim()) {
    toast.warning('请输入社团名称')
    return
  }
  if (!createForm.clubType) {
    toast.warning('请选择社团类型')
    return
  }
  if (!createForm.presidentId.trim()) {
    toast.warning('请指定社长')
    return
  }
  createSubmitting.value = true
  try {
    const res = await api.post('/api/clubs', {
      clubName: createForm.clubName.trim(),
      clubType: createForm.clubType,
      introduction: createForm.introduction.trim(),
      contactInfo: createForm.contactInfo.trim(),
      logoUrl: createForm.logoUrl.trim(),
      presidentId: createForm.presidentId.trim()
    })
    if (res.data.success) {
      toast.success('社团创建成功！')
      createForm.clubName = ''
      createForm.clubType = ''
      createForm.introduction = ''
      createForm.contactInfo = ''
      createForm.logoUrl = ''
      createForm.presidentId = ''
      switchTab('my-clubs')
    } else {
      toast.error('创建失败: ' + res.data.message)
    }
  } catch (e) {
    console.error('创建社团错误:', e)
    toast.error('创建社团时发生错误，请稍后重试')
  }
  createSubmitting.value = false
}

// --- Manage President (teacher) ---
function openManagePresident(clubId, presidentId, presidentName) {
  presidentModal.clubId = clubId
  presidentModal.currentPresidentId = presidentId
  presidentModal.currentDisplay = `${presidentName} (${presidentId})`
  presidentModal.newPresidentId = ''
  presidentModal.newPresidentName = ''
  showPresidentModal.value = true
}

function closePresidentModal() {
  showPresidentModal.value = false
  presidentModal.clubId = null
  presidentModal.currentPresidentId = ''
  presidentModal.currentDisplay = ''
  presidentModal.newPresidentId = ''
  presidentModal.newPresidentName = ''
}

async function onNewPresidentIdChange() {
  const sid = presidentModal.newPresidentId.trim()
  if (!sid) {
    presidentModal.newPresidentName = ''
    return
  }
  try {
    const res = await api.get(`/api/students/${sid}`)
    if (res.data) {
      presidentModal.newPresidentName = res.data.name || res.data.studentName || ''
    } else {
      presidentModal.newPresidentName = ''
    }
  } catch (e) {
    presidentModal.newPresidentName = ''
  }
}

async function changePresident() {
  const newId = presidentModal.newPresidentId.trim()
  if (!newId) {
    toast.warning('请输入新社长学号')
    return
  }
  if (newId === presidentModal.currentPresidentId) {
    toast.warning('新社长学号与当前社长相同')
    return
  }
  if (!confirm(`确定要将社团社长更换为学号 ${newId} 的学生吗？`)) return
  presidentChanging.value = true
  try {
    const res = await api.put(`/api/clubs/${presidentModal.clubId}/president`, {
      newPresidentId: newId
    })
    if (res.data.success) {
      toast.success('社长更换成功！')
      closePresidentModal()
      loadAllClubs()
      if (activeTab.value === 'my-clubs') loadMyClubs()
    } else {
      toast.error('社长更换失败: ' + res.data.message)
    }
  } catch (e) {
    console.error('更换社长错误:', e)
    toast.error('更换社长时发生错误，请重试')
  }
  presidentChanging.value = false
}

async function removePresident() {
  if (!confirm(`确定要移除社团社长 ${presidentModal.currentDisplay} 吗？移除后该社团将没有社长。`)) return
  presidentRemoving.value = true
  try {
    const res = await api.delete(`/api/clubs/${presidentModal.clubId}/president`)
    if (res.data.success) {
      toast.success('社长移除成功！')
      closePresidentModal()
      loadAllClubs()
      if (activeTab.value === 'my-clubs') loadMyClubs()
    } else {
      toast.error('社长移除失败: ' + res.data.message)
    }
  } catch (e) {
    console.error('移除社长错误:', e)
    toast.error('移除社长时发生错误，请重试')
  }
  presidentRemoving.value = false
}

// --- PDF Export (teacher) ---
async function exportClubToPDF(clubId) {
  pdfExporting.value = true
  try {
    const res = await api.get(`/api/clubs/${clubId}/detail`)
    if (!res.data.success) {
      toast.error('获取社团信息失败: ' + res.data.message)
      pdfExporting.value = false
      return
    }
    const club = res.data.data

    // Create a hidden HTML element to render club detail for PDF
    const pdfContent = document.createElement('div')
    pdfContent.style.cssText = `
      position: absolute; top: -10000px; left: -10000px;
      width: 800px; padding: 20px; background: white;
      font-family: Arial, sans-serif; font-size: 12px;
    `

    let membersRows = ''
    if (club.members && club.members.length > 0) {
      club.members.forEach((member, index) => {
        const roleText = member.role === 'president' ? '社长' : member.role === 'manager' ? '管理员' : '成员'
        membersRows += `
          <tr>
            <td style="border:1px solid #ddd;padding:6px;font-size:12px;">${index + 1}</td>
            <td style="border:1px solid #ddd;padding:6px;font-size:12px;">${member.name || ''}</td>
            <td style="border:1px solid #ddd;padding:6px;font-size:12px;">${member.studentId || ''}</td>
            <td style="border:1px solid #ddd;padding:6px;font-size:12px;">${member.college || '暂无'}</td>
            <td style="border:1px solid #ddd;padding:6px;font-size:12px;">${member.majorClass || '暂无'}</td>
            <td style="border:1px solid #ddd;padding:6px;font-size:12px;">${member.phone || '暂无'}</td>
            <td style="border:1px solid #ddd;padding:6px;font-size:12px;">${formatDateTime(member.joinDate)}</td>
            <td style="border:1px solid #ddd;padding:6px;font-size:12px;">${roleText}</td>
          </tr>
        `
      })
    }

    pdfContent.innerHTML = `
      <div style="text-align:center;background:#667eea;color:white;padding:20px;margin-bottom:30px;">
        <h1 style="margin:0;font-size:24px;">社团详细信息</h1>
      </div>
      <div style="margin-bottom:30px;">
        <h2 style="font-size:18px;margin-bottom:10px;border-bottom:2px solid #ddd;padding-bottom:5px;">基本信息</h2>
        <table style="width:100%;border-collapse:collapse;">
          <tr><td style="width:150px;font-weight:bold;">社团名称：</td><td>${club.clubName || '未设置'}</td></tr>
          <tr><td style="font-weight:bold;">社团类型：</td><td>${club.clubType || '未设置'}</td></tr>
          <tr><td style="font-weight:bold;">成员数量：</td><td>${club.memberCount || 0} 人</td></tr>
          <tr><td style="font-weight:bold;">创建时间：</td><td>${formatDateTime(club.createdAt)}</td></tr>
          <tr><td style="font-weight:bold;">联系方式：</td><td>${club.contactInfo || '暂无'}</td></tr>
        </table>
      </div>
      <div style="margin-bottom:30px;">
        <h2 style="font-size:18px;margin-bottom:10px;border-bottom:2px solid #ddd;padding-bottom:5px;">社长信息</h2>
        <table style="width:100%;border-collapse:collapse;">
          <tr><td style="width:150px;font-weight:bold;">学号：</td><td>${club.presidentDetail?.studentId || '暂无'}</td></tr>
          <tr><td style="font-weight:bold;">姓名：</td><td>${club.presidentDetail?.studentName || '暂无'}</td></tr>
          <tr><td style="font-weight:bold;">学院：</td><td>${club.presidentDetail?.college || '暂无'}</td></tr>
          <tr><td style="font-weight:bold;">专业班级：</td><td>${club.presidentDetail?.majorClass || '暂无'}</td></tr>
          <tr><td style="font-weight:bold;">联系电话：</td><td>${club.presidentDetail?.phone || '暂无'}</td></tr>
          <tr><td style="font-weight:bold;">邮箱：</td><td>${club.presidentDetail?.email || '暂无'}</td></tr>
          <tr><td style="font-weight:bold;">加入时间：</td><td>${club.presidentDetail?.joinDate ? formatDateTime(club.presidentDetail.joinDate) : '暂无'}</td></tr>
        </table>
      </div>
      <div style="margin-bottom:30px;">
        <h2 style="font-size:18px;margin-bottom:10px;border-bottom:2px solid #ddd;padding-bottom:5px;">成员列表</h2>
        ${club.members && club.members.length > 0 ? `
          <table style="width:100%;border-collapse:collapse;margin-top:15px;">
            <thead>
              <tr style="background:#f5f5f5;">
                <th style="border:1px solid #ddd;padding:8px;text-align:left;width:50px;">序号</th>
                <th style="border:1px solid #ddd;padding:8px;text-align:left;width:100px;">姓名</th>
                <th style="border:1px solid #ddd;padding:8px;text-align:left;width:120px;">学号</th>
                <th style="border:1px solid #ddd;padding:8px;text-align:left;width:120px;">学院</th>
                <th style="border:1px solid #ddd;padding:8px;text-align:left;width:120px;">专业班级</th>
                <th style="border:1px solid #ddd;padding:8px;text-align:left;width:120px;">电话</th>
                <th style="border:1px solid #ddd;padding:8px;text-align:left;">加入时间</th>
                <th style="border:1px solid #ddd;padding:8px;text-align:left;width:80px;">角色</th>
              </tr>
            </thead>
            <tbody>
              ${membersRows}
            </tbody>
          </table>
        ` : '<p style="font-size:14px;color:#666;">暂无成员信息</p>'}
      </div>
      <div style="margin-bottom:30px;">
        <h2 style="font-size:18px;margin-bottom:10px;border-bottom:2px solid #ddd;padding-bottom:5px;">社团介绍</h2>
        <div style="white-space:pre-wrap;">
          <p style="font-size:14px;line-height:1.6;">${club.introduction || '暂无详细介绍'}</p>
        </div>
      </div>
      <div style="margin-top:50px;text-align:center;color:#999;font-size:10px;">
        社团管理系统 - 社团详情报告
      </div>
    `

    document.body.appendChild(pdfContent)

    const canvas = await html2canvas(pdfContent, {
      scale: 2,
      useCORS: true
    })

    const pdf = new jsPDF({
      orientation: 'portrait',
      unit: 'mm',
      format: 'a4'
    })

    const imgWidth = 210
    const imgHeight = (canvas.height * imgWidth) / canvas.width
    pdf.addImage(canvas.toDataURL('image/png'), 'PNG', 0, 0, imgWidth, imgHeight)
    pdf.save(`${club.clubName}_详细信息.pdf`)

    document.body.removeChild(pdfContent)
    toast.success('PDF生成成功！')
  } catch (e) {
    console.error('导出PDF错误:', e)
    toast.error('生成PDF时发生错误，请稍后重试')
  }
  pdfExporting.value = false
}

// --- Init ---
onMounted(() => {
  if (!auth.token) {
    router.push('/login')
    return
  }
  // Teachers default to all-clubs tab
  if (userRole.value === 'TEACHER') {
    activeTab.value = 'all-clubs'
    loadAllClubs()
  } else {
    loadMyClubs()
  }
})
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.club-manage-page {
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

/* Header */
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

/* Tabs */
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

/* Content Section */
.content-section {
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

/* Card */
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

/* Buttons */
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

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.btn-sm {
  padding: 10px 20px;
  font-size: 13px;
}

.btn-lg {
  padding: 18px 36px;
  font-size: 17px;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-success {
  background: linear-gradient(135deg, #4ade80 0%, #22c55e 100%);
  color: white;
}

.btn-warning {
  background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
  color: white;
}

.btn-danger {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  color: white;
}

.btn-info {
  background: linear-gradient(135deg, #06b6d4 0%, #0891b2 100%);
  color: white;
}

.btn-secondary {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

.btn-outline {
  background: transparent;
  color: #667eea;
  border: 2px solid #667eea;
}

.btn-outline:hover {
  background: #667eea;
  color: white;
}

/* Search Box */
.search-box {
  margin-bottom: 20px;
}

.search-input-wrapper {
  position: relative;
  max-width: 500px;
}

.search-input-wrapper i {
  position: absolute;
  left: 18px;
  top: 50%;
  transform: translateY(-50%);
  color: #999;
  font-size: 16px;
  pointer-events: none;
}

.search-input-wrapper input {
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

.search-input-wrapper input:focus {
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

/* Club Grid */
.club-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 30px;
  margin-top: 10px;
}

.club-card {
  background: #fff;
  border-radius: 25px;
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.16);
  border: 1px solid rgba(0, 0, 0, 0.05);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  min-height: 300px;
}

.club-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.club-card::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.club-card:hover {
  transform: translateY(-12px) scale(1.02);
  box-shadow: 0 30px 60px rgba(0, 0, 0, 0.25);
}

.club-card-inner {
  padding: 35px;
  position: relative;
}

.club-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.club-title {
  font-size: 24px;
  font-weight: 700;
  color: #2d3748;
  line-height: 1.4;
  margin-bottom: 8px;
}

.club-card:hover .club-title {
  color: #667eea;
}

.club-type {
  font-size: 16px;
  color: #4a5568;
  margin-top: 8px;
}

.club-status {
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
}

.status-active,
.status-approved {
  background: #dcfce7;
  color: #166534;
  border: 1px solid #bbf7d0;
}

.status-inactive {
  background: #f3f4f6;
  color: #4b5563;
  border: 1px solid #e5e7eb;
}

.status-pending {
  background: #fef3c7;
  color: #92400e;
  border: 1px solid #fde68a;
}

.club-info {
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

.club-description {
  color: #4a5568;
  margin-bottom: 25px;
  line-height: 1.7;
  font-size: 16px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.club-actions {
  display: flex;
  gap: 15px;
  margin-top: 25px;
  flex-wrap: wrap;
}

/* Form */
.form-group {
  margin-bottom: 25px;
}

.form-group label {
  display: block;
  margin-bottom: 10px;
  font-weight: 600;
  color: #2d3748;
  font-size: 15px;
}

.form-group label i {
  margin-right: 8px;
  color: #667eea;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 14px 18px;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  font-size: 14px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: #f7fafc;
  color: #2d3748;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #667eea;
  background: white;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
}

.form-group textarea {
  height: 140px;
  resize: vertical;
  line-height: 1.6;
}

.input-with-icon {
  position: relative;
}

.input-with-icon i {
  position: absolute;
  left: 18px;
  top: 50%;
  transform: translateY(-50%);
  color: #667eea;
  font-size: 14px;
}

.input-with-icon input {
  padding-left: 48px;
}

/* Empty State */
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

.empty-sub {
  color: #9ca3af;
  font-size: 14px;
}

/* Loading State */
.loading-state {
  text-align: center;
  padding: 60px 20px;
  color: #667eea;
}

.spinner {
  border: 4px solid rgba(102, 126, 234, 0.3);
  border-top: 4px solid #667eea;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  animation: spin 1s linear infinite;
  margin: 0 auto 15px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading-state p {
  font-size: 16px;
}

/* Modal */
.modal-overlay {
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

.modal-content {
  background: #fff;
  border-radius: 25px;
  padding: 35px;
  max-width: 600px;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 30px 80px rgba(0, 0, 0, 0.3);
  animation: modalSlideIn 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.modal-content.modal-lg {
  max-width: 900px;
}

@keyframes modalSlideIn {
  from { opacity: 0; transform: scale(0.85); }
  to { opacity: 1; transform: scale(1); }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  padding-bottom: 20px;
  border-bottom: 2px solid #f7fafc;
}

.modal-header h3 {
  color: #2d3748;
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 10px;
  border: none;
  padding: 0;
}

.modal-header h3 i {
  color: #667eea;
}

.modal-close {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #4a5568;
  padding: 0;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.3s ease;
}

.modal-close:hover {
  background: #f8fafc;
  color: #2d3748;
}

.modal-body {
  /* default body styles */
}

.modal-actions {
  display: flex;
  gap: 15px;
  margin-top: 25px;
  flex-wrap: wrap;
}

.modal-footer-center {
  margin-top: 20px;
  text-align: center;
}

/* Detail Modal Sections */
.detail-body {
  /* scrollable detail */
}

.detail-section {
  margin-bottom: 30px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.detail-info .info-item {
  font-size: 15px;
  margin-bottom: 8px;
}

.president-section h4,
.members-section h4 {
  margin-top: 0;
  margin-bottom: 15px;
  color: #2d3748;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
}

.president-section h4 i,
.members-section h4 i {
  color: #667eea;
}

.president-details {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 16px;
}

.detail-description {
  font-size: 15px;
  line-height: 1.8;
  display: block;
  -webkit-line-clamp: unset;
  overflow: visible;
}

/* Members Grid (in detail modal) */
.members-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.member-card {
  display: flex;
  align-items: flex-start;
  background: #f8f9fa;
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  padding: 20px;
  transition: all 0.3s ease;
  border: 1px solid #e5e7eb;
}

.member-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
  background: #f1f5f9;
}

.member-card.role-president {
  border-left: 4px solid rgba(243, 156, 18, 0.8);
}

.member-card.role-manager {
  border-left: 4px solid rgba(52, 152, 219, 0.8);
}

.member-card.role-member {
  border-left: 4px solid rgba(39, 174, 96, 0.8);
}

.member-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: bold;
  margin-right: 16px;
  flex-shrink: 0;
}

.member-info {
  flex: 1;
  min-width: 0;
}

.member-name {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: #2d3748;
}

.member-detail {
  font-size: 14px;
  margin: 4px 0;
  color: #4a5568;
}

.member-role {
  margin-left: auto;
  display: flex;
  align-items: flex-start;
  flex-shrink: 0;
}

.role-badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  white-space: nowrap;
}

.badge-president {
  background-color: rgba(243, 156, 18, 0.8);
  color: white;
}

.badge-manager {
  background-color: rgba(52, 152, 219, 0.8);
  color: white;
}

.badge-member {
  background-color: rgba(39, 174, 96, 0.8);
  color: white;
}

.no-members {
  text-align: center;
  padding: 40px 20px;
  color: #4a5568;
  font-size: 16px;
  background: #f8f9fa;
  border-radius: 12px;
  border: 2px dashed #e5e7eb;
}

/* PDF Loading Overlay */
.pdf-overlay {
  z-index: 1100;
}

.pdf-loading {
  text-align: center;
  color: #667eea;
  background: white;
  padding: 40px 60px;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.pdf-loading p {
  font-size: 18px;
  font-weight: 600;
  margin-top: 15px;
}

/* Responsive */
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

  .club-grid {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .club-card {
    min-height: auto;
  }

  .club-card-inner {
    padding: 20px;
  }

  .club-actions {
    flex-direction: column;
  }

  .club-actions .btn {
    width: 100%;
    justify-content: center;
  }

  .card {
    padding: 20px;
  }

  .modal-content {
    padding: 20px;
    width: 95%;
  }

  .modal-content.modal-lg {
    max-width: 95%;
  }

  .members-grid {
    grid-template-columns: 1fr;
  }

  .member-card {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .member-avatar {
    margin-right: 0;
    margin-bottom: 12px;
  }

  .member-role {
    margin-left: 0;
    margin-top: 12px;
  }

  .modal-actions {
    flex-direction: column;
  }

  .modal-actions .btn {
    width: 100%;
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .club-manage-page {
    padding: 10px 0;
  }

  .header {
    padding: 15px;
  }

  .header h1 {
    font-size: 20px;
  }

  .club-title {
    font-size: 20px;
  }
}

/* Scrollbar styling for modal */
.modal-content::-webkit-scrollbar {
  width: 6px;
}

.modal-content::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 10px;
}

.modal-content::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 10px;
}

.modal-content::-webkit-scrollbar-thumb:hover {
  background: #a1a1a1;
}
</style>
