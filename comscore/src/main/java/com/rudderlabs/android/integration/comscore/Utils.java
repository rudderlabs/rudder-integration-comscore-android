package com.rudderlabs.android.integration.comscore;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class Utils {
    public static final String PUBLISHER_ID = "publisherId";
    public static final String APP_NAME = "appName";
    public static final String FOREGROUND_AND_BACKGROUND = "foregroundAndBackground";
    public static final String AUTO_UPDATE_INTERVAL = "autoUpdateInterval";
    public static final String FOREGROUND_ONLY = "foregroundOnly";

    static ComscoreDestinationConfig createConfig(Object config) {
        if (config == null) {
            return null;
        }

        if (config instanceof Map) {
            Map<String, Object> configMap = (Map<String, Object>) config;
            if (isEmpty(configMap)) {
                return null;
            }
            String publisherId = getString(configMap.get(PUBLISHER_ID));
            String appName = getString(configMap.get(APP_NAME));
            boolean foregroundAndBackground = getBoolean(configMap.get(FOREGROUND_AND_BACKGROUND), false);
            int autoUpdateInterval = getAutoUpdateInterval(configMap.get(AUTO_UPDATE_INTERVAL), 60);
            boolean useHTTPS = true;
            boolean foregroundOnly = getBoolean(configMap.get(FOREGROUND_ONLY), true);

            return new ComscoreDestinationConfig(publisherId, appName, foregroundAndBackground, autoUpdateInterval, useHTTPS, foregroundOnly);
        }
        return null;
    }

    private static int getAutoUpdateInterval(Object interval, int defaultInterval) {
        String autoUpdateInterval = getString(interval);
        if (isEmpty(autoUpdateInterval)) {
            return defaultInterval;
        }
        return getInt(autoUpdateInterval, defaultInterval);
    }

    static int getInt(Object value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return Integer.parseInt(value.toString());
    }

    static boolean getBoolean(Object value, boolean defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value.toString());
    }

    static boolean isEmpty(String str) {
        return str == null || str.trim().length() == 0;
    }

    static boolean isEmpty(Map<String, Object> map) {
        return map == null || map.isEmpty();
    }

    static String getString(Object value) {
        return value == null ? null : value.toString();
    }

    @NotNull
    static Map<String, String> getStringMap(@Nullable Map<String, Object> input){
        Map<String, String> output = new HashMap<>();
        if (input != null) {
            for (Map.Entry<String, Object> entry : input.entrySet()) {
                output.put(entry.getKey(), getString(entry.getValue()));
            }
        }
        return output;
    }
}
