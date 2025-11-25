package com.lessup.medledger.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import java.util.*

object JwtConfig {
    private val secret = System.getenv("JWT_SECRET") ?: "medledger-secret-key-change-in-production"
    private val issuer = System.getenv("JWT_ISSUER") ?: "medledger"
    private val audience = System.getenv("JWT_AUDIENCE") ?: "medledger-users"
    private val validityMs = 24 * 60 * 60 * 1000L // 24 hours
    private val refreshValidityMs = 30 * 24 * 60 * 60 * 1000L // 30 days
    
    private val algorithm = Algorithm.HMAC256(secret)
    
    val verifier = JWT
        .require(algorithm)
        .withIssuer(issuer)
        .withAudience(audience)
        .build()
    
    fun generateAccessToken(userId: String): String = JWT.create()
        .withSubject(userId)
        .withIssuer(issuer)
        .withAudience(audience)
        .withClaim("type", "access")
        .withExpiresAt(Date(System.currentTimeMillis() + validityMs))
        .sign(algorithm)
    
    fun generateRefreshToken(userId: String): String = JWT.create()
        .withSubject(userId)
        .withIssuer(issuer)
        .withAudience(audience)
        .withClaim("type", "refresh")
        .withExpiresAt(Date(System.currentTimeMillis() + refreshValidityMs))
        .sign(algorithm)
    
    fun getExpiresIn(): Long = validityMs / 1000
}

data class UserPrincipal(val userId: String) : Principal

fun Application.configureSecurity() {
    install(Authentication) {
        jwt("auth-jwt") {
            realm = "medledger"
            verifier(JwtConfig.verifier)
            validate { credential ->
                val userId = credential.payload.subject
                val type = credential.payload.getClaim("type").asString()
                if (userId != null && type == "access") {
                    UserPrincipal(userId)
                } else null
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Token is invalid or expired"))
            }
        }
    }
}
