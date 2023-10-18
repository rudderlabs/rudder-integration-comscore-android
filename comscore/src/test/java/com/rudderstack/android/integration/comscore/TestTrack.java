package com.rudderstack.android.integration.comscore;

import androidx.annotation.Nullable;


import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TestTrack {
    List<? extends Map<?, ?>> track;
    public TestTrack(@Nullable List<? extends Map<?, ?>> track) {
        this.track = track;
    }


    @Override
    public boolean equals(Object o) {
        return this.checkForTrack(o) && this.checkForTrack(o);
    }

    private boolean checkForTrack(Object o) {
        if (this == o) return true;
        if (!(o instanceof TestTrack)) return false;
        TestTrack that = (TestTrack) o;
        if (!Objects.equals(track, that.track)) {
            throw new AssertionError("The 'identify' lists are not equal.");
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(track);
    }
}
