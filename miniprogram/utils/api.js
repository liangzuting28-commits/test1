// API 请求封装（替代 Axios）

var app = getApp()

function request(options) {
  return new Promise(function (resolve, reject) {
    var token = wx.getStorageSync('token') || ''
    var user = wx.getStorageSync('user') || {}
    var baseUrl = app.globalData.baseUrl

    wx.request({
      url: baseUrl + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': options.contentType || 'application/json',
        'Authorization': token ? 'Bearer ' + token : '',
        'X-User-Id': user.studentId || ''
      },
      success: function (res) {
        if (res.statusCode === 401) {
          wx.removeStorageSync('token')
          wx.removeStorageSync('user')
          wx.reLaunch({ url: '/pages/login/login' })
          reject(new Error('未登录'))
          return
        }
        resolve(res.data)
      },
      fail: function (err) {
        wx.showToast({ title: '网络错误', icon: 'none' })
        reject(err)
      }
    })
  })
}

function get(url) {
  return request({ url: url, method: 'GET' })
}

function post(url, data) {
  return request({ url: url, method: 'POST', data: data })
}

function put(url, data) {
  return request({ url: url, method: 'PUT', data: data })
}

function del(url) {
  return request({ url: url, method: 'DELETE' })
}

function upload(url, filePath, formData) {
  return new Promise(function (resolve, reject) {
    var token = wx.getStorageSync('token') || ''
    var user = wx.getStorageSync('user') || {}
    var baseUrl = app.globalData.baseUrl

    wx.uploadFile({
      url: baseUrl + url,
      filePath: filePath,
      name: 'file',
      formData: formData || {},
      header: {
        'Authorization': token ? 'Bearer ' + token : '',
        'X-User-Id': user.studentId || ''
      },
      success: function (res) {
        if (res.statusCode === 401) {
          wx.removeStorageSync('token')
          wx.removeStorageSync('user')
          wx.reLaunch({ url: '/pages/login/login' })
          reject(new Error('未登录'))
          return
        }
        resolve(JSON.parse(res.data))
      },
      fail: function (err) {
        wx.showToast({ title: '上传失败', icon: 'none' })
        reject(err)
      }
    })
  })
}

module.exports = {
  get: get,
  post: post,
  put: put,
  del: del,
  upload: upload
}
