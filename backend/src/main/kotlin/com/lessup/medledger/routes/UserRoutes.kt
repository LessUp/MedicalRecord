package com.lessup.medledger.routes

import com.lessup.medledger.plugins.UserPrincipal
import com.lessup.medledger.service.AuthService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject

@Serializable
data class UpdateProfileRequest(
    val nickname: String?,
    val avatar: String?
)

fun Route.userRoutes() {
    val authService by inject<AuthService>()
    
    authenticate("auth-jwt") {
        route("/users") {
            // 获取当前用户信息
            get("/me") {
                val principal = call.principal<UserPrincipal>()!!
                val user = authService.getUserById(principal.userId)
                
                if (user != null) {
                    call.respond(UserResponse(
                        id = user.id,
                        phone = user.phone,
                        email = user.email,
                        nickname = user.nickname,
                        avatar = user.avatar,
                        createdAt = user.createdAt
                    ))
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "User not found"))
                }
            }
            
            // 更新用户信息
            put("/me") {
                val principal = call.principal<UserPrincipal>()!!
                val request = call.receive<UpdateProfileRequest>()
                
                authService.updateProfile(principal.userId, request.nickname, request.avatar)
                call.respond(mapOf("message" to "Profile updated"))
            }
        }
    }
}
