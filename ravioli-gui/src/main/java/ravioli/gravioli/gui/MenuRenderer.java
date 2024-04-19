package ravioli.gravioli.gui;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

public final class MenuRenderer {
    private final Table<Integer, Integer, Consumer<InventoryClickEvent>> clickEvents = HashBasedTable.create();
    private final Table<Integer, Integer, Consumer<InventoryClickEvent>> previousClickEvents = HashBasedTable.create();
    private final Table<Integer, Integer, ItemStack> positionableItems = HashBasedTable.create();
    private final Table<Integer, Integer, ItemStack> previousPositionableItems = HashBasedTable.create();
    private final ReadWriteLock itemLock = new ReentrantReadWriteLock();

    public void queue(final int x, final int y, @NotNull final ItemStack itemStack) {
        this.itemLock.writeLock().lock();

        try {
            this.positionableItems.put(x, y, itemStack);
        } finally {
            this.itemLock.writeLock().unlock();
        }
    }

    public void queue(final int x, final int y, @NotNull final Consumer<InventoryClickEvent> clickEventConsumer) {
        this.clickEvents.put(x, y, clickEventConsumer);
    }

    public void prepare() {
        this.clickEvents.clear();
    }

    public void render(@NotNull final Inventory inventory) {
        this.itemLock.writeLock().lock();

        try {
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
                final int x = cell.getRowKey();
                final int y = cell.getColumnKey();
                final int slot = y * 9 + x;
                final ItemStack currentItem = inventory.getItem(slot);
                final ItemStack newItem = cell.getValue();

                if (currentItem == null || currentItem.getType().isAir()) {
                    inventory.setItem(slot, newItem);

                    return;
                }
                if (newItem.getItemMeta() instanceof SkullMeta || currentItem.getType() != newItem.getType()) {
                    inventory.setItem(slot, newItem);
                } else {
                    currentItem.setItemMeta(newItem.getItemMeta());
                    currentItem.setAmount(newItem.getAmount());
                }
            });
            this.previousPositionableItems.clear();
            this.previousPositionableItems.putAll(this.positionableItems);
            this.positionableItems.clear();
        } finally {
            this.itemLock.writeLock().unlock();
        }
    }

    public void handleClick(@NotNull final InventoryClickEvent event) {
        final Inventory inventory = event.getClickedInventory();
        final int slot = event.getSlot();
        final int x = slot % 9;
        final int y = slot / 9;
        final var eventHandler = this.clickEvents.get(x, y);

        if (eventHandler == null) {
            return;
        }
        eventHandler.accept(event);
    }
}
