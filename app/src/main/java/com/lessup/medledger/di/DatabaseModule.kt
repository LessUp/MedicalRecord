package com.lessup.medledger.di

import android.content.Context
import androidx.room.Room
import com.lessup.medledger.data.dao.*
import com.lessup.medledger.data.db.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "medledger.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideVisitDao(db: AppDatabase): VisitDao = db.visitDao()
    @Provides fun provideDocumentDao(db: AppDatabase): DocumentDao = db.documentDao()
    @Provides fun providePrescriptionDao(db: AppDatabase): PrescriptionDao = db.prescriptionDao()
    @Provides fun provideDrugItemDao(db: AppDatabase): DrugItemDao = db.drugItemDao()
    @Provides fun provideChronicConditionDao(db: AppDatabase): ChronicConditionDao = db.chronicConditionDao()
    @Provides fun provideCheckupPlanDao(db: AppDatabase): CheckupPlanDao = db.checkupPlanDao()
}
