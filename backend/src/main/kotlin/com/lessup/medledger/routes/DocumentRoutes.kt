package com.lessup.medledger.routes

import com.lessup.medledger.plugins.UserPrincipal
import com.lessup.medledger.service.FileService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject

@Serializable
data class PresignRequest(
    val filename: String,
    val contentType: String
)

@Serializable
data class PresignResponse(
    val uploadUrl: String,
    val downloadUrl: String,
    val key: String,
    val expiresAt: Long
)

fun Route.documentRoutes() {
    val fileService by inject<FileService>()
    
    authenticate("auth-jwt") {
        route("/documents") {
            get {
                val principal = call.principal<UserPrincipal>()!!
                // TODO: 获取用户文档列表
                call.respond(emptyList<Any>())
            }
        }
        
        route("/oss") {
            // 获取预签名上传 URL
            post("/presign") {
                val principal = call.principal<UserPrincipal>()!!
                val request = call.receive<PresignRequest>()
                
                val presign = fileService.generatePresignedUrl(
                    userId = principal.userId,
                    filename = request.filename,
                    contentType = request.contentType
                )
                
                call.respond(presign)
            }
        }
    }
}
