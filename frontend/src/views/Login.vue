<template>
  <div class="login-page">
    <!-- 浮动装饰元素 -->
    <div class="floating-element"></div>
    <div class="floating-element"></div>
    <div class="floating-element"></div>
    <div class="floating-element"></div>

    <!-- 登录/注册容器 -->
    <div class="login-container">
      <div class="system-info">
        <div class="logo">🎓</div>
        <h2>社团活动管理系统</h2>
        <p>基于Spring Boot的学生社团管理平台</p>
      </div>

      <!-- ========== 登录表单 ========== -->
      <template v-if="!isRegister">
        <div class="login-header">
          <h1>用户登录</h1>
          <p>请选择您的身份并输入登录信息</p>
        </div>

        <div class="role-selector">
          <button type="button" v-for="r in roles" :key="r.value"
            :class="['role-option', { active: selectedRole === r.value }]"
            @click="selectRole(r.value)">
            {{ r.label }}
          </button>
        </div>

        <form @submit.prevent="handleLogin">
          <div class="form-group">
            <label for="username">学号/工号</label>
            <input type="text" id="username" v-model="username" :placeholder="selectedRole === 'TEACHER' ? '请输入工号' : '请输入学号'" required>
            <div class="error-message" v-show="usernameError">请输入有效的{{ selectedRole === 'TEACHER' ? '工号' : '学号' }}</div>
          </div>

          <div class="form-group">
            <label for="password">密码</label>
            <input :type="showPassword ? 'text' : 'password'" id="password" v-model="password" placeholder="请输入密码" required>
            <span class="password-toggle" @click="showPassword = !showPassword">{{ showPassword ? '🙈' : '👁️' }}</span>
            <div class="error-message" v-show="passwordError">请输入密码</div>
          </div>

          <div class="auto-login-row">
            <label class="auto-login-label">
              <input type="checkbox" v-model="autoLogin" class="auto-login-checkbox">
              <span class="auto-login-text">自动登录</span>
            </label>
          </div>

          <button type="submit" class="login-btn" :disabled="loading" :class="{ loading: loading }">
            {{ loading ? '登录中...' : '登录系统' }}
          </button>
        </form>

        <div class="mode-switch">
          还没有账号？<a href="#" @click.prevent="switchMode(true)">去注册</a>
        </div>

        <div class="role-description" v-html="roleDescriptions[selectedRole]"></div>
      </template>

      <!-- ========== 注册表单 ========== -->
      <template v-else>
        <div class="login-header">
          <h1>用户注册</h1>
          <p>请填写以下信息完成注册</p>
        </div>

        <div class="role-selector">
          <button type="button" v-for="r in registerRoles" :key="r.value"
            :class="['role-option', { active: regRole === r.value }]"
            @click="regRole = r.value">
            {{ r.label }}
          </button>
        </div>

        <form @submit.prevent="handleRegister">
          <div class="form-group">
            <label for="reg-username">学号/工号</label>
            <input type="text" id="reg-username" v-model="regUsername" :placeholder="regRole === 'TEACHER' ? '请输入工号' : '请输入学号'" required>
            <div class="error-message" v-show="regUsernameError">{{ regUsernameError }}</div>
          </div>

          <div class="form-group">
            <label for="reg-name">姓名</label>
            <input type="text" id="reg-name" v-model="regName" placeholder="请输入您的姓名" required>
            <div class="error-message" v-show="regNameError">请输入姓名</div>
          </div>

          <div class="form-group">
            <label for="reg-password">密码</label>
            <input :type="showPassword ? 'text' : 'password'" id="reg-password" v-model="regPassword" placeholder="密码长度不少于6位" required>
            <span class="password-toggle" @click="showPassword = !showPassword">{{ showPassword ? '🙈' : '👁️' }}</span>
            <div class="error-message" v-show="regPasswordError">{{ regPasswordError }}</div>
          </div>

          <div class="form-group">
            <label for="reg-confirm-password">确认密码</label>
            <input :type="showPassword ? 'text' : 'password'" id="reg-confirm-password" v-model="regConfirmPassword" placeholder="请再次输入密码" required>
            <div class="error-message" v-show="regConfirmPasswordError">两次输入的密码不一致</div>
          </div>

          <div class="form-row">
            <div class="form-group form-group-half">
              <label for="reg-college">学院</label>
              <input type="text" id="reg-college" v-model="regCollege" placeholder="选填">
            </div>
            <div class="form-group form-group-half">
              <label for="reg-major">专业班级</label>
              <input type="text" id="reg-major" v-model="regMajorClass" placeholder="选填">
            </div>
          </div>

          <button type="submit" class="login-btn" :disabled="regLoading" :class="{ loading: regLoading }">
            {{ regLoading ? '注册中...' : '立即注册' }}
          </button>
        </form>

        <div class="mode-switch">
          已有账号？<a href="#" @click.prevent="switchMode(false)">去登录</a>
        </div>
      </template>

      <div class="success-message" v-show="successMsg">{{ successMsg }}</div>
      <div class="error-message-global" v-show="errorMsg">{{ errorMsg }}</div>

      <div class="login-footer">
        <p>© 2024 康东工业大学 - 社团活动管理系统</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()

// ========== 登录相关状态 ==========
const username = ref('')
const password = ref('')
const selectedRole = ref('STUDENT')
const showPassword = ref(false)
const loading = ref(false)
const usernameError = ref(false)
const passwordError = ref(false)
const successMsg = ref('')
const errorMsg = ref('')
const autoLogin = ref(true)

const roles = [
  { value: 'STUDENT', label: '学生' },
  { value: 'PRESIDENT', label: '社长' },
  { value: 'TEACHER', label: '团委老师' },
  { value: 'ADMIN', label: '系统管理员' }
]

const roleDescriptions = {
  STUDENT: '<strong>学生权限：</strong>浏览活动、报名参与、扫码签到、查询积分',
  PRESIDENT: '<strong>社长权限：</strong>创建活动、生成签到码、管理本社活动、查看统计数据',
  TEACHER: '<strong>团委老师权限：</strong>审批活动、制订积分规则、查看全局数据、管理社团',
  ADMIN: '<strong>系统管理员权限：</strong>管理所有用户、活动和社团，系统配置和维护'
}

// ========== 注册相关状态 ==========
const isRegister = ref(false)
const regUsername = ref('')
const regName = ref('')
const regPassword = ref('')
const regConfirmPassword = ref('')
const regRole = ref('STUDENT')
const regCollege = ref('')
const regMajorClass = ref('')
const regLoading = ref(false)
const regUsernameError = ref('')
const regNameError = ref(false)
const regPasswordError = ref('')
const regConfirmPasswordError = ref(false)

const registerRoles = [
  { value: 'STUDENT', label: '学生' },
  { value: 'PRESIDENT', label: '社长' },
  { value: 'TEACHER', label: '团委老师' }
]

// ========== 登录逻辑 ==========
function selectRole(role) {
  selectedRole.value = role
}

watch(username, (val) => {
  if (val.trim()) usernameError.value = false
})

watch(password, (val) => {
  if (val.trim()) passwordError.value = false
})

function validate() {
  let valid = true
  if (!username.value.trim()) {
    usernameError.value = true
    valid = false
  } else {
    usernameError.value = false
  }
  if (!password.value.trim()) {
    passwordError.value = true
    valid = false
  } else {
    passwordError.value = false
  }
  return valid
}

async function handleLogin() {
  if (!validate()) return

  loading.value = true
  successMsg.value = ''
  errorMsg.value = ''

  try {
    const result = await auth.login(username.value, password.value, selectedRole.value, autoLogin.value)
    if (result.success) {
      successMsg.value = '✅ ' + (result.message || '登录成功')
      setTimeout(() => {
        const r = auth.userRole
        if (r === 'ADMIN') {
          router.push('/admin-dashboard')
        } else {
          router.push('/dashboard')
        }
      }, 1500)
    } else {
      errorMsg.value = '❌ ' + result.message
    }
  } catch (e) {
    errorMsg.value = '❌ 登录过程中发生错误，请检查网络连接'
  } finally {
    loading.value = false
  }
}

// ========== 注册逻辑 ==========
function switchMode(toRegister) {
  isRegister.value = toRegister
  successMsg.value = ''
  errorMsg.value = ''
  regUsernameError.value = ''
  regNameError.value = false
  regPasswordError.value = ''
  regConfirmPasswordError.value = false
}

function validateRegister() {
  let valid = true
  regUsernameError.value = ''
  regNameError.value = false
  regPasswordError.value = ''
  regConfirmPasswordError.value = false

  if (!regUsername.value.trim()) {
    regUsernameError.value = '请输入学号/工号'
    valid = false
  }
  if (!regName.value.trim()) {
    regNameError.value = true
    valid = false
  }
  if (!regPassword.value || regPassword.value.length < 6) {
    regPasswordError.value = '密码长度不能少于6位'
    valid = false
  }
  if (regPassword.value !== regConfirmPassword.value) {
    regConfirmPasswordError.value = true
    valid = false
  }
  return valid
}

async function handleRegister() {
  if (!validateRegister()) return

  regLoading.value = true
  successMsg.value = ''
  errorMsg.value = ''

  try {
    const result = await auth.register(
      regUsername.value,
      regPassword.value,
      regConfirmPassword.value,
      regName.value,
      regRole.value,
      regCollege.value || undefined,
      regMajorClass.value || undefined
    )
    if (result.success) {
      successMsg.value = '✅ 注册成功！请登录'
      // 自动填入学号到登录表单（在清空前保存）
      username.value = regUsername.value
      // 清空注册表单
      regUsername.value = ''
      regName.value = ''
      regPassword.value = ''
      regConfirmPassword.value = ''
      regCollege.value = ''
      regMajorClass.value = ''
      setTimeout(() => {
        isRegister.value = false
      }, 1000)
    } else {
      errorMsg.value = '❌ ' + result.message
    }
  } catch (e) {
    errorMsg.value = '❌ 注册过程中发生错误，请检查网络连接'
  } finally {
    regLoading.value = false
  }
}
</script>

<style scoped>
.login-page {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background-attachment: fixed;
  background-size: cover;
  position: relative;
  overflow: hidden;
}

.login-page::before, .login-page::after {
  content: '';
  position: absolute;
  width: 300px;
  height: 300px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  animation: float 6s ease-in-out infinite;
}

.login-page::before {
  top: -100px;
  left: -100px;
  animation-delay: 0s;
}

.login-page::after {
  bottom: -100px;
  right: -100px;
  animation-delay: 3s;
}

@keyframes float {
  0%, 100% { transform: translateY(0px) rotate(0deg); }
  50% { transform: translateY(-20px) rotate(180deg); }
}

.floating-element {
  position: absolute;
  width: 100px;
  height: 100px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 50%;
  animation: float 8s ease-in-out infinite;
}

.floating-element:nth-child(1) { top: 20%; right: 15%; animation-delay: 1s; }
.floating-element:nth-child(2) { bottom: 20%; left: 15%; animation-delay: 4s; }
.floating-element:nth-child(3) { top: 50%; left: 10%; animation-delay: 2s; }
.floating-element:nth-child(4) { bottom: 30%; right: 10%; animation-delay: 5s; }

.login-container {
  background: rgba(255, 255, 255, 0.98);
  border-radius: 25px;
  padding: 50px 40px;
  box-shadow: 0 25px 80px rgba(0, 0, 0, 0.2);
  width: 100%;
  max-width: 450px;
  position: relative;
  overflow: hidden;
  backdrop-filter: blur(15px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  z-index: 10;
  animation: slideUp 0.6s ease-out;
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(50px) scale(0.95); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}

.system-info {
  text-align: center;
  margin-bottom: 25px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.system-info .logo {
  font-size: 60px;
  margin-bottom: 20px;
  color: #667eea;
  display: block;
  transition: transform 0.3s ease;
  animation: pulse 2s ease-in-out infinite;
  text-align: center;
}

.system-info .logo:hover {
  transform: scale(1.15);
  animation-play-state: paused;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.05); }
}

.system-info h2 {
  color: #333;
  font-size: 22px;
  margin-bottom: 5px;
}

.system-info p {
  color: #666;
  font-size: 14px;
}

/* 登录/注册切换链接 */
.mode-switch {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: #999;
}

.mode-switch a {
  color: #667eea;
  text-decoration: none;
  font-weight: 600;
  transition: color 0.3s;
}

.mode-switch a:hover {
  color: #764ba2;
  text-decoration: underline;
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h1 {
  color: #333;
  margin-bottom: 15px;
  font-size: 32px;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.login-header p {
  color: #666;
  font-size: 14px;
}

.role-selector {
  display: flex;
  margin-bottom: 30px;
  border: 2px solid #e0e0e0;
  border-radius: 15px;
  overflow: hidden;
  background: #f8f9fa;
  box-shadow: inset 0 2px 5px rgba(0, 0, 0, 0.05);
}

.role-option {
  flex: 1;
  padding: 15px 12px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  border: none;
  background: transparent;
  font-size: 14px;
  font-weight: 600;
  position: relative;
  overflow: hidden;
}

.role-option::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: rgba(255, 255, 255, 0.3);
  transition: left 0.3s ease;
}

.role-option:hover::before {
  left: 100%;
}

.role-option.active {
  background: linear-gradient(135deg, #667eea 0%, #5a6fd8 100%);
  color: white;
  box-shadow: inset 0 2px 8px rgba(0, 0, 0, 0.1);
}

.role-option:not(.active):hover {
  background: #e9ecef;
}

.form-group {
  margin-bottom: 25px;
  position: relative;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #555;
  font-weight: 500;
  font-size: 14px;
}

.form-group input {
  width: 100%;
  padding: 14px 15px;
  border: 2px solid #e0e0e0;
  border-radius: 10px;
  font-size: 14px;
  transition: all 0.3s ease;
  background-color: #fafafa;
}

.form-group input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.2);
  transform: translateY(-1px);
}

.form-row {
  display: flex;
  gap: 15px;
}

.form-group-half {
  flex: 1;
}

.password-toggle {
  position: absolute;
  right: 15px;
  top: 45px;
  cursor: pointer;
  color: #999;
  transition: color 0.3s;
  font-size: 16px;
}

.password-toggle:hover {
  color: #667eea;
}

/* 自动登录行 */
.auto-login-row {
  margin-bottom: 20px;
}

.auto-login-label {
  display: flex;
  align-items: center;
  cursor: pointer;
  user-select: none;
}

.auto-login-checkbox {
  width: 18px;
  height: 18px;
  margin-right: 8px;
  accent-color: #667eea;
  cursor: pointer;
}

.auto-login-text {
  color: #555;
  font-size: 14px;
  font-weight: 500;
}

.login-btn {
  width: 100%;
  padding: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 15px;
  font-size: 18px;
  cursor: pointer;
  transition: all 0.4s ease;
  margin-top: 15px;
  font-weight: 600;
  box-shadow: 0 6px 25px rgba(102, 126, 234, 0.4);
  position: relative;
  overflow: hidden;
}

.login-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.5s ease;
}

.login-btn:hover::before {
  left: 100%;
}

.login-btn:hover {
  background: linear-gradient(135deg, #5a6fd8 0%, #6a4092 100%);
  box-shadow: 0 10px 40px rgba(102, 126, 234, 0.6);
  transform: translateY(-3px);
}

.login-btn:active {
  transform: translateY(-1px);
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.5);
}

.login-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
  box-shadow: none;
  transform: none;
}

.login-btn:disabled::before {
  display: none;
}

.role-description {
  margin-top: 25px;
  padding: 20px;
  background: linear-gradient(135deg, #f8f9fa, #e9ecef);
  border-radius: 12px;
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  border-left: 5px solid #667eea;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.role-description:hover {
  transform: translateX(5px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
}

.role-description :deep(strong) {
  color: #333;
}

.login-footer {
  text-align: center;
  margin-top: 35px;
  font-size: 14px;
  color: #999;
  padding-top: 25px;
  border-top: 1px solid #eee;
}

.error-message {
  color: #e74c3c;
  font-size: 13px;
  margin-top: 5px;
}

.loading {
  opacity: 0.7;
  pointer-events: none;
}

.success-message {
  background: linear-gradient(135deg, #d4edda, #c3e6cb);
  color: #155724;
  padding: 15px;
  border-radius: 12px;
  margin-bottom: 20px;
  box-shadow: 0 4px 15px rgba(21, 87, 36, 0.1);
  border-left: 5px solid #28a745;
  animation: slideIn 0.3s ease-out;
}

.error-message-global {
  background: linear-gradient(135deg, #f8d7da, #f5c6cb);
  color: #721c24;
  padding: 15px;
  border-radius: 12px;
  margin-bottom: 20px;
  box-shadow: 0 4px 15px rgba(114, 28, 36, 0.1);
  border-left: 5px solid #dc3545;
  animation: slideIn 0.3s ease-out;
}

@keyframes slideIn {
  from { opacity: 0; transform: translateX(-20px); }
  to { opacity: 1; transform: translateX(0); }
}

@media (max-width: 480px) {
  .login-container {
    padding: 40px 25px;
    margin: 15px;
  }

  .login-page::before, .login-page::after {
    width: 200px;
    height: 200px;
  }

  .floating-element {
    width: 80px;
    height: 80px;
  }

  .role-selector {
    flex-direction: column;
  }

  .role-option {
    padding: 15px;
  }

  .login-header h1 {
    font-size: 28px;
  }

  .system-info .logo {
    font-size: 50px;
  }

  .form-row {
    flex-direction: column;
    gap: 0;
  }
}
</style>
