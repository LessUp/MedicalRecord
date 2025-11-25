package com.lessup.medledger.di

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.lessup.medledger.db.DatabaseDriverFactory
import com.lessup.medledger.db.MedLedgerDatabase
import com.lessup.medledger.db.createDatabase
import com.lessup.medledger.network.TokenProvider
import com.lessup.medledger.sync.SyncPreferences
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    // Database
    single { DatabaseDriverFactory(androidContext()) }
    single { createDatabase(get()) }
    
    // Encrypted SharedPreferences for tokens
    single<SharedPreferences> {
        val context: Context = androidContext()
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "medledger_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
    // Token Provider
    single<TokenProvider> { AndroidTokenProvider(get()) }
    
    // Sync Preferences
    single<SyncPreferences> { AndroidSyncPreferences(get()) }
}

/**
 * Android Token Provider 实现
 */
class AndroidTokenProvider(
    private val prefs: SharedPreferences
) : TokenProvider {
    override suspend fun getAccessToken(): String? = prefs.getString("access_token", null)
    override suspend fun getRefreshToken(): String? = prefs.getString("refresh_token", null)
    
    override suspend fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit()
            .putString("access_token", accessToken)
            .putString("refresh_token", refreshToken)
            .apply()
    }
    
    override suspend fun clearTokens() {
        prefs.edit()
            .remove("access_token")
            .remove("refresh_token")
            .apply()
    }
}

/**
 * Android Sync Preferences 实现
 */
class AndroidSyncPreferences(
    private val prefs: SharedPreferences
) : SyncPreferences {
    override suspend fun getLastSyncTime(): Long = prefs.getLong("last_sync_time", 0)
    
    override suspend fun setLastSyncTime(time: Long) {
        prefs.edit().putLong("last_sync_time", time).apply()
    }
}
