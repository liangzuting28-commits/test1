<template>
  <div class="my-points-page">
    <div class="container">
      <div class="header">
        <h1><i class="fas fa-star"></i> 我的积分</h1>
        <div style="display: flex; gap: 10px;">
          <button class="btn btn-primary" @click="loadMyPoints">
            <i class="fas fa-sync-alt"></i> 刷新数据
          </button>
          <button class="btn btn-primary" @click="$router.push('/dashboard')">
            <i class="fas fa-arrow-left"></i> 返回仪表盘
          </button>
        </div>
      </div>

      <div class="stats-cards">
        <div class="stat-card">
          <div class="stat-number">{{ totalPoints }}</div>
          <div class="stat-label">当前总积分</div>
          <div class="stat-desc">累计获得的所有积分</div>
        </div>
        <div class="stat-card">
          <div class="stat-number">{{ totalRecords }}</div>
          <div class="stat-label">积分记录数</div>
          <div class="stat-desc">参与活动获得的积分次数</div>
        </div>
        <div class="stat-card">
          <div class="stat-number">{{ monthlyPoints }}</div>
          <div class="stat-label">本月获得积分</div>
          <div class="stat-desc">本月参与活动获得的积分</div>
        </div>
        <div class="stat-card">
          <div class="stat-number">{{ avgPoints }}</div>
          <div class="stat-label">平均每次活动积分</div>
          <div class="stat-desc">每次活动获得的平均积分</div>
        </div>
      </div>

      <div class="points-history">
        <h3><i class="fas fa-history"></i> 积分记录</h3>
        <div v-if="loading" class="loading"><i class="fas fa-spinner fa-spin"></i></div>
        <template v-else-if="records.length > 0">
          <div class="history-item" v-for="record in records" :key="record.recordId || record.activityId">
            <div class="history-info">
              <h4>{{ record.activityName || (record.activityType ? record.activityType + '活动' : '活动ID: ' + record.activityId) }}</h4>
              <div class="history-details">
                <div><i class="fas fa-map-marker-alt"></i> 地点: {{ record.location || '未知' }}</div>
                <div><i class="fas fa-calendar-alt"></i> 开始时间: {{ formatDateTime(record.startTime) }}</div>
                <div><i class="fas fa-check-circle"></i> 签到成功时间: {{ formatDateTime(record.earnedTime) }}</div>
                <div v-if="record.activityType"><i class="fas fa-tag"></i> 类型: {{ record.activityType }}</div>
                <div v-if="record.clubName"><i class="fas fa-users"></i> 社团: {{ record.clubName }}</div>
              </div>
            </div>
            <div class="history-points">+{{ record.pointsEarned }} 积分</div>
          </div>
        </template>
        <div v-else class="empty-state">
          <i class="fas fa-history"></i>
          <p>暂无积分记录</p>
          <p class="subtext">参与活动并签到后，积分将在活动结束后由社长统一发放</p>
          <div class="empty-tips">
            <h5>获取积分小贴士：</h5>
            <ul>
              <li>积极参加社团组织的各类活动</li>
              <li>活动开始前按时签到</li>
              <li>不同类型的活动获得的积分不同</li>
              <li>积分可以用于兑换社团福利</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import api from '../api'

const router = useRouter()
const auth = useAuthStore()
const totalPoints = ref(0)
const totalRecords = ref(0)
const monthlyPoints = ref(0)
const avgPoints = ref(0)
const records = ref([])
const loading = ref(true)

function formatDateTime(d) {
  if (!d) return '未知时间'
  return new Date(d).toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

async function loadMyPoints() {
  loading.value = true
  try {
    const totalRes = await api.get(`/api/points/student/${auth.user.studentId}/total`)
    if (totalRes.data.success) totalPoints.value = totalRes.data.data

    const historyRes = await api.get(`/api/points/student/${auth.user.studentId}`)
    if (historyRes.data.success && historyRes.data.data?.length > 0) {
      records.value = historyRes.data.data
      totalRecords.value = historyRes.data.data.length
      const currentMonth = new Date().getMonth()
      monthlyPoints.value = historyRes.data.data.filter(r => new Date(r.earnedTime).getMonth() === currentMonth).reduce((s, r) => s + r.pointsEarned, 0)
      const totalSum = historyRes.data.data.reduce((s, r) => s + r.pointsEarned, 0)
      avgPoints.value = Math.round(totalSum / historyRes.data.data.length)
    } else {
      records.value = []; totalRecords.value = 0; monthlyPoints.value = 0; avgPoints.value = 0
    }
  } catch (e) { console.error('加载积分数据错误:', e) }
  loading.value = false
}

onMounted(() => { if (!auth.token) { router.push('/login'); return } loadMyPoints() })
</script>

<style scoped>
* { margin: 0; padding: 0; box-sizing: border-box; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; }
.my-points-page { background: linear-gradient(135deg, #667eea15 0%, #764ba215 100%); color: #1f2937; line-height: 1.6; min-height: 100vh; }
.container { max-width: 1200px; margin: 0 auto; padding: 20px; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 30px; background: #fff; padding: 24px 30px; border-radius: 16px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); border-top: 4px solid #667eea; }
.header h1 { font-size: 28px; font-weight: 700; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; display: flex; align-items: center; gap: 12px; }
.header h1 i { font-size: 32px; }
.btn { padding: 10px 20px; border: none; border-radius: 6px; cursor: pointer; font-size: 14px; font-weight: 500; transition: all 0.3s ease; display: flex; align-items: center; gap: 8px; text-decoration: none; }
.btn-primary { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
.btn-primary:hover { transform: translateY(-2px); box-shadow: 0 4px 6px rgba(0,0,0,0.1); }
.stats-cards { display: grid; grid-template-columns: repeat(auto-fit, minmax(260px, 1fr)); gap: 25px; margin-bottom: 30px; }
.stat-card { background: #fff; border-radius: 16px; padding: 25px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); text-align: center; transition: all 0.3s ease; position: relative; overflow: hidden; }
.stat-card::before { content: ''; position: absolute; top: 0; left: 0; right: 0; height: 4px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.stat-card:hover { transform: translateY(-5px); box-shadow: 0 20px 25px rgba(0,0,0,0.1); }
.stat-number { font-size: 36px; font-weight: 700; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; margin-bottom: 10px; }
.stat-label { color: #6b7280; font-size: 14px; margin-bottom: 5px; font-weight: 500; }
.stat-desc { color: #9ca3af; font-size: 12px; }
.points-history { background: #fff; border-radius: 16px; padding: 30px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }
.points-history h3 { color: #1f2937; margin-bottom: 25px; font-size: 20px; font-weight: 600; display: flex; align-items: center; gap: 10px; }
.points-history h3 i { color: #667eea; }
.history-item { display: flex; justify-content: space-between; align-items: center; padding: 20px; border-bottom: 1px solid #f3f4f6; transition: all 0.3s ease; border-radius: 6px; }
.history-item:last-child { border-bottom: none; }
.history-item:hover { background: #f8fafc; }
.history-info h4 { color: #1f2937; margin-bottom: 8px; font-size: 16px; font-weight: 600; }
.history-details { color: #6b7280; font-size: 14px; display: flex; flex-direction: column; gap: 5px; }
.history-details div { display: flex; align-items: center; gap: 8px; }
.history-details i { color: #667eea; width: 18px; text-align: center; font-size: 16px; flex-shrink: 0; }
.history-points { font-size: 20px; font-weight: 700; color: #4ade80; background: linear-gradient(135deg, #dcfce7 0%, #f0fdf4 100%); padding: 8px 16px; border-radius: 20px; }
.empty-state { text-align: center; padding: 60px 20px; color: #6b7280; }
.empty-state i { font-size: 64px; color: #e5e7eb; margin-bottom: 20px; display: block; }
.empty-state p { margin-bottom: 10px; font-size: 16px; color: #6b7280; }
.subtext { color: #9ca3af; font-size: 14px; }
.empty-tips { margin-top: 30px; text-align: left; max-width: 400px; margin-left: auto; margin-right: auto; background: #f8fafc; padding: 20px; border-radius: 10px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
.empty-tips h5 { margin-bottom: 10px; color: #1f2937; font-size: 16px; }
.empty-tips ul { padding-left: 20px; }
.empty-tips li { margin-bottom: 5px; color: #6b7280; font-size: 14px; }
.loading { display: flex; justify-content: center; align-items: center; padding: 60px 0; }
.loading i { font-size: 32px; color: #667eea; animation: spin 1s linear infinite; }
@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }
@media (max-width: 768px) { .container { padding: 15px; } .header { padding: 20px; margin-bottom: 20px; flex-direction: column; gap: 15px; } .header h1 { font-size: 24px; } .stats-cards { grid-template-columns: repeat(2, 1fr); gap: 15px; } .stat-card { padding: 20px; } .stat-number { font-size: 28px; } .points-history { padding: 20px; } .history-item { flex-direction: column; align-items: flex-start; gap: 15px; } .history-points { align-self: flex-end; } }
</style>
