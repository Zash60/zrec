package com.zash60.zrec

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

/**
 * Application class for Zrec.
 * Initializes app-wide configuration.
 */
class ZrecApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Default to dark theme
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }
}
