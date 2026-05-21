var api = require('../../utils/api')
var auth = require('../../utils/auth')

Page({
  data: {
    roles: [
      { value: 'STUDENT', label: '学生' },
      { value: 'PRESIDENT', label: '社长' },
      { value: 'TEACHER', label: '教师' },
      { value: 'ADMIN', label: '管理员' }
    ],
    selectedRole: 'STUDENT',
    username: '',
    password: '',
    showPassword: false,
    loading: false,
    roleLabel: '学生',
    roleDesc: '浏览活动、报名参与、扫码签到、查询积分'
  },

  selectRole: function (e) {
    var role = e.currentTarget.dataset.role
    var roleDescs = {
      'STUDENT': '浏览活动、报名参与、扫码签到、查询积分',
      'PRESIDENT': '创建活动、生成签到码、管理本社活动、查看统计数据',
      'TEACHER': '审批活动、制订积分规则、查看全局数据、管理社团',
      'ADMIN': '管理所有用户、活动和社团，系统配置和维护'
    }
    var roleLabels = {
      'STUDENT': '学生',
      'PRESIDENT': '社长',
      'TEACHER': '教师',
      'ADMIN': '管理员'
    }
    this.setData({
      selectedRole: role,
      roleLabel: roleLabels[role],
      roleDesc: roleDescs[role]
    })
  },

  onInputUsername: function (e) {
    this.setData({ username: e.detail.value })
  },

  onInputPassword: function (e) {
    this.setData({ password: e.detail.value })
  },

  doLogin: function () {
    var that = this
    var username = that.data.username.trim()
    var password = that.data.password

    if (!username) {
      wx.showToast({ title: '请输入学号/工号', icon: 'none' })
      return
    }
    if (!password) {
      wx.showToast({ title: '请输入密码', icon: 'none' })
      return
    }

    that.setData({ loading: true })

    api.post('/api/auth/login', {
      username: username,
      password: password,
      role: that.data.selectedRole
    }).then(function (res) {
      that.setData({ loading: false })
      if (res.success) {
        wx.setStorageSync('token', res.data.token)
        wx.setStorageSync('user', res.data.user)
        wx.showToast({ title: '登录成功', icon: 'success' })
        setTimeout(function () {
          if (res.data.user.role === 'ADMIN') {
            wx.reLaunch({ url: '/pages/admin/dashboard/dashboard' })
          } else {
            wx.reLaunch({ url: '/pages/dashboard/dashboard' })
          }
        }, 1000)
      } else {
        wx.showToast({ title: res.message || '登录失败', icon: 'none' })
      }
    }).catch(function (err) {
      that.setData({ loading: false })
      wx.showToast({ title: '网络错误', icon: 'none' })
    })
  },

  onLoad: function () {
    if (auth.isLoggedIn()) {
      var role = auth.getRole()
      if (role === 'ADMIN') {
        wx.reLaunch({ url: '/pages/admin/dashboard/dashboard' })
      } else {
        wx.reLaunch({ url: '/pages/dashboard/dashboard' })
      }
    }
  }
})
