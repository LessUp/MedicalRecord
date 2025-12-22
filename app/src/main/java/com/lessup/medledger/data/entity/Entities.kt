package com.lessup.medledger.data.entity

data class Visit(
    val id: Long = 0,
    val date: Long,
    val hospital: String,
    val department: String? = null,
    val doctor: String? = null,
    val items: String? = null, // 逗号分隔
    val cost: Double? = null,
    val note: String? = null
)

data class Document(
    val id: Long = 0,
    val title: String,
    val type: String, // scan/prescription/report
    val createdAt: Long,
    val pages: Int = 1,
    val path: String,
    val thumbPath: String? = null,
    val visitId: Long? = null,
    val tags: String? = null // 逗号分隔
)

data class Prescription(
    val id: Long = 0,
    val visitId: Long,
    val title: String? = null,
    val note: String? = null
)

data class DrugItem(
    val id: Long = 0,
    val prescriptionId: Long,
    val name: String,
    val spec: String? = null,
    val dose: String? = null,
    val frequency: String? = null,
    val duration: String? = null,
    val note: String? = null
)

data class ChronicCondition(
    val id: Long = 0,
    val name: String,
    val diagnosedAt: Long? = null,
    val department: String? = null,
    val note: String? = null
)

data class CheckupPlan(
    val id: Long = 0,
    val conditionId: Long,
    val items: String? = null, // 逗号分隔
    val intervalMonths: Int,
    val startDate: Long? = null,
    val remindDaysBefore: Int? = null
)
