# 2025-12-19 Android 核心页面 ViewModel 注入迁移到 Koin

## 变更概要
- 将核心流程页面（Home / VisitDetail / VisitEdit）从 `hiltViewModel()` 切换为 `koinViewModel()`，与 app 模块的 `appModule` ViewModel 注册保持一致。
- UI 侧列表 key / 路由参数使用 shared 模型的 `localId`，避免继续依赖 Room 实体的 `id`。
- VisitDetail 文档图标判断从旧字段切换到 shared `Document.localPath/remotePath`。

## 修改文件
- app/src/main/java/com/lessup/medledger/ui/home/HomeScreen.kt
- app/src/main/java/com/lessup/medledger/ui/visit/VisitDetailScreen.kt
- app/src/main/java/com/lessup/medledger/ui/visit/VisitEditScreen.kt

## 后续工作
- 仍有页面使用 `hiltViewModel()`：Login / Calendar / Chronic / Profile / Scan / Settings / Stats。
- 项目根目录缺少 `gradlew` 脚本，建议补齐 Gradle Wrapper（或使用本机 `gradle`）以继续进行编译验证。
