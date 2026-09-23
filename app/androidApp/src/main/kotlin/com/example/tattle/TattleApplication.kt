package com.example.tattle

import android.app.Application
import com.example.tattle.di.initKoin
import org.koin.android.ext.koin.androidContext

class TattleApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        try {
            initKoin {
                androidContext(this@TattleApplication)
            }
        } catch (e: Exception) {
            println("TattleApplication: Koin init notice: ${e.message}")
        }
    }
}
