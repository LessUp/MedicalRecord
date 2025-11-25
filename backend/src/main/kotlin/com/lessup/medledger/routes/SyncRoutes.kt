package com.lessup.medledger.routes

import com.lessup.medledger.plugins.UserPrincipal
import com.lessup.medledger.service.SyncService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject

@Serializable
data class SyncChange(
    val entityType: String,
    val entityId: String,
    val action: String,
    val data: String?,
    val version: Long,
    val timestamp: Long
)

@Serializable
data class SyncRequest(
    val changes: List<SyncChange>,
    val lastSyncAt: Long
)

@Serializable
data class SyncResponse(
    val changes: List<SyncChange>,
    val serverTime: Long,
    val hasMore: Boolean = false
)

@Serializable
data class SyncConfirmation(
    val entityType: String,
    val localId: Long,
    val remoteId: String,
    val version: Long
)

fun Route.syncRoutes() {
    val syncService by inject<SyncService>()
    
    authenticate("auth-jwt") {
        route("/sync") {
            // 拉取变更
            get {
                val principal = call.principal<UserPrincipal>()!!
                val since = call.request.queryParameters["since"]?.toLongOrNull() ?: 0
                
                val changes = syncService.getChanges(principal.userId, since)
                
                call.respond(SyncResponse(
                    changes = changes,
                    serverTime = System.currentTimeMillis(),
                    hasMore = false
                ))
            }
            
            // 推送变更
            post {
                val principal = call.principal<UserPrincipal>()!!
                val request = call.receive<SyncRequest>()
                
                val confirmations = syncService.applyChanges(principal.userId, request.changes)
                
                call.respond(mapOf(
                    "code" to 0,
                    "message" to "Success",
                    "data" to confirmations
                ))
            }
        }
    }
}
