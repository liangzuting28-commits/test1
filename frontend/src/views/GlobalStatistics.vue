<template>
  <div class="global-statistics-page">
    <div class="container">
      <!-- Header -->
      <div class="header">
        <h1><i class="fas fa-chart-pie"></i> 数据统计</h1>
        <button class="btn btn-primary" @click="router.push('/dashboard')">
          <i class="fas fa-arrow-left"></i> 返回仪表盘
        </button>
      </div>

      <!-- Stat Cards -->
      <div class="stats-cards">
        <!-- Students Card -->
        <div class="stat-card" @click="showDetailModal('students')">
          <i class="fas fa-users stat-icon"></i>
          <div class="stat-number">{{ overview.studentCount }}</div>
          <div class="stat-label">学生总数</div>
          <div class="stat-desc">包含普通学生、社长和教师</div>
          <div class="stat-extra">
            <div class="extra-item">
              <span class="extra-label">普通学生:</span>
              <span class="extra-value">{{ regularStudentCount }}</span>
            </div>
            <div class="extra-item">
              <span class="extra-label">社长:</span>
              <span class="extra-value">{{ overview.presidentCount }}</span>
            </div>
            <div class="extra-item">
              <span class="extra-label">教师:</span>
              <span class="extra-value">{{ overview.teacherCount }}</span>
            </div>
          </div>
        </div>

        <!-- Clubs Card -->
        <div class="stat-card" @click="showDetailModal('clubs')">
          <i class="fas fa-graduation-cap stat-icon"></i>
          <div class="stat-number">{{ overview.clubCount }}</div>
          <div class="stat-label">社团总数</div>
          <div class="stat-desc">包含各类社团和学生组织</div>
          <div class="stat-extra">
            <div class="extra-item">
              <span class="extra-label">文体类:</span>
              <span class="extra-value">{{ clubTypeCounts.culture }}</span>
            </div>
            <div class="extra-item">
              <span class="extra-label">学术类:</span>
              <span class="extra-value">{{ clubTypeCounts.academic }}</span>
            </div>
            <div class="extra-item">
              <span class="extra-label">实践类:</span>
              <span class="extra-value">{{ clubTypeCounts.practice }}</span>
            </div>
          </div>
        </div>

        <!-- Activities Card -->
        <div class="stat-card" @click="showDetailModal('activities')">
          <i class="fas fa-calendar-alt stat-icon"></i>
          <div class="stat-number">{{ overview.activityCount }}</div>
          <div class="stat-label">活动总数</div>
          <div class="stat-desc">本学期所有活动</div>
          <div class="stat-extra">
            <div class="extra-item">
              <span class="extra-label">已发布:</span>
              <span class="extra-value">{{ overview.publishedActivityCount }}</span>
            </div>
            <div class="extra-item">
              <span class="extra-label">进行中:</span>
              <span class="extra-value">{{ overview.ongoingActivityCount }}</span>
            </div>
            <div class="extra-item">
              <span class="extra-label">已结束:</span>
              <span class="extra-value">{{ overview.endedActivityCount }}</span>
            </div>
          </div>
        </div>

        <!-- Points Card -->
        <div class="stat-card" @click="showDetailModal('points')">
          <i class="fas fa-star stat-icon"></i>
          <div class="stat-number">{{ overview.totalPoints }}</div>
          <div class="stat-label">发放积分总数</div>
          <div class="stat-desc">本学期发放的积分</div>
          <div class="stat-extra">
            <div class="extra-item">
              <span class="extra-label">平均每人:</span>
              <span class="extra-value">{{ avgPointsPerStudent }}</span>
            </div>
            <div class="extra-item">
              <span class="extra-label">最高积分:</span>
              <span class="extra-value">{{ maxPoints }}</span>
            </div>
            <div class="extra-item">
              <span class="extra-label">0分学生:</span>
              <span class="extra-value">{{ zeroPointsStudents }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Detail Modal -->
      <div
        v-if="detailModalVisible"
        class="stat-details"
        style="display: flex"
        @click.self="closeDetailModal"
      >
        <div class="stat-details-content">
          <div class="stat-details-header">
            <h3 class="stat-details-title">{{ detailTitle }}</h3>
            <button class="close-btn" @click="closeDetailModal">&times;</button>
          </div>
          <div v-if="detailLoading" class="loading">
            <i class="fas fa-spinner stat-icon" style="opacity:1;font-size:48px;color:var(--primary-color);animation:pulse 1.5s infinite"></i>
            <p>加载中...</p>
          </div>
          <div v-else v-html="detailContent"></div>
        </div>
      </div>

      <!-- Filter -->
      <div class="filter-container">
        <div class="filter-group">
          <label for="semesterFilter">学期筛选:</label>
          <select id="semesterFilter" v-model="selectedSemester" @change="refreshCharts">
            <option value="all">全部学期</option>
            <option value="2023-1">2023年第一学期</option>
            <option value="2023-2">2023年第二学期</option>
            <option value="2024-1">2024年第一学期</option>
            <option value="2024-2">2024年第二学期</option>
            <option value="2025-1">2025年第一学期</option>
            <option value="2025-2">2025年第二学期</option>
          </select>
        </div>
        <button class="refresh-btn" @click="refreshData">
          <i class="fas fa-sync-alt"></i> 刷新数据
        </button>
      </div>

      <!-- Activity Type Pie Chart -->
      <div class="chart-container">
        <div class="chart-header">
          <h3 class="chart-title">活动类型分布</h3>
        </div>
        <div class="chart-wrapper">
          <canvas ref="activityTypeChartRef"></canvas>
        </div>
      </div>

      <!-- Club Activity Bar Chart -->
      <div class="chart-container">
        <div class="chart-header">
          <h3 class="chart-title">社团活动数量</h3>
        </div>
        <div class="chart-wrapper">
          <canvas ref="clubActivityChartRef"></canvas>
        </div>
      </div>
    </div>

    <!-- Toast -->
    <Transition name="toast">
      <div v-if="toastVisible" :class="['toast', toastType]">
        <i :class="toastType === 'success' ? 'fas fa-check-circle' : 'fas fa-exclamation-circle'"></i>
        <span>{{ toastMessage }}</span>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { Chart, registerables } from 'chart.js'
import api from '../api'
import { useAuthStore } from '../stores/auth'

Chart.register(...registerables)

const router = useRouter()
const authStore = useAuthStore()

// --- Reactive State ---
const overview = reactive({
  studentCount: 0,
  clubCount: 0,
  activityCount: 0,
  totalPoints: 0,
  presidentCount: 0,
  teacherCount: 0,
  publishedActivityCount: 0,
  ongoingActivityCount: 0,
  endedActivityCount: 0
})

const clubTypeCounts = reactive({ culture: 0, academic: 0, practice: 0 })
const maxPoints = ref(0)
const zeroPointsStudents = ref(0)
const selectedSemester = ref('2025-1')

// Chart refs
const activityTypeChartRef = ref(null)
const clubActivityChartRef = ref(null)
let activityTypeChart = null
let clubActivityChart = null

// Detail modal
const detailModalVisible = ref(false)
const detailTitle = ref('')
const detailContent = ref('')
const detailLoading = ref(false)

// Toast
const toastVisible = ref(false)
const toastMessage = ref('')
const toastType = ref('success')
let toastTimer = null

// --- Computed ---
const regularStudentCount = computed(() => {
  return overview.studentCount - overview.presidentCount - overview.teacherCount
})

const avgPointsPerStudent = computed(() => {
  if (overview.studentCount === 0) return 0
  return Math.round(overview.totalPoints / overview.studentCount)
})

// --- Toast ---
function showToast(message, type = 'success') {
  if (toastTimer) clearTimeout(toastTimer)
  toastMessage.value = message
  toastType.value = type
  toastVisible.value = true
  toastTimer = setTimeout(() => {
    toastVisible.value = false
  }, 3000)
}

// --- Data Loading ---
async function loadOverview() {
  try {
    const res = await api.get('/api/statistics/overview')
    if (res.data.success) {
      const data = res.data.data
      Object.assign(overview, {
        studentCount: data.studentCount || 0,
        clubCount: data.clubCount || 0,
        activityCount: data.activityCount || 0,
        totalPoints: data.totalPoints || 0,
        presidentCount: data.presidentCount || 0,
        teacherCount: data.teacherCount || 0,
        publishedActivityCount: data.publishedActivityCount || 0,
        ongoingActivityCount: data.ongoingActivityCount || 0,
        endedActivityCount: data.endedActivityCount || 0
      })

      // Try to get club type counts from overview data
      if (data.clubTypeCount) {
        clubTypeCounts.culture = data.clubTypeCount['文体类'] || 0
        clubTypeCounts.academic = data.clubTypeCount['学术类'] || 0
        clubTypeCounts.practice = data.clubTypeCount['实践类'] || 0
      } else {
        await loadClubTypes()
      }
    }
  } catch (e) {
    console.error('Failed to load overview:', e)
  }
}

async function loadClubTypes() {
  try {
    const res = await api.get('/api/clubs')
    if (res.data.success && res.data.data) {
      const clubs = res.data.data
      let culture = 0, academic = 0, practice = 0
      clubs.forEach(club => {
        const type = (club.clubType || '').toString()
        if (type.includes('文体')) culture++
        else if (type.includes('学术')) academic++
        else if (type.includes('实践')) practice++
        else practice++ // default to practice
      })
      Object.assign(clubTypeCounts, { culture, academic, practice })
    }
  } catch (e) {
    console.error('Failed to load club types:', e)
  }
}

async function loadMaxPoints() {
  try {
    const res = await api.get('/api/students')
    if (res.data.success && res.data.data && res.data.data.length > 0) {
      maxPoints.value = Math.max(...res.data.data.map(s => s.totalPoints || 0))
    } else {
      maxPoints.value = 0
    }
  } catch (e) {
    console.error('Failed to load max points:', e)
    maxPoints.value = 0
  }
}

async function loadPointsDistribution() {
  try {
    const res = await api.get('/api/statistics/points')
    if (res.data.success && res.data.data) {
      const dist = res.data.data.pointsDistribution || {}
      zeroPointsStudents.value = dist['0'] || 0
    } else {
      zeroPointsStudents.value = 0
    }
  } catch (e) {
    console.error('Failed to load points distribution:', e)
    zeroPointsStudents.value = 0
  }
}

async function loadActivityTypeChart(semester) {
  if (!activityTypeChartRef.value) return
  try {
    const params = {}
    if (semester && semester !== 'all') params.semester = semester
    const res = await api.get('/api/statistics/activity-types', { params })
    if (res.data.success) {
      const data = res.data.data || []
      const labels = data.map(item => item.type || '未知类型')
      const values = data.map(item => item.count || 0)

      if (activityTypeChart) activityTypeChart.destroy()
      activityTypeChart = new Chart(activityTypeChartRef.value.getContext('2d'), {
        type: 'pie',
        data: {
          labels,
          datasets: [{
            data: values,
            backgroundColor: [
              '#667eea', '#764ba2', '#f093fb', '#4facfe', '#43e97b',
              '#fa709a', '#fee140', '#30cfd0', '#a8edea', '#fed6e3'
            ],
            borderWidth: 2,
            borderColor: '#ffffff'
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: {
              position: 'right',
              labels: { padding: 15, font: { size: 12 } }
            },
            tooltip: {
              callbacks: {
                label(ctx) {
                  const label = ctx.label || ''
                  const value = ctx.parsed || 0
                  const total = ctx.dataset.data.reduce((a, b) => a + b, 0)
                  const percentage = total > 0 ? ((value / total) * 100).toFixed(1) : 0
                  return `${label}: ${value} (${percentage}%)`
                }
              }
            }
          },
          animation: { animateRotate: true, animateScale: true }
        }
      })
    }
  } catch (e) {
    console.error('Failed to load activity type chart:', e)
  }
}

async function loadClubActivityChart(semester) {
  if (!clubActivityChartRef.value) return
  try {
    const params = {}
    if (semester && semester !== 'all') params.semester = semester
    const res = await api.get('/api/statistics/club-activities', { params })
    if (res.data.success) {
      const data = res.data.data || []
      const labels = data.map(item => item.clubName || '未知社团')
      const values = data.map(item => item.count || 0)

      if (clubActivityChart) clubActivityChart.destroy()
      clubActivityChart = new Chart(clubActivityChartRef.value.getContext('2d'), {
        type: 'bar',
        data: {
          labels,
          datasets: [{
            label: '活动数量',
            data: values,
            backgroundColor: 'rgba(102, 126, 234, 0.7)',
            borderColor: 'rgba(102, 126, 234, 1)',
            borderWidth: 2,
            borderRadius: 5
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          scales: {
            y: { beginAtZero: true, ticks: { precision: 0 } },
            x: { ticks: { maxRotation: 45, minRotation: 45 } }
          },
          plugins: {
            legend: { display: false },
            tooltip: {
              callbacks: {
                label(ctx) {
                  const value = ctx.parsed.y || 0
                  return `活动数量: ${value}`
                }
              }
            }
          },
          animation: { duration: 1000, easing: 'easeOutQuart' }
        }
      })
    }
  } catch (e) {
    console.error('Failed to load club activity chart:', e)
  }
}

async function refreshCharts() {
  await Promise.all([
    loadActivityTypeChart(selectedSemester.value),
    loadClubActivityChart(selectedSemester.value)
  ])
}

async function refreshData() {
  try {
    showToast('正在刷新数据...', 'success')
    await Promise.all([
      loadOverview(),
      loadMaxPoints(),
      loadPointsDistribution()
    ])
    await refreshCharts()
    showToast('数据已更新', 'success')
  } catch (e) {
    console.error('Failed to refresh data:', e)
    showToast('刷新数据失败: ' + e.message, 'error')
  }
}

// --- Detail Modals ---
async function showDetailModal(type) {
  detailModalVisible.value = true
  detailLoading.value = true
  detailContent.value = ''

  try {
    switch (type) {
      case 'students':
        detailTitle.value = '学生统计详情'
        await loadStudentsDetail()
        break
      case 'clubs':
        detailTitle.value = '社团统计详情'
        await loadClubsDetail()
        break
      case 'activities':
        detailTitle.value = '活动统计详情'
        await loadActivitiesDetail()
        break
      case 'points':
        detailTitle.value = '积分统计详情'
        await loadPointsDetail()
        break
    }
  } catch (e) {
    detailContent.value = `<div class="error-message">加载详情失败: ${e.message}</div>`
  } finally {
    detailLoading.value = false
  }
}

function closeDetailModal() {
  detailModalVisible.value = false
}

async function loadStudentsDetail() {
  const res = await api.get('/api/students')
  if (res.data.success && res.data.data) {
    const students = res.data.data
    const regular = students.filter(s => s.role === 'STUDENT')
    const presidents = students.filter(s => s.role === 'PRESIDENT')
    const teachers = students.filter(s => s.role === 'TEACHER')

    let html = `
      <div class="detail-item"><span class="detail-label">普通学生数量</span><span class="detail-value">${regular.length}</span></div>
      <div class="detail-item"><span class="detail-label">社长数量</span><span class="detail-value">${presidents.length}</span></div>
      <div class="detail-item"><span class="detail-label">教师数量</span><span class="detail-value">${teachers.length}</span></div>
      <div class="detail-section-title">活跃社长</div>
      <div class="detail-list">
    `

    const topPresidents = presidents.slice(0, 5)
    for (const p of topPresidents) {
      let clubNames = '无'
      try {
        const clubRes = await api.get(`/api/clubs/by-president/${p.studentId}`)
        if (clubRes.data.success && clubRes.data.data && clubRes.data.data.length > 0) {
          clubNames = clubRes.data.data.map(c => c.clubName).join(', ')
        }
      } catch (_) { /* ignore */ }
      html += `
        <div class="detail-list-item">
          <div class="item-name">${p.name || p.studentId}</div>
          <div class="item-desc">管理社团: ${clubNames}</div>
          <div class="item-stats">积分: ${p.totalPoints || 0}</div>
        </div>
      `
    }
    html += `</div><div class="detail-section-title">活跃学生</div><div class="detail-list">`

    const topStudents = [...regular].sort((a, b) => (b.totalPoints || 0) - (a.totalPoints || 0)).slice(0, 5)
    for (const s of topStudents) {
      html += `
        <div class="detail-list-item">
          <div class="item-name">${s.name || s.studentId}</div>
          <div class="item-desc">学号: ${s.studentId || '未知'}</div>
          <div class="item-stats">积分: ${s.totalPoints || 0}</div>
        </div>
      `
    }
    html += `</div>`
    detailContent.value = html
  } else {
    detailContent.value = `<div class="error-message">加载学生数据失败</div>`
  }
}

async function loadClubsDetail() {
  const res = await api.get('/api/clubs')
  if (res.data.success && res.data.data) {
    const clubs = res.data.data

    let html = `
      <div class="detail-item"><span class="detail-label">社团总数</span><span class="detail-value">${clubs.length}</span></div>
    `

    const typeCount = {}
    clubs.forEach(club => {
      const type = club.clubType || '未分类'
      typeCount[type] = (typeCount[type] || 0) + 1
    })
    for (const [type, count] of Object.entries(typeCount)) {
      html += `<div class="detail-item"><span class="detail-label">${type}社团</span><span class="detail-value">${count}</span></div>`
    }

    html += `<div class="detail-section-title">活跃社团</div><div class="detail-list">`

    const clubActivityCounts = await Promise.all(
      clubs.map(async club => {
        try {
          const actRes = await api.get(`/api/activities/club/${club.clubId}`)
          const count = actRes.data.success ? (actRes.data.data || []).length : 0
          return { ...club, activityCount: count }
        } catch (_) {
          return { ...club, activityCount: 0 }
        }
      })
    )

    const topClubs = clubActivityCounts.sort((a, b) => b.activityCount - a.activityCount).slice(0, 5)
    for (const club of topClubs) {
      html += `
        <div class="detail-list-item">
          <div class="item-name">${club.clubName}</div>
          <div class="item-desc">类型: ${club.clubType || '未分类'}</div>
          <div class="item-stats">活动数: ${club.activityCount}</div>
        </div>
      `
    }
    html += `</div>`
    detailContent.value = html
  } else {
    detailContent.value = `<div class="error-message">加载社团数据失败</div>`
  }
}

async function loadActivitiesDetail() {
  const res = await api.get('/api/activities')
  if (res.data.success && res.data.data) {
    const activities = res.data.data

    let html = `
      <div class="detail-item"><span class="detail-label">活动总数</span><span class="detail-value">${activities.length}</span></div>
    `

    const statusCount = {}
    activities.forEach(a => {
      const status = a.status || 'UNKNOWN'
      statusCount[status] = (statusCount[status] || 0) + 1
    })
    const statusLabels = { PUBLISHED: '已发布', ONGOING: '进行中', ENDED: '已结束', DRAFT: '草稿' }
    for (const [status, count] of Object.entries(statusCount)) {
      html += `<div class="detail-item"><span class="detail-label">${statusLabels[status] || status}</span><span class="detail-value">${count}</span></div>`
    }

    const typeCount = {}
    activities.forEach(a => {
      const type = a.activityType || '未分类'
      typeCount[type] = (typeCount[type] || 0) + 1
    })
    for (const [type, count] of Object.entries(typeCount)) {
      html += `<div class="detail-item"><span class="detail-label">${type}</span><span class="detail-value">${count}</span></div>`
    }

    html += `<div class="detail-section-title">热门活动</div><div class="detail-list">`

    const withParticipants = await Promise.all(
      activities.map(async activity => {
        try {
          const pRes = await api.get(`/api/activities/${activity.activityId}/participants`)
          const count = pRes.data.success ? (pRes.data.data || 0) : 0
          return { ...activity, participantCount: count }
        } catch (_) {
          return { ...activity, participantCount: 0 }
        }
      })
    )

    const popular = withParticipants.sort((a, b) => b.participantCount - a.participantCount).slice(0, 5)
    for (const act of popular) {
      let clubName = '未知社团'
      try {
        const cRes = await api.get(`/api/clubs/${act.clubId}`)
        if (cRes.data.success && cRes.data.data) clubName = cRes.data.data.clubName
      } catch (_) { /* ignore */ }
      html += `
        <div class="detail-list-item">
          <div class="item-name">${act.activityName}</div>
          <div class="item-desc">社团: ${clubName}</div>
          <div class="item-stats">参与人数: ${act.participantCount}/${act.maxParticipants || '不限'}</div>
        </div>
      `
    }
    html += `</div>`
    detailContent.value = html
  } else {
    detailContent.value = `<div class="error-message">加载活动数据失败</div>`
  }
}

async function loadPointsDetail() {
  const res = await api.get('/api/statistics/points')
  if (res.data.success && res.data.data) {
    const pointsData = res.data.data

    let html = `
      <div class="detail-item"><span class="detail-label">积分发放总量</span><span class="detail-value">${pointsData.totalPointsEarned || 0}</span></div>
    `

    if (pointsData.pointsDistribution) {
      const rangeLabels = {
        '0': '0积分学生', '1-10': '1-10积分学生', '11-50': '11-50积分学生',
        '51-100': '51-100积分学生', '100+': '100+积分学生'
      }
      for (const [range, count] of Object.entries(pointsData.pointsDistribution)) {
        html += `<div class="detail-item"><span class="detail-label">${rangeLabels[range] || range}</span><span class="detail-value">${count}</span></div>`
      }
    }

    html += `<div class="detail-section-title">积分排行榜</div><div class="detail-list">`

    try {
      const sRes = await api.get('/api/students')
      if (sRes.data.success && sRes.data.data) {
        const topStudents = sRes.data.data.sort((a, b) => (b.totalPoints || 0) - (a.totalPoints || 0)).slice(0, 5)
        const medals = ['\u{1F947}', '\u{1F948}', '\u{1F949}']
        topStudents.forEach((s, i) => {
          const medal = i < 3 ? medals[i] : `${i + 1}.`
          html += `
            <div class="detail-list-item">
              <div class="item-name">${medal} ${s.name || s.studentId}</div>
              <div class="item-desc">学号: ${s.studentId || '未知'}</div>
              <div class="item-stats">总积分: ${s.totalPoints || 0}</div>
            </div>
          `
        })
      }
    } catch (_) { /* ignore */ }

    html += `</div>`
    detailContent.value = html
  } else {
    detailContent.value = `<div class="error-message">加载积分数据失败</div>`
  }
}

// --- Lifecycle ---
onMounted(async () => {
  // Permission check
  if (authStore.userRole !== 'TEACHER') {
    showToast('您没有权限访问此页面', 'error')
    router.push('/dashboard')
    return
  }

  await nextTick()
  await refreshData()
})

onBeforeUnmount(() => {
  if (activityTypeChart) { activityTypeChart.destroy(); activityTypeChart = null }
  if (clubActivityChart) { clubActivityChart.destroy(); clubActivityChart = null }
  if (toastTimer) clearTimeout(toastTimer)
})
</script>

<style scoped>
/* CSS Variables */
.global-statistics-page {
  --primary-color: #667eea;
  --primary-dark: #5a6fd8;
  --secondary-color: #764ba2;
  --success-color: #48bb78;
  --warning-color: #ed8936;
  --danger-color: #f56565;
  --light-gray: #f7fafc;
  --medium-gray: #e2e8f0;
  --dark-gray: #4a5568;
  --text-color: #2d3748;
  --card-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  --card-radius: 16px;
  --transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: var(--text-color);
  min-height: 100vh;
  padding: 20px 0;
  margin: 0;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
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
  margin-bottom: 30px;
  background: white;
  padding: 25px;
  border-radius: var(--card-radius);
  box-shadow: var(--card-shadow);
  border-left: 4px solid var(--primary-color);
}

.header h1 {
  color: var(--text-color);
  font-size: 28px;
  display: flex;
  align-items: center;
}

.header h1 i {
  margin-right: 15px;
  color: var(--primary-color);
}

/* Buttons */
.btn {
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  transition: var(--transition);
  display: inline-flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
  font-size: 14px;
  position: relative;
  overflow: hidden;
}

.btn-primary {
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
  color: white;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
}

/* Stat Cards */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: white;
  border-radius: var(--card-radius);
  padding: 25px;
  box-shadow: var(--card-shadow);
  transition: var(--transition);
  position: relative;
  overflow: hidden;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  height: 100%;
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.stat-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: linear-gradient(180deg, var(--primary-color), var(--secondary-color));
}

.stat-number {
  font-size: 42px;
  font-weight: bold;
  margin-bottom: 10px;
  color: var(--primary-color);
  text-shadow: 0 2px 10px rgba(102, 126, 234, 0.3);
}

.stat-label {
  color: var(--dark-gray);
  font-size: 16px;
  margin-bottom: 5px;
  font-weight: 600;
}

.stat-desc {
  color: var(--dark-gray);
  font-size: 13px;
}

.stat-extra {
  margin-top: 15px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.extra-item {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
}

.extra-label {
  color: var(--dark-gray);
}

.extra-value {
  font-weight: 600;
  color: var(--primary-color);
}

.stat-icon {
  position: absolute;
  right: 20px;
  top: 20px;
  font-size: 48px;
  opacity: 0.1;
  color: var(--primary-color);
}

/* Filter */
.filter-container {
  display: flex;
  gap: 15px;
  margin-bottom: 25px;
  flex-wrap: wrap;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.filter-group label {
  font-size: 14px;
  color: var(--dark-gray);
  font-weight: 500;
}

.filter-group select {
  padding: 12px 16px;
  border: 2px solid var(--medium-gray);
  border-radius: 10px;
  font-size: 14px;
  min-width: 150px;
  transition: var(--transition);
  background: var(--light-gray);
}

.filter-group select:focus {
  outline: none;
  border-color: var(--primary-color);
  background: white;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
}

.refresh-btn {
  background: var(--primary-color);
  color: white;
  border: none;
  padding: 12px 20px;
  border-radius: 10px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  transition: var(--transition);
  margin-top: 20px;
}

.refresh-btn:hover {
  background: var(--primary-dark);
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
}

/* Charts */
.chart-container {
  background: white;
  border-radius: var(--card-radius);
  padding: 30px;
  margin-bottom: 30px;
  box-shadow: var(--card-shadow);
  border: 1px solid rgba(0, 0, 0, 0.05);
  transition: var(--transition);
}

.chart-container:hover {
  transform: translateY(-2px);
  box-shadow: 0 15px 50px rgba(0, 0, 0, 0.15);
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  padding-bottom: 15px;
  border-bottom: 2px solid var(--light-gray);
}

.chart-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-color);
}

.chart-wrapper {
  position: relative;
  height: 350px;
  margin-bottom: 20px;
}

/* Detail Modal */
.stat-details {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.6);
  z-index: 1000;
  justify-content: center;
  align-items: center;
  backdrop-filter: blur(8px);
}

.stat-details-content {
  background: white;
  border-radius: var(--card-radius);
  padding: 35px;
  max-width: 700px;
  width: 90%;
  max-height: 80%;
  overflow-y: auto;
  position: relative;
  box-shadow: 0 30px 80px rgba(0, 0, 0, 0.3);
  animation: modalSlideIn 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(0, 0, 0, 0.05);
}

@keyframes modalSlideIn {
  from { opacity: 0; transform: scale(0.85); }
  to { opacity: 1; transform: scale(1); }
}

.stat-details-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  padding-bottom: 20px;
  border-bottom: 2px solid var(--light-gray);
}

.stat-details-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--text-color);
}

.close-btn {
  background: none;
  border: none;
  font-size: 28px;
  color: var(--dark-gray);
  cursor: pointer;
  transition: var(--transition);
  width: 35px;
  height: 35px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
}

.close-btn:hover {
  color: var(--text-color);
  background: var(--light-gray);
  transform: rotate(90deg);
}

/* Detail content items (rendered via v-html) */
.detail-item {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid var(--light-gray);
}

.detail-label {
  color: var(--dark-gray);
  font-weight: 600;
}

.detail-value {
  color: var(--text-color);
  font-weight: 600;
  font-size: 16px;
}

.detail-section-title {
  margin-top: 25px;
  margin-bottom: 15px;
  font-size: 18px;
  font-weight: 600;
  color: var(--primary-color);
  border-bottom: 2px solid var(--light-gray);
  padding-bottom: 8px;
}

.detail-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-list-item {
  background: var(--light-gray);
  border-radius: 10px;
  padding: 15px;
  border-left: 4px solid var(--primary-color);
  transition: var(--transition);
}

.detail-list-item:hover {
  transform: translateX(5px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.item-name {
  font-weight: 600;
  font-size: 16px;
  margin-bottom: 5px;
  color: var(--text-color);
}

.item-desc {
  font-size: 14px;
  color: var(--dark-gray);
  margin-bottom: 8px;
}

.item-stats {
  font-size: 14px;
  color: var(--primary-color);
  font-weight: 600;
}

.error-message {
  color: var(--danger-color);
  padding: 20px;
  background: rgba(245, 101, 101, 0.1);
  border-radius: 10px;
  text-align: center;
  border: 2px solid rgba(245, 101, 101, 0.2);
}

/* Loading */
.loading {
  text-align: center;
  padding: 60px;
  color: var(--dark-gray);
}

/* Toast */
.toast {
  position: fixed;
  bottom: 30px;
  right: 30px;
  background: white;
  padding: 18px 24px;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
  display: flex;
  align-items: center;
  gap: 12px;
  z-index: 1100;
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.toast.success {
  border-left: 4px solid var(--success-color);
}

.toast.error {
  border-left: 4px solid var(--danger-color);
}

.toast i {
  font-size: 24px;
}

.toast.success i {
  color: var(--success-color);
}

.toast.error i {
  color: var(--danger-color);
}

/* Toast transitions */
.toast-enter-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.toast-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.toast-enter-from {
  opacity: 0;
  transform: translateX(400px);
}

.toast-leave-to {
  opacity: 0;
  transform: translateX(400px);
}

/* Pulse animation for loading spinner */
@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.2); opacity: 0.7; }
  100% { transform: scale(1); }
}

/* Responsive */
@media (max-width: 768px) {
  .container {
    padding: 15px;
  }

  .header {
    padding: 20px 25px;
    margin-bottom: 25px;
  }

  .header h1 {
    font-size: 24px;
  }

  .header h1 i {
    font-size: 28px;
  }

  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
    gap: 15px;
  }

  .stat-card {
    padding: 20px;
  }

  .stat-number {
    font-size: 36px;
  }

  .chart-container {
    padding: 20px;
  }

  .chart-wrapper {
    height: 280px;
  }

  .filter-container {
    flex-direction: column;
  }

  .filter-group {
    width: 100%;
  }

  .filter-group select {
    width: 100%;
  }

  .stat-details-content {
    padding: 25px;
    margin: 20px;
  }

  .btn {
    width: 100%;
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .stats-cards {
    grid-template-columns: 1fr;
  }

  .stat-number {
    font-size: 32px;
  }

  .chart-wrapper {
    height: 250px;
  }
}
</style>
