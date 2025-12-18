package com.lessup.medledger.sync

import com.lessup.medledger.model.SyncStatus
import com.lessup.medledger.model.Visit
import com.lessup.medledger.network.ApiClient
import com.lessup.medledger.network.SyncChange
import com.lessup.medledger.network.SyncRequest
import com.lessup.medledger.repository.VisitRepository
import com.lessup.medledger.repository.currentTimeMillis
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * 同步状态
 */
sealed class SyncState {
    object Idle : SyncState()
    object Syncing : SyncState()
    data class Success(val timestamp: Long) : SyncState()
    data class Error(val message: String) : SyncState()
}

/**
 * 冲突解决策略
 */
enum class ConflictStrategy {
    SERVER_WINS,    // 服务端优先
    CLIENT_WINS,    // 客户端优先
    LAST_WRITE_WINS // 最后写入优先
}

/**
 * 同步引擎
 */
class SyncEngine(
    private val apiClient: ApiClient,
    private val visitRepository: VisitRepository,
    private val syncPreferences: SyncPreferences,
    private val json: Json = Json { ignoreUnknownKeys = true }
) {
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState
    
    private val conflictStrategy = ConflictStrategy.LAST_WRITE_WINS
    
    /**
     * 执行同步
     */
    suspend fun sync(): Result<Unit> {
        if (_syncState.value == SyncState.Syncing) {
            return Result.failure(Exception("Sync already in progress"))
        }
        
        _syncState.value = SyncState.Syncing
        
        return try {
            // 1. Pull - 从服务端拉取变更
            val lastSyncAt = syncPreferences.getLastSyncTime()
            val pullResponse = apiClient.pullChanges(lastSyncAt)
            
            // 2. Merge - 合并远程变更到本地
            for (change in pullResponse.changes) {
                mergeRemoteChange(change)
            }
            
            // 3. Push - 推送本地变更到服务端
            val localChanges = collectLocalChanges()
            if (localChanges.isNotEmpty()) {
                val pushRequest = SyncRequest(localChanges, lastSyncAt)
                val pushResponse = apiClient.pushChanges(pushRequest)
                
                // 4. Confirm - 更新本地同步状态
                pushResponse.data?.forEach { confirmation ->
                    when (confirmation.entityType) {
                        "visit" -> visitRepository.markSynced(
                            confirmation.localId,
                            confirmation.remoteId,
                            confirmation.version
                        )
                        // TODO: 其他实体类型
                    }
                }
            }
            
            // 5. 更新同步时间
            val syncTime = pullResponse.serverTime
            syncPreferences.setLastSyncTime(syncTime)
            
            _syncState.value = SyncState.Success(syncTime)
            Result.success(Unit)
        } catch (e: Exception) {
            _syncState.value = SyncState.Error(e.message ?: "Sync failed")
            Result.failure(e)
        }
    }
    
    /**
     * 合并远程变更
     */
    private suspend fun mergeRemoteChange(change: SyncChange) {
        when (change.entityType) {
            "visit" -> mergeVisitChange(change)
            // TODO: 其他实体类型
        }
    }
    
    private suspend fun mergeVisitChange(change: SyncChange) {
        val remoteVisit = change.data?.let { json.decodeFromString<Visit>(it) }
        val remoteId = change.entityId.ifBlank { remoteVisit?.remoteId ?: return }

        when (change.action) {
            "DELETE" -> {
                val localVisit = visitRepository.getByRemoteId(remoteId) ?: return
                visitRepository.delete(localVisit.localId)
                visitRepository.markSynced(localVisit.localId, remoteId, change.version)
            }
            "INSERT", "UPDATE" -> {
                val remote = (remoteVisit ?: return).copy(remoteId = remoteId, version = change.version)
                val localVisit = visitRepository.getByRemoteId(remoteId)

                if (localVisit == null) {
                    val localId = visitRepository.insert(remote.copy(localId = 0, syncStatus = SyncStatus.SYNCED))
                    visitRepository.markSynced(localId, remoteId, change.version)
                    return
                }

                val resolved = resolveConflict(localVisit, remote)
                if (resolved.syncStatus == SyncStatus.SYNCED) {
                    visitRepository.update(resolved.copy(localId = localVisit.localId))
                    visitRepository.markSynced(localVisit.localId, remoteId, change.version)
                }
            }
        }
    }
    
    /**
     * 解决冲突
     */
    private fun resolveConflict(local: Visit, remote: Visit): Visit {
        return when (conflictStrategy) {
            ConflictStrategy.SERVER_WINS -> remote.copy(
                localId = local.localId,
                syncStatus = SyncStatus.SYNCED
            )
            ConflictStrategy.CLIENT_WINS -> local.copy(
                syncStatus = SyncStatus.PENDING
            )
            ConflictStrategy.LAST_WRITE_WINS -> {
                if (local.updatedAt > remote.updatedAt) {
                    local.copy(syncStatus = SyncStatus.PENDING)
                } else {
                    remote.copy(localId = local.localId, syncStatus = SyncStatus.SYNCED)
                }
            }
        }
    }
    
    /**
     * 收集本地待同步的变更
     */
    private suspend fun collectLocalChanges(): List<SyncChange> {
        val changes = mutableListOf<SyncChange>()
        
        // Visit changes
        val pendingVisits = visitRepository.getPendingChanges()
        for (visit in pendingVisits) {
            val action = if (visit.deletedAt != null) "DELETE" else {
                if (visit.remoteId == null) "INSERT" else "UPDATE"
            }
            changes.add(
                SyncChange(
                    entityType = "visit",
                    localId = visit.localId,
                    entityId = visit.remoteId ?: "",
                    action = action,
                    data = json.encodeToString(visit),
                    version = visit.version,
                    timestamp = visit.updatedAt
                )
            )
        }
        
        // TODO: 其他实体类型
        
        return changes
    }
}

/**
 * 同步偏好设置接口
 */
interface SyncPreferences {
    suspend fun getLastSyncTime(): Long
    suspend fun setLastSyncTime(time: Long)
}
