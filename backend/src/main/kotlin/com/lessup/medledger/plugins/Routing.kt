package com.lessup.medledger.plugins

import com.lessup.medledger.routes.*
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        route("/api/v1") {
            authRoutes()
            userRoutes()
            syncRoutes()
            visitRoutes()
            chronicRoutes()
            documentRoutes()
            familyRoutes()
        }
        
        // Health check
        get("/health") {
            call.respond(mapOf("status" to "ok"))
        }
    }
}
