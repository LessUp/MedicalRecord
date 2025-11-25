package com.lessup.medledger.service

import com.lessup.medledger.routes.PresignResponse
import java.util.*

class FileService {
    private val baseUrl = System.getenv("OSS_BASE_URL") ?: "https://oss.medledger.lessup.com"
    
    fun generatePresignedUrl(userId: String, filename: String, contentType: String): PresignResponse {
        val extension = filename.substringAfterLast(".", "")
        val key = "users/$userId/documents/${UUID.randomUUID()}.$extension"
        val expiresAt = System.currentTimeMillis() + 3600 * 1000 // 1 hour
        
        // TODO: 使用实际的 OSS SDK 生成预签名 URL
        // 这里返回模拟数据
        return PresignResponse(
            uploadUrl = "$baseUrl/$key?upload=true&expires=$expiresAt",
            downloadUrl = "$baseUrl/$key",
            key = key,
            expiresAt = expiresAt
        )
    }
}
