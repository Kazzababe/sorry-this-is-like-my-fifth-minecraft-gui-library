package ravioli.gravioli.gui.reconcilation;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import ravioli.gravioli.gui.MenuComponent;
import ravioli.gravioli.gui.MenuComponentLike;
import ravioli.gravioli.gui.model.ComponentType;

import java.util.ArrayList;
import java.util.List;

public final class MenuComponentNode implements MenuComponentLike {
    private final List<MenuComponentNode> children = new ArrayList<>();

    @Getter
    private final MenuComponent<?> component;

    @Getter
    private final int x;

    @Getter
    private final int y;

    public MenuComponentNode(@NotNull final MenuComponent<?> component, final int x, final int y) {
        this.component = component;
        this.x = x;
        this.y = y;
    }

    @UnmodifiableView
    public @NotNull List<MenuComponentNode> getChildren() {
        return ImmutableList.copyOf(this.children);
    }

    public void clear() {
        this.children.clear();
    }

    public void addChild(@NotNull final MenuComponentNode node) {
        this.children.add(node);
    }

    @Override
    public @NotNull ComponentType getType() {
        return this.component.getType();
    }
}
