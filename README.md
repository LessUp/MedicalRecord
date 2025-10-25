# 病历本（Med Ledger）

一个优雅、实用的 Android 病历管理应用。

- 功能：就诊记录、处方/文档扫描、慢病复查提醒、搜索/筛选、备份导出
- 技术栈：Kotlin + Jetpack Compose + Material 3 + Hilt + Room + DataStore + WorkManager + CameraX
- 包名：`com.lessup.medledger`
- SDK：minSdk 26，targetSdk 35

## 快速开始

1. Android Studio 打开工程并 Sync
2. 直接运行到模拟器或真机

或使用脚本（首次先赋权）：

```bash
chmod +x scripts/*.sh
bash scripts/setup_gradle_wrapper.sh
bash scripts/build_debug.sh
bash scripts/install_debug.sh
```

更多部署细节见：`docs/部署.md`

## 当前进度（MVP）

- [x] 工程脚手架、依赖与主题
- [x] 导航与三大页面（首页/慢病/设置）占位
- [x] 数据层（Room 实体/DAO/DB）与 Hilt DI
- [x] 就诊记录：列表展示、删除、编辑/新增表单
- [ ] 文档扫描 MVP（CameraX 预览/拍照/保存）
- [ ] 慢病与复查提醒骨架（WorkManager + 通知）
- [ ] 搜索/筛选与日历视图
- [ ] 备份导出（ZIP/PDF）

## 目录结构

- `app/` Android 应用模块
- `docs/` 设计与部署文档
- `changelog/` 变更记录
- `scripts/` 构建与安装脚本

## 许可与隐私

- 默认离线，本地存储；导出前会提示内容范围
- 后续将补充隐私合规说明
