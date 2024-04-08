package ravioli.gravioli.gui;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class State<T> {
    Runnable refreshMethod;

    private T value;
    public final UUID id;

    public State(@NotNull final Runnable refreshMethod, final T defaultValue) {
        this.value = defaultValue;
        this.refreshMethod = refreshMethod;

        this.id = UUID.randomUUID();
    }

    public State(@NotNull final Runnable refreshMethod, final Supplier<T> defaultValue) {
        this.refreshMethod = refreshMethod;

        this.id = UUID.randomUUID();

        CompletableFuture.runAsync(() -> this.set(defaultValue.get()));
    }

    public T get() {
        return this.value;
    }

    public void set(final T value) {
        if (Objects.equals(value, this.value)) {
            return;
        }
        this.value = value;

        this.refreshMethod.run();
    }

    public void queue(final T value) {
        this.value = value;
    }
}
