package com.rudderstack.android.integration.comscore;

import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TestIdentify {
    @Nullable
    List<? extends Map<?, ?>> identify;

    public TestIdentify(@NotNull List<? extends Map<?, ?>> identify) {
        this.identify = identify;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TestIdentify)) return false;
        TestIdentify that = (TestIdentify) o;
        if (!Objects.equals(identify, that.identify)) {
            throw new AssertionError("The 'identify' lists are not equal.");
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(identify);
    }
}