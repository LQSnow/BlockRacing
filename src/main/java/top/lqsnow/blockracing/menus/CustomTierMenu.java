package top.lqsnow.blockracing.menus;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import top.lqsnow.blockracing.managers.Block;
import top.lqsnow.blockracing.managers.Message;
import top.lqsnow.blockracing.pools.PoolEntry.Dimension;
import top.lqsnow.blockracing.toolkit.item.ItemBuilder;
import top.lqsnow.blockracing.toolkit.menu.MenuButton;
import top.lqsnow.blockracing.toolkit.menu.MenuView;
import top.lqsnow.blockracing.utils.TranslationUtil;
import top.lqsnow.blockracing.managers.LanguageManager;

public final class CustomTierMenu extends MenuView {
    public CustomTierMenu(Dimension dimension) {
        super(27, player -> Message.CUSTOM_DETAILS.getString(player)
                .replace("%dimension%", DifficultyMenu.dimensionName(dimension).getString(player)));
        for (int tier = 1; tier <= 6; tier++) {
            int selectedTier = tier;
            setButton(9 + tier, MenuButton.of(player -> ItemBuilder.of(Block.customSettings().tierEnabled(dimension, selectedTier)
                            ? Material.GREEN_CONCRETE : Material.RED_CONCRETE)
                    .name("T" + selectedTier + "：" + (Block.customSettings().tierEnabled(dimension, selectedTier)
                            ? Message.MENU_ENABLED : Message.MENU_DISABLED).getString(player))
                    .lore(tierLore(dimension, selectedTier, player)).build(), (player, click) -> {
                if (!DifficultyMenu.pregame(player)) return;
                CustomBlockMenu.edit(player, () -> Block.saveCustomSettings(Block.customSettings().toggleTier(dimension, selectedTier)));
            }));
        }
        setButton(18, MenuButton.of(player -> ItemBuilder.of(Material.ARROW)
                .name(Message.CUSTOM_TITLE.getString(player)).build(), (player, click) -> {
            if (DifficultyMenu.pregame(player)) new CustomBlockMenu().open(player);
        }));
    }

    private static java.util.List<String> tierLore(Dimension dimension, int tier, Player player) {
        var summary = Block.tierSummary(dimension, tier);
        String examples = summary.examples().isEmpty() ? Message.CUSTOM_EMPTY_TIER.getString(player)
                : summary.examples().stream().map(id -> TranslationUtil.getValue(id, player))
                .collect(java.util.stream.Collectors.joining(LanguageManager.usesChinese(player) ? "、" : ", "));
        return Message.CUSTOM_TIER_LORE.getStringList(player).stream().map(line -> line
                .replace("%count%", String.valueOf(summary.count())).replace("%examples%", examples)).toList();
    }

    @Override protected ItemStack getBackgroundItem(int slot, Player player) {
        return null;
    }
}
