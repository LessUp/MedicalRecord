package com.lessup.medledger.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.lessup.medledger.data.dao.*
import com.lessup.medledger.data.entity.*

@Database(
    entities = [
        Visit::class,
        Document::class,
        Prescription::class,
        DrugItem::class,
        ChronicCondition::class,
        CheckupPlan::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun visitDao(): VisitDao
    abstract fun documentDao(): DocumentDao
    abstract fun prescriptionDao(): PrescriptionDao
    abstract fun drugItemDao(): DrugItemDao
    abstract fun chronicConditionDao(): ChronicConditionDao
    abstract fun checkupPlanDao(): CheckupPlanDao
}
