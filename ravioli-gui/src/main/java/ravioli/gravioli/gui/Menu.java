package ravioli.gravioli.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ravioli.gravioli.gui.reconcilation.MenuComponentNode;
import ravioli.gravioli.gui.render.ItemRenderer;
import ravioli.gravioli.gui.render.MenuRenderOutput;
import ravioli.gravioli.gui.render.RenderContext;
import ravioli.gravioli.gui.render.RenderOutput;
import ravioli.gravioli.gui.render.RenderOutputNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public abstract class Menu extends MenuComponent.PositionableMenuComponent<Menu> implements InventoryHolder {
    private final Player player;
    private final Plugin plugin;
    private final MenuListeners menuListeners;

    private final MenuInitializer menuInitializer;
    private final MenuRenderer menuRenderer;
    private final Map<UUID, StateController> cachedMenuStates = new HashMap<>();
    private final ReadWriteLock renderLock = new ReentrantReadWriteLock();

    private MenuComponentNode currentRenderOutput;
    private Inventory inventory;

    private Component previousTitle;
    private Component queuedTitle;

    public Menu(@NotNull final Plugin plugin, @NotNull final Player player) {
        this.player = player;
        this.plugin = plugin;

        this.menuListeners = new MenuListeners();
        this.menuInitializer = new MenuInitializer();
        this.menuRenderer = new MenuRenderer();
    }

    @Override
    public int getWidth() {
        return 9;
    }

    @Override
    public int getHeight() {
        return this.menuInitializer.getHeight();
    }

    public final void open() {
        this.init(this.menuInitializer);
        this.reconcile();

        this.inventory = Bukkit.createInventory(
            this,
            9 * this.menuInitializer.getHeight(),
            this.queuedTitle
        );
        this.renderRoot();

        Bukkit.getScheduler().getMainThreadExecutor(this.plugin).execute(() -> {
            this.player.openInventory(this.inventory);
        });
        Bukkit.getPluginManager().registerEvents(this.menuListeners, this.plugin);
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
    public abstract @Nullable MenuRenderOutput render(@NotNull RenderContext renderContext);

    @Override
    public final @NotNull Inventory getInventory() {
        return this.inventory;
    }

    private void reconcile() {
        if (this.currentRenderOutput == null) {
            this.currentRenderOutput = this.renderTree(this, 0, 0);

            return;
        }
        this.currentRenderOutput = this.reconcile(
            this.currentRenderOutput,
            new MenuComponentNode(this, 0, 0),
            0,
            0
        );
    }

    private void render() {
        this.renderLock.writeLock().lock();

        try {
            this.reconcile();
            this.renderRoot();
        } finally {
            this.renderLock.writeLock().unlock();
        }
    }

    private void renderRoot() {
        this.menuRenderer.prepare();
        this.renderNode(this.currentRenderOutput);
        this.menuRenderer.render(this.inventory);

        if (Objects.equals(this.previousTitle, this.queuedTitle)) {
            return;
        }
        this.inventory.getViewers().forEach(viewer -> {
            final InventoryView view = viewer.getOpenInventory();

            view.setTitle(LegacyComponentSerializer.legacySection().serialize(this.queuedTitle));
        });

        this.previousTitle = this.queuedTitle;
    }

    private void renderNode(@NotNull final MenuComponentNode node) {
        final MenuComponent<?> component = node.getComponent();

        node.getChildren().forEach(this::renderNode);

        if (component instanceof final ItemRenderer itemRenderer) {
            this.menuRenderer.queue(node.getX(), node.getY(), itemRenderer.getItemStack());
            this.menuRenderer.queue(node.getX(), node.getY(), itemRenderer.getClickHandler());
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private @Nullable MenuComponentNode reconcile(@Nullable final MenuComponentNode current, @NotNull final MenuComponentNode building, final int originX, final int originY) {
        if (current == null) {
            return null;
        }
        final MenuComponent<?> currentComponent = building.getComponent();
        final StateController stateController = this.getState(currentComponent, true);

        currentComponent.setStateController(stateController);
        currentComponent.init();
        stateController.markInitialized();

        final RenderOutput<?> renderOutput = currentComponent.render(
            new RenderContext(this.player, this.plugin, currentComponent.getWidth(), currentComponent.getHeight())
        );

        if (renderOutput == null) {
            return building;
        }
        final List<RenderOutputNode> renderOutputNodes = renderOutput.getChildren();
        final List<MenuComponentNode> currentChildren = current.getChildren();

        for (int i = 0; i < Math.max(renderOutputNodes.size(), currentChildren.size()); i++) {
            if (i >= renderOutputNodes.size()) {
                // Node being removed from the tree
                final MenuComponentNode currentNode = currentChildren.get(i);
                final MenuComponent<?> currentMenuComponent = currentNode.getComponent();

                this.flattenChildren(currentNode).forEach(MenuComponent::unmount);

                currentMenuComponent.unmount();

                continue;
            }
            final RenderOutputNode renderingNode = renderOutputNodes.get(i);
            final int x = renderingNode instanceof final RenderOutputNode.PositionedRenderOutputNode positionedNode ?
                positionedNode.getX() :
                0;
            final int y = renderingNode instanceof final RenderOutputNode.PositionedRenderOutputNode positionedNode ?
                positionedNode.getY() :
                0;
            final int newOriginX = originX + x;
            final int newOriginY = originY + y;

            if (i >= currentChildren.size()) {
                // New node being added to tree
                building.addChild(this.renderTree(renderingNode.getComponent(), newOriginX, newOriginY));

                continue;
            }
            final MenuComponentNode currentNode = currentChildren.get(i);

            // TODO: Compare positions
            if (this.areSimilar(currentNode.getComponent(), renderingNode.getComponent())) {
                final MenuComponent component = currentNode.getComponent();
                final MenuComponent renderingComponent = renderingNode.getComponent();
                final Ref ref = component.getRef();

                renderingComponent.setInternalId(component.getInternalId());
                renderingComponent.mounted = currentComponent.mounted;

                if (ref != null) {
                    renderingComponent.setRef(ref);
                }
                final MenuComponentNode reconciled = this.reconcile(currentNode, new MenuComponentNode(renderingComponent, newOriginX, newOriginY), newOriginX, newOriginY);

                renderingComponent.update();

                if (reconciled != null) {
                    building.addChild(reconciled);
                }
            } else {
                this.cachedMenuStates.remove(current.getComponent().getInternalId());

                building.addChild(
                    this.renderTree(renderingNode.getComponent(), newOriginX, newOriginY)
                );
            }
        }
        this.tryReconcileTitle(renderOutput);

        return building;
    }

    private boolean areSimilar(@NotNull final MenuComponent<?> component1, @NotNull final MenuComponent<?> component2) {
        if (component1 == component2) {
            return true;
        }
        if (component1.getType() != component2.getType()) {
            return false;
        }
        return component1.getClass() == component2.getClass();
    }

    private void tryReconcileTitle(@NotNull final RenderOutput<?> renderOutput) {
        if (!(renderOutput instanceof final MenuRenderOutput menuRenderOutput) || !menuRenderOutput.hasSetTitle()) {
            return;
        }
        this.queuedTitle = menuRenderOutput.getTitle();
    }

    private @NotNull List<MenuComponent<?>> flattenChildren(@NotNull final MenuComponentNode node) {
        return node.getChildren()
            .stream()
            .flatMap(child -> {
                final List<MenuComponent<?>> descendants = new ArrayList<>();

                descendants.add(child.getComponent());
                descendants.addAll(this.flattenChildren(child));

                return descendants.stream();
            })
            .collect(Collectors.toList());
    }

    private @NotNull MenuComponentNode renderTree(@NotNull final MenuComponent<?> component, final int originX, final int originY) {
        final StateController stateController = this.getState(component, true);

        component.setStateController(stateController);
        component.init();
        stateController.markInitialized();
        component._mount();

        final RenderOutput<?> renderOutput = component.render(new RenderContext(this.player, this.plugin, component.getWidth(), component.getHeight()));
        final MenuComponentNode componentNode = new MenuComponentNode(component, originX, originY);

        if (renderOutput == null) {
            return componentNode;
        }
        renderOutput.getChildren().forEach(child -> {
            final int x = child instanceof final RenderOutputNode.PositionedRenderOutputNode positionedNode ?
                positionedNode.getX() :
                0;
            final int y = child instanceof final RenderOutputNode.PositionedRenderOutputNode positionedNode ?
                positionedNode.getY() :
                0;

            componentNode.addChild(
                this.renderTree(child.getComponent(), originX + x, originY + y)
            );
        });
        this.tryReconcileTitle(renderOutput);

        return componentNode;
    }

    private @NotNull StateController getState(@NotNull final MenuComponent<?> component, final boolean reset) {
        this.renderLock.writeLock().lock();

        try {
            final StateController state = this.cachedMenuStates.computeIfAbsent(
                component.getInternalId(),
                __ -> new StateController(() -> {
                    if (!component.mounted) {
                        return;
                    }
                    Bukkit.getScheduler().getMainThreadExecutor(this.plugin).execute(this::render);
                }, component.getStateController())
            );

            if (reset) {
                state.reset();
            }
            return state;
        } finally {
            this.renderLock.writeLock().unlock();
        }
    }

    public abstract void init(@NotNull MenuInitializer menuInitializer);

    private class MenuListeners implements Listener {
        @EventHandler
        private void onClose(@NotNull final InventoryCloseEvent event) {
            if (!event.getInventory().equals(Menu.this.inventory)) {
                return;
            }
            HandlerList.unregisterAll(this);

            this.unmountMenu(Menu.this.currentRenderOutput);
        }

        @EventHandler
        private void onClick(@NotNull final InventoryClickEvent event) {
            final Inventory inventory = event.getClickedInventory();

            if (inventory == null || !inventory.equals(Menu.this.inventory)) {
                return;
            }
            event.setCancelled(true);

            Menu.this.menuRenderer.handleClick(event);
        }

        private void unmountMenu(@NotNull final MenuComponentNode node) {
            node.getComponent().unmount();
            node.getChildren().forEach(this::unmountMenu);
        }
    }
}
