package com.lessup.medledger.routes

import com.lessup.medledger.plugins.UserPrincipal
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.familyRoutes() {
    authenticate("auth-jwt") {
        route("/family") {
            get {
                val principal = call.principal<UserPrincipal>()!!
                // TODO: 获取家庭成员列表
                call.respond(emptyList<Any>())
            }
            
            get("/{id}") {
                val id = call.parameters["id"]
                // TODO: 获取单个家庭成员
                call.respond(mapOf("id" to id))
            }
        }
    }
}
