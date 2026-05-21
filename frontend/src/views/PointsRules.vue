<template>
  <div class="points-rules-container">
    <div class="container">
      <!-- Header -->
      <div class="header">
        <h1><i class="fas fa-cog"></i> 积分规则管理</h1>
        <button class="btn btn-primary" @click="goBack">
          <i class="fas fa-arrow-left"></i> 返回仪表盘
        </button>
      </div>

      <!-- Stats Cards -->
      <div class="stats-cards">
        <div class="stat-card">
          <div class="stat-number">{{ stats.totalRules }}</div>
          <div class="stat-label">总规则数</div>
        </div>
        <div class="stat-card">
          <div class="stat-number">{{ stats.avgPoints }}</div>
          <div class="stat-label">平均积分</div>
        </div>
        <div class="stat-card">
          <div class="stat-number">{{ stats.maxPoints }}</div>
          <div class="stat-label">最高积分</div>
        </div>
        <div class="stat-card">
          <div class="stat-number">{{ stats.minPoints }}</div>
          <div class="stat-label">最低积分</div>
        </div>
      </div>

      <!-- Rules Card -->
      <div class="card">
        <div class="card-header-row">
          <h3><i class="fas fa-list"></i> 积分规则列表</h3>
          <button class="btn btn-primary" @click="openAddModal">
            <i class="fas fa-plus"></i> 添加规则
          </button>
        </div>

        <!-- Search -->
        <div class="search-box">
          <input
            type="text"
            v-model="searchTerm"
            placeholder="搜索活动类型..."
          />
        </div>

        <!-- Rules List -->
        <div class="rules-list" v-if="filteredRules.length > 0">
          <div class="rule-item" v-for="rule in filteredRules" :key="rule.ruleId">
            <div class="rule-info">
              <h4>{{ rule.activityType }}</h4>
              <div class="rule-details">
                最后更新: {{ formatDateTime(rule.lastUpdated) }} |
                规则ID: {{ rule.ruleId }}
              </div>
            </div>
            <div class="rule-points">{{ rule.basePointsValue }} 积分</div>
            <div class="rule-actions">
              <button class="btn btn-warning btn-sm" @click="openEditModal(rule)">
                <i class="fas fa-edit"></i> 编辑
              </button>
              <button class="btn btn-danger btn-sm" @click="openDeleteModal(rule)">
                <i class="fas fa-trash"></i> 删除
              </button>
            </div>
          </div>
        </div>

        <!-- Empty State -->
        <div class="empty-state" v-else>
          <i class="fas fa-cog" v-if="!searchTerm && rules.length === 0"></i>
          <i class="fas fa-search" v-else></i>
          <p v-if="searchTerm">未找到匹配的规则</p>
          <p v-else-if="rules.length === 0">暂无积分规则</p>
          <p class="empty-hint" v-if="!searchTerm && rules.length === 0">
            点击"添加规则"按钮创建新的积分规则
          </p>
        </div>
      </div>
    </div>

    <!-- Add/Edit Rule Modal -->
    <div class="modal" v-if="showRuleModal" @click.self="closeRuleModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>
            <i class="fas fa-plus-circle"></i>
            {{ editingRule ? '编辑积分规则' : '添加积分规则' }}
          </h3>
          <button class="modal-close" @click="closeRuleModal">&times;</button>
        </div>
        <form @submit.prevent="submitRule">
          <div class="form-group">
            <label for="activityType"><i class="fas fa-tag"></i> 活动类型</label>
            <input
              type="text"
              id="activityType"
              v-model="form.activityType"
              placeholder="请输入活动类型（如：学术讲座、文体活动、志愿公益等）"
              required
            />
            <small class="form-hint">活动类型必须唯一，不能重复</small>
          </div>
          <div class="form-group">
            <label for="basePointsValue"><i class="fas fa-star"></i> 基础积分</label>
            <input
              type="number"
              id="basePointsValue"
              v-model.number="form.basePointsValue"
              placeholder="请输入基础积分值"
              min="1"
              max="100"
              required
            />
            <small class="form-hint">设置该类型活动参与后获得的积分值（1-100）</small>
          </div>
          <div class="form-actions">
            <button type="button" class="btn btn-secondary" @click="closeRuleModal">
              <i class="fas fa-times"></i> 取消
            </button>
            <button type="submit" class="btn btn-primary">
              <i class="fas fa-save"></i>
              {{ editingRule ? '更新规则' : '保存规则' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Delete Confirmation Modal -->
    <div class="modal" v-if="showDeleteModal" @click.self="closeDeleteModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3><i class="fas fa-exclamation-triangle"></i> 确认删除</h3>
          <button class="modal-close" @click="closeDeleteModal">&times;</button>
        </div>
        <div class="delete-body">
          <p>确定要删除这个积分规则吗？此操作不可恢复。</p>
          <p class="delete-warning">
            删除后，新创建的活动将无法选择此规则。
          </p>
        </div>
        <div class="form-actions">
          <button class="btn btn-secondary" @click="closeDeleteModal">
            <i class="fas fa-times"></i> 取消
          </button>
          <button class="btn btn-danger" @click="confirmDelete">
            <i class="fas fa-trash"></i> 确认删除
          </button>
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

const rules = ref([])
const searchTerm = ref('')
const showRuleModal = ref(false)
const showDeleteModal = ref(false)
const editingRule = ref(null)
const deletingRule = ref(null)

const form = ref({
  activityType: '',
  basePointsValue: null
})

const filteredRules = computed(() => {
  if (!searchTerm.value) return rules.value
  const term = searchTerm.value.toLowerCase()
  return rules.value.filter(r =>
    r.activityType.toLowerCase().includes(term)
  )
})

const stats = computed(() => {
  const list = rules.value
  if (list.length === 0) {
    return { totalRules: 0, avgPoints: '0', maxPoints: 0, minPoints: 0 }
  }
  const points = list.map(r => r.basePointsValue)
  const sum = points.reduce((a, b) => a + b, 0)
  return {
    totalRules: list.length,
    avgPoints: (sum / points.length).toFixed(1),
    maxPoints: Math.max(...points),
    minPoints: Math.min(...points)
  }
})

function goBack() {
  router.push('/dashboard')
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

function openAddModal() {
  editingRule.value = null
  form.value = { activityType: '', basePointsValue: null }
  showRuleModal.value = true
}

function openEditModal(rule) {
  editingRule.value = rule
  form.value = {
    activityType: rule.activityType,
    basePointsValue: rule.basePointsValue
  }
  showRuleModal.value = true
}

function closeRuleModal() {
  showRuleModal.value = false
  editingRule.value = null
}

function openDeleteModal(rule) {
  deletingRule.value = rule
  showDeleteModal.value = true
}

function closeDeleteModal() {
  showDeleteModal.value = false
  deletingRule.value = null
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

async function submitRule() {
  const activityType = form.value.activityType.trim()
  const basePointsValue = form.value.basePointsValue

  if (!activityType) {
    toast.warning('请输入活动类型')
    return
  }

  if (!basePointsValue || basePointsValue < 1 || basePointsValue > 100) {
    toast.warning('请输入有效的积分值（1-100）')
    return
  }

  const ruleData = { activityType, basePointsValue }

  try {
    let res
    if (editingRule.value) {
      res = await api.put(`/api/points/rules/${editingRule.value.ruleId}`, ruleData)
    } else {
      res = await api.post('/api/points/rules', ruleData)
    }

    if (res.data.success) {
      toast.success(editingRule.value ? '积分规则更新成功！' : '积分规则添加成功！')
      closeRuleModal()
      loadRules()
    } else {
      toast.error((editingRule.value ? '更新' : '添加') + '失败: ' + res.data.message)
    }
  } catch (e) {
    console.error('操作积分规则错误:', e)
    toast.error('操作时发生错误，请稍后重试')
  }
}

async function confirmDelete() {
  if (!deletingRule.value) return

  try {
    const res = await api.delete(`/api/points/rules/${deletingRule.value.ruleId}`)

    if (res.data.success) {
      toast.success('积分规则删除成功！')
      closeDeleteModal()
      loadRules()
    } else {
      toast.error('删除失败: ' + res.data.message)
    }
  } catch (e) {
    console.error('删除积分规则错误:', e)
    toast.error('删除时发生错误，请稍后重试')
  }
}

onMounted(() => {
  if (!auth.token) {
    router.push('/login')
    return
  }
  loadRules()
})
</script>

<style scoped>
.points-rules-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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
  margin-bottom: 30px;
  background: white;
  padding: 25px;
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  border-left: 4px solid #667eea;
}

.header h1 {
  color: #2d3748;
  font-size: 28px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.header h1 i {
  color: #667eea;
}

/* Buttons */
.btn {
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: inline-flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
}

.btn-secondary {
  background: #6c757d;
  color: white;
}

.btn-secondary:hover {
  background: #5a6268;
  transform: translateY(-2px);
}

.btn-warning {
  background: #ed8936;
  color: white;
}

.btn-warning:hover {
  background: #dd6b20;
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(237, 137, 54, 0.4);
}

.btn-danger {
  background: #f56565;
  color: white;
}

.btn-danger:hover {
  background: #e53e3e;
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(245, 101, 101, 0.4);
}

.btn-sm {
  padding: 8px 12px;
  font-size: 12px;
}

/* Stats Cards */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: white;
  border-radius: 16px;
  padding: 25px;
  text-align: center;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(0, 0, 0, 0.05);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 4px;
  background: linear-gradient(135deg, #667eea, #764ba2);
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 50px rgba(0, 0, 0, 0.15);
}

.stat-number {
  font-size: 32px;
  font-weight: bold;
  color: #667eea;
  margin-bottom: 10px;
}

.stat-label {
  color: #4a5568;
  font-size: 14px;
  font-weight: 500;
}

/* Card */
.card {
  background: white;
  border-radius: 16px;
  padding: 30px;
  margin-bottom: 25px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(0, 0, 0, 0.05);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.card:hover {
  transform: translateY(-2px);
  box-shadow: 0 15px 50px rgba(0, 0, 0, 0.15);
}

.card-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.card-header-row h3 {
  color: #2d3748;
  margin: 0;
  font-size: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.card-header-row h3 i {
  color: #667eea;
}

/* Search */
.search-box {
  display: flex;
  gap: 15px;
  margin-bottom: 25px;
  flex-wrap: wrap;
}

.search-box input {
  flex: 1;
  min-width: 250px;
  padding: 14px 18px;
  border: 2px solid #e2e8f0;
  border-radius: 10px;
  font-size: 14px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: #f7fafc;
}

.search-box input:focus {
  outline: none;
  border-color: #667eea;
  background: white;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
}

/* Rules List */
.rules-list {
  display: grid;
  gap: 15px;
}

.rule-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 25px;
  background: white;
  border-radius: 12px;
  border-left: 4px solid #667eea;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.rule-item:hover {
  transform: translateX(5px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

.rule-info h4 {
  color: #2d3748;
  margin-bottom: 5px;
  font-size: 18px;
}

.rule-details {
  font-size: 14px;
  color: #4a5568;
}

.rule-points {
  font-size: 24px;
  font-weight: bold;
  color: #667eea;
  text-shadow: 0 2px 10px rgba(102, 126, 234, 0.3);
}

.rule-actions {
  display: flex;
  gap: 10px;
  margin-left: 15px;
}

/* Empty State */
.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: #4a5568;
}

.empty-state i {
  font-size: 80px;
  margin-bottom: 20px;
  color: #e2e8f0;
}

.empty-state p {
  font-size: 18px;
  margin-bottom: 10px;
}

.empty-hint {
  color: #666;
  font-size: 14px !important;
}

/* Modal */
.modal {
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
}

.modal-content {
  background: white;
  border-radius: 16px;
  padding: 35px;
  max-width: 550px;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 30px 80px rgba(0, 0, 0, 0.3);
  animation: modalSlideIn 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(0, 0, 0, 0.05);
}

@keyframes modalSlideIn {
  from {
    opacity: 0;
    transform: scale(0.85);
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
  margin-bottom: 25px;
  padding-bottom: 20px;
  border-bottom: 2px solid #f7fafc;
}

.modal-header h3 {
  color: #2d3748;
  margin: 0;
  font-size: 22px;
}

.modal-close {
  background: none;
  border: none;
  font-size: 28px;
  cursor: pointer;
  color: #4a5568;
  padding: 0;
  width: 35px;
  height: 35px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.modal-close:hover {
  background: #f7fafc;
  color: #2d3748;
  transform: rotate(90deg);
}

/* Form */
.form-group {
  margin-bottom: 25px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 600;
  color: #4a5568;
  font-size: 14px;
}

.form-group input {
  width: 100%;
  padding: 14px 18px;
  border: 2px solid #e2e8f0;
  border-radius: 10px;
  font-size: 14px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: #f7fafc;
  box-sizing: border-box;
}

.form-group input:focus {
  outline: none;
  border-color: #667eea;
  background: white;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
}

.form-hint {
  color: #666;
  margin-top: 5px;
  display: block;
}

.form-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  margin-top: 25px;
}

.delete-body {
  padding: 20px 0;
}

.delete-body p {
  margin: 0;
}

.delete-warning {
  color: #666;
  font-size: 14px;
  margin-top: 10px !important;
}

/* Responsive */
@media (max-width: 768px) {
  .container {
    padding: 15px;
  }

  .header {
    flex-direction: column;
    gap: 15px;
    padding: 20px 25px;
    margin-bottom: 25px;
    text-align: center;
  }

  .header h1 {
    font-size: 24px;
  }

  .header h1 i {
    font-size: 28px;
  }

  .card {
    padding: 25px;
  }

  .card-header-row {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
  }

  .card-header-row h3 {
    font-size: 20px;
  }

  .stats-cards {
    grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
    gap: 15px;
  }

  .stat-card {
    padding: 20px;
  }

  .stat-number {
    font-size: 28px;
  }

  .rule-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  .rule-points {
    align-self: flex-end;
  }

  .search-box {
    flex-direction: column;
  }

  .search-box input {
    width: 100%;
    min-width: auto;
  }

  .btn {
    width: 100%;
    justify-content: center;
  }

  .modal-content {
    padding: 25px;
    margin: 20px;
  }

  .form-actions {
    flex-direction: column;
  }
}
</style>
