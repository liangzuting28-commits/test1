<template>
  <div class="activity-center-page">
    <div class="container">
      <div class="header">
        <h1><i class="fas fa-calendar-alt"></i> 活动中心</h1>
        <div>
          <button class="btn btn-primary" @click="$router.push('/dashboard')">
            <i class="fas fa-arrow-left"></i> 返回仪表盘
          </button>
        </div>
      </div>

      <!-- 搜索和筛选区域 -->
      <div class="search-filter-section">
        <div class="search-box">
          <i class="fas fa-search"></i>
          <input type="text" v-model="searchText" placeholder="搜索活动名称、描述或社团..." @keyup.enter="applyFilters">
          <button class="search-btn" @click="applyFilters"><i class="fas fa-search"></i> 搜索</button>
        </div>
        <div class="filter-options">
          <select v-model="typeFilter" @change="applyFilters">
            <option value="all">所有类型</option>
            <option value="学术讲座">学术讲座</option>
            <option value="文体活动">文体活动</option>
            <option value="志愿公益">志愿公益</option>
            <option value="竞赛获奖">竞赛获奖</option>
            <option value="社会实践">社会实践</option>
          </select>
          <select v-model="pointsFilter" @change="applyFilters">
            <option value="all">所有积分</option>
            <option value="0-10">0-10分</option>
            <option value="11-20">11-20分</option>
            <option value="21-30">21-30分</option>
            <option value="30+">30分以上</option>
          </select>
          <select v-model="sortBy" @change="applyFilters">
            <option value="time">按时间排序</option>
            <option value="points">按积分排序</option>
            <option value="name">按名称排序</option>
          </select>
        </div>
      </div>

      <div class="activity-grid">
        <template v-if="filteredActivities.length > 0">
          <div class="activity-card" v-for="activity in filteredActivities" :key="activity.activityId">
            <div class="activity-header">
              <div>
                <div class="activity-title">{{ activity.activityName }}</div>
                <div class="activity-club"><i class="fas fa-university"></i> 主办社团: {{ activity.clubName }}</div>
                <div class="activity-type" v-if="activity.activityType">类型: {{ activity.activityType }}</div>
              </div>
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
                <i class="fas fa-users"></i>
                <span>人数限制: {{ activity.maxParticipants === 0 ? '无限制' : activity.maxParticipants + '人' }}</span>
              </div>
              <div class="info-item">
                <i class="fas fa-star"></i>
                <span>积分: {{ activity.awardPoints }}</span>
              </div>
            </div>
            <div class="activity-description">
              {{ activity.description || '暂无描述' }}
            </div>
            <div v-if="activity.posterFileId" class="activity-poster-link">
              <a :href="'/api/files/download/' + activity.posterFileId" target="_blank" class="poster-preview">
                <i class="fas fa-eye"></i> 预览海报
              </a>
              <a :href="'/api/files/download/' + activity.posterFileId" download class="poster-download">
                <i class="fas fa-download"></i> 下载海报
              </a>
            </div>
            <div class="activity-actions">
              <button class="btn btn-success" @click="registerActivity(activity.activityId)">
                <i class="fas fa-calendar-check"></i> 申请报名
              </button>
            </div>
          </div>
        </template>
        <div v-else class="empty-state">
          <i class="fas fa-search"></i>
          <p>没有找到符合条件的活动</p>
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
const filteredActivities = ref([])
const searchText = ref('')
const typeFilter = ref('all')
const pointsFilter = ref('all')
const sortBy = ref('time')

function formatDateTime(dateTimeStr) {
  if (!dateTimeStr) return ''
  const date = new Date(dateTimeStr)
  return date.toLocaleString('zh-CN')
}

async function loadActivities() {
  try {
    const response = await api.get('/api/activities')
    if (response.data.success) {
      allActivities.value = response.data.data
      applyFilters()
    }
  } catch (error) {
    console.error('加载活动错误:', error)
  }
}

function applyFilters() {
  let filtered = allActivities.value.filter(activity => {
    if (activity.status !== 'PUBLISHED') return false

    const matchesSearch = searchText.value === '' ||
      activity.activityName.toLowerCase().includes(searchText.value.toLowerCase()) ||
      (activity.description && activity.description.toLowerCase().includes(searchText.value.toLowerCase())) ||
      (activity.clubName && activity.clubName.toLowerCase().includes(searchText.value.toLowerCase()))

    const matchesType = typeFilter.value === 'all' || activity.activityType === typeFilter.value

    let matchesPoints = true
    if (pointsFilter.value !== 'all') {
      const points = activity.awardPoints
      switch (pointsFilter.value) {
        case '0-10': matchesPoints = points >= 0 && points <= 10; break
        case '11-20': matchesPoints = points >= 11 && points <= 20; break
        case '21-30': matchesPoints = points >= 21 && points <= 30; break
        case '30+': matchesPoints = points > 30; break
      }
    }

    return matchesSearch && matchesType && matchesPoints
  })

  filtered.sort((a, b) => {
    switch (sortBy.value) {
      case 'time': return new Date(a.activityTime) - new Date(b.activityTime)
      case 'points': return b.awardPoints - a.awardPoints
      case 'name': return a.activityName.localeCompare(b.activityName)
      default: return 0
    }
  })

  filteredActivities.value = filtered
}

async function registerActivity(activityId) {
  try {
    const response = await api.post(`/api/activities/${activityId}/register`, {}, {
      headers: { 'X-User-Id': auth.user.studentId }
    })
    if (response.data.success) {
      toast.success('报名申请已提交，请等待社长审核！')
      loadActivities()
    } else {
      toast.error('报名失败: ' + response.data.message)
    }
  } catch (error) {
    console.error('报名活动错误:', error)
    toast.error('报名时发生错误')
  }
}

onMounted(() => {
  if (!auth.token) {
    router.push('/login')
    return
  }
  loadActivities()
})
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.activity-center-page {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  min-height: 100vh;
  color: #333;
  padding: 20px 0;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  background: #fff;
  padding: 24px 30px;
  border-radius: 16px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
}

.header h1 {
  font-size: 28px;
  font-weight: 700;
  color: #667eea;
  display: flex;
  align-items: center;
  gap: 12px;
}

.header h1 i {
  font-size: 32px;
}

.search-filter-section {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  margin-bottom: 30px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  backdrop-filter: blur(10px);
}

.search-box {
  width: 100%;
  position: relative;
}

.search-box i {
  position: absolute;
  left: 18px;
  top: 50%;
  transform: translateY(-50%);
  color: #999;
  font-size: 18px;
  pointer-events: none;
}

.search-box input {
  width: 100%;
  padding: 16px 100px 16px 50px;
  border: 2px solid #e1e8ed;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 500;
  color: #333;
  outline: none;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: #fafbfc;
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

.filter-options {
  display: flex;
  gap: 18px;
  flex-wrap: wrap;
  align-items: center;
}

.filter-options select {
  flex: 1;
  min-width: 200px;
  padding: 14px 18px;
  border: 2px solid #e1e8ed;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  background: #fafbfc;
  color: #333;
  outline: none;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 12 12'%3E%3Cpath fill='%23666' d='M6 9L1 4h10z'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 18px center;
}

.filter-options select:focus {
  border-color: #667eea;
  background-color: #fff;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.activity-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
  gap: 24px;
}

.activity-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-left: 5px solid #667eea;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.activity-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 4px;
  background: linear-gradient(90deg, #667eea, #f093fb);
  transform: scaleX(0);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.activity-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
}

.activity-card:hover::before {
  transform: scaleX(1);
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
  color: #333;
  line-height: 1.3;
  margin-bottom: 8px;
}

.activity-card:hover .activity-title {
  color: #667eea;
}

.activity-club {
  font-size: 14px;
  font-weight: 500;
  color: #666;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.activity-club i {
  color: #667eea;
}

.activity-type {
  font-size: 13px;
  font-weight: 600;
  color: #667eea;
  background: rgba(102, 126, 234, 0.1);
  padding: 6px 12px;
  border-radius: 20px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  margin-right: auto;
}

.activity-info {
  display: grid;
  gap: 12px;
  margin-bottom: 20px;
  padding: 20px;
  background: #fafbfc;
  border-radius: 10px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #666;
  font-size: 14px;
  font-weight: 500;
}

.info-item i {
  color: #667eea;
  font-size: 18px;
  width: 20px;
  text-align: center;
}

.activity-description {
  color: #666;
  line-height: 1.6;
  margin-bottom: 20px;
  font-size: 14px;
  padding: 16px;
  background: rgba(245, 247, 250, 0.6);
  border-radius: 10px;
  border-left: 3px solid #667eea;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 4;
  -webkit-box-orient: vertical;
}

.activity-poster-link {
  margin-bottom: 16px;
  display: flex;
  gap: 12px;
}

.activity-poster-link a {
  text-decoration: none;
  font-size: 13px;
  font-weight: 500;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  border-radius: 6px;
  transition: all 0.2s;
}

.poster-preview {
  color: #667eea;
  background: rgba(102, 126, 234, 0.08);
}

.poster-preview:hover {
  background: rgba(102, 126, 234, 0.15);
}

.poster-download {
  color: #52c41a;
  background: rgba(82, 196, 26, 0.08);
}

.poster-download:hover {
  background: rgba(82, 196, 26, 0.15);
}

.activity-actions {
  display: flex;
  gap: 12px;
}

.btn {
  flex: 1;
  padding: 14px 24px;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.btn-primary {
  background: #667eea;
  color: white;
}

.btn-primary:hover {
  background: #5a6fd8;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.btn-success {
  background: #4caf50;
  color: white;
}

.btn-success:hover {
  background: #43a047;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.empty-state {
  grid-column: 1 / -1;
  text-align: center;
  padding: 80px 20px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border: 2px dashed #e1e8ed;
}

.empty-state i {
  font-size: 80px;
  color: #999;
  margin-bottom: 20px;
  opacity: 0.5;
}

.empty-state p {
  font-size: 16px;
  color: #999;
}

@media (max-width: 768px) {
  .container {
    padding: 0 15px;
  }

  .header {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }

  .activity-grid {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .filter-options {
    flex-direction: column;
    gap: 12px;
  }

  .filter-options select {
    width: 100%;
    min-width: auto;
  }

  .activity-actions {
    flex-direction: column;
  }

  .empty-state {
    padding: 40px 20px;
  }

  .empty-state i {
    font-size: 60px;
  }
}

@media (max-width: 480px) {
  .activity-center-page {
    padding: 10px 0;
  }

  .header {
    padding: 20px;
  }

  .header h1 {
    font-size: 24px;
  }

  .search-filter-section {
    padding: 20px;
  }

  .activity-card {
    padding: 20px;
  }
}

html {
  scroll-behavior: smooth;
}
</style>
