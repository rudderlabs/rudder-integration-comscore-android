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
    private static final String NAME = "name";
    private static final String ID = "id";

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
        RudderLogger.logVerbose("RudderComscoreIntegration: Comscore SDK initialized");
    }

    private void initComscoreSDK(RudderConfig rudderConfig) {
        final PublisherConfiguration publisherConfiguration = new PublisherConfiguration.Builder()
                .publisherId(destinationConfig.getPublisherID())
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

    public void identify(RudderMessage element) {
        Map<String, String> traits = Utils.getStringMap(element.getTraits());
        // Remove duplicate key, as userId is already present in the traits
        traits.remove(ID);
        Analytics.getConfiguration().addPersistentLabels(traits);
        Analytics.notifyHiddenEvent();
    }

    public void track(RudderMessage element) {
        String eventName = element.getEventName();
        if (Utils.isEmpty(eventName)) {
            RudderLogger.logDebug("RudderComscoreIntegration: Since the event name is not present, the track event sent to Comscore has been dropped.");
            return;
        }

        Map<String, String> comScoreLabels = new HashMap<>();
        comScoreLabels.put(NAME, eventName);

        Map<String, Object> properties = element.getProperties();
        if (!Utils.isEmpty(properties)) {
            comScoreLabels.putAll(Utils.getStringMap(properties));
        }

        Analytics.notifyHiddenEvent(comScoreLabels);
    }

    public void screen(RudderMessage element) {
        Map<String, String> comScoreLabels = new HashMap<>();
        String screenName = element.getEventName();
        if (Utils.isEmpty(screenName)) {
            RudderLogger.logDebug("RudderComscoreIntegration: Since the screen name is not present, the screen event sent to Comscore has been dropped.");
            return;
        }
        comScoreLabels.put(NAME, screenName);
        Map<String, Object> properties = element.getProperties();
        if (!Utils.isEmpty(properties)) {
            comScoreLabels.putAll(Utils.getStringMap(properties));
        }

        Analytics.notifyViewEvent(comScoreLabels);
    }

    @Override
    public void reset() {
        Analytics.getConfiguration().removeAllPersistentLabels();
    }

    @Override
    public void flush() {
        RudderLogger.logInfo("RudderComscoreIntegration: Comscore doesn't support flush.");
    }
}
