package com.example.tattle

import android.app.Application
import com.example.tattle.di.initKoin
import org.koin.android.ext.koin.androidContext

class TattleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@TattleApplication)
        }
    }
}
