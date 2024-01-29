package top.lqsnow.blockracing.menus;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.fo.menu.button.Button;
import org.mineacademy.fo.menu.button.annotation.Position;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.remain.CompMaterial;
import top.lqsnow.blockracing.managers.Game;
import top.lqsnow.blockracing.managers.Message;
import top.lqsnow.blockracing.managers.Setting;
import top.lqsnow.blockracing.managers.Team;

import static top.lqsnow.blockracing.listeners.BasicListener.editAmountPlayer;
import static top.lqsnow.blockracing.managers.Scoreboard.updateScoreboard;
import static top.lqsnow.blockracing.managers.Team.redTeam;
import static top.lqsnow.blockracing.utils.CommandUtil.sendAll;

public class PreGameMenu extends Menu {

    // Define buttons
    @Position(11)
    private final Button joinRedButton;

    @Position(12)
    private final Button joinBlueButton;

    @Position(20)
    private final Button mediumBlock;

    @Position(21)
    private final Button hardBlock;

    @Position(22)
    private final Button dyedBlock;

    @Position(23)
    private final Button endBlock;

    @Position(24)
    private final Button changeBlockAmount;

    @Position(29)
    private final Button normalMode;

    @Position(30)
    private final Button racingMode;

    @Position(33)
    private final Button speedMode;

    @Position(38)
    private final Button ready;

    @Position(39)
    private final Button start;

    public PreGameMenu() {
        setTitle(Message.MENU_PREGAME_TITLE.getString());
        setSize(6 * 9);

        // Join red team
        this.joinRedButton = new Button() {
            @Override
            public void onClickedInMenu(Player player, Menu menu, ClickType click) {
                Team.joinTeam(player, redTeam);
                sendAll(Message.NOTICE_JOIN_RED.getString().replace("%player%", player.getName()));
            }

            @Override
            public ItemStack getItem() {
                return ItemCreator.of(CompMaterial.RED_WOOL, Message.MENU_JOIN_RED.getString()).make();
            }
        };

        // Join blue team
        this.joinBlueButton = new Button() {
            @Override
            public void onClickedInMenu(Player player, Menu menu, ClickType click) {
                Team.joinTeam(player, Team.blueTeam);
                sendAll(Message.NOTICE_JOIN_BLUE.getString().replace("%player%", player.getName()));
            }

            @Override
            public ItemStack getItem() {
                return ItemCreator.of(CompMaterial.BLUE_WOOL, Message.MENU_JOIN_BLUE.getString()).make();
            }
        };

        // Toggle medium block
        this.mediumBlock = new Button() {
            @Override
            public void onClickedInMenu(Player player, Menu menu, ClickType click) {
                Setting.toggleMediumBlock();
                updateMenu(PreGameMenu.this);
                updateScoreboard();
            }

            @Override
            public ItemStack getItem() {
                if (Setting.isEnableMediumBlock()) return ItemCreator.of(CompMaterial.GREEN_CONCRETE,
                        Message.MENU_MEDIUM_BLOCKS.getString() + Message.MENU_ENABLED.getString()).make();
                else return ItemCreator.of(CompMaterial.RED_CONCRETE,
                        Message.MENU_MEDIUM_BLOCKS.getString() + Message.MENU_DISABLED.getString()).make();
            }
        };

        // Toggle hard block
        this.hardBlock = new Button() {
            @Override
            public void onClickedInMenu(Player player, Menu menu, ClickType click) {
                Setting.toggleHardBlock();
                updateMenu(PreGameMenu.this);
                updateScoreboard();
            }

            @Override
            public ItemStack getItem() {
                if (Setting.isEnableHardBlock()) return ItemCreator.of(CompMaterial.GREEN_CONCRETE,
                        Message.MENU_HARD_BLOCKS.getString() + Message.MENU_ENABLED.getString()).make();
                else return ItemCreator.of(CompMaterial.RED_CONCRETE,
                        Message.MENU_HARD_BLOCKS.getString() + Message.MENU_DISABLED.getString()).make();
            }
        };

        // Toggle dyed block
        this.dyedBlock = new Button() {
            @Override
            public void onClickedInMenu(Player player, Menu menu, ClickType click) {
                Setting.toggleDyedBlock();
                updateMenu(PreGameMenu.this);
                updateScoreboard();
            }

            @Override
            public ItemStack getItem() {
                if (Setting.isEnableDyedBlock()) return ItemCreator.of(CompMaterial.GREEN_CONCRETE,
                        Message.MENU_DYED_BLOCKS.getString() + Message.MENU_ENABLED.getString()).make();
                else return ItemCreator.of(CompMaterial.RED_CONCRETE,
                        Message.MENU_DYED_BLOCKS.getString() + Message.MENU_DISABLED.getString()).make();
            }
        };

        // Toggle end block
        this.endBlock = new Button() {
            @Override
            public void onClickedInMenu(Player player, Menu menu, ClickType click) {
                Setting.toggleEndBlock();
                updateMenu(PreGameMenu.this);
                updateScoreboard();
            }

            @Override
            public ItemStack getItem() {
                if (Setting.isEnableEndBlock()) return ItemCreator.of(CompMaterial.GREEN_CONCRETE,
                        Message.MENU_END_BLOCKS.getString() + Message.MENU_ENABLED.getString()).make();
                else return ItemCreator.of(CompMaterial.RED_CONCRETE,
                        Message.MENU_END_BLOCKS.getString() + Message.MENU_DISABLED.getString()).make();
            }
        };

        // Change block amount
        this.changeBlockAmount = new Button() {
            @Override
            public void onClickedInMenu(Player player, Menu menu, ClickType click) {
                player.closeInventory();
                editAmountPlayer.add(player.getName());
                player.sendMessage(Message.NOTICE_SET_BLOCKS.getString());
            }

            @Override
            public ItemStack getItem() {
                return ItemCreator.of(CompMaterial.NAME_TAG, Message.MENU_BLOCK_AMOUNT.getString() + Setting.getBlockAmount(),
                        Message.MENU_BLOCK_AMOUNT_LORE.getStringList()).make();
            }
        };

        // Switch to normal mode
        this.normalMode = new Button() {
            @Override
            public void onClickedInMenu(Player player, Menu menu, ClickType click) {
                if (Setting.getCurrentGameMode().equals(Setting.GameMode.RACING))
                    Setting.setCurrentGameMode(Setting.GameMode.NORMAL);
                updateMenu(PreGameMenu.this);
                updateScoreboard();
            }

            @Override
            public ItemStack getItem() {
                if (Setting.getCurrentGameMode().equals(Setting.GameMode.NORMAL)) {
                    return ItemCreator.of(CompMaterial.GREEN_CONCRETE, Message.MENU_CURRENT_MODE.getString() + Message.MENU_NORMAL_MODE.getString(), Message.MENU_NORMAL_MODE_LORE.getStringList()).make();
                } else if (Setting.getCurrentGameMode().equals(Setting.GameMode.RACING)) {
                    return ItemCreator.of(CompMaterial.YELLOW_CONCRETE, Message.MENU_SWITCH_TO.getString() + Message.MENU_NORMAL_MODE.getString(), Message.MENU_NORMAL_MODE_LORE.getStringList()).make();
                }
                return NO_ITEM;
            }
        };

        // Switch to racing mode
        this.racingMode = new Button() {
            @Override
            public void onClickedInMenu(Player player, Menu menu, ClickType click) {
                if (Setting.getCurrentGameMode().equals(Setting.GameMode.NORMAL))
                    Setting.setCurrentGameMode(Setting.GameMode.RACING);
                updateMenu(PreGameMenu.this);
                updateScoreboard();
            }

            @Override
            public ItemStack getItem() {
                if (Setting.getCurrentGameMode().equals(Setting.GameMode.NORMAL)) {
                    return ItemCreator.of(CompMaterial.YELLOW_CONCRETE, Message.MENU_SWITCH_TO.getString() + Message.MENU_RACING_MODE.getString(), Message.MENU_RACING_MODE_LORE.getStringList()).make();
                } else if (Setting.getCurrentGameMode().equals(Setting.GameMode.RACING)) {
                    return ItemCreator.of(CompMaterial.GREEN_CONCRETE, Message.MENU_CURRENT_MODE.getString() + Message.MENU_RACING_MODE.getString(), Message.MENU_RACING_MODE_LORE.getStringList()).make();
                }
                return NO_ITEM;
            }

        };

        // Toggle speed mode
        this.speedMode = new Button() {
            @Override
            public void onClickedInMenu(Player player, Menu menu, ClickType click) {
                Setting.toggleSpeedMode();
                updateMenu(PreGameMenu.this);
                updateScoreboard();
            }

            @Override
            public ItemStack getItem() {
                if (Setting.isSpeedMode()) return ItemCreator.of(CompMaterial.GREEN_CONCRETE,
                        Message.MENU_SPEED_MODE_ENABLED.getString(), Message.MENU_SPEED_MODE_LORE.getStringList()).make();
                else return ItemCreator.of(CompMaterial.YELLOW_CONCRETE,
                        Message.MENU_SPEED_MODE_DISABLED.getString(), Message.MENU_SPEED_MODE_LORE.getStringList()).make();
            }
        };

        // Ready
        this.ready = new Button() {
            @Override
            public void onClickedInMenu(Player player, Menu menu, ClickType click) {
                Game.playerReady(player);
            }

            @Override
            public ItemStack getItem() {
                return ItemCreator.of(CompMaterial.EMERALD, Message.MENU_READY.getString(), Message.MENU_READY_LORE.getStringList()).make();
            }
        };

        // Start
        this.start = new Button() {
            @Override
            public void onClickedInMenu(Player player, Menu menu, ClickType click) {
                Game.checkStartDemands(player);
            }

            @Override
            public ItemStack getItem() {
                return ItemCreator.of(CompMaterial.DIAMOND, Message.MENU_START.getString(), Message.MENU_START_LORE.getStringList()).make();
            }
        };

    }

    // Update menu
    public static void updateMenu(Menu menu) {
        Bukkit.getOnlinePlayers().forEach((Player player) -> {
            try {
                Menu playerMenu = Menu.getMenu(player);
                if (playerMenu.getClass().equals(menu.getClass())) {
                    playerMenu.restartMenu();
                }
            } catch (NullPointerException ignored) {}
        });
    }

    // Generate background glass pane
    @Override
    public ItemStack getItemAt(int slot) {
        if (isGreenBackgroundSlot(slot)) {
            return ItemCreator.of(CompMaterial.LIME_STAINED_GLASS_PANE, " ").make();
        }
        if (isBlueBackgroundSlot(slot)) {
            return ItemCreator.of(CompMaterial.LIGHT_BLUE_STAINED_GLASS_PANE, " ").make();
        }
        if (slot == 10 || slot == 16) {
            return ItemCreator.of(CompMaterial.YELLOW_STAINED_GLASS_PANE, Message.MENU_SELECT_TEAM.getString()).make();
        }
        if (slot == 19 || slot == 25) {
            return ItemCreator.of(CompMaterial.YELLOW_STAINED_GLASS_PANE, Message.MENU_BLOCK_SETTING.getString()).make();
        }
        if (slot == 28 || slot == 34) {
            return ItemCreator.of(CompMaterial.YELLOW_STAINED_GLASS_PANE, Message.MENU_SELECT_MODE.getString()).make();
        }
        if (slot == 37 || slot == 43) {
            return ItemCreator.of(CompMaterial.YELLOW_STAINED_GLASS_PANE, Message.MENU_READY_AND_START.getString()).make();
        }

        return super.getItemAt(slot);
    }

    private boolean isGreenBackgroundSlot(int slot) {
        int[] validSlots = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53};

        for (int validSlot : validSlots) {
            if (validSlot == slot) {
                return true;
            }
        }

        return false;
    }

    private boolean isBlueBackgroundSlot(int slot) {
        int[] validSlots = {13, 14, 15, 31, 32, 40, 41, 42};

        for (int validSlot : validSlots) {
            if (validSlot == slot) {
                return true;
            }
        }

        return false;
    }
}
