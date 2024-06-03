package ravioli.gravioli.gui;

import com.comphenix.protocol.ProtocolLibrary;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ravioli.gravioli.gui.model.RenderType;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

public final class MenuRenderer {
    private static final ItemStack AIR = new ItemStack(Material.AIR);

    private final Table<Integer, Integer, Consumer<InventoryClickEvent>> clickEvents = HashBasedTable.create();
    private final Table<Integer, Integer, Consumer<InventoryClickEvent>> previousClickEvents = HashBasedTable.create();
    private final Table<Integer, Integer, ItemStack> positionableItems = HashBasedTable.create();
    private final Table<Integer, Integer, ItemStack> previousPositionableItems = HashBasedTable.create();
    private final ReadWriteLock itemLock = new ReentrantReadWriteLock();
    private final Menu menu;
    private final PacketMenuRenderer packetMenuRenderer;

    Inventory packetInventory;

    MenuRenderer(@NotNull final Menu menu) {
        this.menu = menu;
        this.packetMenuRenderer = new PacketMenuRenderer(menu, slot -> {
            if (this.packetInventory != null && slot < this.packetInventory.getSize()) {
                return this.packetInventory.getItem(slot);
            }
            final int x = slot % 9;
            final int y = slot / 9;

            return Objects.requireNonNullElse(this.positionableItems.get(x, y), AIR);
        });

        if (!this.packetMenuRenderer.isEnabled()) {
            return;
        }
        ProtocolLibrary.getProtocolManager().addPacketListener(this.packetMenuRenderer);
    }

    public void cleanup() {
        if (!this.packetMenuRenderer.isEnabled()) {
            return;
        }
        ProtocolLibrary.getProtocolManager().removePacketListener(this.packetMenuRenderer);

        Bukkit.getScheduler().scheduleSyncDelayedTask(this.menu.getPlugin(), () -> this.menu.getPlayer().updateInventory());
    }

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

    public void init() {
        if (this.packetMenuRenderer.isEnabled()) {
            this.packetInventory = Bukkit.createInventory(null, this.menu.getInventory().getSize());
        }
    }

    public void prepare() {
        this.clickEvents.clear();
        this.positionableItems.clear();
    }

    public void render(@NotNull final Inventory inventory) {
        final RenderType renderType = this.menu.getRenderType();

        this.itemLock.writeLock().lock();

        try {
            if (renderType == RenderType.PHYSICAL) {
                this.previousPositionableItems.cellSet().forEach(cell -> {
                    final int x = cell.getRowKey();
                    final int y = cell.getColumnKey();
                    final int slot = y * 9 + x;

                    if (slot >= inventory.getSize()) {
                        return;
                    }
                    if (this.positionableItems.contains(x, y)) {
                        final ItemStack newItem = cell.getValue();

                        if (newItem == null || newItem.getType().isAir()) {
                            inventory.clear(slot);
                        }
                        return;
                    }
                    inventory.clear(slot);
                });
            }
            this.positionableItems.cellSet().forEach(cell -> {
                final int x = cell.getRowKey();
                final int y = cell.getColumnKey();
                final int slot = y * this.menu.getWidth() + x;

                if (slot >= inventory.getSize()) {
                    return;
                }
                final ItemStack newItem = cell.getValue();
                final ItemStack currentItem = inventory.getItem(slot);

                this.handlePacketInventoryItem(slot, newItem);

                if (renderType != RenderType.PHYSICAL) {
                    return;
                }
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
            if (renderType == RenderType.PACKET) {
                final List<HumanEntity> viewers = this.menu.getInventory().getViewers();

                for (final HumanEntity viewer : viewers) {
                    if (!(viewer instanceof final Player player)) {
                        continue;
                    }
                    player.updateInventory();
                }
            }
            this.previousPositionableItems.clear();
            this.previousPositionableItems.putAll(this.positionableItems);
        } finally {
            this.itemLock.writeLock().unlock();
        }
    }

    public void handleClick(@NotNull final InventoryClickEvent event) {
        final int slot = event.getRawSlot();
        final int x = slot % 9;
        final int y = slot / 9;
        final var eventHandler = this.clickEvents.get(x, y);

        if (this.menu.getRenderType() == RenderType.PACKET) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(this.menu.getPlugin(), this.menu.getPlayer()::updateInventory);
        }
        if (eventHandler == null) {
            return;
        }
        eventHandler.accept(event);
    }

    private void handlePacketInventoryItem(final int slot, @Nullable final ItemStack itemStack) {
        if (this.packetInventory == null) {
            return;
        }
        this.packetInventory.setItem(slot, itemStack);
    }
}
