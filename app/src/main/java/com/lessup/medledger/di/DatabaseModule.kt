package com.lessup.medledger.di

import android.content.Context
import com.lessup.medledger.ui.auth.AuthViewModel
import com.lessup.medledger.ui.calendar.CalendarViewModel
import com.lessup.medledger.ui.chronic.ChronicViewModel
import com.lessup.medledger.ui.home.HomeViewModel
import com.lessup.medledger.ui.scan.ScanViewModel
import com.lessup.medledger.ui.settings.SettingsViewModel
import com.lessup.medledger.ui.stats.StatsViewModel
import com.lessup.medledger.ui.visit.VisitDetailViewModel
import com.lessup.medledger.ui.visit.VisitEditViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { HomeViewModel(get()) }
    viewModel { VisitDetailViewModel(get(), get()) }
    viewModel { VisitEditViewModel(get()) }
    viewModel { CalendarViewModel(get()) }
    viewModel { StatsViewModel(get()) }
    viewModel { ScanViewModel(get()) }
    viewModel { ChronicViewModel(get(), get<Context>()) }
    viewModel { SettingsViewModel(get<Context>(), get()) }
    viewModel { AuthViewModel(get(), get(), get()) }
}
