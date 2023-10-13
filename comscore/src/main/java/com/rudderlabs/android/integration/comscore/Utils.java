package com.rudderlabs.android.integration.comscore;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;

public class Utils {
    static ComscoreDestinationConfig createConfig(Object config) {
        JsonDeserializer<ComscoreDestinationConfig> deserializer =
                (json, typeOfT, context) -> {
                    JsonObject jsonObject = json.getAsJsonObject();

                    String c2 = jsonObject.get("c2").getAsString();
                    String appName = jsonObject.get("appName").getAsString();
                    boolean foregroundAndBackground = jsonObject.get("foregroundAndBackground").getAsBoolean();
                    int autoUpdateInterval = jsonObject.get("autoUpdateInterval").getAsInt();
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
}
