package ravioli.gravioli.gui.render;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.gui.MenuComponent;

import java.util.ArrayList;
import java.util.List;

@Getter
public class RenderOutput<T extends RenderOutput<T>> {
    public static @NotNull RenderOutput<?> create() {
        return new RenderOutput<>();
    }

    private final List<RenderOutputNode> children;

    protected RenderOutput() {
        this.children = new ArrayList<>();
    }

    public @NotNull T set(final int x, final int y, @NotNull final MenuComponent.PositionableMenuComponent<?> component) {
        this.children.add(
            new RenderOutputNode.PositionedRenderOutputNode(x, y, component)
        );

        return (T) this;
    }

    public @NotNull T add(@NotNull final MenuComponent.DecoratorMenuComponent<?> component) {
        this.children.add(new RenderOutputNode.DecoratorRenderOutputNode(component));

        return (T) this;
    }
}
