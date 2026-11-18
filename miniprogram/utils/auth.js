// 登录状态管理工具

function getUser() {
  return wx.getStorageSync('user') || null
}

function getToken() {
  return wx.getStorageSync('token') || ''
}

function isLoggedIn() {
  return !!getToken()
}

function getRole() {
  var user = getUser()
  return user ? user.role : ''
}

function getRoleLabel(role) {
  var labels = {
    'STUDENT': '学生',
    'PRESIDENT': '社长',
    'TEACHER': '团委老师',
    'ADMIN': '系统管理员'
  }
  return labels[role] || role
}

function logout() {
  wx.removeStorageSync('token')
  wx.removeStorageSync('user')
}

module.exports = {
  getUser: getUser,
  getToken: getToken,
  isLoggedIn: isLoggedIn,
  getRole: getRole,
  getRoleLabel: getRoleLabel,
  logout: logout
}
