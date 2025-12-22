package com.lessup.medledger.data.dao

import com.lessup.medledger.data.entity.*
import kotlinx.coroutines.flow.Flow

interface VisitDao {
    fun getAll(): Flow<List<Visit>>

    suspend fun getById(id: Long): Visit?

    suspend fun insert(entity: Visit): Long

    suspend fun update(entity: Visit)

    suspend fun delete(entity: Visit)
}

interface DocumentDao {
    fun getByVisit(visitId: Long): Flow<List<Document>>

    suspend fun insert(entity: Document): Long

    suspend fun update(entity: Document)

    suspend fun delete(entity: Document)
}

interface PrescriptionDao {
    suspend fun getByVisit(visitId: Long): List<Prescription>

    suspend fun insert(entity: Prescription): Long

    suspend fun update(entity: Prescription)

    suspend fun delete(entity: Prescription)
}

interface DrugItemDao {
    suspend fun getByPrescription(prescriptionId: Long): List<DrugItem>

    suspend fun insert(entity: DrugItem): Long

    suspend fun update(entity: DrugItem)

    suspend fun delete(entity: DrugItem)
}

interface ChronicConditionDao {
    suspend fun getAll(): List<ChronicCondition>

    fun observeAll(): Flow<List<ChronicCondition>>

    suspend fun insert(entity: ChronicCondition): Long

    suspend fun update(entity: ChronicCondition)

    suspend fun delete(entity: ChronicCondition)
}

interface CheckupPlanDao {
    suspend fun getByCondition(conditionId: Long): List<CheckupPlan>

    suspend fun getAll(): List<CheckupPlan>

    fun observeAll(): Flow<List<CheckupPlan>>

    suspend fun insert(entity: CheckupPlan): Long

    suspend fun update(entity: CheckupPlan)

    suspend fun delete(entity: CheckupPlan)
}
