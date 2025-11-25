Page({
  data: {
    year: new Date().getFullYear(),
    month: new Date().getMonth() + 1,
    selectedDate: null,
    visits: []
  },
  onLoad() {
    this.loadVisits()
  },
  loadVisits() {
    const visits = wx.getStorageSync('visits') || []
    this.setData({ visits })
  },
  prevMonth() {
    let { year, month } = this.data
    month--
    if (month < 1) { month = 12; year-- }
    this.setData({ year, month })
  },
  nextMonth() {
    let { year, month } = this.data
    month++
    if (month > 12) { month = 1; year++ }
    this.setData({ year, month })
  }
})
