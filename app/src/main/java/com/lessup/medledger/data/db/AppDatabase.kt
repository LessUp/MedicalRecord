package com.lessup.medledger.data.db

import com.lessup.medledger.data.dao.*

interface AppDatabase {
    fun visitDao(): VisitDao
    fun documentDao(): DocumentDao
    fun prescriptionDao(): PrescriptionDao
    fun drugItemDao(): DrugItemDao
    fun chronicConditionDao(): ChronicConditionDao
    fun checkupPlanDao(): CheckupPlanDao
}
