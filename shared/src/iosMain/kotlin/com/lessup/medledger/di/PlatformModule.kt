package com.lessup.medledger.di

import com.lessup.medledger.db.DatabaseDriverFactory
import com.lessup.medledger.db.createDatabase
import com.lessup.medledger.network.TokenProvider
import com.lessup.medledger.sync.SyncPreferences
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSUserDefaults

actual fun platformModule(): Module = module {
    // Database
    single { DatabaseDriverFactory() }
    single { createDatabase(get()) }
    
    // Token Provider (使用 Keychain 更安全，这里简化用 UserDefaults)
    single<TokenProvider> { IosTokenProvider() }
    
    // Sync Preferences
    single<SyncPreferences> { IosSyncPreferences() }
}

/**
 * iOS Token Provider 实现
 */
class IosTokenProvider : TokenProvider {
    private val defaults = NSUserDefaults.standardUserDefaults
    
    override suspend fun getAccessToken(): String? = 
        defaults.stringForKey("access_token")
    
    override suspend fun getRefreshToken(): String? = 
        defaults.stringForKey("refresh_token")
    
    override suspend fun saveTokens(accessToken: String, refreshToken: String) {
        defaults.setObject(accessToken, forKey = "access_token")
        defaults.setObject(refreshToken, forKey = "refresh_token")
    }
    
    override suspend fun clearTokens() {
        defaults.removeObjectForKey("access_token")
        defaults.removeObjectForKey("refresh_token")
    }
}

/**
 * iOS Sync Preferences 实现
 */
class IosSyncPreferences : SyncPreferences {
    private val defaults = NSUserDefaults.standardUserDefaults
    
    override suspend fun getLastSyncTime(): Long = 
        defaults.doubleForKey("last_sync_time").toLong()
    
    override suspend fun setLastSyncTime(time: Long) {
        defaults.setDouble(time.toDouble(), forKey = "last_sync_time")
    }
}
