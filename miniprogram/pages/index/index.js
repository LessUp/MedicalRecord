const app = getApp()

Page({
  data: {
    visits: [],
    loading: false,
    searchValue: '',
    stats: {
      monthlyCount: 0,
      yearlyCount: 0
    }
  },

  onLoad() {
    this.loadData()
  },

  onShow() {
    this.loadData()
  },

  onPullDownRefresh() {
    this.loadData().then(() => {
      wx.stopPullDownRefresh()
    })
  },

  async loadData() {
    this.setData({ loading: true })
    try {
      // 从本地存储或 API 获取数据
      const visits = wx.getStorageSync('visits') || []
      
      // 计算统计数据
      const now = new Date()
      const monthStart = new Date(now.getFullYear(), now.getMonth(), 1).getTime()
      const yearStart = new Date(now.getFullYear(), 0, 1).getTime()
      
      const monthlyCount = visits.filter(v => v.date >= monthStart).length
      const yearlyCount = visits.filter(v => v.date >= yearStart).length

      this.setData({
        visits,
        stats: { monthlyCount, yearlyCount }
      })
    } catch (e) {
      wx.showToast({ title: '加载失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },

  onSearchInput(e) {
    this.setData({ searchValue: e.detail.value })
  },

  get filteredVisits() {
    const { visits, searchValue } = this.data
    if (!searchValue) return visits
    const keyword = searchValue.toLowerCase()
    return visits.filter(v => 
      v.hospital.toLowerCase().includes(keyword) ||
      (v.department && v.department.toLowerCase().includes(keyword)) ||
      (v.doctor && v.doctor.toLowerCase().includes(keyword))
    )
  },

  goToDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({
      url: `/pages/visit/detail/detail?id=${id}`
    })
  },

  goToAdd() {
    wx.navigateTo({
      url: '/pages/visit/edit/edit'
    })
  },

  goToCalendar() {
    wx.navigateTo({
      url: '/pages/calendar/index/index'
    })
  },

  deleteVisit(e) {
    const id = e.currentTarget.dataset.id
    wx.showModal({
      title: '确认删除',
      content: '确定要删除这条就诊记录吗？',
      success: (res) => {
        if (res.confirm) {
          const visits = this.data.visits.filter(v => v.id !== id)
          wx.setStorageSync('visits', visits)
          this.setData({ visits })
          wx.showToast({ title: '已删除', icon: 'success' })
        }
      }
    })
  },

  formatDate(timestamp) {
    const date = new Date(timestamp)
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
  }
})
