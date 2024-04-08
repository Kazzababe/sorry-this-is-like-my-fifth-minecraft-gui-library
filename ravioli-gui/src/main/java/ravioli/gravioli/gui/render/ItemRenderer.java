package ravioli.gravioli.gui.render;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public interface ItemRenderer {
    @NotNull ItemStack getItemStack();

    default @NotNull Consumer<InventoryClickEvent> getClickHandler() {
        return event -> {};
    }
}
