package com.rudderlabs.android.sample.kotlin

import android.app.Application
import com.rudderstack.android.integration.comscore.ComscoreIntegrationFactory
import com.rudderstack.android.sdk.core.RudderClient
import com.rudderstack.android.sdk.core.RudderConfig
import com.rudderstack.android.sdk.core.RudderLogger

class MainApplication : Application() {
    companion object {
        lateinit var rudderClient: RudderClient
    }

    override fun onCreate() {
        super.onCreate()
        rudderClient = RudderClient.getInstance(
            this,
            BuildConfig.WRITE_KEY,
            RudderConfig.Builder()
                .withControlPlaneUrl(BuildConfig.CONTROL_PLANE_URL)
                .withDataPlaneUrl(BuildConfig.DATA_PLANE_URL)
                .withLogLevel(RudderLogger.RudderLogLevel.VERBOSE)
                .withTrackLifecycleEvents(false)
                .withFactory(ComscoreIntegrationFactory.FACTORY)
                .build()
        )
    }
}