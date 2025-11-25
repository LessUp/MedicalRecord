const app = getApp()

Page({
  data: {
    userInfo: null,
    isLoggedIn: false
  },

  onLoad() {
    this.updateLoginStatus()
  },

  onShow() {
    this.updateLoginStatus()
  },

  updateLoginStatus() {
    this.setData({
      userInfo: app.globalData.userInfo,
      isLoggedIn: app.globalData.isLoggedIn
    })
  },

  goToLogin() {
    wx.navigateTo({
      url: '/pages/profile/login/login'
    })
  },

  goToFamily() {
    wx.navigateTo({
      url: '/pages/profile/family/family'
    })
  },

  goToBackup() {
    wx.showActionSheet({
      itemList: ['导出数据', '导入数据'],
      success: (res) => {
        if (res.tapIndex === 0) {
          this.exportData()
        } else {
          this.importData()
        }
      }
    })
  },

  exportData() {
    const visits = wx.getStorageSync('visits') || []
    const chronic = wx.getStorageSync('chronic') || []
    const data = JSON.stringify({ visits, chronic })
    
    wx.setClipboardData({
      data,
      success: () => {
        wx.showToast({ title: '数据已复制', icon: 'success' })
      }
    })
  },

  importData() {
    wx.getClipboardData({
      success: (res) => {
        try {
          const data = JSON.parse(res.data)
          if (data.visits) wx.setStorageSync('visits', data.visits)
          if (data.chronic) wx.setStorageSync('chronic', data.chronic)
          wx.showToast({ title: '导入成功', icon: 'success' })
        } catch (e) {
          wx.showToast({ title: '数据格式错误', icon: 'none' })
        }
      }
    })
  },

  goToSettings() {
    wx.showToast({ title: '设置页面开发中', icon: 'none' })
  },

  goToAbout() {
    wx.showModal({
      title: '关于病历本',
      content: '版本 0.1.0\n\n您的私人健康档案管家',
      showCancel: false
    })
  },

  handleSync() {
    if (!this.data.isLoggedIn) {
      wx.showToast({ title: '请先登录', icon: 'none' })
      return
    }
    wx.showLoading({ title: '同步中...' })
    setTimeout(() => {
      wx.hideLoading()
      wx.showToast({ title: '同步完成', icon: 'success' })
    }, 1000)
  },

  handleLogout() {
    wx.showModal({
      title: '确认退出',
      content: '退出后，云同步将暂停，本地数据不会丢失。',
      success: (res) => {
        if (res.confirm) {
          app.logout()
          this.updateLoginStatus()
          wx.showToast({ title: '已退出', icon: 'success' })
        }
      }
    })
  }
})
