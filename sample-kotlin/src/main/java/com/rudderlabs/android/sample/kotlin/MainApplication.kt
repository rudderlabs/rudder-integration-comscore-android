package com.rudderlabs.android.sample.kotlin

import android.app.Application
import com.rudderlabs.android.integration.comscore.ComscoreIntegrationFactory
import com.rudderstack.android.sdk.core.RudderClient
import com.rudderstack.android.sdk.core.RudderConfig

class MainApplication : Application() {
    companion object {
        private const val WRITE_KEY = "1WjrlaTWlxqRJh77YaNDXgDiRv2"
        private const val END_POINT_URI = "https://7cfa36c2.ngrok.io"
        lateinit var rudderClient: RudderClient
    }

    override fun onCreate() {
        super.onCreate()
        rudderClient = RudderClient.getInstance(
            this,
            WRITE_KEY,
            RudderConfig.Builder()
                .withEndPointUri(END_POINT_URI)
                .withLogLevel(4)
                .withFactory(ComscoreIntegrationFactory.FACTORY)
                .build()
        )
    }
}