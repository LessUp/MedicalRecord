package com.lessup.medledger.data.dao

import androidx.room.*
import com.lessup.medledger.data.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VisitDao {
    @Query("SELECT * FROM visits ORDER BY date DESC")
    fun getAll(): Flow<List<Visit>>

    @Query("SELECT * FROM visits WHERE id = :id")
    suspend fun getById(id: Long): Visit?

    @Insert
    suspend fun insert(entity: Visit): Long

    @Update
    suspend fun update(entity: Visit)

    @Delete
    suspend fun delete(entity: Visit)
}

@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents WHERE visitId = :visitId ORDER BY createdAt DESC")
    suspend fun getByVisit(visitId: Long): List<Document>

    @Insert
    suspend fun insert(entity: Document): Long

    @Update
    suspend fun update(entity: Document)

    @Delete
    suspend fun delete(entity: Document)
}

@Dao
interface PrescriptionDao {
    @Query("SELECT * FROM prescriptions WHERE visitId = :visitId")
    suspend fun getByVisit(visitId: Long): List<Prescription>

    @Insert
    suspend fun insert(entity: Prescription): Long

    @Update
    suspend fun update(entity: Prescription)

    @Delete
    suspend fun delete(entity: Prescription)
}

@Dao
interface DrugItemDao {
    @Query("SELECT * FROM drug_items WHERE prescriptionId = :prescriptionId")
    suspend fun getByPrescription(prescriptionId: Long): List<DrugItem>

    @Insert
    suspend fun insert(entity: DrugItem): Long

    @Update
    suspend fun update(entity: DrugItem)

    @Delete
    suspend fun delete(entity: DrugItem)
}

@Dao
interface ChronicConditionDao {
    @Query("SELECT * FROM chronic_conditions ORDER BY id DESC")
    suspend fun getAll(): List<ChronicCondition>

    @Insert
    suspend fun insert(entity: ChronicCondition): Long

    @Update
    suspend fun update(entity: ChronicCondition)

    @Delete
    suspend fun delete(entity: ChronicCondition)
}

@Dao
interface CheckupPlanDao {
    @Query("SELECT * FROM checkup_plans WHERE conditionId = :conditionId")
    suspend fun getByCondition(conditionId: Long): List<CheckupPlan>

    @Insert
    suspend fun insert(entity: CheckupPlan): Long

    @Update
    suspend fun update(entity: CheckupPlan)

    @Delete
    suspend fun delete(entity: CheckupPlan)
}
