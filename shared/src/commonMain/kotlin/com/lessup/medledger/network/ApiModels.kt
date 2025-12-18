package com.lessup.medledger.network

import kotlinx.serialization.Serializable

/**
 * 登录请求
 */
@Serializable
sealed class LoginRequest {
    @Serializable
    data class Phone(
        val phone: String,
        val code: String
    ) : LoginRequest()
    
    @Serializable
    data class WeChat(
        val code: String
    ) : LoginRequest()
    
    @Serializable
    data class Apple(
        val identityToken: String,
        val authorizationCode: String
    ) : LoginRequest()
}

/**
 * 登录响应
 */
@Serializable
data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val user: UserResponse
)

/**
 * 用户信息响应
 */
@Serializable
data class UserResponse(
    val id: String,
    val phone: String?,
    val email: String?,
    val nickname: String,
    val avatar: String?,
    val createdAt: Long
)

/**
 * 刷新Token请求
 */
@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)

/**
 * 同步变更
 */
@Serializable
data class SyncChange(
    val entityType: String,     // visit, document, chronic_condition, checkup_plan, family_member
    val localId: Long = 0,
    val entityId: String,       // remoteId
    val action: String,         // INSERT, UPDATE, DELETE
    val data: String?,          // JSON serialized entity data
    val version: Long,
    val timestamp: Long
)

/**
 * 同步请求
 */
@Serializable
data class SyncRequest(
    val changes: List<SyncChange>,
    val lastSyncAt: Long
)

/**
 * 同步响应
 */
@Serializable
data class SyncResponse(
    val changes: List<SyncChange>,
    val serverTime: Long,
    val hasMore: Boolean = false
)

/**
 * 同步确认
 */
@Serializable
data class SyncConfirmation(
    val entityType: String,
    val localId: Long,
    val remoteId: String,
    val version: Long
)

/**
 * 上传预签名URL请求
 */
@Serializable
data class PresignRequest(
    val filename: String,
    val contentType: String
)

/**
 * 上传预签名URL响应
 */
@Serializable
data class PresignResponse(
    val uploadUrl: String,
    val downloadUrl: String,
    val key: String,
    val expiresAt: Long
)

/**
 * 发送验证码请求
 */
@Serializable
data class SendCodeRequest(
    val phone: String
)

/**
 * 通用响应包装
 */
@Serializable
data class ApiResponse<T>(
    val code: Int,
    val message: String,
    val data: T?
)

/**
 * 错误响应
 */
@Serializable
data class ApiError(
    val code: Int,
    val message: String,
    val details: String? = null
)
