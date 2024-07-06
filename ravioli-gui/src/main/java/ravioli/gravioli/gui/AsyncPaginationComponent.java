package ravioli.gravioli.gui;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ravioli.gravioli.gui.mask.Mask;
import ravioli.gravioli.gui.render.RenderContext;
import ravioli.gravioli.gui.render.RenderOutput;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class AsyncPaginationComponent extends MenuComponent.DecoratorMenuComponent<AsyncPaginationComponent> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncPaginationComponent.class);

    private final long defaultPage;
    private final Mask mask;
    private final PageProvider pageProvider;

    private State<Long> page;
    private State<Boolean> hasNext;
    private State<List<PositionableMenuComponent<?>>> items;

    private Ref<CompletableFuture<Void>> loadTaskRef;

    public AsyncPaginationComponent(final long defaultPage, @NotNull final Mask mask, @NotNull final PageProvider pageProvider) {
        this.defaultPage = defaultPage;
        this.mask = mask;
        this.pageProvider = pageProvider;
    }

    @Override
    public void init() {
        this.items = this.useState(new ArrayList<>());
        this.page = this.useState(this.defaultPage);
        this.hasNext = this.useState(false);
        this.loadTaskRef = this.useRef();
    }

    @Override
    public void mount() {
        this.load(this.page.get(), true, false)
            .whenCompleteAsync((result, error) -> {
                this.processStateQueue();
            });
    }

    @Override
    public void unmount() {
        final CompletableFuture<Void> task = this.loadTaskRef.get();

        if (task != null && !task.isDone()) {
            task.cancel(true);
        }
    }

    @Override
    public void update() {

    }

    @Override
    public boolean shouldUpdate(@NotNull final StateController nextState) {
        return true;
    }

    @Override
    public @Nullable RenderOutput<?> render(@NotNull final RenderContext renderContext) {
        final RenderOutput<?> renderOutput = RenderOutput.create();
        final AtomicInteger atomicIndex = new AtomicInteger();
        final List<PositionableMenuComponent<?>> pageItems = this.items.get();

        this.mask.iterator().forEachRemaining(position -> {
            final int index = atomicIndex.getAndIncrement();

            if (pageItems == null || index >= pageItems.size()) {
                return;
            }
            final PositionableMenuComponent<?> pageItem = pageItems.get(index);

            if (pageItem == null) {
                return;
            }
            renderOutput.set(position.x(), position.y(), pageItem);
        });

        return renderOutput;
    }

    public void refresh() {
        this.refresh(true);
    }

    public void refresh(final boolean clear) {
        this.load(this.page.get(), false, clear)
            .whenComplete((result, error) -> this.processStateQueue());
    }

    public boolean hasNext() {
        return this.hasNext.get();
    }

    public void nextPage() {
        if (!this.hasNext.get()) {
            return;
        }
        this.load(this.page.get() + 1, false, true)
            .whenComplete((result, error) -> {
                this.page.queue(this.page.get() + 1);
                this.processStateQueue();
            });
    }

    public void previousPage() {
        if (this.page.get() <= 0) {
            return;
        }
        this.load(this.page.get() - 1, false, true)
            .whenComplete((result, error) -> {
                if (error != null) {
                    return;
                }
                this.page.queue(this.page.get() - 1);
                this.processStateQueue();
            });
    }

    public long getPage() {
        return this.page.get();
    }

    @Override
    public boolean propsMatch(final @NotNull AsyncPaginationComponent menuComponent) {
        return this.mask.equals(menuComponent.mask);
    }

    private @NotNull CompletableFuture<Void> load(final long page, final boolean init, final boolean clear) {
        final CompletableFuture<Void> task = this.loadTaskRef.get();

        if (task != null && !task.isDone()) {
            task.completeExceptionally(new RuntimeException("New page loading task started."));
        }
        if (!init) {
            this.hasNext.queue(false);

            if (clear) {
                this.items.queue(new ArrayList<>());
            }
            this.processStateQueue();
        }
        final CompletableFuture<Void> newTask = CompletableFuture.supplyAsync(() -> this.pageProvider.load(page, this.mask.getSize()))
            .exceptionally((e) -> {
                LOGGER.error("Unable to load page.", e);

                return new PageData(Collections.emptyList(), false);
            })
            .thenAccept(pageData -> {
                this.hasNext.queue(pageData.hasNext);
                this.items.queue(pageData.items);
            });

        this.loadTaskRef.set(newTask);

        return newTask;
    }

    @FunctionalInterface
    public interface PageProvider {
        @NotNull PageData load(long page, long pageSize);
    }

    public record PageData(@NotNull List<PositionableMenuComponent<?>> items, boolean hasNext) {

    }
}
