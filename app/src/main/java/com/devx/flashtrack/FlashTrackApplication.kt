package com.devx.flashtrack

import android.app.Application
import com.devx.flashtrack.data.local.AppDatabase
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class FlashTrackApplication : Application() {

    @Inject
    lateinit var database: AppDatabase

    override fun onCreate() {
        super.onCreate()
        // Trigger prepopulation on first run
        AppDatabase.prepopulate(database)
    }
}
