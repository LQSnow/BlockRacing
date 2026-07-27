package top.lqsnow.blockracing.menus;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import top.lqsnow.blockracing.commands.RandomTeam;
import top.lqsnow.blockracing.managers.Game;
import top.lqsnow.blockracing.managers.Message;
import top.lqsnow.blockracing.managers.Setting;
import top.lqsnow.blockracing.managers.Team;
import top.lqsnow.blockracing.toolkit.item.ItemBuilder;
import top.lqsnow.blockracing.toolkit.menu.MenuButton;
import top.lqsnow.blockracing.toolkit.menu.MenuView;

import java.util.Set;

import static top.lqsnow.blockracing.listeners.BasicListener.editAmountPlayer;
import static top.lqsnow.blockracing.managers.Gui.updateMenu;
import static top.lqsnow.blockracing.managers.Scoreboard.updateScoreboard;
import static top.lqsnow.blockracing.managers.Team.redTeam;

public final class PreGameMenu extends MenuView {
    private static final Set<Integer> GREEN_BACKGROUND = Set.of(
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44,
            45, 46, 47, 48, 49, 50, 51, 52, 53
    );
    private static final Set<Integer> BLUE_BACKGROUND = Set.of(13, 14, 15, 31, 32, 40, 41, 42);

    public PreGameMenu() {
        super(54, Message.MENU_PREGAME_TITLE.getString());

        setButton(11, MenuButton.of(
                () -> item(Material.RED_WOOL, Message.MENU_JOIN_RED.getString()),
                (player, click) -> Team.joinTeam(player, redTeam, true)
        ));
        setButton(12, MenuButton.of(
                () -> item(Material.BLUE_WOOL, Message.MENU_JOIN_BLUE.getString()),
                (player, click) -> Team.joinTeam(player, Team.blueTeam, true)
        ));
        setButton(20, toggleButton(
                Setting::isEnableMediumBlock,
                Setting::toggleMediumBlock,
                Message.MENU_MEDIUM_BLOCKS
        ));
        setButton(21, toggleButton(
                Setting::isEnableHardBlock,
                Setting::toggleHardBlock,
                Message.MENU_HARD_BLOCKS
        ));
        setButton(22, toggleButton(
                Setting::isEnableDyedBlock,
                Setting::toggleDyedBlock,
                Message.MENU_DYED_BLOCKS
        ));
        setButton(23, toggleButton(
                Setting::isEnableEndBlock,
                Setting::toggleEndBlock,
                Message.MENU_END_BLOCKS
        ));
        setButton(24, MenuButton.of(
                () -> ItemBuilder.of(Material.NAME_TAG)
                        .name(Message.MENU_BLOCK_AMOUNT.getString() + Setting.getBlockAmount())
                        .lore(Message.MENU_BLOCK_AMOUNT_LORE.getStringList())
                        .build(),
                (player, click) -> {
                    player.closeInventory();
                    editAmountPlayer.add(player.getName());
                    player.sendMessage(Message.NOTICE_SET_BLOCKS.getString());
                }
        ));
        setButton(29, MenuButton.of(this::normalModeItem, (player, click) -> {
            Setting.setCurrentGameMode(Setting.GameMode.NORMAL);
            refreshSettings();
        }));
        setButton(30, MenuButton.of(this::racingModeItem, (player, click) -> {
            Setting.setCurrentGameMode(Setting.GameMode.RACING);
            refreshSettings();
        }));
        setButton(33, MenuButton.of(
                () -> ItemBuilder.of(Setting.isSpeedMode() ? Material.GREEN_CONCRETE : Material.YELLOW_CONCRETE)
                        .name(Setting.isSpeedMode()
                                ? Message.MENU_SPEED_MODE_ENABLED.getString()
                                : Message.MENU_SPEED_MODE_DISABLED.getString())
                        .lore(Message.MENU_SPEED_MODE_LORE.getStringList())
                        .build(),
                (player, click) -> {
                    Setting.toggleSpeedMode();
                    refreshSettings();
                }
        ));
        setButton(38, MenuButton.of(
                () -> ItemBuilder.of(Material.EMERALD)
                        .name(Message.MENU_READY.getString())
                        .lore(Message.MENU_READY_LORE.getStringList())
                        .build(),
                (player, click) -> Game.playerReady(player)
        ));
        setButton(39, MenuButton.of(
                () -> ItemBuilder.of(Material.DIAMOND)
                        .name(Message.MENU_START.getString())
                        .lore(Message.MENU_START_LORE.getStringList())
                        .build(),
                (player, click) -> Game.checkStartDemands(player)
        ));
        setButton(41, MenuButton.of(
                () -> ItemBuilder.of(Material.PLAYER_HEAD)
                        .name(Message.MENU_RANDOM_TEAM.getString())
                        .lore(Message.MENU_RANDOM_TEAM_LORE.getStringList())
                        .build(),
                (player, click) -> RandomTeam.requestConfirmation(player)
        ));
    }

    @Override
    protected ItemStack getBackgroundItem(int slot, Player player) {
        if (GREEN_BACKGROUND.contains(slot)) {
            return item(Material.LIME_STAINED_GLASS_PANE, " ");
        }
        if (BLUE_BACKGROUND.contains(slot)) {
            return item(Material.LIGHT_BLUE_STAINED_GLASS_PANE, " ");
        }
        if (slot == 10 || slot == 16) {
            return item(Material.YELLOW_STAINED_GLASS_PANE, Message.MENU_SELECT_TEAM.getString());
        }
        if (slot == 19 || slot == 25) {
            return item(Material.YELLOW_STAINED_GLASS_PANE, Message.MENU_BLOCK_SETTING.getString());
        }
        if (slot == 28 || slot == 34) {
            return item(Material.YELLOW_STAINED_GLASS_PANE, Message.MENU_SELECT_MODE.getString());
        }
        if (slot == 37 || slot == 43) {
            return item(Material.YELLOW_STAINED_GLASS_PANE, Message.MENU_READY_AND_START.getString());
        }
        return null;
    }

    private MenuButton toggleButton(BooleanSupplier enabled, Runnable toggle, Message label) {
        return MenuButton.of(
                () -> item(
                        enabled.getAsBoolean() ? Material.GREEN_CONCRETE : Material.RED_CONCRETE,
                        label.getString() + (enabled.getAsBoolean()
                                ? Message.MENU_ENABLED.getString()
                                : Message.MENU_DISABLED.getString())
                ),
                (player, click) -> {
                    toggle.run();
                    refreshSettings();
                }
        );
    }

    private ItemStack normalModeItem() {
        boolean selected = Setting.getCurrentGameMode() == Setting.GameMode.NORMAL;
        return ItemBuilder.of(selected ? Material.GREEN_CONCRETE : Material.YELLOW_CONCRETE)
                .name((selected ? Message.MENU_CURRENT_MODE : Message.MENU_SWITCH_TO).getString()
                        + Message.MENU_NORMAL_MODE.getString())
                .lore(Message.MENU_NORMAL_MODE_LORE.getStringList())
                .build();
    }

    private ItemStack racingModeItem() {
        boolean selected = Setting.getCurrentGameMode() == Setting.GameMode.RACING;
        return ItemBuilder.of(selected ? Material.GREEN_CONCRETE : Material.YELLOW_CONCRETE)
                .name((selected ? Message.MENU_CURRENT_MODE : Message.MENU_SWITCH_TO).getString()
                        + Message.MENU_RACING_MODE.getString())
                .lore(Message.MENU_RACING_MODE_LORE.getStringList())
                .build();
    }

    private void refreshSettings() {
        updateMenu(this);
        updateScoreboard();
    }

    private static ItemStack item(Material material, String name) {
        return ItemBuilder.of(material).name(name).build();
    }

    @FunctionalInterface
    private interface BooleanSupplier {
        boolean getAsBoolean();
    }
}
