package ravioli.gravioli.gui.render;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.gui.MenuComponent;
import ravioli.gravioli.gui.MenuComponentLike;
import ravioli.gravioli.gui.model.ComponentType;

public sealed interface RenderOutputNode extends MenuComponentLike permits RenderOutputNode.PositionedRenderOutputNode, RenderOutputNode.DecoratorRenderOutputNode {
    @NotNull MenuComponent<?> getComponent();

    @RequiredArgsConstructor(access = AccessLevel.PUBLIC)
    @Getter
    final class PositionedRenderOutputNode implements RenderOutputNode {
        private final int x;
        private final int y;
        private final MenuComponent<?> component;

        @Override
        public @NotNull ComponentType getType() {
            return ComponentType.POSITIONED;
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PUBLIC)
    @Getter
    final class DecoratorRenderOutputNode implements RenderOutputNode {
        private final MenuComponent<?> component;

        @Override
        public @NotNull ComponentType getType() {
            return ComponentType.DECORATOR;
        }
    }
}
