package com.lessup.medledger.model

import kotlinx.serialization.Serializable

/**
 * 同步状态
 */
enum class SyncStatus {
    SYNCED,     // 已同步
    PENDING,    // 待同步
    CONFLICT    // 冲突
}

/**
 * 家庭成员关系
 */
enum class Relationship(val displayName: String) {
    SELF("本人"),
    SPOUSE("配偶"),
    CHILD("子女"),
    PARENT("父母"),
    OTHER("其他")
}

/**
 * 用户
 */
@Serializable
data class User(
    val id: String,
    val phone: String? = null,
    val email: String? = null,
    val nickname: String,
    val avatar: String? = null,
    val wechatUnionId: String? = null,
    val appleId: String? = null,
    val createdAt: Long,
    val lastLoginAt: Long? = null,
    val lastSyncAt: Long? = null
)

/**
 * 家庭成员
 */
@Serializable
data class FamilyMember(
    val localId: Long = 0,
    val remoteId: String? = null,
    val userId: String,
    val name: String,
    val relationship: Relationship,
    val gender: String? = null,
    val birthDate: Long? = null,
    val avatar: String? = null,
    val medicalCardNo: String? = null,
    val isDefault: Boolean = false,
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val version: Long = 1
)

/**
 * 就诊记录
 */
@Serializable
data class Visit(
    val localId: Long = 0,
    val remoteId: String? = null,
    val userId: String? = null,
    val memberId: String? = null,
    val date: Long,
    val hospital: String,
    val department: String? = null,
    val doctor: String? = null,
    val items: String? = null,
    val cost: Double? = null,
    val note: String? = null,
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
    val deletedAt: Long? = null,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val version: Long = 1
)

/**
 * 文档/扫描件
 */
@Serializable
data class Document(
    val localId: Long = 0,
    val remoteId: String? = null,
    val userId: String? = null,
    val visitId: Long? = null,
    val title: String,
    val type: String,
    val pages: Int = 1,
    val localPath: String? = null,
    val remotePath: String? = null,
    val thumbPath: String? = null,
    val tags: String? = null,
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
    val deletedAt: Long? = null,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val version: Long = 1
)

/**
 * 慢病档案
 */
@Serializable
data class ChronicCondition(
    val localId: Long = 0,
    val remoteId: String? = null,
    val userId: String? = null,
    val memberId: String? = null,
    val name: String,
    val diagnosedAt: Long? = null,
    val department: String? = null,
    val note: String? = null,
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
    val deletedAt: Long? = null,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val version: Long = 1
)

/**
 * 复查计划
 */
@Serializable
data class CheckupPlan(
    val localId: Long = 0,
    val remoteId: String? = null,
    val userId: String? = null,
    val conditionId: Long,
    val items: String? = null,
    val intervalMonths: Int,
    val startDate: Long? = null,
    val remindDaysBefore: Int? = null,
    val lastRemindAt: Long? = null,
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
    val deletedAt: Long? = null,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val version: Long = 1
)

/**
 * 慢病概览（含复查计划）
 */
data class ConditionOverview(
    val condition: ChronicCondition,
    val plans: List<PlanOverview>
)

/**
 * 计划概览
 */
data class PlanOverview(
    val plan: CheckupPlan,
    val nextCheckDate: Long?,
    val remindDate: Long?
)

/**
 * 费用统计摘要
 */
data class CostSummary(
    val totalCost: Double,
    val visitCount: Int,
    val avgCostPerVisit: Double,
    val byHospital: Map<String, Double>,
    val byDepartment: Map<String, Double>
)

/**
 * 月度费用
 */
data class MonthlyCost(
    val year: Int,
    val month: Int,
    val cost: Double,
    val visitCount: Int
)
