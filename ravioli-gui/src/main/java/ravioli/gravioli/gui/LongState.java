package ravioli.gravioli.gui;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public final class LongState extends State<Long> {
    public LongState(@NotNull final Runnable refreshMethod, final Long defaultValue) {
        super(refreshMethod, defaultValue);
    }

    public LongState(@NotNull final Runnable refreshMethod, final Supplier<Long> defaultValue) {
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
