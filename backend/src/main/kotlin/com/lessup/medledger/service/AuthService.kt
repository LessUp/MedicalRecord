package com.lessup.medledger.service

import com.auth0.jwt.JWT
import com.lessup.medledger.plugins.JwtConfig
import java.util.*

data class User(
    val id: String,
    val phone: String?,
    val email: String?,
    val nickname: String,
    val avatar: String?,
    val wechatUnionId: String? = null,
    val createdAt: Long
)

class AuthService {
    // 临时存储验证码 (生产环境应使用 Redis)
    private val verificationCodes = mutableMapOf<String, String>()
    
    // 临时用户存储 (生产环境应使用数据库)
    private val users = mutableMapOf<String, User>()
    
    fun sendVerificationCode(phone: String) {
        // TODO: 实际发送短信
        val code = (100000..999999).random().toString()
        verificationCodes[phone] = code
        println("验证码: $phone -> $code") // 开发时打印
    }
    
    fun loginWithPhone(phone: String, code: String): User {
        val storedCode = verificationCodes[phone]
        if (storedCode != code && code != "123456") { // 开发时允许 123456
            throw IllegalArgumentException("验证码错误")
        }
        verificationCodes.remove(phone)
        
        // 查找或创建用户
        val existingUser = users.values.find { it.phone == phone }
        if (existingUser != null) {
            return existingUser
        }
        
        val newUser = User(
            id = UUID.randomUUID().toString(),
            phone = phone,
            email = null,
            nickname = "用户${phone.takeLast(4)}",
            avatar = null,
            createdAt = System.currentTimeMillis()
        )
        users[newUser.id] = newUser
        return newUser
    }
    
    fun loginWithWeChat(code: String): User {
        // TODO: 调用微信 API 获取 openid 和用户信息
        val mockUnionId = "wx_$code"
        
        val existingUser = users.values.find { it.wechatUnionId == mockUnionId }
        if (existingUser != null) {
            return existingUser
        }
        
        val newUser = User(
            id = UUID.randomUUID().toString(),
            phone = null,
            email = null,
            nickname = "微信用户",
            avatar = null,
            wechatUnionId = mockUnionId,
            createdAt = System.currentTimeMillis()
        )
        users[newUser.id] = newUser
        return newUser
    }
    
    fun validateRefreshToken(token: String): String? {
        return try {
            val decodedJWT = JwtConfig.verifier.verify(token)
            val type = decodedJWT.getClaim("type").asString()
            if (type == "refresh") {
                decodedJWT.subject
            } else null
        } catch (e: Exception) {
            null
        }
    }
    
    fun getUserById(userId: String): User? {
        return users[userId]
    }
    
    fun updateProfile(userId: String, nickname: String?, avatar: String?) {
        val user = users[userId] ?: return
        users[userId] = user.copy(
            nickname = nickname ?: user.nickname,
            avatar = avatar ?: user.avatar
        )
    }
}
