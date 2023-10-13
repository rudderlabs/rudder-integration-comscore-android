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

    static ComscoreDestinationConfig createConfig(Object config) {
        JsonDeserializer<ComscoreDestinationConfig> deserializer =
                (json, typeOfT, context) -> {
                    JsonObject jsonObject = json.getAsJsonObject();

                    String c2 = jsonObject.get("c2").getAsString();
                    String appName = jsonObject.get("appName").getAsString();
                    boolean foregroundAndBackground = jsonObject.get("foregroundAndBackground").getAsBoolean();
                    int autoUpdateInterval = 60;    // Default value mentioned in comScore doc is 60 seconds
                    if (!Utils.isEmpty(jsonObject.get("autoUpdateInterval").getAsString())) {
                        autoUpdateInterval = jsonObject.get("autoUpdateInterval").getAsInt();
                    }

                    boolean useHTTPS = true;
                    boolean foregroundOnly = jsonObject.get("foregroundOnly").getAsBoolean();

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
