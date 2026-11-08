package top.lqsnow.blockracing.menus;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import top.lqsnow.blockracing.managers.*;
import top.lqsnow.blockracing.pools.DifficultyPreset;
import top.lqsnow.blockracing.toolkit.item.ItemBuilder;
import top.lqsnow.blockracing.toolkit.menu.MenuButton;
import top.lqsnow.blockracing.toolkit.menu.MenuView;
import top.lqsnow.blockracing.utils.TranslationUtil;
import java.util.List;
import java.util.ArrayList;

public final class DifficultyMenu extends MenuView {
    public DifficultyMenu() {
        super(27, player -> Message.BLOCK_LIBRARY_TITLE.getString(player));
        for (DifficultyPreset.Kind kind : DifficultyPreset.Kind.standards()) {
            setButton(10 + kind.ordinal(), MenuButton.of(player -> presetItem(kind, player), (player, click) -> {
                if (!pregame(player)) return;
                Setting.setDifficulty(kind);
                Block.addUpBlocks();
                refreshMenus();
            }));
        }
        setButton(15, MenuButton.of(this::customItem, (player, click) -> {
            if (!pregame(player)) return;
            CustomBlockMenu.edit(player, () -> Block.activateCustom(Block.customSettings().source()));
        }));
        setButton(16, MenuButton.of(player -> ItemBuilder.of(Material.COMPARATOR)
                .name(Message.CUSTOM_TITLE.getString(player))
                .lore(Message.CUSTOM_ENTRY_LORE.getStringList(player)).build(), (player, click) -> {
            if (pregame(player)) new CustomBlockMenu().open(player);
        }));
        setButton(22, copperButton());
        setButton(18, MenuButton.of(player -> ItemBuilder.of(Material.ARROW)
                .name(Message.BLOCK_LIBRARY_BACK.getString(player)).build(), (player, click) -> {
            if (pregame(player)) new PreGameMenu().open(player);
        }));
    }

    public static Message name(DifficultyPreset.Kind kind) {
        return Message.valueOf("BLOCK_LIBRARY_" + kind.name());
    }

    private ItemStack customItem(Player player) {
        boolean selected = Setting.getDifficulty() == DifficultyPreset.Kind.CUSTOM;
        String source = (Block.customSettings().source() == top.lqsnow.blockracing.pools.CustomSettings.Source.LOCAL
                ? Message.CUSTOM_SOURCE_LOCAL : Message.CUSTOM_SOURCE_TIERS).getString(player);
        return ItemBuilder.of(selected ? Material.GREEN_CONCRETE : Material.YELLOW_CONCRETE)
                .name((selected ? Message.BLOCK_LIBRARY_SELECTED : Message.BLOCK_LIBRARY_SELECT).getString(player)
                        + name(DifficultyPreset.Kind.CUSTOM).getString(player))
                .lore(Message.CUSTOM_SELECT_LORE.getStringList(player).stream()
                        .map(line -> line.replace("%source%", source)).toList()).build();
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

    static boolean pregame(Player player) {
        if (Game.getCurrentGameState() == Game.GameState.PREGAME) return true;
        player.closeInventory();
        return false;
    }

    static void refreshMenus() {
        Gui.updateMenu(new DifficultyMenu());
        Gui.updateMenu(new PreGameMenu());
        Gui.updateMenu(new CustomBlockMenu());
        top.lqsnow.blockracing.toolkit.menu.MenuManager.refreshOpenMenus(CustomTierMenu.class);
        Scoreboard.updateScoreboard();
    }

    private ItemStack presetItem(DifficultyPreset.Kind kind, Player player) {
        DifficultyPreset preset = Block.preset(kind);
        boolean selected = Setting.getDifficulty() == kind;
        List<String> described = new ArrayList<>();
        described.addFirst(Message.valueOf("BLOCK_LIBRARY_" + kind.name() + "_DESCRIPTION").getString(player));
        described.add(Message.BLOCK_LIBRARY_EXAMPLES.getString(player).replace("%blocks%", examples(kind, player)));
        for (int dimension = 0; dimension < 3; dimension++) {
            if (preset.maxTiers().get(dimension) == 0 || preset.dimensionWeights().get(dimension) == 0) continue;
            described.add(Message.BLOCK_LIBRARY_DIMENSION_LORE.getString(player)
                    .replace("%dimension%", dimensionName(top.lqsnow.blockracing.pools.PoolEntry.Dimension.values()[dimension]).getString(player))
                    .replace("%tiers%", "T1–T" + preset.maxTiers().get(dimension))
                    .replace("%unlock%", preset.unlockPercent().get(dimension) + "%"));
        }
        if (preset.tierProgression().enabled()) described.add(Message.BLOCK_LIBRARY_PROGRESSION_LORE.getString(player));
        described.add(Message.BLOCK_LIBRARY_CLICK_SELECT.getString(player));
        return ItemBuilder.of(selected ? Material.GREEN_CONCRETE : Material.YELLOW_CONCRETE)
                .name((selected ? Message.BLOCK_LIBRARY_SELECTED : Message.BLOCK_LIBRARY_SELECT).getString(player)
                        + name(kind).getString(player) + (kind == DifficultyPreset.Kind.NORMAL ? Message.BLOCK_LIBRARY_DEFAULT.getString(player) : ""))
                .lore(described).build();
    }

    public static Message dimensionName(top.lqsnow.blockracing.pools.PoolEntry.Dimension dimension) {
        return Message.valueOf("BLOCK_LIBRARY_" + dimension.name());
    }

    private static String examples(DifficultyPreset.Kind kind, Player player) {
        List<String> preferred = switch (kind) {
            case CASUAL, EASY -> List.of("dirt", "cobblestone", "glass", "furnace");
            case NORMAL -> List.of("cherry_log", "cake", "beehive", "netherrack");
            case HARD -> List.of("sponge", "turtle_egg", "shulker_box", "ancient_debris");
            case NIGHTMARE -> List.of("dragon_head", "netherite_block", "beacon", "copper_golem_statue");
            case CUSTOM -> List.of();
        };
        java.util.Set<String> available = Block.candidates(kind, Setting.isCopperExtras()).stream()
                .map(top.lqsnow.blockracing.pools.PoolEntry::id).collect(java.util.stream.Collectors.toSet());
        List<String> examples = new ArrayList<>();
        for (String id : preferred) if (available.contains("minecraft:" + id)) examples.add("minecraft:" + id);
        if (examples.size() < 3) available.stream().sorted().filter(id -> !examples.contains(id))
                .limit(3 - examples.size()).forEach(examples::add);
        return examples.stream().map(id -> TranslationUtil.getValue(id, player)).collect(java.util.stream.Collectors.joining("、"));
    }

    @Override protected ItemStack getBackgroundItem(int slot, Player player) {
        return null;
    }
}
