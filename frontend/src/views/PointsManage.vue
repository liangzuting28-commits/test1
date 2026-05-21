<template>
  <div class="points-manage-page">
    <div class="container">
      <!-- Header -->
      <div class="header">
        <h1><i class="fas fa-star"></i> 积分管理中心</h1>
        <button class="btn btn-primary" @click="$router.push('/dashboard')">
          <i class="fas fa-arrow-left"></i> 返回仪表盘
        </button>
      </div>

      <!-- Tabs -->
      <div class="tabs">
        <button
          class="tab"
          :class="{ active: activeTab === 'ranking' }"
          @click="switchTab('ranking')"
        >
          <i class="fas fa-trophy"></i> 积分排行榜
        </button>
        <button
          class="tab"
          :class="{ active: activeTab === 'my-points' }"
          @click="switchTab('my-points')"
        >
          <i class="fas fa-history"></i> 我的积分
        </button>
        <button
          v-if="isTeacher"
          class="tab"
          :class="{ active: activeTab === 'rules' }"
          @click="switchTab('rules')"
        >
          <i class="fas fa-cog"></i> 积分规则
        </button>
      </div>

      <!-- Ranking Tab -->
      <div v-show="activeTab === 'ranking'" class="content-section" :class="{ active: activeTab === 'ranking' }">
        <div class="card">
          <h3><i class="fas fa-trophy"></i> 学生积分排行榜</h3>
          <div v-if="ranking.length > 0" class="ranking-list">
            <div
              v-for="(student, index) in ranking"
              :key="student.studentId"
              class="ranking-item"
            >
              <div class="rank" :class="rankClass(index)">{{ index + 1 }}</div>
              <div class="student-info">
                <div class="student-name">{{ student.name }}</div>
                <div class="student-details">
                  {{ student.studentId }} | {{ student.college }} | {{ student.majorClass }}
                </div>
              </div>
              <div class="points">{{ student.totalPoints }} 积分</div>
            </div>
          </div>
          <div v-else class="empty-state">
            <i class="fas fa-trophy"></i>
            <p>暂无积分数据</p>
            <p>参与活动获取积分后即可显示在排行榜中</p>
          </div>
        </div>
      </div>

      <!-- My Points Tab -->
      <div v-show="activeTab === 'my-points'" class="content-section" :class="{ active: activeTab === 'my-points' }">
        <div class="card">
          <h3><i class="fas fa-chart-line"></i> 我的积分统计</h3>
          <div class="stats-row">
            <div class="stats-box stats-box-purple">
              <div class="stats-box-label">当前积分</div>
              <div class="stats-box-value">{{ totalPoints }}</div>
            </div>
            <div class="stats-box stats-box-green">
              <div class="stats-box-label">积分记录数</div>
              <div class="stats-box-value">{{ recordsCount }}</div>
            </div>
          </div>

          <h4><i class="fas fa-list"></i> 积分记录</h4>
          <div v-if="pointsRecords.length > 0" class="points-history">
            <div
              v-for="record in pointsRecords"
              :key="record.recordId || record.activityId"
              class="points-record"
            >
              <div class="record-info">
                <h4>活动积分</h4>
                <div class="record-details">
                  活动ID: {{ record.activityId }} |
                  获得时间: {{ formatDateTime(record.earnedTime) }}
                </div>
              </div>
              <div class="record-points">+{{ record.pointsEarned }} 积分</div>
            </div>
          </div>
          <div v-else class="empty-state">
            <i class="fas fa-history"></i>
            <p>暂无积分记录</p>
            <p>参与活动并签到后即可获得积分</p>
          </div>
        </div>
      </div>

      <!-- Rules Tab (Teacher only) -->
      <div v-if="isTeacher" v-show="activeTab === 'rules'" class="content-section" :class="{ active: activeTab === 'rules' }">
        <div class="card">
          <div class="rules-header">
            <h3><i class="fas fa-cog"></i> 积分规则管理</h3>
            <button class="btn btn-primary" @click="showAddRuleModal">
              <i class="fas fa-plus"></i> 添加规则
            </button>
          </div>
          <div v-if="rules.length > 0" class="rules-list">
            <div
              v-for="rule in rules"
              :key="rule.ruleId || rule.activityType"
              class="rule-item"
            >
              <div class="rule-info">
                <h4>{{ rule.activityType }}</h4>
                <div class="rule-details">
                  最后更新: {{ formatDateTime(rule.lastUpdated) }}
                </div>
              </div>
              <div class="rule-points">{{ rule.basePointsValue }} 积分</div>
            </div>
          </div>
          <div v-else class="empty-state">
            <i class="fas fa-cog"></i>
            <p>暂无积分规则</p>
            <p>点击"添加规则"按钮创建新的积分规则</p>
          </div>
        </div>
      </div>

      <!-- Add Rule Modal -->
      <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
        <div class="modal-content">
          <div class="modal-header">
            <h3><i class="fas fa-plus-circle"></i> 添加积分规则</h3>
            <button class="modal-close" @click="closeModal">&times;</button>
          </div>
          <form @submit.prevent="submitRule">
            <div class="form-group">
              <label><i class="fas fa-tag"></i> 活动类型</label>
              <input
                v-model="newRule.activityType"
                type="text"
                placeholder="请输入活动类型（如：学术讲座、文体活动等）"
                required
              />
            </div>
            <div class="form-group">
              <label><i class="fas fa-star"></i> 基础积分</label>
              <input
                v-model.number="newRule.basePointsValue"
                type="number"
                placeholder="请输入基础积分值"
                min="1"
                max="100"
                required
              />
            </div>
            <div class="modal-actions">
              <button type="button" class="btn btn-secondary" @click="closeModal">
                <i class="fas fa-times"></i> 取消
              </button>
              <button type="submit" class="btn btn-primary">
                <i class="fas fa-save"></i> 保存规则
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useToastStore } from '../stores/toast'
import api from '../api'

const router = useRouter()
const auth = useAuthStore()
const toast = useToastStore()

const isTeacher = computed(() => auth.user?.role === 'TEACHER')
const studentId = computed(() => auth.user?.studentId)

const activeTab = ref('ranking')

const ranking = ref([])
const totalPoints = ref(0)
const recordsCount = ref(0)
const pointsRecords = ref([])
const rules = ref([])

const showModal = ref(false)
const newRule = reactive({
  activityType: '',
  basePointsValue: null
})

function rankClass(index) {
  if (index === 0) return 'rank-1'
  if (index === 1) return 'rank-2'
  if (index === 2) return 'rank-3'
  return 'rank-other'
}

function formatDateTime(dateTimeStr) {
  if (!dateTimeStr) return '未知时间'
  const date = new Date(dateTimeStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

function switchTab(tabName) {
  activeTab.value = tabName
  if (tabName === 'ranking') {
    loadRanking()
  } else if (tabName === 'my-points') {
    loadMyPoints()
  } else if (tabName === 'rules') {
    loadRules()
  }
}

async function loadRanking() {
  try {
    const res = await api.get('/api/points/ranking')
    if (res.data.success) {
      ranking.value = res.data.data || []
    }
  } catch (e) {
    console.error('加载排行榜错误:', e)
  }
}

async function loadMyPoints() {
  try {
    const totalRes = await api.get(`/api/points/student/${studentId.value}/total`)
    if (totalRes.data.success) {
      totalPoints.value = totalRes.data.data || 0
    }

    const historyRes = await api.get(`/api/points/student/${studentId.value}`)
    if (historyRes.data.success && historyRes.data.data?.length > 0) {
      pointsRecords.value = historyRes.data.data
      recordsCount.value = historyRes.data.data.length
    } else {
      pointsRecords.value = []
      recordsCount.value = 0
    }
  } catch (e) {
    console.error('加载积分数据错误:', e)
  }
}

async function loadRules() {
  try {
    const res = await api.get('/api/points/rules')
    if (res.data.success) {
      rules.value = res.data.data || []
    }
  } catch (e) {
    console.error('加载积分规则错误:', e)
  }
}

function showAddRuleModal() {
  newRule.activityType = ''
  newRule.basePointsValue = null
  showModal.value = true
}

function closeModal() {
  showModal.value = false
}

async function submitRule() {
  if (!newRule.activityType.trim()) {
    toast.warning('请输入活动类型')
    return
  }
  if (!newRule.basePointsValue || newRule.basePointsValue < 1) {
    toast.warning('请输入有效的积分值')
    return
  }

  try {
    const res = await api.post('/api/points/rules', {
      activityType: newRule.activityType.trim(),
      basePointsValue: newRule.basePointsValue
    })
    if (res.data.success) {
      toast.success('积分规则添加成功！')
      closeModal()
      loadRules()
    } else {
      toast.error('添加失败: ' + res.data.message)
    }
  } catch (e) {
    console.error('添加积分规则错误:', e)
    toast.error('添加时发生错误，请稍后重试')
  }
}

onMounted(() => {
  if (!auth.token) {
    router.push('/login')
    return
  }

  if (isTeacher.value) {
    activeTab.value = 'ranking'
    loadRanking()
  } else {
    activeTab.value = 'my-points'
    loadMyPoints()
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

.points-manage-page {
  background: #f5f7fa;
  min-height: 100vh;
  color: #333;
}

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
  padding: 25px;
  border-radius: 15px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border-left: 4px solid #667eea;
}

.header h1 {
  color: #333;
  font-size: 28px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.header h1 i {
  color: #667eea;
}

/* Tabs */
.tabs {
  display: flex;
  margin-bottom: 25px;
  background: white;
  border-radius: 12px;
  padding: 8px;
  box-shadow: 0 2px 15px rgba(0, 0, 0, 0.06);
  gap: 5px;
}

.tab {
  flex: 1;
  padding: 15px 20px;
  text-align: center;
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.3s ease;
  font-weight: 500;
  color: #666;
  border: none;
  background: transparent;
  font-size: 14px;
}

.tab:hover {
  background: #f8f9fa;
  color: #333;
}

.tab.active {
  background: #667eea;
  color: white;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

/* Content sections */
.content-section {
  animation: fadeIn 0.5s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Card */
.card {
  background: white;
  border-radius: 15px;
  padding: 30px;
  margin-bottom: 25px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid #f0f0f0;
}

.card h3 {
  color: #333;
  margin-bottom: 20px;
  font-size: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.card h3 i {
  color: #667eea;
}

.card h4 {
  color: #333;
  margin-bottom: 20px;
  font-size: 16px;
  display: flex;
  align-items: center;
  gap: 10px;
}

/* Buttons */
.btn {
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
}

.btn-primary {
  background: #667eea;
  color: white;
}

.btn-primary:hover {
  background: #5a6fd8;
  transform: translateY(-1px);
}

.btn-secondary {
  background: #6c757d;
  color: white;
}

.btn-secondary:hover {
  background: #5a6268;
}

/* Ranking */
.ranking-list {
  display: grid;
  gap: 15px;
}

.ranking-item {
  display: flex;
  align-items: center;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 10px;
  transition: all 0.3s ease;
}

.ranking-item:hover {
  background: #e9ecef;
  transform: translateX(5px);
}

.rank {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 16px;
  margin-right: 15px;
  flex-shrink: 0;
}

.rank-1 {
  background: #ffd700;
  color: #333;
}

.rank-2 {
  background: #c0c0c0;
  color: #333;
}

.rank-3 {
  background: #cd7f32;
  color: #333;
}

.rank-other {
  background: #667eea;
  color: white;
}

.student-info {
  flex: 1;
}

.student-name {
  font-weight: 600;
  color: #333;
  margin-bottom: 5px;
}

.student-details {
  font-size: 14px;
  color: #666;
}

.points {
  font-size: 20px;
  font-weight: bold;
  color: #667eea;
}

/* Stats boxes */
.stats-row {
  display: flex;
  gap: 20px;
  margin-bottom: 30px;
  flex-wrap: wrap;
}

.stats-box {
  color: white;
  padding: 20px;
  border-radius: 10px;
  text-align: center;
  min-width: 200px;
}

.stats-box-purple {
  background: linear-gradient(135deg, #667eea, #764ba2);
}

.stats-box-green {
  background: linear-gradient(135deg, #4CAF50, #45a049);
}

.stats-box-label {
  font-size: 14px;
  margin-bottom: 10px;
}

.stats-box-value {
  font-size: 32px;
  font-weight: bold;
}

/* Points history */
.points-history {
  display: grid;
  gap: 15px;
}

.points-record {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: white;
  border-radius: 10px;
  border-left: 4px solid #4CAF50;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.record-info h4 {
  color: #333;
  margin-bottom: 5px;
}

.record-details {
  font-size: 14px;
  color: #666;
}

.record-points {
  font-size: 18px;
  font-weight: bold;
  color: #4CAF50;
}

/* Rules */
.rules-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.rules-list {
  display: grid;
  gap: 15px;
}

.rule-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: white;
  border-radius: 10px;
  border-left: 4px solid #667eea;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.rule-info h4 {
  color: #333;
  margin-bottom: 5px;
}

.rule-details {
  font-size: 14px;
  color: #666;
}

.rule-points {
  font-size: 18px;
  font-weight: bold;
  color: #667eea;
}

/* Empty state */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #666;
}

.empty-state i {
  font-size: 64px;
  margin-bottom: 20px;
  color: #dee2e6;
  display: block;
}

.empty-state p {
  font-size: 16px;
  margin-bottom: 10px;
}

/* Modal */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  display: flex;
  justify-content: center;
  align-items: center;
  backdrop-filter: blur(5px);
}

.modal-content {
  background: white;
  border-radius: 15px;
  padding: 30px;
  max-width: 500px;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  animation: modalSlideIn 0.3s ease;
}

@keyframes modalSlideIn {
  from {
    opacity: 0;
    transform: scale(0.9);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e9ecef;
}

.modal-header h3 {
  color: #333;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.modal-close {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #666;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: background 0.3s;
}

.modal-close:hover {
  background: #f8f9fa;
}

/* Form */
.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 600;
  color: #555;
  font-size: 14px;
}

.form-group input {
  width: 100%;
  padding: 12px 16px;
  border: 2px solid #e9ecef;
  border-radius: 8px;
  font-size: 14px;
  transition: all 0.3s ease;
  background: #fafbfc;
}

.form-group input:focus {
  outline: none;
  border-color: #667eea;
  background: white;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.modal-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  margin-top: 25px;
}

/* Responsive */
@media (max-width: 768px) {
  .header {
    flex-direction: column;
    gap: 15px;
    text-align: center;
  }

  .ranking-item,
  .points-record,
  .rule-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .points,
  .record-points,
  .rule-points {
    align-self: flex-end;
  }

  .stats-row {
    flex-direction: column;
  }

  .stats-box {
    min-width: auto;
  }

  .rules-header {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
  }
}
</style>
