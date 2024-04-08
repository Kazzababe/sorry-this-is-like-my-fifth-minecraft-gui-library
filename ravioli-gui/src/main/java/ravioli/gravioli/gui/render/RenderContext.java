package ravioli.gravioli.gui.render;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public record RenderContext(@NotNull Player player, @NotNull Plugin plugin, int parentWidth, int parentHeight) {
}
