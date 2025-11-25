package com.lessup.medledger.routes

import com.lessup.medledger.plugins.JwtConfig
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
data class SendCodeRequest(val phone: String)

@Serializable
data class PhoneLoginRequest(val phone: String, val code: String)

@Serializable
data class WeChatLoginRequest(val code: String)

@Serializable
data class RefreshTokenRequest(val refreshToken: String)

@Serializable
data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val user: UserResponse
)

@Serializable
data class UserResponse(
    val id: String,
    val phone: String?,
    val email: String?,
    val nickname: String,
    val avatar: String?,
    val createdAt: Long
)

fun Route.authRoutes() {
    val authService by inject<AuthService>()
    
    route("/auth") {
        // 发送验证码
        post("/send-code") {
            val request = call.receive<SendCodeRequest>()
            authService.sendVerificationCode(request.phone)
            call.respond(mapOf("message" to "验证码已发送"))
        }
        
        // 手机号登录
        post("/login/phone") {
            val request = call.receive<PhoneLoginRequest>()
            val user = authService.loginWithPhone(request.phone, request.code)
            
            call.respond(LoginResponse(
                accessToken = JwtConfig.generateAccessToken(user.id),
                refreshToken = JwtConfig.generateRefreshToken(user.id),
                expiresIn = JwtConfig.getExpiresIn(),
                user = UserResponse(
                    id = user.id,
                    phone = user.phone,
                    email = user.email,
                    nickname = user.nickname,
                    avatar = user.avatar,
                    createdAt = user.createdAt
                )
            ))
        }
        
        // 微信登录
        post("/login/wechat") {
            val request = call.receive<WeChatLoginRequest>()
            val user = authService.loginWithWeChat(request.code)
            
            call.respond(LoginResponse(
                accessToken = JwtConfig.generateAccessToken(user.id),
                refreshToken = JwtConfig.generateRefreshToken(user.id),
                expiresIn = JwtConfig.getExpiresIn(),
                user = UserResponse(
                    id = user.id,
                    phone = user.phone,
                    email = user.email,
                    nickname = user.nickname,
                    avatar = user.avatar,
                    createdAt = user.createdAt
                )
            ))
        }
        
        // 刷新 Token
        post("/refresh") {
            val request = call.receive<RefreshTokenRequest>()
            val userId = authService.validateRefreshToken(request.refreshToken)
            
            if (userId != null) {
                val user = authService.getUserById(userId)
                if (user != null) {
                    call.respond(LoginResponse(
                        accessToken = JwtConfig.generateAccessToken(user.id),
                        refreshToken = JwtConfig.generateRefreshToken(user.id),
                        expiresIn = JwtConfig.getExpiresIn(),
                        user = UserResponse(
                            id = user.id,
                            phone = user.phone,
                            email = user.email,
                            nickname = user.nickname,
                            avatar = user.avatar,
                            createdAt = user.createdAt
                        )
                    ))
                    return@post
                }
            }
            call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid refresh token"))
        }
        
        // 登出
        authenticate("auth-jwt") {
            post("/logout") {
                // 可以在这里添加 token 黑名单逻辑
                call.respond(mapOf("message" to "Logged out"))
            }
        }
    }
}
