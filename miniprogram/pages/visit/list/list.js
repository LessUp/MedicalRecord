Page({
  data: {
    visits: [],
    loading: false
  },

  onLoad() {
    this.loadData()
  },

  onShow() {
    this.loadData()
  },

  loadData() {
    const visits = wx.getStorageSync('visits') || []
    this.setData({ visits })
  }
})
