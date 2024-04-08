package ravioli.gravioli.gui;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ravioli.gravioli.gui.mask.Mask;
import ravioli.gravioli.gui.render.RenderContext;
import ravioli.gravioli.gui.render.RenderOutput;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class PaginationComponent extends MenuComponent.DecoratorMenuComponent<PaginationComponent> {
    private final long defaultPage;
    private final Mask mask;
    private final PositionableMenuComponent<?>[] items;

    private State<Long> page;

    public PaginationComponent(final long defaultPage, @NotNull final Mask mask, @NotNull final PositionableMenuComponent<?>... items) {
        this.defaultPage = defaultPage;
        this.items = items;
        this.mask = mask;
    }

    @Override
    public void init() {
        this.page = this.useState(this.defaultPage);
    }

    @Override
    public void mount() {

    }

    @Override
    public void unmount() {

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
        final int pageSize = this.mask.getSize();
        final int maxPages = (int) Math.ceil((double) this.items.length / pageSize);

        if (this.page.get() >= maxPages) {
            throw new RuntimeException("BAD!");
        }
        final RenderOutput<?> renderOutput = RenderOutput.create();
        final AtomicInteger atomicIndex = new AtomicInteger();
        final List<PositionableMenuComponent<?>> pageComponents = Arrays.stream(this.items)
            .skip(this.page.get() * pageSize)
            .limit(pageSize)
            .toList();

        this.mask.iterator().forEachRemaining(position -> {
            final int index = atomicIndex.getAndIncrement();

            if (index >= pageComponents.size()) {
                return;
            }
            renderOutput.set(position.x(), position.y(), pageComponents.get(index));
        });

        return renderOutput;
    }

    public void setPage(final long page) {
        if (this.page == null) {
            return;
        }
        if (page < 0) {
            return;
        }
        final int pageSize = this.mask.getSize();
        final int maxPages = (int) Math.ceil((double) this.items.length / pageSize);

        if (page >= maxPages) {
            return;
        }
        this.page.set(page);
    }

    public void nextPage() {
        this.setPage(this.page.get() + 1);
    }

    public void previousPage() {
        this.setPage(this.page.get() - 1);
    }

    public long getPage() {
        return this.page.get();
    }

    @Override
    public boolean propsMatch(final @NotNull PaginationComponent menuComponent) {
        return this.mask.equals(menuComponent.mask);
    }
}
