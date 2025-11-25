# 病历本（Med Ledger）

一个优雅、实用的**跨平台**病历管理应用。

## 核心功能
- 📋 就诊记录管理（搜索、筛选、日历视图）
- 📷 处方/文档扫描（拍照、PDF导出）
- 💊 慢病复查提醒（智能提醒、进度跟踪）
- 👨‍👩‍👧‍👦 家庭成员管理（多人健康档案）
- ☁️ 云端同步（多设备数据同步）
- 🔐 隐私安全（本地加密、用户认证）

## 技术架构
- **共享层 (KMP)**: Kotlin Multiplatform + SQLDelight + Ktor + Koin
- **Android**: Jetpack Compose + Material 3 + Hilt + WorkManager + CameraX
- **iOS**: SwiftUI (规划中)
- **小程序**: 原生/Taro (规划中)

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

## 当前进度

### ✅ 已完成
- [x] 工程脚手架、依赖与主题
- [x] 导航与四大页面（首页/慢病/我的/设置）
- [x] 就诊记录：列表展示、搜索筛选、编辑/新增
- [x] 文档扫描（CameraX 预览/拍照/PDF导出）
- [x] 慢病与复查提醒（WorkManager + 通知）
- [x] 日历视图
- [x] 备份导出（ZIP压缩包）
- [x] 美化UI界面（Material 3 主题、卡片布局、动画效果）
- [x] **跨平台架构 (KMP)**：共享模块、SQLDelight、Koin
- [x] **网络层**：Ktor Client、API 定义
- [x] **同步引擎**：数据同步、冲突解决
- [x] **用户认证**：手机号登录、微信登录UI
- [x] **家庭成员**：多人健康档案管理UI

### 🚧 进行中
- [ ] 后端 API 服务部署
- [ ] iOS 平台开发
- [ ] 微信小程序开发
- [ ] 费用统计分析

## 项目结构

```
MedicalRecord/
├── app/                    # Android 应用模块
│   └── src/main/java/com/lessup/medledger/
│       ├── ui/             # Compose UI 组件
│       │   ├── auth/       # 登录认证
│       │   ├── home/       # 首页
│       │   ├── chronic/    # 慢病管理
│       │   ├── profile/    # 个人中心
│       │   ├── family/     # 家庭成员
│       │   ├── calendar/   # 日历视图
│       │   └── ...
│       ├── data/           # 数据层 (Room - 渐进迁移中)
│       └── di/             # 依赖注入
├── shared/                 # KMP 共享模块
│   └── src/
│       ├── commonMain/     # 通用代码
│       │   ├── model/      # 数据模型
│       │   ├── network/    # 网络层
│       │   ├── repository/ # 数据仓库
│       │   ├── sync/       # 同步引擎
│       │   └── di/         # Koin 模块
│       ├── androidMain/    # Android 特定实现
│       └── iosMain/        # iOS 特定实现
└── docs/                   # 文档
    ├── 跨平台架构规划.md
    └── 技术实施指南.md
```

## 许可与隐私

- 默认离线，本地存储；导出前会提示内容范围
- 后续将补充隐私合规说明
