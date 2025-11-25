package com.lessup.medledger.plugins

import com.lessup.medledger.service.AuthService
import com.lessup.medledger.service.FileService
import com.lessup.medledger.service.SyncService
import io.ktor.server.application.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

val appModule = module {
    single { AuthService() }
    single { SyncService() }
    single { FileService() }
}

fun Application.configureDI() {
    install(Koin) {
        slf4jLogger()
        modules(appModule)
    }
}
