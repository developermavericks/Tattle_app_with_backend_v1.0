package com.example.tattle.di

import com.example.tattle.data.PreferencesRepository
import com.example.tattle.data.SettingsPreferencesRepository
import com.example.tattle.ui.viewmodels.AppViewModel
import com.russhwolf.settings.Settings
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

fun initKoin(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(commonModule)
    }

fun initKoin() = initKoin {}

val commonModule = module {
    single<Settings> { Settings() }
    single<PreferencesRepository> { SettingsPreferencesRepository(get()) }
    viewModelOf(::AppViewModel)
}
