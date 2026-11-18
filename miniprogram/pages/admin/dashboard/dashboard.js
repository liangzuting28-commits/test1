var api = require('../../../utils/api')
var auth = require('../../../utils/auth')

Page({
  data: {
    stats: {
      totalUsers: 0,
      totalActivities: 0,
      totalClubs: 0
    }
  },

  onLoad: function () {
    var user = auth.getUser()
    if (!user || user.role !== 'ADMIN') {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }
    this.loadStats()
  },

  loadStats: function () {
    var that = this
    Promise.all([
      api.get('/api/statistics/students/count').catch(function () { return { data: 0 } }),
      api.get('/api/statistics/activities/count').catch(function () { return { data: 0 } }),
      api.get('/api/statistics/clubs/count').catch(function () { return { data: 0 } })
    ]).then(function (results) {
      that.setData({
        stats: {
          totalUsers: (results[0] && results[0].data) || 0,
          totalActivities: (results[1] && results[1].data) || 0,
          totalClubs: (results[2] && results[2].data) || 0
        }
      })
    })
  },

  goTo: function (e) {
    var url = e.currentTarget.dataset.url
    wx.navigateTo({ url: url })
  },

  doLogout: function () {
    api.post('/api/auth/logout').catch(function () {})
    auth.logout()
    wx.reLaunch({ url: '/pages/login/login' })
  }
})
