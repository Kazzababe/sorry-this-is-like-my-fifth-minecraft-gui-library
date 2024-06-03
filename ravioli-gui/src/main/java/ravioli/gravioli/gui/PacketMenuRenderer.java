package ravioli.gravioli.gui;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.gui.model.RenderType;
import ravioli.gravioli.gui.util.ReflectionUtil;

import java.util.List;
import java.util.function.Function;

public class PacketMenuRenderer extends PacketAdapter {
    private final Menu menu;
    private final Function<Integer, ItemStack> itemStackSupplier;

    public PacketMenuRenderer(@NotNull final Menu menu, @NotNull final Function<Integer, ItemStack> itemStackSupplier) {
        super(menu.getPlugin(), ListenerPriority.HIGHEST, PacketType.Play.Client.WINDOW_CLICK, PacketType.Play.Server.SET_SLOT, PacketType.Play.Server.WINDOW_ITEMS);

        this.menu = menu;
        this.itemStackSupplier = itemStackSupplier;
    }

    public boolean isEnabled() {
        return Bukkit.getPluginManager().isPluginEnabled("ProtocolLib");
    }

    @Override
    public void onPacketSending(@NotNull final PacketEvent event) {
        final Player player = event.getPlayer();

        if (!player.getUniqueId().equals(this.menu.getPlayer().getUniqueId())) {
            return;
        }
        if (this.menu.getRenderType() != RenderType.PACKET) {
            return;
        }
        final PacketType packetType = event.getPacketType();

        if (packetType == PacketType.Play.Server.SET_SLOT) {
            this.handleSetSlot(event);
        } else {
            this.handleWindowItems(event);
        }
    }

    private void handleWindowItems(@NotNull final PacketEvent event) {
        final PacketContainer packet = event.getPacket();
        final ServerPlayer serverPlayer = ReflectionUtil.getServerPlayer(event.getPlayer());
        final AbstractContainerMenu container = serverPlayer.containerMenu;
        final int windowId = packet.getIntegers().read(0);

        if (windowId != container.containerId) {
            return;
        }
        final List<ItemStack> itemStacks = packet.getItemListModifier().read(0);
        final int totalSize = this.menu.getWidth() * this.menu.getHeight();

        for (int i = 0; i < totalSize; i++) {
            final ItemStack itemStack = this.itemStackSupplier.apply(i);

            if (itemStack == null) {
                continue;
            }
            itemStacks.set(i, itemStack);
        }
        packet.getItemListModifier().write(0, itemStacks);
    }

    @Override
    public void onPacketReceiving(@NotNull final PacketEvent event) {
        final Player player = event.getPlayer();

        if (!player.getUniqueId().equals(this.menu.getPlayer().getUniqueId())) {
            return;
        }
        if (this.menu.getRenderType() != RenderType.PACKET) {
            return;
        }
        final PacketType packetType = event.getPacketType();

        if (packetType == PacketType.Play.Client.WINDOW_CLICK) {
            this.handleWindowClick(event);
        }
    }

    private void handleSetSlot(@NotNull final PacketEvent event) {
        final Player player = event.getPlayer();
        final PacketContainer packet = event.getPacket();
        final ServerPlayer serverPlayer = ReflectionUtil.getServerPlayer(player);
        final AbstractContainerMenu container = serverPlayer.containerMenu;
        final int windowId = packet.getIntegers().read(0);

        if (windowId != container.containerId) {
            return;
        }
        event.setCancelled(true);

        Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, player::updateInventory);
    }

    private void handleWindowClick(@NotNull final PacketEvent event) {
        final Player player = event.getPlayer();
        final PacketContainer packet = event.getPacket();
        final ServerPlayer serverPlayer = ReflectionUtil.getServerPlayer(player);
        final AbstractContainerMenu container = serverPlayer.containerMenu;
        final int windowId = packet.getIntegers().read(0);

        if (windowId != container.containerId) {
            return;
        }
        final int slot = packet.getIntegers().read(2);

        if (slot >= this.menu.getSize()) {
            return;
        }
        final int button = packet.getIntegers().read(3);
        final InventoryClickType mode = packet.getEnumModifier(InventoryClickType.class, 4).read(0);
        ClickType clickType = ClickType.UNKNOWN;

        event.setCancelled(true);

        switch (mode) {
            case PICKUP -> {
                if (button == 0) {
                    clickType = ClickType.LEFT;
                } else if (button == 1) {
                    clickType = ClickType.RIGHT;
                }
            }
            case QUICK_MOVE -> {
                if (button == 0) {
                    clickType = ClickType.SHIFT_LEFT;
                } else if (button == 1) {
                    clickType = ClickType.SHIFT_RIGHT;
                }
            }
            case SWAP -> {
                if ((button >= 0 && button < 9) || button == 40) {
                    if (button == 40) {
                        clickType = ClickType.SWAP_OFFHAND;
                    } else {
                        clickType = ClickType.NUMBER_KEY;
                    }
                }
            }
            case CLONE -> {
                if (button == 2) {
                    clickType = ClickType.MIDDLE;
                }
            }
            case THROW -> {
                if (slot >= 0) {
                    if (button == 0) {
                        clickType = ClickType.DROP;
                    } else if (button == 1) {
                        clickType = ClickType.CONTROL_DROP;
                    }
                } else if (button == 1) {
                    clickType = ClickType.RIGHT;
                } else {
                    clickType = ClickType.LEFT;
                }
            }
            case PICKUP_ALL -> clickType = ClickType.DOUBLE_CLICK;
        }
        final ClickType finalClickType = clickType;

        Bukkit.getScheduler().getMainThreadExecutor(this.plugin).execute(() -> {
            this.menu.simulateClick(slot, finalClickType);
        });
    }

    private enum InventoryClickType {
        PICKUP, QUICK_MOVE, SWAP, CLONE, THROW, QUICK_CRAFT, PICKUP_ALL;
    }
}
