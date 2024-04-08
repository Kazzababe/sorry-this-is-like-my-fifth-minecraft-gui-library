package ravioli.gravioli;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.gui.AsyncPaginationComponent;
import ravioli.gravioli.gui.ItemComponent;
import ravioli.gravioli.gui.Menu;
import ravioli.gravioli.gui.MenuInitializer;
import ravioli.gravioli.gui.Ref;
import ravioli.gravioli.gui.mask.StringMask;
import ravioli.gravioli.gui.render.MenuRenderOutput;
import ravioli.gravioli.gui.render.RenderContext;

import java.util.List;
import java.util.stream.Collectors;

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
        menuInitializer.setHeight(6);

        this.paginationRef = this.useRef();
    }

    @Override
    public @NotNull MenuRenderOutput render(@NotNull final RenderContext renderContext) {
        final MenuRenderOutput renderOutput = MenuRenderOutput.create()
            .set(0, 0, new ItemComponent(
                    () -> {
                        final ItemStack item = new ItemStack(Material.RED_STAINED_GLASS_PANE);
                        final ItemMeta itemMeta = item.getItemMeta();
                        itemMeta.displayName(Component.text("DECREASE PAGE"));
                        item.setItemMeta(itemMeta);

                        return item;
                    },
                    (event) -> {
                        this.paginationRef.get().previousPage();
                    }
                )
            )
            .set(1, 0, new ItemComponent(
                    () -> {
                        final ItemStack item = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
                        final ItemMeta itemMeta = item.getItemMeta();
                        itemMeta.displayName(Component.text("INCREASE PAGE"));
                        item.setItemMeta(itemMeta);

                        return item;
                    },
                    (event) -> {
                        this.paginationRef.get().nextPage();
                    }
                )
            );

        renderOutput.add(
            new AsyncPaginationComponent(
                0,
                new StringMask("010101010"),
                (page, pageSize) -> {
                    final List<ItemComponent> pageItems = PAGINATION_COMPONENTS.stream()
                        .skip(page * pageSize)
                        .limit(pageSize + 1)
                        .toList();

                    try {
                        Thread.sleep(50);
                    } catch (final InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    return new AsyncPaginationComponent.PageData(
                        pageItems.stream()
                            .limit(pageSize)
                            .collect(Collectors.toList()),
                        pageItems.size() > pageSize
                    );
                })
                .withRef(this.paginationRef)
        );

        return renderOutput.setTitle(
            () -> Component.text("Page " + this.paginationRef.get().getPage())
        );
    }
}
