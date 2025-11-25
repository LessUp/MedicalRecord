Page({
  data: {
    members: []
  },
  onLoad() {
    this.setData({
      members: [{ id: '1', name: '本人', relationship: '本人', isDefault: true }]
    })
  }
})
