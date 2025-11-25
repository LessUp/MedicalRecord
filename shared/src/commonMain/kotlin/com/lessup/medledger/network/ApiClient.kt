package com.lessup.medledger.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * Token 提供者接口
 */
interface TokenProvider {
    suspend fun getAccessToken(): String?
    suspend fun getRefreshToken(): String?
    suspend fun saveTokens(accessToken: String, refreshToken: String)
    suspend fun clearTokens()
}

/**
 * API 客户端
 */
class ApiClient(
    private val baseUrl: String,
    private val tokenProvider: TokenProvider
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }
    
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(json)
        }
        
        install(Logging) {
            level = LogLevel.BODY
            logger = object : Logger {
                override fun log(message: String) {
                    println("HTTP: $message")
                }
            }
        }
        
        install(Auth) {
            bearer {
                loadTokens {
                    val access = tokenProvider.getAccessToken()
                    val refresh = tokenProvider.getRefreshToken()
                    if (access != null && refresh != null) {
                        BearerTokens(access, refresh)
                    } else null
                }
                
                refreshTokens {
                    val refresh = tokenProvider.getRefreshToken() ?: return@refreshTokens null
                    try {
                        val response = client.post("$baseUrl/api/v1/auth/refresh") {
                            contentType(ContentType.Application.Json)
                            setBody(RefreshTokenRequest(refresh))
                        }
                        val tokens: LoginResponse = response.body()
                        tokenProvider.saveTokens(tokens.accessToken, tokens.refreshToken)
                        BearerTokens(tokens.accessToken, tokens.refreshToken)
                    } catch (e: Exception) {
                        tokenProvider.clearTokens()
                        null
                    }
                }
            }
        }
        
        defaultRequest {
            contentType(ContentType.Application.Json)
        }
        
        HttpResponseValidator {
            handleResponseExceptionWithRequest { exception, _ ->
                throw ApiException(message = exception.message ?: "Network error")
            }
        }
    }
    
    // ==================== Auth ====================
    
    suspend fun sendVerificationCode(phone: String): ApiResponse<Unit> {
        return client.post("$baseUrl/api/v1/auth/send-code") {
            setBody(SendCodeRequest(phone))
        }.body()
    }
    
    suspend fun loginWithPhone(phone: String, code: String): LoginResponse {
        return client.post("$baseUrl/api/v1/auth/login/phone") {
            setBody(LoginRequest.Phone(phone, code))
        }.body()
    }
    
    suspend fun loginWithWeChat(code: String): LoginResponse {
        return client.post("$baseUrl/api/v1/auth/login/wechat") {
            setBody(LoginRequest.WeChat(code))
        }.body()
    }
    
    suspend fun loginWithApple(identityToken: String, authorizationCode: String): LoginResponse {
        return client.post("$baseUrl/api/v1/auth/login/apple") {
            setBody(LoginRequest.Apple(identityToken, authorizationCode))
        }.body()
    }
    
    suspend fun logout() {
        client.post("$baseUrl/api/v1/auth/logout")
        tokenProvider.clearTokens()
    }
    
    suspend fun getCurrentUser(): UserResponse {
        return client.get("$baseUrl/api/v1/users/me").body()
    }
    
    // ==================== Sync ====================
    
    suspend fun pullChanges(since: Long): SyncResponse {
        return client.get("$baseUrl/api/v1/sync") {
            parameter("since", since)
        }.body()
    }
    
    suspend fun pushChanges(request: SyncRequest): ApiResponse<List<SyncConfirmation>> {
        return client.post("$baseUrl/api/v1/sync") {
            setBody(request)
        }.body()
    }
    
    // ==================== File Upload ====================
    
    suspend fun getUploadUrl(filename: String, contentType: String): PresignResponse {
        return client.post("$baseUrl/api/v1/oss/presign") {
            setBody(PresignRequest(filename, contentType))
        }.body()
    }
    
    suspend fun uploadFile(uploadUrl: String, data: ByteArray, contentType: String) {
        client.put(uploadUrl) {
            setBody(data)
            contentType(ContentType.parse(contentType))
        }
    }
}

/**
 * API 异常
 */
class ApiException(
    val code: Int = -1,
    override val message: String,
    val details: String? = null
) : Exception(message)
