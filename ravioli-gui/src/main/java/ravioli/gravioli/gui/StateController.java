package ravioli.gravioli.gui;

import lombok.AccessLevel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public final class StateController {
    @Getter(value = AccessLevel.PACKAGE)
    private final Runnable refreshMethod;
    private final List<State<?>> menuProperties;
    private final List<Ref<?>> menuRefs;

    private int stateCounter;
    private int refCounter;
    private boolean initialized;

    StateController(@NotNull final Runnable refreshMethod) {
        this.refreshMethod = refreshMethod;
        this.menuProperties = new ArrayList<>();
        this.menuRefs = new ArrayList<>();
    }

    StateController(@NotNull final Runnable refreshMethod, @NotNull final StateController controller) {
        this.refreshMethod = refreshMethod;
        this.menuProperties = new ArrayList<>(controller.menuProperties);
        this.menuRefs = new ArrayList<>(controller.menuRefs);
        this.initialized = controller.initialized;

        this.menuProperties.forEach(state -> state.refreshMethod = refreshMethod);
    }

    void reset() {
        this.stateCounter = 0;
        this.refCounter = 0;
    }

    void markInitialized() {
        this.initialized = true;
    }

    @SuppressWarnings("unchecked")
    public synchronized <T> @NotNull Ref<T> ref(@Nullable final T defaultValue) {
        if (this.initialized) {
            if (this.refCounter >= this.menuRefs.size()) {
                throw new IllegalStateException("Can't access refs!");
            }
            return (Ref<T>) this.menuRefs.get(this.refCounter++);
        }
        final Ref<T> ref = new Ref<>(defaultValue);

        this.menuRefs.add(ref);

        return ref;
    }

    @SuppressWarnings("unchecked")
    public synchronized <T> @NotNull State<T> state(@Nullable final T defaultValue) {
        if (this.initialized) {
            if (this.stateCounter >= this.menuProperties.size()) {
                throw new IllegalStateException("Can't access state! " + this.stateCounter + ", " + this.menuProperties.size());
            }
            return (State<T>) this.menuProperties.get(this.stateCounter++);
        }
        final State<T> property = new State<>(this.refreshMethod, defaultValue);

        this.menuProperties.add(property);

        return property;
    }

    @SuppressWarnings("unchecked")
    public synchronized <T> @NotNull State<T> state(@NotNull final Supplier<T> defaultValue) {
        if (this.initialized) {
            if (this.stateCounter >= this.menuProperties.size()) {
                throw new IllegalStateException("Can't access state!");
            }
            return (State<T>) this.menuProperties.get(this.stateCounter++);
        }
        final State<T> property = new State<>(this.refreshMethod, null);

        this.menuProperties.add(property);

        CompletableFuture.runAsync(() -> {
            property.set(defaultValue.get());
        });

        return property;
    }

    public void process() {
        this.refreshMethod.run();
    }
}
