package com.rudderlabs.android.integration.comscore;

import android.app.Application;
import com.rudderstack.android.sdk.core.RudderClient;
import com.rudderstack.android.sdk.core.RudderConfig;
import com.rudderstack.android.sdk.core.RudderIntegration;
import com.rudderstack.android.sdk.core.RudderLogger;
import com.rudderstack.android.sdk.core.RudderMessage;
import java.util.HashMap;
import java.util.Map;

public class ComscoreIntegrationFactory extends RudderIntegration<ComScoreAnalytics> {
    private static final String COMSCORE_KEY = "Comscore";

    private ComScoreAnalytics comScoreAnalytics;
    private Settings settings;
    private Application applicationContext;
    private Map<String, Object> comscoreConfig;

    public static Factory FACTORY = new Factory() {
        @Override
        public RudderIntegration<?> create(Object settings, RudderClient client, RudderConfig config) {
            return new ComscoreIntegrationFactory(settings, client, config);
        }

        @Override
        public String key() {
            return COMSCORE_KEY;
        }
    };

    private ComscoreIntegrationFactory(Object config, RudderClient client, RudderConfig rudderConfig) {
        this.applicationContext = client.getApplication();
        this.comscoreConfig = (Map<String, Object>)config;
        this.settings = new Settings(this.comscoreConfig);

        comScoreAnalytics = ComScoreAnalytics.DefaultcomScoreAnalytics.getInstance();
        comScoreAnalytics.start(applicationContext.getApplicationContext(), settings.toPublisherConfiguration());
        settings.analyticsConfig();
    }

    @Override
    public void reset() {
        RudderLogger.logWarn("Method not supported!");
    }

    @Override
    public void dump(RudderMessage element) {
        if (element == null) return;

        String eventType = element.getType();
        if (eventType != null) {
            switch (eventType) {
                case "identify":
                    this.identify(element);
                    break;
                case "track":
                    this.track(element);
                    break;
                case "screen":
                    this.screen(element);
                    break;
                default:
                    break;
            }
        }
    }

    public void track(RudderMessage element) {
        String event = element.getEventName();
        Map<String, Object> properties = element.getProperties();
    }

    private Map<String, String> getStringMap(Map<String, Object> input){
        final Map<String, String> result = new HashMap<>();
        for (final Map.Entry<String, Object> entry : input.entrySet()) {
            result.put(entry.getKey(), String.valueOf(entry.getValue()));
        }
        return result;
    }

    public void identify(RudderMessage element) {
        String userId = element.getUserId();
        String anonymousId = element.getAnonymousId();
        Map<String, String> traits = this.getStringMap(element.getTraits());
        traits.put("userId", userId);
        traits.put("anonymousId", anonymousId);
        comScoreAnalytics.setPersistentLabels(traits);
    }

    public void screen(RudderMessage element) {
        String name = element.getEventName() != null ? element.getEventName() : (String) element.getProperties().get("name");//screen.name();
        String category = (String) element.getProperties().get("category");
        Map<String, String> properties = this.getStringMap(element.getProperties());//(HashMap<String, String>) element.properties().toStringMap();
        properties.put("name", name);
        properties.put("category", category);
        comScoreAnalytics.notifyViewEvent(properties);
    }

    @Override
    public ComScoreAnalytics getUnderlyingInstance() {
        return comScoreAnalytics;
    }
}
