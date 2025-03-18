package com.example.getcab

import android.app.Application
import com.onesignal.OneSignal

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize OneSignal
        OneSignal.initWithContext(this, "2e0d780a-5c3a-4c9a-babc-fbdfca306f2f") // Replace with your OneSignal App ID
    }
}
