<template>
  <div class="create-activity-page">
    <div class="container">
      <div class="header">
        <h1><i class="fas fa-plus-circle"></i> 创建新活动</h1>
        <button class="btn-back" @click="$router.push('/dashboard')">
          <i class="fas fa-arrow-left"></i> 返回仪表盘
        </button>
      </div>

      <div class="card">
        <h3><i class="fas fa-rocket"></i> 创建精彩活动</h3>
        <form @submit.prevent="createActivity">
          <div class="form-group">
            <label><i class="fas fa-heading"></i> 活动名称</label>
            <input type="text" v-model="form.activityName" placeholder="请输入活动名称（例如：秋季招新宣讲会）" required>
          </div>

          <div class="form-group">
            <label><i class="fas fa-users"></i> 所属社团</label>
            <select v-model="form.clubId" required>
              <option value="">请选择社团</option>
              <option v-for="club in userClubs" :key="club.clubId" :value="club.clubId">{{ club.clubName }}</option>
            </select>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label><i class="fas fa-tag"></i> 活动类型</label>
              <select v-model="form.activityType" required @change="updateAwardPoints">
                <option value="">请选择活动类型</option>
                <option v-for="t in activityTypes" :key="t.activityType" :value="t.activityType" :data-points="t.basePointsValue">
                  {{ t.activityType }} ({{ t.basePointsValue }}积分)
                </option>
              </select>
              <small><i class="fas fa-info-circle"></i> 选择活动类型后，系统将自动设置对应的积分奖励</small>
            </div>
            <div class="form-group">
              <label><i class="fas fa-star"></i> 奖励积分</label>
              <input type="number" v-model.number="form.awardPoints" required min="1" max="100" readonly>
              <small><i class="fas fa-info-circle"></i> 积分值根据活动类型自动设置，范围1-100</small>
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label><i class="fas fa-clock"></i> 活动时间</label>
              <input type="datetime-local" v-model="form.activityTime" required>
              <small><i class="fas fa-info-circle"></i> 请选择具体的活动日期和时间</small>
            </div>
            <div class="form-group">
              <label><i class="fas fa-map-marker-alt"></i> 活动地点</label>
              <input type="text" v-model="form.location" placeholder="请输入活动地点（例如：学术报告厅）" required>
              <small><i class="fas fa-info-circle"></i> 请详细填写活动地点，方便成员查找</small>
            </div>
          </div>

          <div class="form-group">
            <label><i class="fas fa-user-friends"></i> 人数限制</label>
            <input type="number" v-model.number="form.maxParticipants" min="0" max="1000" placeholder="0表示无限制">
            <small><i class="fas fa-info-circle"></i> 设置活动最大参与人数，0表示无限制</small>
          </div>

          <div class="form-group">
            <label><i class="fas fa-file-alt"></i> 活动描述</label>
            <textarea v-model="form.description" placeholder="请详细描述活动内容、流程、注意事项等..."></textarea>
            <small><i class="fas fa-info-circle"></i> 详细的活动描述有助于成员更好地了解活动</small>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label><i class="fas fa-image"></i> 活动宣传图</label>
              <div class="file-upload-area" @click="$refs.posterInput.click()" @dragover.prevent @drop.prevent="onDropPoster">
                <div v-if="posterPreview" class="file-preview">
                  <img :src="posterPreview" alt="宣传图预览" />
                  <button class="file-remove" @click.stop="removePoster">&times;</button>
                </div>
                <div v-else class="file-placeholder">
                  <i class="fas fa-cloud-upload-alt"></i>
                  <p>点击或拖拽上传宣传图</p>
                  <span>支持 JPG、PNG 格式</span>
                </div>
              </div>
              <input ref="posterInput" type="file" accept="image/*" style="display:none" @change="onPosterChange">
            </div>
            <div class="form-group">
              <label><i class="fas fa-file-pdf"></i> 活动策划书</label>
              <div class="file-upload-area" @click="$refs.planInput.click()" @dragover.prevent @drop.prevent="onDropPlan">
                <div v-if="planFile" class="file-preview file-doc">
                  <i class="fas fa-file-alt"></i>
                  <span>{{ planFile.name }}</span>
                  <button class="file-remove" @click.stop="removePlan">&times;</button>
                </div>
                <div v-else class="file-placeholder">
                  <i class="fas fa-cloud-upload-alt"></i>
                  <p>点击或拖拽上传策划书</p>
                  <span>支持 PDF、Word 格式</span>
                </div>
              </div>
              <input ref="planInput" type="file" accept=".pdf,.doc,.docx" style="display:none" @change="onPlanChange">
            </div>
          </div>

          <div v-if="errorMsg" class="error-message"><i class="fas fa-exclamation-triangle"></i> {{ errorMsg }}</div>

          <div class="form-actions">
            <button type="button" class="btn btn-secondary" @click="$router.push('/dashboard')">
              <i class="fas fa-times"></i> 取消
            </button>
            <button type="submit" class="btn btn-primary" :disabled="submitting">
              <i :class="submitting ? 'fas fa-spinner fa-spin' : 'fas fa-paper-plane'"></i>
              {{ submitting ? '提交中...' : '提交审核' }}
            </button>
          </div>
        </form>
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
const userClubs = ref([])
const activityTypes = ref([])
const submitting = ref(false)
const errorMsg = ref('')
const form = ref({
  activityName: '', clubId: '', activityType: '', activityTime: '',
  location: '', maxParticipants: 50, awardPoints: 0, description: ''
})

const posterFile = ref(null)
const posterPreview = ref('')
const planFile = ref(null)

function onPosterChange(e) {
  const file = e.target.files[0]
  if (file) {
    posterFile.value = file
    posterPreview.value = URL.createObjectURL(file)
  }
}

function onDropPoster(e) {
  const file = e.dataTransfer.files[0]
  if (file && file.type.startsWith('image/')) {
    posterFile.value = file
    posterPreview.value = URL.createObjectURL(file)
  }
}

function removePoster() {
  posterFile.value = null
  posterPreview.value = ''
}

function onPlanChange(e) {
  const file = e.target.files[0]
  if (file) planFile.value = file
}

function onDropPlan(e) {
  const file = e.dataTransfer.files[0]
  if (file) planFile.value = file
}

function removePlan() {
  planFile.value = null
}

function updateAwardPoints() {
  const selected = activityTypes.value.find(t => t.activityType === form.value.activityType)
  form.value.awardPoints = selected ? selected.basePointsValue : 0
}

async function loadClubs() {
  try {
    const res = await api.get('/api/clubs/my-clubs')
    if (res.data.success && res.data.data) {
      userClubs.value = res.data.data
      if (userClubs.value.length === 1) form.value.clubId = userClubs.value[0].clubId
    }
  } catch (e) { console.error('加载社团失败:', e) }
}

async function loadActivityTypes() {
  try {
    const res = await api.get('/api/points/rules')
    if (res.data.success && res.data.data) {
      activityTypes.value = res.data.data
    } else {
      activityTypes.value = [
        { activityType: '学术讲座', basePointsValue: 5 },
        { activityType: '文体活动', basePointsValue: 3 },
        { activityType: '志愿公益', basePointsValue: 8 },
        { activityType: '竞赛获奖', basePointsValue: 10 },
        { activityType: '社会实践', basePointsValue: 6 }
      ]
    }
  } catch (e) {
    activityTypes.value = [
      { activityType: '学术讲座', basePointsValue: 5 },
      { activityType: '文体活动', basePointsValue: 3 },
      { activityType: '志愿公益', basePointsValue: 8 },
      { activityType: '竞赛获奖', basePointsValue: 10 },
      { activityType: '社会实践', basePointsValue: 6 }
    ]
  }
}

async function createActivity() {
  errorMsg.value = ''
  if (!form.value.activityName.trim()) { errorMsg.value = '请输入活动名称'; return }
  if (!form.value.clubId) { errorMsg.value = '请选择所属社团'; return }
  if (!form.value.activityType) { errorMsg.value = '请选择活动类型'; return }
  if (!form.value.activityTime) { errorMsg.value = '请选择活动时间'; return }
  if (!form.value.location.trim()) { errorMsg.value = '请输入活动地点'; return }
  if (!form.value.awardPoints || form.value.awardPoints < 1) { errorMsg.value = '请选择有效的活动类型以设置积分'; return }

  submitting.value = true
  try {
    const formData = new FormData()
    const data = {
      activityName: form.value.activityName,
      clubId: parseInt(form.value.clubId),
      activityType: form.value.activityType,
      activityTime: form.value.activityTime,
      location: form.value.location,
      maxParticipants: form.value.maxParticipants || 0,
      awardPoints: form.value.awardPoints,
      description: form.value.description
    }
    formData.append('data', new Blob([JSON.stringify(data)], { type: 'application/json' }))
    if (posterFile.value) formData.append('posterFile', posterFile.value)
    if (planFile.value) formData.append('planFile', planFile.value)

    const res = await api.post('/api/activity-manage/create', formData)
    if (res.data.success) {
      toast.success('活动创建成功！等待老师审核。')
      router.push('/dashboard')
    } else {
      errorMsg.value = res.data.message || '创建失败'
    }
  } catch (e) { errorMsg.value = '创建活动时发生错误，请稍后重试' }
  submitting.value = false
}

onMounted(() => {
  if (!auth.token) { router.push('/login'); return }
  if (auth.user.role !== 'PRESIDENT') { toast.warning('您没有权限创建活动'); router.push('/dashboard'); return }
  loadClubs()
  loadActivityTypes()
})
</script>

<style scoped>
* { margin: 0; padding: 0; box-sizing: border-box; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; }
.create-activity-page { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); min-height: 100vh; color: #333; padding: 20px 0; }
.container { max-width: 900px; margin: 0 auto; padding: 0 20px; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 30px; background: rgba(255,255,255,0.95); backdrop-filter: blur(10px); padding: 30px; border-radius: 25px; box-shadow: 0 12px 40px rgba(0,0,0,0.15); border-left: 5px solid #667eea; transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275); }
.header:hover { transform: translateY(-5px); box-shadow: 0 20px 60px rgba(0,0,0,0.2); }
.header h1 { color: #333; font-size: 36px; display: flex; align-items: center; gap: 18px; font-weight: 700; animation: fadeInLeft 0.8s ease; }
@keyframes fadeInLeft { from { opacity: 0; transform: translateX(-30px); } to { opacity: 1; transform: translateX(0); } }
.header h1 i { color: #667eea; font-size: 42px; animation: pulse 2s infinite; }
@keyframes pulse { 0%, 100% { transform: scale(1); } 50% { transform: scale(1.1); } }
.btn-back { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); color: white; padding: 12px 24px; border-radius: 12px; border: none; font-weight: 600; font-size: 14px; display: flex; align-items: center; gap: 8px; cursor: pointer; transition: all 0.3s ease; box-shadow: 0 4px 15px rgba(245,87,108,0.3); animation: fadeInRight 0.8s ease; }
@keyframes fadeInRight { from { opacity: 0; transform: translateX(30px); } to { opacity: 1; transform: translateX(0); } }
.btn-back:hover { transform: translateY(-2px); box-shadow: 0 8px 25px rgba(245,87,108,0.5); }
.card { background: rgba(255,255,255,0.95); backdrop-filter: blur(10px); border-radius: 25px; padding: 45px; margin-bottom: 30px; box-shadow: 0 12px 40px rgba(0,0,0,0.15); border: 1px solid rgba(255,255,255,0.2); transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275); animation: fadeInUp 1s ease; }
@keyframes fadeInUp { from { opacity: 0; transform: translateY(50px); } to { opacity: 1; transform: translateY(0); } }
.card:hover { transform: translateY(-5px); box-shadow: 0 20px 60px rgba(0,0,0,0.2); }
.card h3 { color: #333; margin-bottom: 35px; font-size: 28px; display: flex; align-items: center; gap: 15px; font-weight: 700; padding-bottom: 20px; border-bottom: 3px solid rgba(102,126,234,0.1); background: linear-gradient(135deg, #667eea, #764ba2); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; }
.card h3 i { color: #667eea; font-size: 32px; -webkit-text-fill-color: #667eea; animation: bounce 2s infinite; }
@keyframes bounce { 0%, 20%, 50%, 80%, 100% { transform: translateY(0); } 40% { transform: translateY(-10px); } 60% { transform: translateY(-5px); } }
.form-row { display: grid; grid-template-columns: repeat(auto-fit, minmax(320px, 1fr)); gap: 30px; margin-bottom: 0; }
.form-group { margin-bottom: 30px; position: relative; }
.form-group label { display: block; margin-bottom: 12px; font-weight: 600; color: #444; font-size: 15px; transition: all 0.3s ease; position: relative; padding-left: 30px; }
.form-group label::before { content: ''; position: absolute; left: 0; top: 50%; transform: translateY(-50%); width: 20px; height: 20px; background: linear-gradient(135deg, #667eea, #764ba2); border-radius: 6px; opacity: 0.2; transition: all 0.3s ease; }
.form-group label i { color: #667eea; position: absolute; left: 2px; top: 50%; transform: translateY(-50%); z-index: 1; transition: all 0.3s ease; }
.form-group input, .form-group select, .form-group textarea { width: 100%; padding: 18px 25px; border: 2px solid rgba(102,126,234,0.2); border-radius: 15px; font-size: 15px; transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275); background: rgba(255,255,255,0.8); color: #333; font-weight: 500; box-shadow: 0 2px 10px rgba(0,0,0,0.05); }
.form-group input:focus, .form-group select:focus, .form-group textarea:focus { outline: none; border-color: #667eea; background: white; box-shadow: 0 0 0 5px rgba(102,126,234,0.15), 0 8px 30px rgba(102,126,234,0.2); transform: translateY(-2px); }
.form-group textarea { height: 180px; resize: vertical; line-height: 1.8; font-family: inherit; }
select { appearance: none; background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='18' height='18' fill='%23667eea' viewBox='0 0 16 16'%3E%3Cpath d='M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z'/%3E%3C/svg%3E"); background-repeat: no-repeat; background-position: right 25px center; cursor: pointer; }
.btn { padding: 16px 35px; border: none; border-radius: 15px; cursor: pointer; font-size: 16px; font-weight: 700; transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275); display: inline-flex; align-items: center; gap: 12px; min-width: 130px; justify-content: center; position: relative; overflow: hidden; }
.btn::before { content: ''; position: absolute; top: 0; left: 0; width: 100%; height: 100%; background: linear-gradient(135deg, rgba(255,255,255,0.1), rgba(255,255,255,0.05)); transform: translateX(-100%); transition: transform 0.5s ease; }
.btn:hover::before { transform: translateX(100%); }
.btn-primary { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; box-shadow: 0 6px 20px rgba(102,126,234,0.4); }
.btn-primary:hover { transform: translateY(-3px); box-shadow: 0 12px 35px rgba(102,126,234,0.6); }
.btn-primary:disabled { opacity: 0.7; cursor: not-allowed; transform: none; }
.btn-secondary { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); color: white; box-shadow: 0 6px 20px rgba(245,87,108,0.4); }
.btn-secondary:hover { transform: translateY(-3px); box-shadow: 0 12px 35px rgba(245,87,108,0.6); }
.error-message { background: linear-gradient(135deg, rgba(248,215,218,0.9), rgba(245,87,108,0.1)); color: #721c24; padding: 20px 25px; border-radius: 15px; margin: 20px 0; border-left: 6px solid #dc3545; font-size: 15px; box-shadow: 0 6px 20px rgba(220,53,69,0.2); animation: slideIn 0.5s ease; }
@keyframes slideIn { from { opacity: 0; transform: translateX(-30px); } to { opacity: 1; transform: translateX(0); } }
.error-message i { margin-right: 10px; color: #dc3545; }
.form-actions { display: flex; justify-content: flex-end; gap: 20px; margin-top: 50px; padding-top: 35px; border-top: 2px solid rgba(102,126,234,0.1); }
small { color: #666; margin-top: 10px; display: block; font-size: 13px; line-height: 1.6; background: rgba(102,126,234,0.05); padding: 8px 15px; border-radius: 8px; border-left: 3px solid #667eea; transition: all 0.3s ease; }
small:hover { background: rgba(102,126,234,0.1); transform: translateX(5px); }
small i { color: #667eea; margin-right: 6px; }
.file-upload-area { border: 2px dashed rgba(102,126,234,0.3); border-radius: 15px; padding: 30px; text-align: center; cursor: pointer; transition: all 0.3s ease; background: rgba(102,126,234,0.03); min-height: 160px; display: flex; align-items: center; justify-content: center; }
.file-upload-area:hover { border-color: #667eea; background: rgba(102,126,234,0.08); transform: translateY(-2px); }
.file-placeholder i { font-size: 40px; color: #667eea; margin-bottom: 12px; display: block; }
.file-placeholder p { color: #555; font-size: 15px; margin-bottom: 6px; }
.file-placeholder span { color: #999; font-size: 13px; }
.file-preview { position: relative; width: 100%; }
.file-preview img { max-width: 100%; max-height: 200px; border-radius: 10px; object-fit: contain; }
.file-remove { position: absolute; top: -10px; right: -10px; width: 28px; height: 28px; border-radius: 50%; background: #f5576c; color: white; border: none; font-size: 16px; cursor: pointer; display: flex; align-items: center; justify-content: center; box-shadow: 0 2px 8px rgba(245,87,108,0.4); }
.file-doc { display: flex; align-items: center; gap: 12px; padding: 10px; }
.file-doc i { font-size: 32px; color: #667eea; }
.file-doc span { color: #333; font-size: 14px; word-break: break-all; }
@media (max-width: 768px) { .create-activity-page { padding: 15px 0; } .container { padding: 0 15px; } .header { flex-direction: column; gap: 25px; text-align: center; padding: 25px; } .header h1 { font-size: 28px; } .header h1 i { font-size: 36px; } .card { padding: 30px 25px; } .card h3 { font-size: 24px; margin-bottom: 25px; } .form-row { grid-template-columns: 1fr; gap: 25px; } .form-actions { flex-direction: column; gap: 15px; } .btn { width: 100%; padding: 18px; font-size: 17px; } .form-group { margin-bottom: 25px; } .form-group input, .form-group select, .form-group textarea { padding: 16px 20px; } }
html { scroll-behavior: smooth; }
</style>
