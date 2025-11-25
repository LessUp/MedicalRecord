package com.lessup.medledger

import android.app.Application
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import androidx.hilt.work.HiltWorkerFactory
import com.lessup.medledger.di.platformModule
import com.lessup.medledger.di.sharedModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

@HiltAndroidApp
class MedLedgerApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        
        // 初始化 Koin (用于共享模块)
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@MedLedgerApp)
            properties(mapOf("API_BASE_URL" to "https://api.medledger.lessup.com"))
            modules(sharedModule, platformModule())
        }
        
        // 创建通知渠道
        com.lessup.medledger.notifications.NotificationHelper.createChannels(this)
    }

    override fun getWorkManagerConfiguration(): Configuration =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
