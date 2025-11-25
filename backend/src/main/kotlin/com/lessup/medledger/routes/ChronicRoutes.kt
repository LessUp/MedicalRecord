package com.lessup.medledger.routes

import com.lessup.medledger.plugins.UserPrincipal
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.chronicRoutes() {
    authenticate("auth-jwt") {
        route("/chronic") {
            get {
                val principal = call.principal<UserPrincipal>()!!
                // TODO: 从数据库获取用户的慢病记录
                call.respond(emptyList<Any>())
            }
            
            get("/{id}/plans") {
                val id = call.parameters["id"]
                // TODO: 获取复查计划
                call.respond(emptyList<Any>())
            }
        }
    }
}
