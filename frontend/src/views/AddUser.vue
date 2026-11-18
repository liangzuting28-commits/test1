<template>
  <div class="add-user-page">
    <div class="container">
      <div class="header">
        <button class="back-btn" @click="$router.push('/admin-dashboard')">
          <i class="fas fa-arrow-left"></i> 返回仪表盘
        </button>
        <h1><i class="fas fa-user-plus"></i> 添加用户</h1>
      </div>

      <div class="add-user-form">
        <form @submit.prevent="submitForm">
          <div class="form-row">
            <div class="form-group">
              <label>用户ID</label>
              <input type="text" v-model="form.studentId" placeholder="请输入用户ID" :class="{ error: errors.studentId, success: valid.studentId }" @blur="validateField('studentId')">
              <div class="error-message" :class="{ show: errors.studentId }">{{ errors.studentId }}</div>
            </div>
            <div class="form-group">
              <label>姓名</label>
              <input type="text" v-model="form.name" placeholder="请输入用户姓名" :class="{ error: errors.name, success: valid.name }" @blur="validateField('name')">
              <div class="error-message" :class="{ show: errors.name }">{{ errors.name }}</div>
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>密码</label>
              <input type="password" v-model="form.password" placeholder="请输入密码" :class="{ error: errors.password, success: valid.password }" @blur="validateField('password')" @input="checkPasswordStrength">
              <div class="password-strength">
                <span>密码强度: {{ strengthText }}</span>
                <div class="strength-indicator">
                  <div :class="['strength-bar', strengthLevel >= 1 ? strengthClass : '']"></div>
                  <div :class="['strength-bar', strengthLevel >= 2 ? strengthClass : '']"></div>
                  <div :class="['strength-bar', strengthLevel >= 3 ? strengthClass : '']"></div>
                </div>
              </div>
              <div class="error-message" :class="{ show: errors.password }">{{ errors.password }}</div>
            </div>
            <div class="form-group">
              <label>角色</label>
              <select v-model="form.role" :class="{ error: errors.role, success: valid.role }" @blur="validateField('role')">
                <option value="">请选择角色</option>
                <option value="STUDENT">学生</option>
                <option value="PRESIDENT">社长</option>
                <option value="TEACHER">教师</option>
                <option value="ADMIN">管理员</option>
              </select>
              <div class="error-message" :class="{ show: errors.role }">{{ errors.role }}</div>
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>学院</label>
              <input type="text" v-model="form.college" placeholder="请输入学院" :class="{ error: errors.college, success: valid.college }" @blur="validateField('college')">
              <div class="error-message" :class="{ show: errors.college }">{{ errors.college }}</div>
            </div>
            <div class="form-group">
              <label>班级</label>
              <input type="text" v-model="form.majorClass" placeholder="请输入班级" :class="{ error: errors.majorClass, success: valid.majorClass }" @blur="validateField('majorClass')">
              <div class="error-message" :class="{ show: errors.majorClass }">{{ errors.majorClass }}</div>
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>手机号</label>
              <input type="tel" v-model="form.phone" placeholder="请输入手机号" :class="{ error: errors.phone, success: valid.phone }" @blur="validateField('phone')">
              <div class="error-message" :class="{ show: errors.phone }">{{ errors.phone }}</div>
            </div>
            <div class="form-group">
              <label>邮箱</label>
              <input type="email" v-model="form.email" placeholder="请输入邮箱" :class="{ error: errors.email, success: valid.email }" @blur="validateField('email')">
              <div class="error-message" :class="{ show: errors.email }">{{ errors.email }}</div>
            </div>
          </div>

          <div class="form-actions">
            <button type="button" class="btn btn-secondary" @click="$router.push('/admin-dashboard')">取消</button>
            <button type="submit" class="btn btn-primary" :disabled="submitting">
              {{ submitting ? '添加中...' : '添加用户' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useToastStore } from '../stores/toast'
import api from '../api'

const router = useRouter()
const auth = useAuthStore()
const toast = useToastStore()
const submitting = ref(false)
const strengthText = ref('弱')
const strengthLevel = ref(0)
const strengthClass = ref('weak')

const form = ref({
  studentId: '', name: '', password: '', role: '',
  college: '', majorClass: '', phone: '', email: ''
})

const errors = reactive({ studentId: '', name: '', password: '', role: '', college: '', majorClass: '', phone: '', email: '' })
const valid = reactive({ studentId: false, name: false, password: false, role: false, college: false, majorClass: false, phone: false, email: false })

function validateField(field) {
  const v = form.value[field]
  errors[field] = ''
  valid[field] = false

  switch (field) {
    case 'studentId':
      if (!v.trim()) errors[field] = '用户ID不能为空'
      else if (v.trim().length > 20) errors[field] = '用户ID不能超过20个字符'
      else valid[field] = true
      break
    case 'name':
      if (!v.trim()) errors[field] = '姓名不能为空'
      else if (v.trim().length > 20) errors[field] = '姓名不能超过20个字符'
      else valid[field] = true
      break
    case 'password':
      if (!v) errors[field] = '密码不能为空'
      else if (v.length < 6) errors[field] = '密码至少6位'
      else valid[field] = true
      break
    case 'role':
      if (!v) errors[field] = '请选择角色'
      else valid[field] = true
      break
    case 'college':
      if (!v.trim()) errors[field] = '学院不能为空'
      else if (v.trim().length > 50) errors[field] = '学院名称不能超过50个字符'
      else valid[field] = true
      break
    case 'majorClass':
      if (!v.trim()) errors[field] = '班级不能为空'
      else if (v.trim().length > 20) errors[field] = '班级名称不能超过20个字符'
      else valid[field] = true
      break
    case 'phone':
      if (!v) errors[field] = '手机号不能为空'
      else if (!/^1[3-9]\d{9}$/.test(v)) errors[field] = '请输入有效的手机号'
      else valid[field] = true
      break
    case 'email':
      if (!v) errors[field] = '邮箱不能为空'
      else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v)) errors[field] = '请输入有效的邮箱地址'
      else valid[field] = true
      break
  }
}

function checkPasswordStrength() {
  const p = form.value.password
  let s = 0
  if (p.length >= 6) s++
  if (p.length >= 8) s++
  if (/[A-Z]/.test(p)) s++
  if (/[a-z]/.test(p)) s++
  if (/[0-9]/.test(p)) s++
  if (/[^A-Za-z0-9]/.test(p)) s++

  if (s <= 2) { strengthLevel.value = 1; strengthClass.value = 'weak'; strengthText.value = '弱' }
  else if (s <= 4) { strengthLevel.value = 2; strengthClass.value = 'medium'; strengthText.value = '中' }
  else { strengthLevel.value = 3; strengthClass.value = 'strong'; strengthText.value = '强' }
}

async function submitForm() {
  ;['studentId', 'name', 'password', 'role', 'college', 'majorClass', 'phone', 'email'].forEach(validateField)
  if (Object.values(errors).some(e => e)) { toast.warning('请检查表单中的错误并重新提交！'); return }

  submitting.value = true
  try {
    const res = await api.post('/api/students', { ...form.value, totalPoints: 0 })
    if (res.data.success) {
      toast.success('用户添加成功！')
      router.push('/admin-dashboard')
    } else {
      toast.error('用户添加失败：' + res.data.message)
    }
  } catch (e) { toast.error('添加用户失败，请稍后重试！') }
  submitting.value = false
}

onMounted(() => { if (!auth.token) router.push('/login') })
</script>

<style scoped>
* { margin: 0; padding: 0; box-sizing: border-box; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; }
.add-user-page { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); min-height: 100vh; color: #333; display: flex; align-items: center; padding: 20px 0; }
.container { max-width: 1200px; margin: 0 auto; width: 100%; padding: 0 20px; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 30px; background: white; padding: 25px; border-radius: 15px; box-shadow: 0 10px 40px rgba(0,0,0,0.1); border-left: 4px solid #667eea; transition: transform 0.3s ease, box-shadow 0.3s ease; }
.header:hover { transform: translateY(-2px); box-shadow: 0 15px 50px rgba(0,0,0,0.15); }
.header h1 { color: #333; font-size: 28px; display: flex; align-items: center; gap: 12px; font-weight: 700; }
.header h1 i { color: #667eea; animation: pulse 2s infinite; }
@keyframes pulse { 0%, 100% { transform: scale(1); } 50% { transform: scale(1.1); } }
.add-user-form { background: white; padding: 40px; border-radius: 20px; box-shadow: 0 15px 50px rgba(0,0,0,0.1); max-width: 800px; margin: 0 auto; transition: transform 0.3s ease, box-shadow 0.3s ease; }
.add-user-form:hover { box-shadow: 0 20px 60px rgba(0,0,0,0.15); }
.form-row { display: flex; gap: 20px; margin-bottom: 25px; }
.form-group { flex: 1; margin-bottom: 25px; }
.form-group label { display: block; margin-bottom: 8px; font-weight: 600; color: #555; font-size: 14px; transition: color 0.3s ease; }
.form-group:focus-within label { color: #667eea; }
.form-group input, .form-group select { width: 100%; padding: 14px 18px; border: 2px solid #e0e0e0; border-radius: 10px; font-size: 16px; transition: all 0.3s ease; background-color: #fafafa; }
.form-group input:focus, .form-group select:focus { outline: none; border-color: #667eea; box-shadow: 0 0 0 4px rgba(102,126,234,0.1); background-color: white; transform: translateY(-1px); }
.form-group input.error, .form-group select.error { border-color: #e74c3c; box-shadow: 0 0 0 4px rgba(231,76,60,0.1); }
.form-group input.success, .form-group select.success { border-color: #2ecc71; box-shadow: 0 0 0 4px rgba(46,204,113,0.1); }
.error-message { color: #e74c3c; font-size: 12px; margin-top: 5px; display: none; }
.error-message.show { display: block; animation: fadeIn 0.3s ease; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(-5px); } to { opacity: 1; transform: translateY(0); } }
.password-strength { margin-top: 8px; font-size: 12px; }
.strength-indicator { display: flex; gap: 3px; margin-top: 5px; }
.strength-bar { flex: 1; height: 4px; border-radius: 2px; background-color: #e0e0e0; transition: all 0.3s ease; }
.strength-bar.weak { background-color: #e74c3c; }
.strength-bar.medium { background-color: #f39c12; }
.strength-bar.strong { background-color: #2ecc71; }
.form-actions { display: flex; gap: 15px; justify-content: flex-end; margin-top: 50px; padding-top: 30px; border-top: 1px solid #f0f0f0; }
.btn { padding: 14px 30px; border: none; border-radius: 10px; font-size: 16px; font-weight: 600; cursor: pointer; transition: all 0.3s ease; text-transform: uppercase; letter-spacing: 0.5px; position: relative; overflow: hidden; }
.btn::before { content: ''; position: absolute; top: 0; left: -100%; width: 100%; height: 100%; background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent); transition: left 0.5s ease; }
.btn:hover::before { left: 100%; }
.btn-primary { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; box-shadow: 0 4px 15px rgba(102,126,234,0.3); }
.btn-primary:hover { transform: translateY(-2px); box-shadow: 0 8px 25px rgba(102,126,234,0.4); }
.btn-primary:disabled { opacity: 0.7; cursor: not-allowed; transform: none; }
.btn-secondary { background: #f0f0f0; color: #333; box-shadow: 0 4px 15px rgba(0,0,0,0.1); }
.btn-secondary:hover { background: #e0e0e0; transform: translateY(-2px); box-shadow: 0 8px 25px rgba(0,0,0,0.15); }
.back-btn { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 12px 24px; border-radius: 10px; border: none; text-decoration: none; font-weight: 600; display: flex; align-items: center; gap: 8px; cursor: pointer; transition: all 0.3s ease; box-shadow: 0 4px 15px rgba(102,126,234,0.3); }
.back-btn:hover { transform: translateY(-2px); box-shadow: 0 8px 25px rgba(102,126,234,0.4); }
@media (max-width: 768px) { .form-row { flex-direction: column; gap: 0; } .add-user-form { padding: 25px; } .form-actions { flex-direction: column; } .btn { width: 100%; } }
</style>
