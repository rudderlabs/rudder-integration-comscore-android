package com.rudderlabs.android.integration.comscore;

import android.app.Application;
import android.util.Log;

import com.comscore.streaming.AdvertisementMetadata;
import com.comscore.streaming.ContentMetadata;
import com.comscore.streaming.StreamingAnalytics;
import com.google.gson.Gson;
import com.rudderstack.android.sdk.core.RudderClient;
import com.rudderstack.android.sdk.core.RudderConfig;
import com.rudderstack.android.sdk.core.RudderIntegration;
import com.rudderstack.android.sdk.core.RudderLogger;
import com.rudderstack.android.sdk.core.RudderMessage;
import com.rudderstack.android.sdk.core.ecomm.ECommerceEvents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ComscoreIntegrationFactory extends RudderIntegration<ComScoreAnalytics> {
    private static final String COMSCORE_KEY = "Comscore";

    private ComScoreAnalytics comScoreAnalytics;
    private StreamingAnalytics streamingAnalytics;
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

    private Map<String, String> eventMap = new HashMap<>();

    private ComscoreIntegrationFactory(Object config, RudderClient client, RudderConfig rudderConfig) {

        System.out.println("+++++++++++++++++++++" + config);

        this.applicationContext = client.getApplication();
        this.comscoreConfig = (Map<String, Object>)config;
        this.settings = new Settings(this.comscoreConfig);

        comScoreAnalytics = ComScoreAnalytics.DefaultcomScoreAnalytics.getInstance();
        comScoreAnalytics.start(applicationContext.getApplicationContext(), settings.toPublisherConfiguration());
        settings.analyticsConfig();

        Log.e("Comscore-Rudder","Comscore initialized!");

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
                    String userId = element.getUserId();
                    /*if (userId != null) {
                        // branch supports userId to be max 127 characters
                        if (userId.length() > 127) userId = userId.substring(0, 127);
                        branchInstance.setIdentity(userId);
                    }*/
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

    private void trackVideoPlayback(
            RudderMessage element, Map<String, Object> properties, Map<String, Object> comScoreOptions) {
        String name = element.getEventName();
        long playbackPosition = properties.get("playbackPosition") != null ? (long) properties.get("playbackPosition") : 0;

        Map<String, String> playbackMapper = new LinkedHashMap<>();
        playbackMapper.put("videoPlayer", "ns_st_mp");
        playbackMapper.put("sound", "ns_st_vo");

        Map<String, String> mappedPlaybackProperties =
                mapPlaybackProperties(properties, comScoreOptions, playbackMapper);

        if (name.equals("Video Playback Started")) {
            streamingAnalytics = comScoreAnalytics.createStreamingAnalytics();
            streamingAnalytics.createPlaybackSession();
            streamingAnalytics.getConfiguration().addLabels(mappedPlaybackProperties);

            // The label ns_st_ci must be set through a setAsset call
            Map<String, String> contentIdMapper = new LinkedHashMap<>();
            contentIdMapper.put("assetId", "ns_st_ci");

            Map<String, String> mappedContentProperties = mapSpecialKeys(properties, contentIdMapper);

            streamingAnalytics.setMetadata(getContentMetadata(mappedContentProperties));
            return;
        }

        if (streamingAnalytics == null) {
            RudderLogger.logWarn(
                    "streamingAnalytics instance not initialized correctly. Please call Video Playback Started to initialize.");
            return;
        }
        streamingAnalytics.getConfiguration().addLabels(mappedPlaybackProperties);

        switch (name) {
            case "Video Playback Paused":
            case "Video Playback Interrupted":
                streamingAnalytics.notifyPause();
                RudderLogger.logWarn("streamingAnalytics.notifyPause " + playbackPosition);
                break;
            case "Video Playback Buffer Started":
                streamingAnalytics.startFromPosition(playbackPosition);
                streamingAnalytics.notifyBufferStart();
                RudderLogger.logWarn("streamingAnalytics.notifyBufferStart " + playbackPosition);
                break;
            case "Video Playback Buffer Completed":
                streamingAnalytics.startFromPosition(playbackPosition);
                streamingAnalytics.notifyBufferStop();
                RudderLogger.logWarn("streamingAnalytics.notifyBufferStop " + playbackPosition);
                break;
            case "Video Playback Seek Started":
                streamingAnalytics.notifySeekStart();
                RudderLogger.logWarn("streamingAnalytics.notifySeekStart " + playbackPosition);
                break;
            case "Video Playback Seek Completed":
                streamingAnalytics.startFromPosition(playbackPosition);
                streamingAnalytics.notifyPlay();
                RudderLogger.logWarn("streamingAnalytics.notifyEnd " + playbackPosition);
                break;
            case "Video Playback Resumed":
                streamingAnalytics.startFromPosition(playbackPosition);
                streamingAnalytics.notifyPlay();
                RudderLogger.logWarn("streamingAnalytics.notifyPlay " + playbackPosition);
                break;
        }
    }

    private void trackVideoContent(
            RudderMessage element, Map<String, Object> properties, Map<String, Object> comScoreOptions) {
        String name = element.getEventName();
        long playbackPosition = properties.get("playbackPosition") != null ? (long) properties.get("playbackPosition") : 0;//properties.getLong("playbackPosition", 0);

        Map<String, String> contentMapper = new LinkedHashMap<>();
        contentMapper.put("title", "ns_st_ep");
        contentMapper.put("season", "ns_st_sn");
        contentMapper.put("episode", "ns_st_en");
        contentMapper.put("genre", "ns_st_ge");
        contentMapper.put("program", "ns_st_pr");
        contentMapper.put("channel", "ns_st_st");
        contentMapper.put("publisher", "ns_st_pu");
        contentMapper.put("fullEpisode", "ns_st_ce");
        contentMapper.put("podId", "ns_st_pn");

        Map<String, String> mappedContentProperties =
                mapContentProperties(properties, comScoreOptions, contentMapper);

        if (streamingAnalytics == null) {
            RudderLogger.logWarn(
                    "streamingAnalytics instance not initialized correctly. Please call Video Playback Started to initialize.");
            return;
        }

        switch (name) {
            case "Video Content Started":
                streamingAnalytics.setMetadata(getContentMetadata(mappedContentProperties));
                RudderLogger.logWarn("streamingAnalytics.setMetadata " + mappedContentProperties);
                streamingAnalytics.startFromPosition(playbackPosition);
                streamingAnalytics.notifyPlay();
                RudderLogger.logWarn("streamingAnalytics.notifyPlay " + playbackPosition);
                break;

            case "Video Content Playing":
                // The presence of ns_st_ad on the StreamingAnalytics's asset means that we just exited an ad break, so
                // we need to call setAsset with the content metadata.  If ns_st_ad is not present, that means the last
                // observed event was related to content, in which case a setAsset call should not be made (because asset
                // did not change).
                if(streamingAnalytics.getConfiguration().containsLabel("ns_st_ad")) {
                    streamingAnalytics.setMetadata(getContentMetadata(mappedContentProperties));
                    RudderLogger.logWarn("streamingAnalytics.setMetadata " + mappedContentProperties);
                }

                streamingAnalytics.startFromPosition(playbackPosition);
                streamingAnalytics.notifyPlay();
                RudderLogger.logWarn("streamingAnalytics.notifyEnd " + playbackPosition);
                break;

            case "Video Content Completed":
                streamingAnalytics.notifyEnd();
                RudderLogger.logWarn("streamingAnalytics.notifyEnd " + playbackPosition);
                break;
        }
    }

    public void trackVideoAd(
            RudderMessage element, Map<String, Object> properties, Map<String, Object> comScoreOptions) {
        String name = element.getEventName();
        long playbackPosition = properties.get("playbackPosition") != null ? (long) properties.get("playbackPosition") : 0;//properties.getLong("playbackPosition", 0);

        Map<String, String> adMapper = new LinkedHashMap<>();
        adMapper.put("assetId", "ns_st_ami");
        adMapper.put("title", "ns_st_amt");
        adMapper.put("publisher", "ns_st_pu");

        Map<String, String> mappedAdProperties = mapAdProperties(properties, comScoreOptions, adMapper);

        if (streamingAnalytics == null) {
            RudderLogger.logWarn(
                    "streamingAnalytics instance not initialized correctly. Please call Video Playback Started to initialize.");
            return;
        }

        switch (name) {
            case "Video Ad Started":
                // The ID for content is not available on Ad Start events, however it will be available on the current
                // StreamingAnalytics's asset. This is because ns_st_ci will have already been set on Content Started
                // calls (if this is a mid or post-roll), or on Video Playback Started (if this is a pre-roll).
                String contentId = streamingAnalytics.getConfiguration().getLabel("ns_st_ci");
                if (contentId != null  && !contentId.trim().isEmpty()) {
                    mappedAdProperties.put("ns_st_ci", contentId);
                }

                streamingAnalytics.setMetadata(getAdvertisementMetadata(mappedAdProperties));
                RudderLogger.logWarn("streamingAnalytics.setMetadata " + mappedAdProperties);
                streamingAnalytics.startFromPosition(playbackPosition);
                streamingAnalytics.notifyPlay();
                RudderLogger.logWarn("streamingAnalytics.notifyPlay " + playbackPosition);
                break;

            case "Video Ad Playing":
                streamingAnalytics.startFromPosition(playbackPosition);
                streamingAnalytics.notifyPlay();
                RudderLogger.logWarn("streamingAnalytics.notifyPlay " + playbackPosition);
                break;

            case "Video Ad Completed":
                streamingAnalytics.notifyEnd();
                RudderLogger.logWarn("streamingAnalytics.notifyEnd " + playbackPosition);
                break;
        }
    }

    public void track(RudderMessage element) {

        Log.e("Comscore-Rudder","In track!");

        String event = element.getEventName();
        Map<String, Object> properties = element.getProperties();

        /*Map<String, Object> comScoreOptions = track.integrations().getValueMap("comScore");
        if (isNullOrEmpty(comScoreOptions)) {
            comScoreOptions = Collections.emptyMap();
        }*/

        Map<String, Object> comScoreOptions = (Map<String, Object>)this.comscoreConfig;
        if(comScoreOptions == null){
            comScoreOptions = Collections.emptyMap();
        }

        switch (event) {
            case "Video Playback Started":
            case "Video Playback Paused":
            case "Video Playback Interrupted":
            case "Video Playback Buffer Started":
            case "Video Playback Buffer Completed":
            case "Video Playback Seek Started":
            case "Video Playback Seek Completed":
            case "Video Playback Resumed":
                trackVideoPlayback(element, properties, comScoreOptions);
                break;
            case "Video Content Started":
            case "Video Content Playing":
            case "Video Content Completed":
                trackVideoContent(element, properties, comScoreOptions);
                break;
            case "Video Ad Started":
            case "Video Ad Playing":
            case "Video Ad Completed":
                trackVideoAd(element, properties, comScoreOptions);
                break;
            default:
                Map<String, String> props = new HashMap<>();
                if(properties != null && !properties.isEmpty()){
                    props = getStringMap(properties);
                }
                props.put("name", event);
                comScoreAnalytics.notifyHiddenEvent(props);
        }
    }

    private Map<String, String> getStringMap(Map<String, Object> input){
        final Map<String, String> result = new HashMap<>();
        for (final Map.Entry<String, Object> entry : input.entrySet()) {
            result.put(entry.getKey(), String.valueOf(entry.getValue()));
        }
        return result;

    }

    public void identify(RudderMessage element) {
        //super.identify(identify);
        String userId = element.getUserId();//identify.userId();
        String anonymousId = element.getAnonymousId();//identify.anonymousId();
        Map<String, String> traits = this.getStringMap(element.getTraits());//(HashMap<String, String>) identify.traits().toStringMap();
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

    private String getStringOrDefaultValue(
            Map<String, ?> m, String key, String defaultValue) {
        Object value = m.get(key);
        if (value instanceof String) {
            return (String) value;
        }
        if (value != null) {
            return String.valueOf(value);
        }

        return defaultValue;
    }

    private void setNullIfNotProvided(
            Map<String, String> asset,
            Map<String, ?> comScoreOptions,
            Map<String, ?> stringProperties,
            String key) {
        String option = getStringOrDefaultValue(comScoreOptions, key, null);
        if (option != null) {
            asset.put(key, option);
            return;
        }

        String property = getStringOrDefaultValue(stringProperties, key, null);
        if (property != null) {
            asset.put(key, property);
            return;
        }
        asset.put(key, "*null");
    }

    private Map<String, String> mapSpecialKeys(
            Map<String, Object> properties, Map<String, String> mapper) {
        Map<String, String> asset = new LinkedHashMap<>(mapper.size());

        // Map special keys and preserve only the special keys.
        for (Map.Entry<String, ?> entry : properties.entrySet()) {
            String key = entry.getKey();
            String mappedKey = mapper.get(key);
            Object value = entry.getValue();
            if (mappedKey != null && !mappedKey.isEmpty()) {
                asset.put(mappedKey, String.valueOf(value));
            }
        }

        return asset;
    }

    private Map<String, String> mapPlaybackProperties(
            Map<String, Object> properties,
            Map<String, ?> options,
            Map<String, String> mapper) {

        Map<String, String> asset = mapSpecialKeys(properties, mapper);

        boolean fullScreen = properties.get("fullScreen") != null  ? (boolean) properties.get("fullScreen") : false;
        asset.put("ns_st_ws", fullScreen ? "full" : "norm");

        int bitrate = (properties.get("bitrate") != null  ? (int) properties.get("bitrate") : 0) * 1000; //properties.getInt("bitrate", 0) * 1000; // comScore expects bps.

        asset.put("ns_st_br", String.valueOf(bitrate));

        setNullIfNotProvided(asset, options, properties, "c3");
        setNullIfNotProvided(asset, options, properties, "c4");
        setNullIfNotProvided(asset, options, properties, "c6");

        return asset;
    }

    private Map<String, String> mapContentProperties(
            Map<String, Object> properties,
            Map<String, ?> options,
            Map<String, String> mapper) {

        Map<String, String> asset = mapSpecialKeys(properties, mapper);

        int contentAssetId = properties.get("assetId") != null  ? (int) properties.get("assetId") : 0 ;//properties.getInt("assetId", 0);
        asset.put("ns_st_ci", String.valueOf(contentAssetId));

        if (properties.containsKey("totalLength")) {
            int length = (properties.get("totalLength") != null  ? (int) properties.get("totalLength") : 0) * 1000;//properties.getInt("totalLength", 0) * 1000; // comScore expects milliseconds.
            asset.put("ns_st_cl", String.valueOf(length));
        }

        if (options.containsKey("contentClassificationType")) {
            String contentClassificationType = String.valueOf(options.get("contentClassificationType"));
            asset.put("ns_st_ct", contentClassificationType);
        } else {
            asset.put("ns_st_ct", "vc00");
        }

        if (options.containsKey("digitalAirdate")) {
            String digitalAirdate = String.valueOf(options.get("digitalAirdate"));
            asset.put("ns_st_ddt", digitalAirdate);
        }

        if (options.containsKey("tvAirdate")) {
            String tvAirdate = String.valueOf(options.get("tvAirdate"));
            asset.put("ns_st_tdt", tvAirdate);
        }

        setNullIfNotProvided(asset, options, properties, "c3");
        setNullIfNotProvided(asset, options, properties, "c4");
        setNullIfNotProvided(asset, options, properties, "c6");

        return asset;
    }

    private Map<String, String> mapAdProperties(
            Map<String, Object> properties,
            Map<String, ?> options,
            Map<String, String> mapper) {

        Map<String, String> asset = mapSpecialKeys(properties, mapper);

        if (properties.containsKey("totalLength")) {
            int length = (properties.get("totalLength") != null  ? (int) properties.get("totalLength") : 0) * 1000;//properties.getInt("totalLength", 0) * 1000; // comScore expects milliseconds.
            asset.put("ns_st_cl", String.valueOf(length));
        }

        if (options.containsKey("adClassificationType")) {
            String adClassificationType = String.valueOf(options.get("adClassificationType"));
            asset.put("ns_st_ct", adClassificationType);
        } else {
            asset.put("ns_st_ct", "va00");
        }

        String adType = String.valueOf(properties.get("type"));
        switch (adType) {
            case "pre-roll":
            case "mid-roll":
            case "post-roll":
                asset.put("ns_st_ad", adType);
                break;
            default:
                asset.put("ns_st_ad", "1");
        }

        setNullIfNotProvided(asset, options, properties, "c3");
        setNullIfNotProvided(asset, options, properties, "c4");
        setNullIfNotProvided(asset, options, properties, "c6");
        return asset;
    }

    private ContentMetadata getContentMetadata(Map<String, String> mappedContentProperties){
        return new ContentMetadata.Builder()
                .customLabels(mappedContentProperties)
                .build();
    }

    private AdvertisementMetadata getAdvertisementMetadata(Map<String, String> mappedAdProperties){
        return new AdvertisementMetadata.Builder()
                .customLabels(mappedAdProperties)
                .build();
    }

    @Override
    public ComScoreAnalytics getUnderlyingInstance() {
        return comScoreAnalytics;
    }
}
