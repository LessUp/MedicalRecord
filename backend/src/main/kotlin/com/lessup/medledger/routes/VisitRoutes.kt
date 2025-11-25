package com.lessup.medledger.routes

import com.lessup.medledger.plugins.UserPrincipal
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.visitRoutes() {
    authenticate("auth-jwt") {
        route("/visits") {
            get {
                val principal = call.principal<UserPrincipal>()!!
                // TODO: 从数据库获取用户的就诊记录
                call.respond(emptyList<Any>())
            }
            
            get("/{id}") {
                val id = call.parameters["id"]
                // TODO: 获取单条记录
                call.respond(mapOf("id" to id))
            }
        }
    }
}
