package com.lessup.medledger.service

import com.lessup.medledger.routes.SyncChange
import com.lessup.medledger.routes.SyncConfirmation
import java.util.*

class SyncService {
    // 临时存储同步数据 (生产环境应使用数据库)
    private val syncLogs = mutableMapOf<String, MutableList<SyncChange>>()
    
    fun getChanges(userId: String, since: Long): List<SyncChange> {
        return syncLogs[userId]
            ?.filter { it.timestamp > since }
            ?: emptyList()
    }
    
    fun applyChanges(userId: String, changes: List<SyncChange>): List<SyncConfirmation> {
        val userLogs = syncLogs.getOrPut(userId) { mutableListOf() }
        val confirmations = mutableListOf<SyncConfirmation>()
        
        changes.forEach { change ->
            // 生成远程 ID
            val remoteId = if (change.entityId.isBlank()) {
                UUID.randomUUID().toString()
            } else {
                change.entityId
            }
            
            // 存储变更
            userLogs.add(change.copy(
                entityId = remoteId,
                timestamp = System.currentTimeMillis()
            ))
            
            // 生成确认
            confirmations.add(SyncConfirmation(
                entityType = change.entityType,
                localId = 0, // 客户端会根据 entityType 和时间戳匹配
                remoteId = remoteId,
                version = change.version + 1
            ))
        }
        
        return confirmations
    }
}
