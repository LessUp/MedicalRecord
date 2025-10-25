package com.lessup.medledger.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "visits", indices = [Index("date")])
data class Visit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long,
    val hospital: String,
    val department: String? = null,
    val doctor: String? = null,
    val items: String? = null, // 逗号分隔
    val cost: Double? = null,
    val note: String? = null
)

@Entity(
    tableName = "documents",
    indices = [Index("visitId")]
)
data class Document(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val type: String, // scan/prescription/report
    val createdAt: Long,
    val pages: Int = 1,
    val path: String,
    val thumbPath: String? = null,
    val visitId: Long? = null,
    val tags: String? = null // 逗号分隔
)

@Entity(
    tableName = "prescriptions",
    indices = [Index("visitId")]
)
data class Prescription(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val visitId: Long,
    val title: String? = null,
    val note: String? = null
)

@Entity(
    tableName = "drug_items",
    indices = [Index("prescriptionId")]
)
data class DrugItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val prescriptionId: Long,
    val name: String,
    val spec: String? = null,
    val dose: String? = null,
    val frequency: String? = null,
    val duration: String? = null,
    val note: String? = null
)

@Entity(tableName = "chronic_conditions")
data class ChronicCondition(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val diagnosedAt: Long? = null,
    val department: String? = null,
    val note: String? = null
)

@Entity(
    tableName = "checkup_plans",
    indices = [Index("conditionId")]
)
data class CheckupPlan(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val conditionId: Long,
    val items: String? = null, // 逗号分隔
    val intervalMonths: Int,
    val startDate: Long? = null,
    val remindDaysBefore: Int? = null
)
