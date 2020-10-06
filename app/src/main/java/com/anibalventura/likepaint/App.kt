package com.anibalventura.likepaint

import android.app.Application
import android.content.res.Resources

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        resourses = resources
    }

    companion object {
        // Get app resources anywhere.
        var resourses: Resources? = null
            private set
    }
}