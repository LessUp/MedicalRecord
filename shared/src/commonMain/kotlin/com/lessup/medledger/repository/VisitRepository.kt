package com.lessup.medledger.repository

import com.lessup.medledger.db.MedLedgerDatabase
import com.lessup.medledger.model.SyncStatus
import com.lessup.medledger.model.Visit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class VisitRepository(
    private val database: MedLedgerDatabase
) {
    private val queries get() = database.visitQueries
    
    fun observeAll(): Flow<List<Visit>> {
        return queries.selectAll().asFlow().mapToList().map { list ->
            list.map { it.toModel() }
        }
    }
    
    fun observeByDateRange(startDate: Long, endDate: Long): Flow<List<Visit>> {
        return queries.selectByDateRange(startDate, endDate).asFlow().mapToList().map { list ->
            list.map { it.toModel() }
        }
    }
    
    suspend fun getById(id: Long): Visit? {
        return queries.selectById(id).executeAsOneOrNull()?.toModel()
    }
    
    suspend fun insert(visit: Visit): Long {
        val now = currentTimeMillis()
        queries.insert(
            remoteId = visit.remoteId,
            userId = visit.userId,
            memberId = visit.memberId,
            date = visit.date,
            hospital = visit.hospital,
            department = visit.department,
            doctor = visit.doctor,
            items = visit.items,
            cost = visit.cost,
            note = visit.note,
            createdAt = now,
            updatedAt = now,
            syncStatus = SyncStatus.PENDING.name,
            version = 1
        )
        return queries.getLastInsertId().executeAsOne()
    }
    
    suspend fun update(visit: Visit) {
        val now = currentTimeMillis()
        queries.update(
            memberId = visit.memberId,
            date = visit.date,
            hospital = visit.hospital,
            department = visit.department,
            doctor = visit.doctor,
            items = visit.items,
            cost = visit.cost,
            note = visit.note,
            updatedAt = now,
            localId = visit.localId
        )
    }
    
    suspend fun delete(id: Long) {
        val now = currentTimeMillis()
        queries.softDelete(deletedAt = now, updatedAt = now, localId = id)
    }
    
    suspend fun getPendingChanges(): List<Visit> {
        return queries.selectPending().executeAsList().map { it.toModel() }
    }
    
    suspend fun markSynced(localId: Long, remoteId: String, version: Long) {
        queries.updateSyncStatus(
            syncStatus = SyncStatus.SYNCED.name,
            remoteId = remoteId,
            version = version,
            localId = localId
        )
    }
    
    // 统计方法
    suspend fun countByMonth(startOfMonth: Long, endOfMonth: Long): Long {
        return queries.countByMonth(startOfMonth, endOfMonth).executeAsOne()
    }
    
    suspend fun sumCostByMonth(startOfMonth: Long, endOfMonth: Long): Double {
        return queries.sumCostByMonth(startOfMonth, endOfMonth).executeAsOneOrNull() ?: 0.0
    }
    
    private fun com.lessup.medledger.db.Visit.toModel() = Visit(
        localId = localId,
        remoteId = remoteId,
        userId = userId,
        memberId = memberId,
        date = date,
        hospital = hospital,
        department = department,
        doctor = doctor,
        items = items,
        cost = cost,
        note = note,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
        syncStatus = SyncStatus.valueOf(syncStatus),
        version = version
    )
}

// Platform-specific implementations will be needed
expect fun currentTimeMillis(): Long

// Flow extensions for SQLDelight
expect fun <T : Any> app.cash.sqldelight.Query<T>.asFlow(): Flow<app.cash.sqldelight.Query<T>>
expect fun <T : Any> Flow<app.cash.sqldelight.Query<T>>.mapToList(): Flow<List<T>>
