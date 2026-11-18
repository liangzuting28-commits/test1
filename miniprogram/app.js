App({
  onLaunch: function () {
    // 检查登录状态
    var token = wx.getStorageSync('token')
    if (!token) {
      // 未登录，跳转到登录页
      wx.reLaunch({ url: '/pages/login/login' })
    }
  },
  globalData: {
    baseUrl: 'http://localhost:8080'
  }
})
