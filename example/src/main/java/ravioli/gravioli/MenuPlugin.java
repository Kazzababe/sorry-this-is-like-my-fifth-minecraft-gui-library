package ravioli.gravioli;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class MenuPlugin extends JavaPlugin implements Listener {
    private ExampleMenu exampleMenu;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    private void onDoSomething(@NotNull final PlayerJumpEvent event) {
        this.exampleMenu = new ExampleMenu(this, event.getPlayer());
        this.exampleMenu.open();
    }

    @EventHandler
    private void onInventoryClick(@NotNull final InventoryClickEvent event) {
        if (this.exampleMenu == null) {
            return;
        }
    }
}
