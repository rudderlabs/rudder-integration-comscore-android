package com.rudderlabs.android.integration.comscore;

import com.comscore.Analytics;
import com.comscore.Configuration;
import com.comscore.PublisherConfiguration;
import com.comscore.UsagePropertiesAutoUpdateMode;
import com.comscore.util.log.LogLevel;
import com.rudderstack.android.sdk.core.RudderClient;
import com.rudderstack.android.sdk.core.RudderConfig;
import com.rudderstack.android.sdk.core.RudderIntegration;
import com.rudderstack.android.sdk.core.RudderLogger;
import com.rudderstack.android.sdk.core.RudderMessage;
import java.util.HashMap;
import java.util.Map;

public class ComscoreIntegrationFactory extends RudderIntegration<Void> {
    private static final String COMSCORE_KEY = "Comscore";

    private final ComscoreDestinationConfig destinationConfig;

    public static Factory FACTORY = new Factory() {
        @Override
        public RudderIntegration<?> create(Object config, RudderClient client, RudderConfig rudderConfig) {
            return new ComscoreIntegrationFactory(config, rudderConfig);
        }

        @Override
        public String key() {
            return COMSCORE_KEY;
        }
    };

    private ComscoreIntegrationFactory(Object config, RudderConfig rudderConfig) {
        this.destinationConfig = Utils.createConfig(config);
        initComscoreSDK(rudderConfig);
        RudderLogger.logVerbose("Comscore SDK initialized");
    }

    private void initComscoreSDK(RudderConfig rudderConfig) {
        final PublisherConfiguration publisherConfiguration = new PublisherConfiguration.Builder()
                .publisherId(destinationConfig.getC2())
                .secureTransmission(destinationConfig.isUseHTTPS())
                .build();

        setLog(rudderConfig);

        Configuration configuration = Analytics.getConfiguration();

        configuration.addClient(publisherConfiguration);
        Analytics.start(RudderClient.getApplication());

        if (destinationConfig.getAppName() != null) {
            configuration.setApplicationName(destinationConfig.getAppName());
        }
        updateApplicationUsageTime();
    }

    private void setLog(RudderConfig rudderConfig) {
        if (rudderConfig.getLogLevel() >= RudderLogger.RudderLogLevel.VERBOSE) {
            Analytics.setLogLevel(LogLevel.VERBOSE);
        } else if (rudderConfig.getLogLevel() >= RudderLogger.RudderLogLevel.INFO) {
            Analytics.setLogLevel(LogLevel.DEBUG);
        } else if (rudderConfig.getLogLevel() >= RudderLogger.RudderLogLevel.WARN) {
            Analytics.setLogLevel(LogLevel.WARN);
        } else if (rudderConfig.getLogLevel() >= RudderLogger.RudderLogLevel.ERROR) {
            Analytics.setLogLevel(LogLevel.ERROR);
        } else {
            Analytics.setLogLevel(LogLevel.NONE);
        }
    }

    private void updateApplicationUsageTime() {
        Configuration configuration = Analytics.getConfiguration();
        configuration.setUsagePropertiesAutoUpdateInterval(destinationConfig.getAutoUpdateInterval());
        if (destinationConfig.isForegroundOnly()) {   // Default mode
            configuration.setUsagePropertiesAutoUpdateMode(UsagePropertiesAutoUpdateMode.FOREGROUND_ONLY);
        } else if (destinationConfig.isForegroundAndBackground()) {
            configuration.setUsagePropertiesAutoUpdateMode(UsagePropertiesAutoUpdateMode.FOREGROUND_AND_BACKGROUND);
        } else {
            configuration.setUsagePropertiesAutoUpdateMode(UsagePropertiesAutoUpdateMode.DISABLED);
        }
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
//        Analytics.setPersistentLabels(traits);
    }

    public void screen(RudderMessage element) {
        String name = element.getEventName() != null ? element.getEventName() : (String) element.getProperties().get("name");//screen.name();
        String category = (String) element.getProperties().get("category");
        Map<String, String> properties = this.getStringMap(element.getProperties());//(HashMap<String, String>) element.properties().toStringMap();
        properties.put("name", name);
        properties.put("category", category);
//        Analytics.notifyViewEvent(properties);
    }
}
