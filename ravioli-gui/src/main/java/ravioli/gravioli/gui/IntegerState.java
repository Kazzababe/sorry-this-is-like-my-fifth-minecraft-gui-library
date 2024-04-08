package ravioli.gravioli.gui;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public final class IntegerState extends State<Integer> {
    public IntegerState(@NotNull final Runnable refreshMethod, final Integer defaultValue) {
        super(refreshMethod, defaultValue);
    }

    public IntegerState(@NotNull final Runnable refreshMethod, final Supplier<Integer> defaultValue) {
        super(refreshMethod, defaultValue);
    }

    public void increment() {
        this.set(this.get() + 1);
    }

    public void decrement() {
        this.set(this.get() - 1);
    }

    public void queueIncrement() {
        this.queue(this.get() + 1);
    }

    public void queueDecrement() {
        this.queue(this.get() - 1);
    }
}
