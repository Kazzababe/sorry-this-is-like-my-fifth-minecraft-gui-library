package ravioli.gravioli.gui;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ravioli.gravioli.gui.mask.Mask;
import ravioli.gravioli.gui.model.ComponentType;
import ravioli.gravioli.gui.render.RenderContext;
import ravioli.gravioli.gui.render.RenderOutput;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

public sealed abstract class MenuComponent<T extends MenuComponent<T>> implements MenuComponentLike permits MenuComponent.PositionableMenuComponent, MenuComponent.DecoratorMenuComponent {

    public static @NotNull ItemComponent item(@NotNull final Supplier<ItemStack> itemSupplier) {
        return item(itemSupplier, event -> {});
    }

    public static @NotNull ItemComponent item(@NotNull final Supplier<ItemStack> itemSupplier, @NotNull final Consumer<InventoryClickEvent> eventHandler) {
        return new ItemComponent(itemSupplier, eventHandler);
    }

    public static @NotNull PaginationComponent pagination(@NotNull final Mask mask, final int page, @NotNull final PositionableMenuComponent<?>... items) {
        return new PaginationComponent(page, mask, items);
    }

    public static @NotNull AsyncPaginationComponent asyncPagination(@NotNull final Mask mask, final int page, @NotNull final AsyncPaginationComponent.PageProvider pageProvider) {
        return new AsyncPaginationComponent(page, mask, pageProvider);
    }

    private final AtomicReference<UUID> internalId;
    private final AtomicReference<Ref<T>> ref;
    private final AtomicReference<StateController> stateController;

    boolean mounted;

    public MenuComponent() {
        this.internalId = new AtomicReference<>(UUID.randomUUID());
        this.ref = new AtomicReference<>();
        this.stateController = new AtomicReference<>(new StateController(() -> {}));
    }

    @ApiStatus.Internal
    final void setInternalId(@NotNull final UUID id) {
        this.internalId.set(id);
    }

    @ApiStatus.Internal
    final @NotNull UUID getInternalId() {
        return this.internalId.get();
    }

    @ApiStatus.Internal
    final void setStateController(@NotNull final StateController stateController) {
        this.stateController.set(stateController);
    }

    @ApiStatus.Internal
    final @NotNull StateController getStateController() {
        return this.stateController.get();
    }

    public final void setRef(@NotNull final Ref<T> ref) {
        this.ref.set(ref);

        ref.set((T) this);
    }

    public final Ref<T> getRef() {
        return this.ref.get();
    }

    void _mount() {
        this.mount();
        this.mounted = true;
    }

    public abstract void init();

    public abstract void mount();

    public abstract void unmount();

    public abstract void update();

    public abstract boolean shouldUpdate(@NotNull StateController nextState);

    public int getWidth() {
        return 1;
    }

    public int getHeight() {
        return 1;
    }

    @SuppressWarnings("unchecked")
    public final @NotNull T withRef(@NotNull final Ref<T> ref) {
        this.setRef(ref);

        return (T) this;
    }

    public boolean propsMatch(@NotNull final T menuComponent) {
        return true;
    }

    public abstract @Nullable RenderOutput<?> render(@NotNull RenderContext renderContext);

    protected final <K> @NotNull State<K> useState(@Nullable final K defaultValue) {
        return this.stateController.get().state(defaultValue);
    }

    protected final <K> @NotNull State<K> useState(@NotNull final Supplier<K> defaultValue) {
        return this.stateController.get().state(defaultValue);
    }

    protected final <K> @NotNull Ref<K> useRef(@Nullable final K defaultValue) {
        return this.stateController.get().ref(defaultValue);
    }

    protected final <K> @NotNull Ref<K> useRef() {
        return this.stateController.get().ref(null);
    }

    protected final void processStateQueue() {
        this.stateController.get().process();
    }

    public abstract static non-sealed class PositionableMenuComponent<T extends PositionableMenuComponent<T>> extends MenuComponent<T> {
        @Override
        public final @NotNull ComponentType getType() {
            return ComponentType.POSITIONED;
        }
    }

    public abstract static non-sealed class DecoratorMenuComponent<T extends DecoratorMenuComponent<T>> extends MenuComponent<T> {
        @Override
        public final @NotNull ComponentType getType() {
            return ComponentType.DECORATOR;
        }
    }
}
