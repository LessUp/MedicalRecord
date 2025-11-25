const app = getApp()

Page({
  data: {
    loading: false
  },

  async loginWithWeChat() {
    this.setData({ loading: true })
    try {
      await app.login()
      wx.showToast({ title: '登录成功', icon: 'success' })
      setTimeout(() => wx.navigateBack(), 1500)
    } catch (e) {
      wx.showToast({ title: e.message || '登录失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },

  skipLogin() {
    wx.navigateBack()
  }
})
