package ravioli.gravioli.gui;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public final class MenuRenderer {
    private final Table<Integer, Integer, Consumer<InventoryClickEvent>> clickEvents = HashBasedTable.create();
    private final Table<Integer, Integer, Consumer<InventoryClickEvent>> previousClickEvents = HashBasedTable.create();
    private final Table<Integer, Integer, ItemStack> positionableItems = HashBasedTable.create();
    private final Table<Integer, Integer, ItemStack> previousPositionableItems = HashBasedTable.create();

    public void queue(final int x, final int y, @NotNull final ItemStack itemStack) {
        this.positionableItems.put(x, y, itemStack);
    }

    public void queue(final int x, final int y, @NotNull final Consumer<InventoryClickEvent> clickEventConsumer) {
        this.clickEvents.put(x, y, clickEventConsumer);
    }

    public void prepare() {
        this.clickEvents.clear();
    }

    public synchronized void render(@NotNull final Inventory inventory) {
        inventory.clear();

        this.previousPositionableItems.cellSet().forEach(cell -> {
            final int x = cell.getRowKey();
            final int y = cell.getColumnKey();
            final int slot = y * 9 + x;

            if (this.positionableItems.contains(x, y)) {
                final ItemStack newItem = cell.getValue();

                if (newItem == null || newItem.getType().isAir()) {
                    inventory.clear(slot);
                }
                return;
            }
            inventory.clear(slot);
        });
        this.positionableItems.cellSet().forEach(cell -> {
            final int slot = cell.getColumnKey() * 9 + cell.getRowKey();
            final ItemStack currentItem = inventory.getItem(slot);
            final ItemStack newItem = cell.getValue();

            if (currentItem == null || currentItem.getType().isAir()) {
                inventory.setItem(slot, newItem);

                return;
            }
            if (currentItem.getType() != newItem.getType()) {
                currentItem.setType(newItem.getType());
            }
            currentItem.setItemMeta(newItem.getItemMeta());inventory.setItem(slot, newItem);
        });
        this.previousPositionableItems.clear();
        this.previousPositionableItems.putAll(this.positionableItems);
        this.positionableItems.clear();
    }

    public void handleClick(@NotNull final InventoryClickEvent event) {
        final Inventory inventory = event.getClickedInventory();
        final int slot = event.getSlot();
        final int x = slot % 9;
        final int y = slot / 9;
        final var c = this.clickEvents.get(x, y);

        if (c != null) {
            c.accept(event);
        }
    }
}
