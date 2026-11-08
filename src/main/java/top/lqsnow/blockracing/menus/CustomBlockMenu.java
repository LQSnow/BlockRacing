package top.lqsnow.blockracing.menus;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import top.lqsnow.blockracing.managers.Block;
import top.lqsnow.blockracing.managers.Message;
import top.lqsnow.blockracing.managers.Setting;
import top.lqsnow.blockracing.pools.CustomSettings;
import top.lqsnow.blockracing.pools.DifficultyPreset;
import top.lqsnow.blockracing.pools.PoolEntry.Dimension;
import top.lqsnow.blockracing.toolkit.item.ItemBuilder;
import top.lqsnow.blockracing.toolkit.menu.MenuButton;
import top.lqsnow.blockracing.toolkit.menu.MenuView;

public final class CustomBlockMenu extends MenuView {
    public CustomBlockMenu() {
        super(54, player -> Message.CUSTOM_TITLE.getString(player));
        for (Dimension dimension : Dimension.values()) {
            int column = 11 + dimension.ordinal() * 2;
            setButton(column, MenuButton.of(player -> ItemBuilder.of(Block.customSettings().enabled(dimension)
                            ? Material.GREEN_CONCRETE : Material.RED_CONCRETE)
                    .name(DifficultyMenu.dimensionName(dimension).getString(player) + "：" + (Block.customSettings().enabled(dimension)
                            ? Message.MENU_ENABLED : Message.MENU_DISABLED).getString(player))
                    .lore(Message.CUSTOM_DIMENSION_LORE.getStringList(player)).build(), (player, click) -> {
                if (!DifficultyMenu.pregame(player)) return;
                edit(player, () -> Block.saveCustomSettings(Block.customSettings().toggleDimension(dimension)));
            }));
            Material icon = switch (dimension) { case OVERWORLD -> Material.GRASS_BLOCK; case NETHER -> Material.NETHERRACK; case END -> Material.END_STONE; };
            setButton(column + 9, MenuButton.of(player -> ItemBuilder.of(icon)
                    .name(Message.CUSTOM_DETAILS.getString(player).replace("%dimension%", DifficultyMenu.dimensionName(dimension).getString(player)))
                    .lore(Message.CUSTOM_DETAILS_LORE.getStringList(player).stream().map(line -> line.replace("%tiers%", tiers(dimension))).toList())
                    .build(), (player, click) -> {
                if (DifficultyMenu.pregame(player)) new CustomTierMenu(dimension).open(player);
            }));
            setButton(column + 18, MenuButton.of(player -> ItemBuilder.of(Material.CLOCK)
                    .name(Message.CUSTOM_THRESHOLD.getString(player)
                            .replace("%dimension%", DifficultyMenu.dimensionName(dimension).getString(player))
                            .replace("%percent%", String.valueOf(Block.customSettings().unlockPercent().get(dimension.ordinal()))))
                    .lore(Message.CUSTOM_THRESHOLD_LORE.getStringList(player)).build(), (player, click) -> {
                if (!DifficultyMenu.pregame(player)) return;
                int step = click.isShiftClick() ? 10 : 5;
                int change = click.isRightClick() ? step : -step;
                edit(player, () -> Block.saveCustomSettings(Block.customSettings().withThreshold(dimension,
                        Block.customSettings().unlockPercent().get(dimension.ordinal()) + change)));
            }));
        }
        setButton(45, MenuButton.of(player -> ItemBuilder.of(Material.ARROW)
                .name(Message.CUSTOM_BACK.getString(player)).build(), (player, click) -> {
            if (DifficultyMenu.pregame(player)) new DifficultyMenu().open(player);
        }));
        setButton(49, sourceButton(CustomSettings.Source.TIERS, Material.LIME_DYE, Message.CUSTOM_USE_TIERS, Message.CUSTOM_USE_TIERS_LORE));
        setButton(52, DifficultyMenu.copperButton());
        setButton(53, sourceButton(CustomSettings.Source.LOCAL, Material.WRITABLE_BOOK, Message.CUSTOM_USE_LOCAL, Message.CUSTOM_USE_LOCAL_LORE));
    }

    private MenuButton sourceButton(CustomSettings.Source source, Material icon, Message name, Message lore) {
        return MenuButton.of(player -> ItemBuilder.of(icon).name((Setting.getDifficulty() == DifficultyPreset.Kind.CUSTOM
                        && Block.customSettings().source() == source ? Message.BLOCK_LIBRARY_SELECTED.getString(player) : "") + name.getString(player))
                .lore(lore.getStringList(player)).build(), (player, click) -> {
            if (!DifficultyMenu.pregame(player)) return;
            edit(player, () -> {
                Block.activateCustom(source);
                player.sendMessage(Message.CUSTOM_APPLIED.getString(player));
            });
        });
    }

    static void edit(Player player, Runnable action) {
        try {
            action.run();
            Block.addUpBlocks();
            DifficultyMenu.refreshMenus();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            player.sendMessage(Message.CUSTOM_INVALID.getString(player).replace("%reason%", ex.getMessage()));
        }
    }

    private static String tiers(Dimension dimension) {
        java.util.List<String> tiers = new java.util.ArrayList<>();
        for (int tier = 1; tier <= 6; tier++) if (Block.customSettings().tierEnabled(dimension, tier)) tiers.add("T" + tier);
        return tiers.isEmpty() ? "—" : String.join(" / ", tiers);
    }

    @Override protected ItemStack getBackgroundItem(int slot, Player player) {
        return null;
    }
}
