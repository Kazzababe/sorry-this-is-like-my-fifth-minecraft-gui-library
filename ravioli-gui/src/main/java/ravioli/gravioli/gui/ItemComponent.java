package ravioli.gravioli.gui;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.gui.render.ItemRenderer;
import ravioli.gravioli.gui.render.RenderContext;
import ravioli.gravioli.gui.render.RenderOutput;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ItemComponent extends MenuComponent.PositionableMenuComponent<ItemComponent> implements ItemRenderer {
    private final Supplier<ItemStack> itemStack;
    private final Consumer<InventoryClickEvent> clickEventHandler;

    public ItemComponent(@NotNull final Supplier<ItemStack> itemStack, @NotNull final Consumer<InventoryClickEvent> clickEventHandler) {
        this.itemStack = itemStack;
        this.clickEventHandler = clickEventHandler;
    }

    @Override
    public void init() {

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
    public @NotNull RenderOutput render(@NotNull final RenderContext renderContext) {
        return RenderOutput.create();
    }

    @Override
    public @NotNull ItemStack getItemStack() {
        return this.itemStack.get();
    }

    @Override
    public @NotNull Consumer<InventoryClickEvent> getClickHandler() {
        return this.clickEventHandler;
    }
}
