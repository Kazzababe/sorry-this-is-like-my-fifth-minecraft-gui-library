package ravioli.gravioli.gui;

import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.gui.model.ComponentType;

public interface MenuComponentLike {
    @NotNull ComponentType getType();
}
