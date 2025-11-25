Page({
  data: {
    name: '',
    note: '',
    diagnosedAt: '',
    intervalMonths: 3,
    remindDays: 7,
    intervalOptions: [
      { value: 1, label: '1个月' },
      { value: 3, label: '3个月' },
      { value: 6, label: '6个月' },
      { value: 12, label: '12个月' }
    ],
    remindOptions: [
      { value: 1, label: '提前1天' },
      { value: 3, label: '提前3天' },
      { value: 7, label: '提前7天' },
      { value: 14, label: '提前14天' }
    ],
    saving: false
  },

  onNameInput(e) {
    this.setData({ name: e.detail.value })
  },

  onNoteInput(e) {
    this.setData({ note: e.detail.value })
  },

  onDateChange(e) {
    this.setData({ diagnosedAt: e.detail.value })
  },

  onIntervalChange(e) {
    this.setData({ intervalMonths: this.data.intervalOptions[e.detail.value].value })
  },

  onRemindChange(e) {
    this.setData({ remindDays: this.data.remindOptions[e.detail.value].value })
  },

  async save() {
    if (!this.data.name.trim()) {
      wx.showToast({ title: '请输入疾病名称', icon: 'none' })
      return
    }

    this.setData({ saving: true })

    try {
      const conditions = wx.getStorageSync('chronic') || []
      const now = Date.now()
      
      const newCondition = {
        id: `chronic_${now}`,
        name: this.data.name.trim(),
        note: this.data.note.trim(),
        diagnosedAt: this.data.diagnosedAt ? new Date(this.data.diagnosedAt).getTime() : null,
        diagnosedAtStr: this.data.diagnosedAt,
        plans: [{
          id: `plan_${now}`,
          intervalMonths: this.data.intervalMonths,
          remindDaysBefore: this.data.remindDays,
          startDate: now
        }],
        createdAt: now,
        updatedAt: now
      }

      // 计算下次复查日期
      const nextDate = new Date(now)
      nextDate.setMonth(nextDate.getMonth() + this.data.intervalMonths)
      newCondition.nextCheckDate = nextDate.getTime()
      newCondition.nextCheckDateStr = `${nextDate.getFullYear()}年${nextDate.getMonth() + 1}月${nextDate.getDate()}日`

      conditions.unshift(newCondition)
      wx.setStorageSync('chronic', conditions)

      wx.showToast({ title: '添加成功', icon: 'success' })
      
      setTimeout(() => {
        wx.navigateBack()
      }, 1500)
    } catch (e) {
      wx.showToast({ title: '保存失败', icon: 'none' })
    } finally {
      this.setData({ saving: false })
    }
  }
})
