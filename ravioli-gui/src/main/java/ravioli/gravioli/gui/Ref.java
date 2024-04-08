package ravioli.gravioli.gui;

import org.jetbrains.annotations.Nullable;

public class Ref<T> {
    private T value;

    public Ref(@Nullable final T defaultValue) {
        this.value = defaultValue;
    }

    public T get() {
        return this.value;
    }

    public void set(final T value) {
        this.value = value;
    }

    public boolean isSet() {
        return this.value != null;
    }
}
