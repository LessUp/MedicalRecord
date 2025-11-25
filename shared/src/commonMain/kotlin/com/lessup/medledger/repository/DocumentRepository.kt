package com.lessup.medledger.repository

import com.lessup.medledger.db.MedLedgerDatabase
import com.lessup.medledger.model.Document
import com.lessup.medledger.model.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DocumentRepository(
    private val database: MedLedgerDatabase
) {
    private val queries get() = database.documentQueries
    
    fun observeByVisit(visitId: Long): Flow<List<Document>> {
        return queries.selectByVisit(visitId).asFlow().mapToList().map { list ->
            list.map { it.toModel() }
        }
    }
    
    suspend fun getById(id: Long): Document? {
        return queries.selectById(id).executeAsOneOrNull()?.toModel()
    }
    
    suspend fun insert(document: Document): Long {
        val now = currentTimeMillis()
        queries.insert(
            remoteId = document.remoteId,
            userId = document.userId,
            visitId = document.visitId,
            title = document.title,
            type = document.type,
            pages = document.pages.toLong(),
            localPath = document.localPath,
            remotePath = document.remotePath,
            thumbPath = document.thumbPath,
            tags = document.tags,
            createdAt = now,
            updatedAt = now,
            syncStatus = SyncStatus.PENDING.name,
            version = 1
        )
        return queries.getLastInsertId().executeAsOne()
    }
    
    suspend fun insertScan(
        localPath: String,
        title: String,
        visitId: Long? = null,
        pages: Int = 1
    ): Long {
        return insert(
            Document(
                title = title,
                type = "scan",
                pages = pages,
                localPath = localPath,
                visitId = visitId
            )
        )
    }
    
    suspend fun delete(id: Long) {
        val now = currentTimeMillis()
        queries.softDelete(deletedAt = now, updatedAt = now, localId = id)
    }
    
    suspend fun getNeedUpload(): List<Document> {
        return queries.selectNeedUpload().executeAsList().map { it.toModel() }
    }
    
    suspend fun updateRemotePath(localId: Long, remotePath: String) {
        val now = currentTimeMillis()
        queries.updateRemotePath(remotePath, now, localId)
    }
    
    private fun com.lessup.medledger.db.Document.toModel() = Document(
        localId = localId,
        remoteId = remoteId,
        userId = userId,
        visitId = visitId,
        title = title,
        type = type,
        pages = pages.toInt(),
        localPath = localPath,
        remotePath = remotePath,
        thumbPath = thumbPath,
        tags = tags,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
        syncStatus = SyncStatus.valueOf(syncStatus),
        version = version
    )
}
