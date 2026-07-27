package top.lqsnow.blockracing.toolkit.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class MenuButton {
    private final Function<Player, ItemStack> itemFactory;
    private final BiConsumer<Player, ClickType> clickHandler;

    private MenuButton(Function<Player, ItemStack> itemFactory, BiConsumer<Player, ClickType> clickHandler) {
        this.itemFactory = itemFactory;
        this.clickHandler = clickHandler;
    }

    public static MenuButton of(Supplier<ItemStack> itemFactory, BiConsumer<Player, ClickType> clickHandler) {
        return new MenuButton(player -> itemFactory.get(), clickHandler);
    }

    public static MenuButton of(Function<Player, ItemStack> itemFactory,
                                BiConsumer<Player, ClickType> clickHandler) {
        return new MenuButton(itemFactory, clickHandler);
    }

    ItemStack createItem(Player player) {
        return itemFactory.apply(player);
    }

    void click(Player player, ClickType clickType) {
        clickHandler.accept(player, clickType);
    }
}
