import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '../api'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))
  let initPromise = null

  const isLoggedIn = computed(() => !!user.value)
  const userRole = computed(() => user.value?.role || '')

  async function login(username, password, role, autoLogin = true) {
    const res = await api.post('/api/auth/login', { username, password, role, autoLogin })
    if (res.data.success) {
      token.value = res.data.data.token
      user.value = res.data.data.user
      localStorage.setItem('token', token.value)
      localStorage.setItem('user', JSON.stringify(user.value))
      return { success: true, message: res.data.message }
    }
    return { success: false, message: res.data.message }
  }

  async function register(username, password, confirmPassword, name, role, college, majorClass) {
    const res = await api.post('/api/auth/register', { username, password, confirmPassword, name, role, college, majorClass })
    return { success: res.data.success, message: res.data.message }
  }

  async function logout() {
    try { await api.post('/api/auth/logout') } catch (e) { /* ignore */ }
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  async function checkAuth() {
    try {
      const res = await api.get('/api/auth/me')
      if (res.data.success) {
        user.value = res.data.data
        localStorage.setItem('user', JSON.stringify(user.value))
        return true
      }
    } catch (e) { /* ignore */ }
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    return false
  }

  async function init() {
    if (user.value) return
    if (initPromise) return initPromise
    initPromise = checkAuth().finally(() => { initPromise = null })
    return initPromise
  }

  return { token, user, isLoggedIn, userRole, login, register, logout, checkAuth, init }
})
