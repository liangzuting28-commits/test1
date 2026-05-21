import axios from 'axios'

const api = axios.create({
  baseURL: '',
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' }
})

// 请求拦截器：自动添加 token 和 X-User-Id
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  const user = JSON.parse(localStorage.getItem('user') || '{}')

  if (token) {
    config.headers['Authorization'] = 'Bearer ' + token
  }
  if (user.studentId) {
    config.headers['X-User-Id'] = user.studentId
  }
  if (config.data instanceof FormData) {
    delete config.headers['Content-Type']
  }
  return config
})

// 响应拦截器：处理 401
api.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/vue/login'
    }
    return Promise.reject(error)
  }
)

export default api
