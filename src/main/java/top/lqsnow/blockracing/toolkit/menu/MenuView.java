package top.lqsnow.blockracing.toolkit.menu;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import top.lqsnow.blockracing.toolkit.text.Texts;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public abstract class MenuView implements InventoryHolder {
    private final int size;
    private final Function<Player, String> titleFactory;
    private Inventory inventory;
    private final Map<Integer, MenuButton> buttons = new HashMap<>();

    protected MenuView(int size, String title) {
        this(size, player -> title);
    }

    protected MenuView(int size, Function<Player, String> titleFactory) {
        if (size < 9 || size > 54 || size % 9 != 0) {
            throw new IllegalArgumentException("Menu size must be a multiple of 9 between 9 and 54");
        }
        this.size = size;
        this.titleFactory = titleFactory;
    }

    protected final void setButton(int slot, MenuButton button) {
        if (slot < 0 || slot >= size) {
            throw new IllegalArgumentException("Button slot outside menu: " + slot);
        }
        buttons.put(slot, button);
    }

    protected ItemStack getBackgroundItem(int slot, Player player) {
        return null;
    }

    public final void open(Player player) {
        inventory = Bukkit.createInventory(this, size, Texts.component(titleFactory.apply(player)));
        render(player);
        player.openInventory(inventory);
    }

    public final void refresh(Player player) {
        render(player);
        player.updateInventory();
    }

    final void click(Player player, int slot, ClickType clickType) {
        MenuButton button = buttons.get(slot);
        if (button != null) {
            button.click(player, clickType);
        }
    }

    private void render(Player player) {
        inventory.clear();
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack background = getBackgroundItem(slot, player);
            if (background != null) {
                inventory.setItem(slot, background);
            }
        }
        buttons.forEach((slot, button) -> inventory.setItem(slot, button.createItem(player)));
    }

    @Override
    public final @NotNull Inventory getInventory() {
        if (inventory == null) {
            throw new IllegalStateException("Menu inventory has not been opened yet");
        }
        return inventory;
    }

    protected final int getSize() {
        return size;
    }
}
