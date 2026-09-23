package com.example.tattle.di

import com.example.tattle.data.PreferencesRepository
import com.example.tattle.data.SettingsPreferencesRepository
import com.example.tattle.data.LoginRepository
import com.example.tattle.data.ImageRepository
import com.example.tattle.data.ArticleRepository
import com.example.tattle.ui.viewmodels.AppViewModel
import com.russhwolf.settings.Settings
import io.kamel.core.config.KamelConfig
import io.kamel.core.config.httpUrlFetcher
import io.kamel.core.config.stringMapper
import io.kamel.core.config.takeFrom
import io.kamel.core.config.uriMapper
import io.kamel.core.config.urlMapper
import io.kamel.image.config.detectedKamelConfig
import io.ktor.client.*
import io.ktor.client.plugins.HttpRedirect
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
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
        HttpClient {
            install(HttpRedirect) {
                checkHttpMethod = false
            }
            install(ContentNegotiation) {
                json(kotlinx.serialization.json.Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
            defaultRequest {
                header("User-Agent", "Mozilla/5.0 (Linux; Android 10; Mobile; rv:125.0) Gecko/125.0 Firefox/125.0")
            }
        }
    }

    single<KamelConfig> {
        val defaultConfig = detectedKamelConfig ?: KamelConfig {}
        KamelConfig {
            takeFrom(defaultConfig)
            stringMapper()
            uriMapper()
            urlMapper()
            httpUrlFetcher(get<HttpClient>())
        }
    }

    single<Settings> { Settings() }
    single<PreferencesRepository> { SettingsPreferencesRepository(get()) }
    single { LoginRepository(get(), get()) }
    single { ImageRepository(get()) }
    single { ArticleRepository(get()) }
    viewModelOf(::AppViewModel)
}
