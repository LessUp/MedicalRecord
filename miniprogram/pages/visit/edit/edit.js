Page({
  data: {
    id: null,
    hospital: '',
    department: '',
    doctor: '',
    date: '',
    cost: '',
    items: '',
    note: '',
    isEdit: false,
    saving: false
  },

  onLoad(options) {
    if (options.id) {
      this.setData({ id: options.id, isEdit: true })
      this.loadVisit(options.id)
    } else {
      // 默认今天日期
      const today = new Date()
      const dateStr = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`
      this.setData({ date: dateStr })
    }
  },

  loadVisit(id) {
    const visits = wx.getStorageSync('visits') || []
    const visit = visits.find(v => v.id === id)
    if (visit) {
      this.setData({
        hospital: visit.hospital || '',
        department: visit.department || '',
        doctor: visit.doctor || '',
        date: visit.dateStr || '',
        cost: visit.cost ? String(visit.cost) : '',
        items: visit.items || '',
        note: visit.note || ''
      })
    }
  },

  onHospitalInput(e) {
    this.setData({ hospital: e.detail.value })
  },

  onDepartmentInput(e) {
    this.setData({ department: e.detail.value })
  },

  onDoctorInput(e) {
    this.setData({ doctor: e.detail.value })
  },

  onDateChange(e) {
    this.setData({ date: e.detail.value })
  },

  onCostInput(e) {
    this.setData({ cost: e.detail.value })
  },

  onItemsInput(e) {
    this.setData({ items: e.detail.value })
  },

  onNoteInput(e) {
    this.setData({ note: e.detail.value })
  },

  async save() {
    if (!this.data.hospital.trim()) {
      wx.showToast({ title: '请输入医院名称', icon: 'none' })
      return
    }

    if (!this.data.date) {
      wx.showToast({ title: '请选择就诊日期', icon: 'none' })
      return
    }

    this.setData({ saving: true })

    try {
      const visits = wx.getStorageSync('visits') || []
      const now = Date.now()
      const dateTimestamp = new Date(this.data.date).getTime()

      if (this.data.isEdit) {
        // 编辑
        const index = visits.findIndex(v => v.id === this.data.id)
        if (index !== -1) {
          visits[index] = {
            ...visits[index],
            hospital: this.data.hospital.trim(),
            department: this.data.department.trim() || null,
            doctor: this.data.doctor.trim() || null,
            date: dateTimestamp,
            dateStr: this.data.date,
            cost: this.data.cost ? parseFloat(this.data.cost) : null,
            items: this.data.items.trim() || null,
            note: this.data.note.trim() || null,
            updatedAt: now
          }
        }
      } else {
        // 新增
        const newVisit = {
          id: `visit_${now}`,
          hospital: this.data.hospital.trim(),
          department: this.data.department.trim() || null,
          doctor: this.data.doctor.trim() || null,
          date: dateTimestamp,
          dateStr: this.data.date,
          cost: this.data.cost ? parseFloat(this.data.cost) : null,
          items: this.data.items.trim() || null,
          note: this.data.note.trim() || null,
          createdAt: now,
          updatedAt: now
        }
        visits.unshift(newVisit)
      }

      // 按日期排序
      visits.sort((a, b) => b.date - a.date)
      wx.setStorageSync('visits', visits)

      wx.showToast({ title: '保存成功', icon: 'success' })
      
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
