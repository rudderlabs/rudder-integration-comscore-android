package com.rudderstack.android.integration.comscore;

import androidx.annotation.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TestScreen {
    List<? extends Map<?, ?>> screen;
    public TestScreen(@Nullable List<? extends Map<?, ?>> screen) {
        this.screen = screen;
    }


    @Override
    public boolean equals(Object o) {
        return this.checkForScreen(o) && this.checkForScreen(o);
    }

    private boolean checkForScreen(Object o) {
        if (this == o) return true;
        if (!(o instanceof TestScreen)) return false;
        TestScreen that = (TestScreen) o;
        if (!Objects.equals(screen, that.screen)) {
            throw new AssertionError("The 'identify' lists are not equal.");
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(screen);
    }
}
