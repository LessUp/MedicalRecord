# 2025-12-19 Android 核心页面 ViewModel 注入迁移到 Koin
 
 ## 变更概要
 - 将核心流程页面（Home / VisitDetail / VisitEdit）从 `hiltViewModel()` 切换为 `koinViewModel()`，与 app 模块的 `appModule` ViewModel 注册保持一致。
 - 将剩余核心页面（Login / Calendar / Chronic / Profile / Scan / Settings / Stats）统一切换为 `koinViewModel()`。
 - UI 侧列表 key / 路由参数使用 shared 模型的 `localId`，避免继续依赖 Room 实体的 `id`。
 - Calendar/Chronic 页面模型引用切换到 shared 模型（如 `Visit` / `ConditionOverview` / `PlanOverview`）。
 - `SettingsViewModel` 注入 `MedLedgerDatabase`，使用 SQLDelight 的 `deleteAll` 查询在事务中实现“一键清空数据”。
 - shared SQLDelight schema 为 Visit/Document/ChronicCondition/CheckupPlan/FamilyMember 补齐 `deleteAll`（`User` 已存在）。
 - 清理构建脚本中的 Hilt/Kapt 注册（`settings.gradle.kts`），并将 app/data 下旧 Room/Hilt 代码去注解化以避免移除依赖后编译失败。
 - VisitDetail 文档图标判断从旧字段切换到 shared `Document.localPath/remotePath`。
 
 ## 修改文件
 - app/src/main/java/com/lessup/medledger/ui/home/HomeScreen.kt
 - app/src/main/java/com/lessup/medledger/ui/visit/VisitDetailScreen.kt
 - app/src/main/java/com/lessup/medledger/ui/visit/VisitEditScreen.kt
 - app/src/main/java/com/lessup/medledger/ui/calendar/CalendarScreen.kt
 - app/src/main/java/com/lessup/medledger/ui/chronic/ChronicScreen.kt
 - app/src/main/java/com/lessup/medledger/ui/settings/SettingsViewModel.kt
 - shared/src/commonMain/sqldelight/com/lessup/medledger/db/Visit.sq
 - shared/src/commonMain/sqldelight/com/lessup/medledger/db/Document.sq
 - shared/src/commonMain/sqldelight/com/lessup/medledger/db/ChronicCondition.sq
 - shared/src/commonMain/sqldelight/com/lessup/medledger/db/CheckupPlan.sq
 - shared/src/commonMain/sqldelight/com/lessup/medledger/db/FamilyMember.sq
 - settings.gradle.kts
 - app/src/main/java/com/lessup/medledger/data/entity/Entities.kt
 - app/src/main/java/com/lessup/medledger/data/dao/Daos.kt
 - app/src/main/java/com/lessup/medledger/data/db/AppDatabase.kt
 - app/src/main/java/com/lessup/medledger/data/repository/VisitRepository.kt
 - app/src/main/java/com/lessup/medledger/data/repository/DocumentRepository.kt
 - app/src/main/java/com/lessup/medledger/data/repository/ChronicRepository.kt
 
 ## 后续工作
 - 项目根目录缺少 `gradlew` 脚本，建议补齐 Gradle Wrapper（或使用本机 `gradle`）以继续进行编译验证。
 - 若确认已无引用，可删除 `app/src/main/java/com/lessup/medledger/data/*` 旧 Room 实现目录（dao/db/entity/repository）以减少维护成本。
