<template>
  <div class="checkin-page">
    <div class="container">
      <div class="header">
        <h1><i class="fas fa-qrcode"></i> 活动签到</h1>
        <button class="btn btn-primary" @click="$router.push('/dashboard')">
          <i class="fas fa-arrow-left"></i> 返回
        </button>
      </div>

      <div class="tabs">
        <div :class="['tab', { active: activeTab === 'scan' }]" @click="switchTab('scan')">
          <i class="fas fa-camera"></i> 扫码签到
        </div>
        <div :class="['tab', { active: activeTab === 'manual' }]" @click="switchTab('manual')">
          <i class="fas fa-keyboard"></i> 输入签到数
        </div>
      </div>

      <div v-show="activeTab === 'scan'" class="card">
        <div id="video-container">
          <video id="video" ref="videoRef"></video>
          <div class="scan-region"><div class="scan-line"></div></div>
        </div>
        <button v-if="!scanning" class="btn btn-primary" @click="startScanning">
          <i class="fas fa-camera"></i> 开始扫描
        </button>
        <button v-else class="btn" style="background: #f44336; color: white; margin-top: 10px;" @click="stopScanning">
          <i class="fas fa-stop"></i> 停止扫描
        </button>
        <div v-if="scanResult" style="margin-top: 15px;">
          <div v-if="scanResult.success" class="success-message">
            <i class="fas fa-check-circle"></i> {{ scanResult.message }}
            <div v-if="scanResult.activityName">活动: {{ scanResult.activityName }}</div>
            <div v-if="scanResult.checkInTime">签到时间: {{ formatDateTime(scanResult.checkInTime) }}</div>
          </div>
          <div v-else class="error-message">
            <i class="fas fa-exclamation-circle"></i> {{ scanResult.message }}
          </div>
        </div>
      </div>

      <div v-show="activeTab === 'manual'" class="card">
        <div class="form-group">
          <label for="checkInNumber">签到数</label>
          <input type="text" id="checkInNumber" v-model="checkInNumber" placeholder="请输入6位签到数" maxlength="6">
        </div>
        <button class="btn btn-success" @click="manualCheckIn">
          <i class="fas fa-check"></i> 提交签到
        </button>
        <div v-if="manualResult" style="margin-top: 15px;">
          <div v-if="manualResult.success" class="success-message">
            <i class="fas fa-check-circle"></i> {{ manualResult.message }}
            <div v-if="manualResult.activityName">活动: {{ manualResult.activityName }}</div>
            <div v-if="manualResult.checkInTime">签到时间: {{ formatDateTime(manualResult.checkInTime) }}</div>
          </div>
          <div v-else class="error-message">
            <i class="fas fa-exclamation-circle"></i> {{ manualResult.message }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useToastStore } from '../stores/toast'
import api from '../api'

const router = useRouter()
const auth = useAuthStore()
const toast = useToastStore()
const activeTab = ref('scan')
const scanning = ref(false)
const checkInNumber = ref('')
const scanResult = ref(null)
const manualResult = ref(null)
const videoRef = ref(null)
let codeReader = null
let currentStream = null

function formatDateTime(d) { return d ? new Date(d).toLocaleString('zh-CN') : '' }

function switchTab(tab) {
  if (tab !== 'scan' && currentStream) stopScanning()
  activeTab.value = tab
  scanResult.value = null
  manualResult.value = null
}

async function startScanning() {
  try {
    if (!codeReader) {
      const ZXing = await import('@zxing/library')
      codeReader = new ZXing.BrowserMultiFormatReader()
    }
    scanning.value = true
    currentStream = await navigator.mediaDevices.getUserMedia({ video: { facingMode: 'environment' } })
    videoRef.value.srcObject = currentStream
    videoRef.value.play()
    codeReader.decodeFromVideoDevice(undefined, videoRef.value, (result) => {
      if (result) { handleScanResult(result.text); stopScanning() }
    })
  } catch (e) { toast.error('无法访问摄像头，请检查权限设置'); scanning.value = false }
}

function stopScanning() {
  if (currentStream) { currentStream.getTracks().forEach(t => t.stop()); currentStream = null }
  if (codeReader) codeReader.reset()
  scanning.value = false
}

async function handleScanResult(text) {
  const parts = text.split(':')
  if (parts.length < 4 || parts[0] !== 'CLUB_ACTIVITY_CHECKIN') {
    scanResult.value = { success: false, message: '无效的二维码' }; return
  }
  await performCheckIn(parts[1], parts[2], 'scan')
}

async function manualCheckIn() {
  if (!checkInNumber.value.trim()) { manualResult.value = { success: false, message: '请输入签到数' }; return }
  if (checkInNumber.value.length !== 6) { manualResult.value = { success: false, message: '签到数必须是6位数字' }; return }
  try {
    const res = await api.get(`/api/checkin/activity/by-number/${checkInNumber.value}`)
    if (!res.data.success) { manualResult.value = { success: false, message: '找不到对应的活动' }; return }
    await performCheckIn(res.data.data.activityId, checkInNumber.value, 'manual')
  } catch (e) { manualResult.value = { success: false, message: '签到时发生错误' } }
}

async function performCheckIn(activityId, code, target) {
  try {
    const res = await api.post('/api/checkin/manual', { code, activityId: activityId.toString() }, { headers: { 'X-User-Id': auth.user.studentId } })
    if (res.data.success) {
      const d = res.data.data
      const r = { success: true, message: d.message, activityName: d.activityName, checkInTime: d.checkInTime }
      if (target === 'scan') scanResult.value = r; else manualResult.value = r
    } else {
      let msg = res.data.message
      if (msg?.includes('您未报名该活动')) msg = '您未报名该活动，请先报名后再签到'
      else if (msg?.includes('您已经签到过了')) msg = '您已经签到过了，无需重复签到'
      else if (msg?.includes('活动尚未开始或已结束')) msg = '活动尚未开始或已结束，无法签到'
      const r = { success: false, message: msg }
      if (target === 'scan') scanResult.value = r; else manualResult.value = r
    }
  } catch (e) {
    const r = { success: false, message: '签到时发生错误' }
    if (target === 'scan') scanResult.value = r; else manualResult.value = r
  }
}

onMounted(() => { if (!auth.token) router.push('/login') })
onUnmounted(() => { stopScanning() })
</script>

<style scoped>
* { margin: 0; padding: 0; box-sizing: border-box; font-family: 'Segoe UI', 'Microsoft YaHei', Tahoma, Geneva, Verdana, sans-serif; }
.checkin-page { background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%); color: #333; padding-bottom: 60px; min-height: 100vh; }
.container { max-width: 600px; margin: 0 auto; padding: 20px; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 30px; background: rgba(255,255,255,0.95); padding: 25px; border-radius: 12px; box-shadow: 0 8px 32px rgba(0,0,0,0.1); backdrop-filter: blur(10px); border: 1px solid rgba(255,255,255,0.2); }
.header h1 { font-size: 24px; font-weight: 700; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; }
.tabs { display: flex; margin-bottom: 25px; background: rgba(255,255,255,0.95); border-radius: 12px; padding: 6px; box-shadow: 0 8px 32px rgba(0,0,0,0.1); backdrop-filter: blur(10px); }
.tab { flex: 1; padding: 15px 20px; text-align: center; cursor: pointer; border-radius: 9px; transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1); font-weight: 500; position: relative; overflow: hidden; }
.tab::before { content: ''; position: absolute; inset: 0; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); opacity: 0; transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1); z-index: -1; }
.tab:hover { transform: translateY(-2px); }
.tab.active { color: white; }
.tab.active::before { opacity: 1; }
.card { background: rgba(255,255,255,0.95); border-radius: 12px; padding: 25px; margin-bottom: 25px; box-shadow: 0 8px 32px rgba(0,0,0,0.1); transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1); backdrop-filter: blur(10px); border: 1px solid rgba(255,255,255,0.2); }
.form-group { margin-bottom: 25px; }
label { display: block; margin-bottom: 10px; font-weight: 600; color: #555; font-size: 14px; text-transform: uppercase; letter-spacing: 0.5px; }
input { width: 100%; padding: 15px 20px; border: 2px solid #e2e8f0; border-radius: 12px; font-size: 16px; transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1); background: rgba(255,255,255,0.8); }
input:focus { outline: none; border-color: #667eea; box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1); background: white; }
.btn { padding: 15px 25px; border: none; border-radius: 12px; cursor: pointer; font-size: 16px; font-weight: 600; transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1); width: 100%; position: relative; overflow: hidden; text-transform: uppercase; letter-spacing: 0.5px; box-shadow: 0 4px 12px rgba(0,0,0,0.1); display: flex; align-items: center; justify-content: center; gap: 8px; }
.btn-primary { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; }
.btn-primary:hover { transform: translateY(-2px); box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4); }
.btn-success { background: linear-gradient(135deg, #48bb78 0%, #38a169 100%); color: white; }
.btn-success:hover { transform: translateY(-2px); box-shadow: 0 6px 16px rgba(72, 187, 120, 0.4); }
#video-container { position: relative; width: 100%; height: 320px; background: #000; border-radius: 12px; overflow: hidden; margin-bottom: 25px; box-shadow: inset 0 0 20px rgba(0,0,0,0.3); border: 3px solid rgba(255,255,255,0.2); }
#video { width: 100%; height: 100%; object-fit: cover; }
.scan-region { position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); width: 220px; height: 220px; border: 2px solid rgba(102, 126, 234, 0.9); border-radius: 16px; background: radial-gradient(circle, transparent 70%, rgba(102, 126, 234, 0.1) 100%); }
.scan-region::before, .scan-region::after { content: ''; position: absolute; width: 25px; height: 25px; border: 3px solid #667eea; box-shadow: 0 0 10px rgba(102, 126, 234, 0.6); }
.scan-region::before { top: -3px; left: -3px; border-right: none; border-bottom: none; }
.scan-region::after { bottom: -3px; right: -3px; border-left: none; border-top: none; }
.scan-line { position: absolute; top: 0; left: 0; width: 100%; height: 3px; background: linear-gradient(to right, transparent, #667eea, transparent); animation: scan 2s linear infinite; box-shadow: 0 0 10px rgba(102, 126, 234, 0.8); }
@keyframes scan { 0% { top: 0; opacity: 1; } 50% { opacity: 1; } 100% { top: 100%; opacity: 0.3; } }
.success-message { background: linear-gradient(135deg, rgba(72, 187, 120, 0.1) 0%, rgba(72, 187, 120, 0.05) 100%); color: #38a169; padding: 20px; border-radius: 12px; margin-bottom: 25px; text-align: center; border: 2px solid #48bb78; box-shadow: 0 4px 12px rgba(72, 187, 120, 0.15); }
.success-message i { font-size: 48px; margin-bottom: 15px; color: #48bb78; }
.error-message { background: linear-gradient(135deg, rgba(245, 101, 101, 0.1) 0%, rgba(245, 101, 101, 0.05) 100%); color: #e53e3e; padding: 20px; border-radius: 12px; margin-bottom: 25px; text-align: center; border: 2px solid #f56565; box-shadow: 0 4px 12px rgba(245, 101, 101, 0.15); }
.error-message i { font-size: 48px; margin-bottom: 15px; color: #f56565; }
@media (max-width: 600px) { .container { padding: 15px; } .header { padding: 20px; flex-direction: column; gap: 15px; text-align: center; } .header h1 { font-size: 20px; } .card { padding: 20px; } #video-container { height: 280px; } .scan-region { width: 200px; height: 200px; } .btn { padding: 14px 20px; font-size: 15px; } }
</style>
