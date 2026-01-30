package com.example.moodtracker

import android.app.Application
import com.example.moodtracker.di.AppGraph

class MoodPulseApp : Application() {
    lateinit var appGraph: AppGraph
        private set

    override fun onCreate() {
        super.onCreate()
        appGraph = AppGraph(this)
    }
}

