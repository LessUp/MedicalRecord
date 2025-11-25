App({
  globalData: {
    userInfo: null,
    isLoggedIn: false,
    apiBaseUrl: 'https://api.medledger.lessup.com/api/v1'
  },

  onLaunch() {
    // 检查登录状态
    this.checkLoginStatus()
  },

  checkLoginStatus() {
    const token = wx.getStorageSync('accessToken')
    if (token) {
      this.globalData.isLoggedIn = true
      this.getUserInfo()
    }
  },

  async getUserInfo() {
    try {
      const res = await this.request({
        url: '/users/me',
        method: 'GET'
      })
      this.globalData.userInfo = res.data
    } catch (e) {
      console.error('获取用户信息失败', e)
    }
  },

  // 统一请求方法
  request(options) {
    return new Promise((resolve, reject) => {
      const token = wx.getStorageSync('accessToken')
      wx.request({
        url: this.globalData.apiBaseUrl + options.url,
        method: options.method || 'GET',
        data: options.data,
        header: {
          'Content-Type': 'application/json',
          'Authorization': token ? `Bearer ${token}` : ''
        },
        success(res) {
          if (res.statusCode >= 200 && res.statusCode < 300) {
            resolve(res.data)
          } else if (res.statusCode === 401) {
            // Token 过期，清除登录状态
            wx.removeStorageSync('accessToken')
            wx.removeStorageSync('refreshToken')
            reject(new Error('登录已过期'))
          } else {
            reject(new Error(res.data.error || '请求失败'))
          }
        },
        fail(err) {
          reject(err)
        }
      })
    })
  },

  // 登录
  async login() {
    return new Promise((resolve, reject) => {
      wx.login({
        success: async (res) => {
          if (res.code) {
            try {
              const loginRes = await this.request({
                url: '/auth/login/wechat',
                method: 'POST',
                data: { code: res.code }
              })
              wx.setStorageSync('accessToken', loginRes.accessToken)
              wx.setStorageSync('refreshToken', loginRes.refreshToken)
              this.globalData.isLoggedIn = true
              this.globalData.userInfo = loginRes.user
              resolve(loginRes)
            } catch (e) {
              reject(e)
            }
          } else {
            reject(new Error('微信登录失败'))
          }
        },
        fail: reject
      })
    })
  },

  // 登出
  logout() {
    wx.removeStorageSync('accessToken')
    wx.removeStorageSync('refreshToken')
    this.globalData.isLoggedIn = false
    this.globalData.userInfo = null
  }
})
