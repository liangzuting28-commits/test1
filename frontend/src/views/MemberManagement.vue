<template>
  <div class="member-management-page">
    <div class="container">
      <div class="header">
        <h1><i class="fas fa-users-cog"></i> 成员管理</h1>
        <button class="btn btn-primary" @click="$router.push('/dashboard')">
          <i class="fas fa-arrow-left"></i> 返回仪表盘
        </button>
      </div>

      <div class="card">
        <h3><i class="fas fa-users-cog"></i> 社团成员管理</h3>
        <div class="form-group">
          <label>选择要管理的社团</label>
          <select v-model="selectedClubId" @change="loadClubMembers">
            <option value="">请选择社团</option>
            <option v-for="club in clubs" :key="club.clubId" :value="club.clubId">{{ club.clubName }}</option>
          </select>
        </div>

        <div v-if="loading" class="empty-state"><i class="fas fa-spinner fa-spin"></i><p>加载中...</p></div>
        <div v-else>
          <!-- 待审核申请 -->
          <div v-if="pendingApplications.length > 0" class="section">
            <h4><i class="fas fa-clock"></i> 待审核申请</h4>
            <div class="members-list">
              <div class="member-item" v-for="app in pendingApplications" :key="app.membershipId || app.studentId">
                <div class="member-info">
                  <h4>{{ app.studentName || app.studentId }}</h4>
                  <div class="member-details">学号：{{ app.studentId }} | 申请时间：{{ formatDateTime(app.joinDate) }}</div>
                </div>
                <div class="member-actions">
                  <button class="btn btn-success btn-sm" @click="approveApplication(app.membershipId)">
                    <i class="fas fa-check"></i> 通过
                  </button>
                  <button class="btn btn-danger btn-sm" @click="rejectApplication(app.membershipId)">
                    <i class="fas fa-times"></i> 拒绝
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- 已通过成员 -->
          <div v-if="approvedMembers.length > 0" class="section">
            <h4><i class="fas fa-check-circle"></i> 已通过成员</h4>
            <div class="members-list">
              <div class="member-item" v-for="member in approvedMembers" :key="member.studentId">
                <div class="member-info">
                  <h4>{{ member.name }}</h4>
                  <div class="member-details">学号：{{ member.studentId }} | 学院：{{ member.college || '未知' }} | 班级：{{ member.majorClass || '未知' }}</div>
                </div>
                <div class="member-actions">
                  <button class="btn btn-danger btn-sm" @click="showRemoveModal(member.studentId, member.name)">
                    <i class="fas fa-user-times"></i> 删除
                  </button>
                </div>
              </div>
            </div>
          </div>

          <div v-if="!loading && pendingApplications.length === 0 && approvedMembers.length === 0 && selectedClubId" class="empty-state">
            <i class="fas fa-users-slash"></i><p>暂无成员</p>
          </div>
          <div v-if="!selectedClubId" class="empty-state">
            <i class="fas fa-info-circle"></i><p>请选择一个社团</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 删除成员模态框 -->
    <div v-if="showRemoveModal_" class="modal-overlay" @click.self="closeRemoveModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3><i class="fas fa-user-times"></i> 删除成员</h3>
          <button class="modal-close" @click="closeRemoveModal">&times;</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>删除原因</label>
            <textarea v-model="removalReason" placeholder="请输入删除原因..." rows="4"></textarea>
          </div>
          <div class="form-actions">
            <button class="btn btn-danger" @click="confirmRemoveMember"><i class="fas fa-trash"></i> 确认删除</button>
            <button class="btn btn-primary" @click="closeRemoveModal"><i class="fas fa-times"></i> 取消</button>
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
import { useToastStore } from '../stores/toast'
import api from '../api'

const router = useRouter()
const auth = useAuthStore()
const toast = useToastStore()
const clubs = ref([])
const selectedClubId = ref('')
const pendingApplications = ref([])
const approvedMembers = ref([])
const loading = ref(false)
const showRemoveModal_ = ref(false)
const removalReason = ref('')
const currentMemberId = ref(null)

function formatDateTime(d) {
  if (!d) return '未知时间'
  return new Date(d).toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

async function loadClubs() {
  try {
    const res = await api.get('/api/clubs/my-clubs')
    if (res.data.success && res.data.data) clubs.value = res.data.data
  } catch (e) { console.error('加载社团列表错误:', e) }
}

async function loadClubMembers() {
  if (!selectedClubId.value) { pendingApplications.value = []; approvedMembers.value = []; return }
  loading.value = true
  try {
    const pendingRes = await api.get(`/api/clubs/${selectedClubId.value}/pending-applications`)
    pendingApplications.value = pendingRes.data.success ? (pendingRes.data.data || []) : []
  } catch (e) { console.error('加载待审批列表错误:', e); pendingApplications.value = [] }
  try {
    const approvedRes = await api.get(`/api/member-manage/club/${selectedClubId.value}/members`)
    approvedMembers.value = approvedRes.data.success ? (approvedRes.data.data || []) : []
  } catch (e) { console.error('加载成员列表错误:', e); approvedMembers.value = [] }
  loading.value = false
}

async function approveApplication(membershipId) {
  try {
    const res = await api.put(`/api/clubs/applications/${membershipId}/review`, { status: 'APPROVED' })
    if (res.data.success) { toast.success('审批通过！'); loadClubMembers() }
    else toast.error('审批失败：' + res.data.message)
  } catch (e) { toast.error('审批时发生错误') }
}

async function rejectApplication(membershipId) {
  try {
    const res = await api.put(`/api/clubs/applications/${membershipId}/review`, { status: 'REJECTED' })
    if (res.data.success) { toast.info('已拒绝'); loadClubMembers() }
    else toast.error('操作失败：' + res.data.message)
  } catch (e) { toast.error('操作时发生错误') }
}

function showRemoveModal(studentId, name) {
  currentMemberId.value = studentId
  removalReason.value = ''
  showRemoveModal_.value = true
}

function closeRemoveModal() {
  showRemoveModal_.value = false
  currentMemberId.value = null
}

async function confirmRemoveMember() {
  if (!removalReason.value.trim()) { toast.warning('请输入删除原因'); return }
  if (!confirm('确定要删除该成员吗？此操作不可撤销。')) return
  try {
    const res = await api.delete(`/api/member-manage/club/${selectedClubId.value}/member/${currentMemberId.value}`, { data: { reason: removalReason.value } })
    if (res.data.success) { closeRemoveModal(); toast.success('成员删除成功！'); loadClubMembers() }
    else toast.error('删除失败：' + res.data.message)
  } catch (e) { toast.error('删除过程中发生错误，请重试') }
}

onMounted(() => {
  if (!auth.token) { router.push('/login'); return }
  loadClubs()
})
</script>

<style scoped>
* { margin: 0; padding: 0; box-sizing: border-box; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; }
.member-management-page { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); min-height: 100vh; color: #333; }
.container { max-width: 1200px; margin: 0 auto; padding: 20px; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 30px; background: white; padding: 25px; border-radius: 15px; box-shadow: 0 4px 20px rgba(0,0,0,0.08); border-left: 4px solid #667eea; }
.header h1 { color: #333; font-size: 28px; display: flex; align-items: center; gap: 12px; }
.header h1 i { color: #667eea; }
.card { background: white; border-radius: 15px; padding: 30px; margin-bottom: 25px; box-shadow: 0 4px 20px rgba(0,0,0,0.08); }
.card h3 { color: #333; margin-bottom: 20px; font-size: 20px; display: flex; align-items: center; gap: 10px; }
.card h3 i { color: #667eea; }
.btn { padding: 12px 24px; border: none; border-radius: 8px; cursor: pointer; font-size: 14px; font-weight: 600; transition: all 0.3s ease; display: inline-flex; align-items: center; gap: 8px; }
.btn-primary { background: linear-gradient(45deg, #667eea, #764ba2); color: white; box-shadow: 0 4px 15px rgba(102,126,234,0.3); }
.btn-primary:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(102,126,234,0.4); }
.btn-success { background: linear-gradient(45deg, #48bb78, #38a169); color: white; }
.btn-danger { background: linear-gradient(45deg, #f44336, #d32f2f); color: white; box-shadow: 0 4px 15px rgba(244,67,54,0.3); }
.btn-danger:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(244,67,54,0.4); }
.btn-sm { padding: 8px 16px; font-size: 12px; }
.form-group { margin-bottom: 20px; }
.form-group label { display: block; margin-bottom: 8px; font-weight: 600; color: #555; font-size: 14px; }
.form-group select, .form-group textarea { width: 100%; padding: 12px 16px; border: 2px solid #e9ecef; border-radius: 8px; font-size: 14px; transition: all 0.3s ease; background: #fafbfc; }
.form-group select:focus, .form-group textarea:focus { outline: none; border-color: #667eea; background: white; box-shadow: 0 0 0 3px rgba(102,126,234,0.1); }
.form-group textarea { resize: vertical; line-height: 1.5; font-family: inherit; }
.section { margin-bottom: 30px; }
.section h4 { color: #333; margin-bottom: 15px; font-size: 16px; display: flex; align-items: center; gap: 8px; }
.section h4 i { color: #667eea; }
.members-list { display: grid; gap: 15px; }
.member-item { display: flex; justify-content: space-between; align-items: center; padding: 20px; background: #f8f9fa; border-radius: 10px; transition: all 0.3s ease; }
.member-item:hover { background: #e9ecef; transform: translateX(5px); }
.member-info h4 { color: #333; margin-bottom: 5px; font-size: 16px; }
.member-details { font-size: 14px; color: #666; }
.member-actions { display: flex; gap: 10px; }
.empty-state { text-align: center; padding: 40px; color: #666; }
.empty-state i { font-size: 48px; margin-bottom: 10px; color: #ddd; display: block; }
.modal-overlay { display: flex; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 1000; justify-content: center; align-items: center; backdrop-filter: blur(5px); }
.modal-content { background: white; border-radius: 15px; max-width: 600px; width: 90%; max-height: 90vh; overflow-y: auto; box-shadow: 0 20px 60px rgba(0,0,0,0.2); animation: modalSlideIn 0.3s ease; }
@keyframes modalSlideIn { from { opacity: 0; transform: scale(0.9); } to { opacity: 1; transform: scale(1); } }
.modal-header { display: flex; justify-content: space-between; align-items: center; padding: 20px 25px; border-bottom: 1px solid #e9ecef; }
.modal-header h3 { color: #333; margin: 0; display: flex; align-items: center; gap: 10px; }
.modal-header h3 i { color: #667eea; }
.modal-close { background: none; border: none; font-size: 24px; cursor: pointer; color: #666; padding: 0; width: 30px; height: 30px; display: flex; align-items: center; justify-content: center; border-radius: 50%; }
.modal-close:hover { background: #f8f9fa; }
.modal-body { padding: 25px; }
.form-actions { display: flex; gap: 10px; justify-content: flex-end; margin-top: 15px; }
@media (max-width: 768px) { .header { flex-direction: column; gap: 15px; text-align: center; } .member-item { flex-direction: column; align-items: flex-start; gap: 15px; } .member-actions { width: 100%; justify-content: flex-end; } }
</style>
