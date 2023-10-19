package com.rudderlabs.android.sample.kotlin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rudderstack.android.sdk.core.RudderProperty
import com.rudderstack.android.sdk.core.RudderTraits

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<android.widget.Button>(R.id.identifyWithoutTraits).setOnClickListener { identifyWithoutTraits() }
        findViewById<android.widget.Button>(R.id.identifyWithTraits).setOnClickListener { identifyWithTraits() }
        findViewById<android.widget.Button>(R.id.trackWithoutProperties).setOnClickListener { trackWithoutProperties() }
        findViewById<android.widget.Button>(R.id.trackWithProperties).setOnClickListener { trackWithProperties() }
        findViewById<android.widget.Button>(R.id.screenWithoutProperties).setOnClickListener {screenWithoutProperties()}
        findViewById<android.widget.Button>(R.id.screenWithProperties).setOnClickListener {screenWithProperties()}
        findViewById<android.widget.Button>(R.id.reset).setOnClickListener {reset()}
    }

    private fun identifyWithoutTraits() {
        MainApplication.rudderClient.identify("user_id_without_traits")
    }

    private fun identifyWithTraits() {
        MainApplication.rudderClient.identify(
            "user_id_with_traits",
            RudderTraits()
                .putEmail("some_email@some_domain.com")
                .put("trait_key_1", "trait_value_1")
                .put("trait_key_2", 4567),
            null
        )
    }

    private fun trackWithoutProperties() {
        MainApplication.rudderClient.track("track_event_name_without_properties")
    }

    private fun trackWithProperties() {
        MainApplication.rudderClient.track(
            "track_event_name_with_properties",
            RudderProperty()
                .putValue("property_track_key_1", "property_value_1")
                .putValue("property_track_key_2", 987654)
                .putValue("prop3", "value3")
        )
    }

    private fun screenWithoutProperties() {
        MainApplication.rudderClient.screen("screen_event_name_without_properties")
    }

    private fun screenWithProperties() {
       MainApplication.rudderClient.screen(
            "screen_event_name_with_properties",
            RudderProperty()
                .putValue("category", "category_value")                 // category is mapped to ns_category
                .putValue("property_screen_key_1", "property_value_2")
                .putValue("property_screen_key_2", 9876542)
        )
    }

    private fun reset() {
        MainApplication.rudderClient.reset(true)
    }
}
