package ravioli.gravioli.gui.render;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class MenuRenderOutput extends RenderOutput<MenuRenderOutput> {
    private Supplier<Component> title;
    private boolean setTitle;

    public static @NotNull MenuRenderOutput create() {
        return new MenuRenderOutput();
    }

    public @NotNull MenuRenderOutput setTitle(@Nullable final Supplier<Component> title) {
        this.title = title;
        this.setTitle = true;

        return this;
    }

    public @Nullable Component getTitle() {
        return this.title.get();
    }

    public boolean hasSetTitle() {
        return this.setTitle;
    }
}
