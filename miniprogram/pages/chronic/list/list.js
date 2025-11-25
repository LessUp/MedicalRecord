Page({
  data: {
    conditions: [],
    loading: false
  },

  onLoad() {
    this.loadData()
  },

  onShow() {
    this.loadData()
  },

  onPullDownRefresh() {
    this.loadData().then(() => wx.stopPullDownRefresh())
  },

  async loadData() {
    this.setData({ loading: true })
    try {
      const conditions = wx.getStorageSync('chronic') || []
      this.setData({ conditions })
    } finally {
      this.setData({ loading: false })
    }
  },

  goToAdd() {
    wx.navigateTo({ url: '/pages/chronic/add/add' })
  },

  goToDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: `/pages/chronic/detail/detail?id=${id}` })
  },

  deleteCondition(e) {
    const id = e.currentTarget.dataset.id
    wx.showModal({
      title: '确认删除',
      content: '确定要删除这条慢病记录吗？',
      success: (res) => {
        if (res.confirm) {
          const conditions = this.data.conditions.filter(c => c.id !== id)
          wx.setStorageSync('chronic', conditions)
          this.setData({ conditions })
          wx.showToast({ title: '已删除', icon: 'success' })
        }
      }
    })
  },

  formatDate(timestamp) {
    if (!timestamp) return ''
    const date = new Date(timestamp)
    return `${date.getFullYear()}年${date.getMonth() + 1}月`
  }
})
