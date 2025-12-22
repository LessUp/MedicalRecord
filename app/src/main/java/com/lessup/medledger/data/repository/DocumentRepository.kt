package com.lessup.medledger.data.repository

import com.lessup.medledger.data.dao.DocumentDao
import com.lessup.medledger.data.entity.Document
import kotlinx.coroutines.flow.Flow

class DocumentRepository(
    private val documentDao: DocumentDao
) {
    fun getByVisit(visitId: Long): Flow<List<Document>> = documentDao.getByVisit(visitId)

    suspend fun insertScan(path: String, title: String, visitId: Long? = null, pages: Int = 1): Long {
        val now = System.currentTimeMillis()
        val doc = Document(
            id = 0L,
            title = title,
            type = "scan",
            createdAt = now,
            pages = pages,
            path = path,
            thumbPath = null,
            visitId = visitId,
            tags = null
        )
        return documentDao.insert(doc)
    }
}
