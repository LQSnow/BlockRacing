package top.lqsnow.blockracing.menus;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import top.lqsnow.blockracing.managers.*;
import top.lqsnow.blockracing.pools.DifficultyPreset;
import top.lqsnow.blockracing.toolkit.item.ItemBuilder;
import top.lqsnow.blockracing.toolkit.menu.MenuButton;
import top.lqsnow.blockracing.toolkit.menu.MenuView;
import java.util.List;

public final class DifficultyMenu extends MenuView {
    public DifficultyMenu() {
        super(27, player -> Message.BLOCK_LIBRARY_TITLE.getString(player));
        for (DifficultyPreset.Kind kind : DifficultyPreset.Kind.values()) {
            setButton(10 + kind.ordinal(), MenuButton.of(player -> presetItem(kind, player), (player, click) -> {
                if (!pregame(player)) return;
                Setting.setDifficulty(kind);
                Block.addUpBlocks();
                refreshMenus();
            }));
        }
        setButton(22, copperButton());
        setButton(18, MenuButton.of(player -> ItemBuilder.of(Material.ARROW)
                .name(Message.BLOCK_LIBRARY_BACK.getString(player)).build(), (player, click) -> {
            if (pregame(player)) new PreGameMenu().open(player);
        }));
    }

    public static Message name(DifficultyPreset.Kind kind) {
        return Message.valueOf("BLOCK_LIBRARY_" + kind.name());
    }

    public static MenuButton copperButton() {
        return MenuButton.of(player -> ItemBuilder.of(Setting.isCopperExtras() ? Material.GREEN_CONCRETE : Material.RED_CONCRETE)
                .name(Message.BLOCK_LIBRARY_COPPER.getString(player) + (Setting.isCopperExtras()
                        ? Message.MENU_ENABLED.getString(player) : Message.MENU_DISABLED.getString(player)))
                .lore(Message.BLOCK_LIBRARY_COPPER_LORE.getStringList(player)).build(), (player, click) -> {
            if (!pregame(player)) return;
            Setting.setCopperExtras(!Setting.isCopperExtras());
            Block.addUpBlocks();
            refreshMenus();
        });
    }

    private static boolean pregame(Player player) {
        if (Game.getCurrentGameState() == Game.GameState.PREGAME) return true;
        player.closeInventory();
        return false;
    }

    private static void refreshMenus() {
        Gui.updateMenu(new DifficultyMenu());
        Gui.updateMenu(new PreGameMenu());
        Scoreboard.updateScoreboard();
    }

    private ItemStack presetItem(DifficultyPreset.Kind kind, Player player) {
        DifficultyPreset preset = Block.preset(kind);
        boolean selected = Setting.getDifficulty() == kind;
        List<String> lore = Message.BLOCK_LIBRARY_PRESET_LORE.getStringList(player).stream().map(line -> line
                .replace("%overworld%", range(preset.maxTiers().get(0), player))
                .replace("%nether%", range(preset.maxTiers().get(1), player))
                .replace("%end%", range(preset.maxTiers().get(2), player))
                .replace("%nether_unlock%", threshold(preset, 1, player))
                .replace("%end_unlock%", threshold(preset, 2, player))
                .replace("%capacity%", String.valueOf(Block.candidates(kind, Setting.isCopperExtras()).size()))) .toList();
        List<String> described = new java.util.ArrayList<>(lore);
        described.addFirst(Message.valueOf("BLOCK_LIBRARY_" + kind.name() + "_DESCRIPTION").getString(player));
        return ItemBuilder.of(selected ? Material.GREEN_CONCRETE : Material.YELLOW_CONCRETE)
                .name((selected ? Message.BLOCK_LIBRARY_SELECTED : Message.BLOCK_LIBRARY_SELECT).getString(player)
                        + name(kind).getString(player) + (kind == DifficultyPreset.Kind.NORMAL ? Message.BLOCK_LIBRARY_DEFAULT.getString(player) : ""))
                .lore(described).build();
    }

    private static String range(int cap, Player player) {
        return cap == 0 ? Message.BLOCK_LIBRARY_OFF.getString(player) : "T1–T" + cap;
    }

    private static String threshold(DifficultyPreset preset, int dimension, Player player) {
        return preset.maxTiers().get(dimension) == 0 ? Message.BLOCK_LIBRARY_OFF.getString(player)
                : preset.unlockPercent().get(dimension) + "%";
    }

    @Override protected ItemStack getBackgroundItem(int slot, Player player) {
        return ItemBuilder.of(Material.LIGHT_BLUE_STAINED_GLASS_PANE).name(" ").build();
    }
}
