var api = require('../../utils/api')
var auth = require('../../utils/auth')

Page({
  data: {
    userName: '',
    role: '',
    roleLabel: '',
    stats: [],
    studentMenus: [
      { url: '/pages/activity-center/activity-center', icon: '&#x1F4C5;', iconBg: 'icon-bg-blue', label: '活动中心', desc: '浏览和报名参加活动' },
      { url: '/pages/my-activities/my-activities', icon: '&#x1F4CB;', iconBg: 'icon-bg-green', label: '我的活动', desc: '查看已报名的活动' },
      { url: '/pages/my-points/my-points', icon: '&#x2B50;', iconBg: 'icon-bg-orange', label: '我的积分', desc: '查看积分明细和排名' },
      { url: '/pages/checkin/checkin', icon: '&#x1F4F1;', iconBg: 'icon-bg-purple', label: '扫码签到', desc: '扫描二维码完成签到' },
      { url: '/pages/club-manage/club-manage', icon: '&#x1F3DB;', iconBg: 'icon-bg-teal', label: '社团浏览', desc: '浏览和申请加入社团' },
      { url: '/pages/my-applications/my-applications', icon: '&#x1F4DD;', iconBg: 'icon-bg-indigo', label: '我的申请', desc: '查看社团和活动申请状态' }
    ],
    presidentMenus: [
      { url: '/pages/create-activity/create-activity', icon: '&#x2795;', iconBg: 'icon-bg-green', label: '创建活动', desc: '发布新的社团活动' },
      { url: '/pages/manage-activities/manage-activities', icon: '&#x1F4CB;', iconBg: 'icon-bg-blue', label: '活动管理', desc: '管理本社团所有活动' },
      { url: '/pages/member-management/member-management', icon: '&#x1F465;', iconBg: 'icon-bg-purple', label: '成员管理', desc: '审批成员申请、管理成员' },
      { url: '/pages/checkin-manage/checkin-manage', icon: '&#x1F4F1;', iconBg: 'icon-bg-orange', label: '签到管理', desc: '生成签到码、查看签到情况' },
      { url: '/pages/club-manage/club-manage', icon: '&#x1F3DB;', iconBg: 'icon-bg-teal', label: '社团管理', desc: '管理社团信息' }
    ],
    teacherMenus: [
      { url: '/pages/activity-review/activity-review', icon: '&#x2705;', iconBg: 'icon-bg-green', label: '活动审核', desc: '审核待发布的活动' },
      { url: '/pages/points-rules/points-rules', icon: '&#x1F4CF;', iconBg: 'icon-bg-blue', label: '积分规则', desc: '配置活动积分规则' },
      { url: '/pages/points-manage/points-manage', icon: '&#x1F4B0;', iconBg: 'icon-bg-orange', label: '积分管理', desc: '管理学生积分' },
      { url: '/pages/club-manage/club-manage', icon: '&#x1F3DB;', iconBg: 'icon-bg-teal', label: '社团管理', desc: '查看和管理所有社团' },
      { url: '/pages/global-statistics/global-statistics', icon: '&#x1F4CA;', iconBg: 'icon-bg-purple', label: '全局统计', desc: '查看系统统计数据' }
    ]
  },

  onLoad: function () {
    var user = auth.getUser()
    if (!user) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }
    var roleLabels = { 'STUDENT': '学生', 'PRESIDENT': '社长', 'TEACHER': '教师' }
    this.setData({
      userName: user.name || user.studentId,
      role: user.role,
      roleLabel: roleLabels[user.role] || user.role
    })
    this.loadStats()
  },

  loadStats: function () {
    var that = this
    var role = that.data.role
    var user = auth.getUser()

    if (role === 'STUDENT') {
      // 学生统计：我的活动数、我的积分
      Promise.all([
        api.get('/api/activities/student/' + user.studentId + '/registered-count').catch(function () { return { data: 0 } }),
        api.get('/api/points/student/' + user.studentId).catch(function () { return { data: { totalPoints: 0 } } })
      ]).then(function (results) {
        var activityCount = (results[0] && results[0].data) || 0
        var points = (results[1] && results[1].data && results[1].data.totalPoints) || 0
        that.setData({
          stats: [
            { label: '我的活动', value: activityCount },
            { label: '我的积分', value: points }
          ]
        })
      })
    } else if (role === 'PRESIDENT') {
      // 社长统计：待审核报名数、社团成员数
      var clubId = user.clubId || 1
      Promise.all([
        api.get('/api/member-manage/club/' + clubId + '/pending-count').catch(function () { return { data: 0 } }),
        api.get('/api/member-manage/club/' + clubId + '/member-count').catch(function () { return { data: 0 } })
      ]).then(function (results) {
        var pendingCount = (results[0] && results[0].data) || 0
        var memberCount = (results[1] && results[1].data) || 0
        that.setData({
          stats: [
            { label: '待审核', value: pendingCount },
            { label: '社团成员', value: memberCount }
          ]
        })
      })
    } else if (role === 'TEACHER') {
      // 教师统计：待审核活动数、社团数
      Promise.all([
        api.get('/api/activity-manage/pending-count').catch(function () { return { data: 0 } }),
        api.get('/api/clubs/count').catch(function () { return { data: 0 } })
      ]).then(function (results) {
        var pendingCount = (results[0] && results[0].data) || 0
        var clubCount = (results[1] && results[1].data) || 0
        that.setData({
          stats: [
            { label: '待审核', value: pendingCount },
            { label: '社团数', value: clubCount }
          ]
        })
      })
    }
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
