Page({
  data: { visit: null },
  onLoad(options) {
    if (options.id) {
      const visits = wx.getStorageSync('visits') || []
      const visit = visits.find(v => v.id === options.id)
      this.setData({ visit })
    }
  }
})
