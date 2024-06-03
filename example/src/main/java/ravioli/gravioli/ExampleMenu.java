package ravioli.gravioli;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.gui.AsyncPaginationComponent;
import ravioli.gravioli.gui.ItemComponent;
import ravioli.gravioli.gui.Menu;
import ravioli.gravioli.gui.MenuComponent;
import ravioli.gravioli.gui.MenuInitializer;
import ravioli.gravioli.gui.Ref;
import ravioli.gravioli.gui.model.RenderType;
import ravioli.gravioli.gui.render.MenuRenderOutput;
import ravioli.gravioli.gui.render.RenderContext;

import java.util.List;

public final class ExampleMenu extends Menu {
    private static final List<ItemComponent> PAGINATION_COMPONENTS = List.of(
        new ItemComponent(() -> new ItemStack(Material.YELLOW_STAINED_GLASS_PANE), event -> {
        }),
        new ItemComponent(() -> new ItemStack(Material.RED_STAINED_GLASS_PANE), event -> {
        }),
        new ItemComponent(() -> new ItemStack(Material.LIME_STAINED_GLASS_PANE), event -> {
        }),
        new ItemComponent(() -> new ItemStack(Material.GRAY_STAINED_GLASS_PANE), event -> {
        }),
        new ItemComponent(() -> new ItemStack(Material.BLUE_STAINED_GLASS_PANE), event -> {
        }),
        new ItemComponent(() -> new ItemStack(Material.PURPLE_STAINED_GLASS_PANE), event -> {
        }),
        new ItemComponent(() -> new ItemStack(Material.BLACK_STAINED_GLASS_PANE), event -> System.out.println("CLICK BLACK PANE")),
        new ItemComponent(() -> new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE), event -> {
        }),
        new ItemComponent(() -> new ItemStack(Material.ORANGE_STAINED_GLASS_PANE), event -> {
        })
    );

    private Ref<AsyncPaginationComponent> paginationRef;

    public ExampleMenu(@NotNull final Plugin plugin, final @NotNull Player player) {
        super(plugin, player);
    }

    @Override
    public void init(@NotNull final MenuInitializer menuInitializer) {
        menuInitializer.setHeight(8);

        this.paginationRef = this.useRef();
    }

    @Override
    public @NotNull RenderType getRenderType() {
        return RenderType.PACKET;
    }

    @Override
    public @NotNull MenuRenderOutput render(@NotNull final RenderContext renderContext) {
        final MenuRenderOutput renderOutput = MenuRenderOutput.create();

        for (int i = 0; i < this.getHeight(); i++) {
            final int index = i;

            renderOutput.set(i, i, MenuComponent.item(
                () -> new ItemStack(Material.values()[Material.LIME_STAINED_GLASS.ordinal() + index]),
                event -> this.getPlayer().sendMessage("CLICK " + index)
            ));
        }
        return renderOutput.setTitle(
            () -> Component.text("Page " + 1)
        );
    }
}
