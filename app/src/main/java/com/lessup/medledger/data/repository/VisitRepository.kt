package com.lessup.medledger.data.repository

import com.lessup.medledger.data.dao.VisitDao
import com.lessup.medledger.data.entity.Visit
import kotlinx.coroutines.flow.Flow

class VisitRepository(
    private val visitDao: VisitDao
) {
    fun getVisits(): Flow<List<Visit>> = visitDao.getAll()

    suspend fun getById(id: Long): Visit? = visitDao.getById(id)

    suspend fun upsert(visit: Visit): Long =
        if (visit.id == 0L) visitDao.insert(visit) else {
            visitDao.update(visit)
            visit.id
        }

    suspend fun delete(visit: Visit) = visitDao.delete(visit)
}
