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
    private static final String CATEGORY = "category";
    private static final String NS_CATEGORY = "ns_category";
    public static final String PUBLISHER_ID = "publisherID";
    public static final String APP_NAME = "appName";
    public static final String FOREGROUND_AND_BACKGROUND = "foregroundAndBackground";
    public static final String AUTO_UPDATE_INTERVAL = "autoUpdateInterval";
    public static final String AUTO_UPDATE_INTERVAL1 = "autoUpdateInterval";
    public static final String FOREGROUND_ONLY = "foregroundOnly";

    static ComscoreDestinationConfig createConfig(Object config) {
        JsonDeserializer<ComscoreDestinationConfig> deserializer =
                (json, typeOfT, context) -> {
                    JsonObject jsonObject = json.getAsJsonObject();

                    String c2 = jsonObject.get(PUBLISHER_ID).getAsString();
                    String appName = jsonObject.get(APP_NAME).getAsString();
                    boolean foregroundAndBackground = jsonObject.get(FOREGROUND_AND_BACKGROUND).getAsBoolean();
                    int autoUpdateInterval = 60;    // Default value mentioned in comScore doc is 60 seconds
                    if (!Utils.isEmpty(jsonObject.get(AUTO_UPDATE_INTERVAL).getAsString())) {
                        autoUpdateInterval = jsonObject.get(AUTO_UPDATE_INTERVAL1).getAsInt();
                    }

                    boolean useHTTPS = true;
                    boolean foregroundOnly = jsonObject.get(FOREGROUND_ONLY).getAsBoolean();

                    return new ComscoreDestinationConfig(c2, appName, foregroundAndBackground, autoUpdateInterval, useHTTPS, foregroundOnly);
                };

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(ComscoreDestinationConfig.class, deserializer);
        Gson customGson = gsonBuilder.create();
        return customGson.fromJson(customGson.toJson(config), ComscoreDestinationConfig.class);
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

    static void updateCategoryLabelMappingToNSCategory(@NotNull Map<String, String> labels) {
        if (labels.containsKey(CATEGORY)) {
            labels.put(NS_CATEGORY, labels.get(CATEGORY));
            labels.remove(CATEGORY);
        }
    }
}
