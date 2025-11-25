package com.lessup.medledger

import com.lessup.medledger.di.platformModule
import com.lessup.medledger.di.sharedModule
import org.koin.core.context.startKoin

fun initKoin() {
    startKoin {
        modules(sharedModule, platformModule())
    }
}
