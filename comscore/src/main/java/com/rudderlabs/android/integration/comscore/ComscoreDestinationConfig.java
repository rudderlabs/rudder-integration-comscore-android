package com.rudderlabs.android.integration.comscore;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ComscoreDestinationConfig {
    private final String publisherID;
    private final String appName;
    private final boolean foregroundAndBackground;
    private final int autoUpdateInterval;
    private final boolean useHTTPS;
    private final boolean foregroundOnly;

    public ComscoreDestinationConfig(String publisherID, @Nullable String appName, boolean foregroundAndBackground, int autoUpdateInterval, boolean useHTTPS, boolean foregroundOnly) {
        this.publisherID = publisherID;
        this.appName = appName;
        this.foregroundAndBackground = foregroundAndBackground;
        this.autoUpdateInterval = autoUpdateInterval;
        this.useHTTPS = useHTTPS;
        this.foregroundOnly = foregroundOnly;
    }

    @NotNull
    public String getPublisherID() {
        return publisherID;
    }

    @Nullable
    public String getAppName() {
        return (Utils.isEmpty(appName)) ? null : appName;
    }

    public boolean isForegroundAndBackground() {
        return foregroundAndBackground;
    }

    public int getAutoUpdateInterval() {
        return autoUpdateInterval;
    }

    public boolean isUseHTTPS() {
        return useHTTPS;
    }

    public boolean isForegroundOnly() {
        return foregroundOnly;
    }
}
