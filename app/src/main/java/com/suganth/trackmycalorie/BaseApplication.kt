package com.suganth.trackmycalorie

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class BaseApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        //Enable debug logging using Timber library
        Timber.plant(Timber.DebugTree())
    }
}