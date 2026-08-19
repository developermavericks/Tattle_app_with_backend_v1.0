package com.example.tattle.di

import com.example.tattle.PlatformConfig
import com.example.tattle.data.PreferencesRepository
import com.example.tattle.data.SettingsPreferencesRepository
import com.example.tattle.ui.viewmodels.AppViewModel
import com.russhwolf.settings.Settings
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.SupabaseClient
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
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
    single { 
        createSupabaseClient(
            supabaseUrl = PlatformConfig.SUPABASE_URL,
            supabaseKey = PlatformConfig.SUPABASE_KEY,
        ) {
            install(Auth)
            install(Postgrest)
        }
    }
    single { get<SupabaseClient>().auth }
    
    single { 
        HttpClient {
            install(ContentNegotiation) {
                json()
            }
        }
    }
    single<Settings> { Settings() }
    single<PreferencesRepository> { SettingsPreferencesRepository(get()) }
    viewModelOf(::AppViewModel)
}
