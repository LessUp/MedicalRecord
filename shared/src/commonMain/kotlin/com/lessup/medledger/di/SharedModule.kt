package com.lessup.medledger.di

import com.lessup.medledger.db.MedLedgerDatabase
import com.lessup.medledger.network.ApiClient
import com.lessup.medledger.repository.ChronicRepository
import com.lessup.medledger.repository.DocumentRepository
import com.lessup.medledger.repository.VisitRepository
import com.lessup.medledger.sync.SyncEngine
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * 共享模块的 Koin 配置
 */
val sharedModule = module {
    // JSON
    single {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = true
        }
    }
    
    // API Client
    single {
        ApiClient(
            baseUrl = getProperty("API_BASE_URL", "https://api.medledger.lessup.com"),
            tokenProvider = get()
        )
    }
    
    // Repositories
    single { VisitRepository(get()) }
    single { DocumentRepository(get()) }
    single { ChronicRepository(get()) }
    
    // Sync Engine
    single {
        SyncEngine(
            apiClient = get(),
            visitRepository = get(),
            syncPreferences = get(),
            json = get()
        )
    }
}

/**
 * 平台特定模块（由各平台实现）
 */
expect fun platformModule(): Module
