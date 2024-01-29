package top.lqsnow.blockracing.menus;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.fo.menu.button.Button;
import org.mineacademy.fo.menu.button.ButtonMenu;
import org.mineacademy.fo.menu.button.annotation.Position;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.remain.CompMaterial;
import top.lqsnow.blockracing.managers.Game;
import top.lqsnow.blockracing.managers.Message;
import top.lqsnow.blockracing.managers.Scoreboard;

import static top.lqsnow.blockracing.managers.Game.*;
import static top.lqsnow.blockracing.managers.Gui.*;
import static top.lqsnow.blockracing.managers.Team.redTeamPlayers;
import static top.lqsnow.blockracing.utils.CommandUtil.sendAll;

public class GameMenu extends Menu {

    @Position(0)
    private final Button teamChest;

    @Position(2)
    private final Button roll;

    @Position(4)
    private final Button locate;

    @Position(6)
    private final Button waypoint;

    @Position(8)
    private final Button randomTP;

    public GameMenu() {
        setTitle(Message.MENU_GAME_TITLE.getString());
        setSize(1 * 9);

        // Open team chest menu
        this.teamChest = new ButtonMenu(new TeamChestSelectMenu(), ItemCreator.of(CompMaterial.CHEST, Message.MENU_TEAM_CHEST.getString(), Message.MENU_TEAM_CHEST_LORE.getStringList()).make());

        // Roll
        this.roll = new Button() {
            @Override
            public void onClickedInMenu(Player player, Menu menu, ClickType click) {
                Game.roll(player);
                player.closeInventory();
            }

            @Override
            public ItemStack getItem() {
                return ItemCreator.of(CompMaterial.TOTEM_OF_UNDYING, Message.MENU_ROLL.getString(), Message.MENU_ROLL_LORE.getStringList()).make();
            }
        };

        // Locate
        this.locate = new Button() {
            @Override
            public void onClickedInMenu(Player player, Menu menu, ClickType click) {
                Game.locate(player);
                player.closeInventory();
            }

            @Override
            public ItemStack getItem() {
                return ItemCreator.of(CompMaterial.COMPASS, Message.MENU_LOCATE.getString(), Message.MENU_LOCATE_LORE.getStringList()).make();
            }
        };

        // Open waypoint menu
        this.waypoint = new ButtonMenu(new WaypointMenu(), ItemCreator.of(CompMaterial.PAPER, Message.MENU_WAYPOINTS.getString(), Message.MENU_WAYPOINTS_LORE.getStringList()).make());

        // Random tp
        this.randomTP = new Button() {
            @Override
            public void onClickedInMenu(Player player, Menu menu, ClickType click) {
                if (freeRandomTPList.contains(player.getName())) {
                    Game.randomTP(player, false);
                    freeRandomTPList.remove(player.getName());
                } else {
                    if (redTeamPlayers.contains(player.getName())) {
                        if (redTeamScore < 2){
                            player.sendMessage(Message.NOTICE_NOT_ENOUGH_SCORE.getString());
                            return;
                        }
                    } else {
                        if (blueTeamScore < 2){
                            player.sendMessage(Message.NOTICE_NOT_ENOUGH_SCORE.getString());
                            return;
                        }
                    }
                    player.closeInventory();
                    randomTP(player, false);
                    sendAll(Message.NOTICE_RANDOM_TP.getString().replace("%player%", redTeamPlayers.contains(player.getName()) ? Message.TEAM_RED_COLOR.getString() + player.getName() : Message.TEAM_BLUE_COLOR.getString() + player.getName()));
                    if (redTeamPlayers.contains(player)) redTeamScore -= 2;
                    else blueTeamScore -= 2;
                    Scoreboard.updateScoreboard();
                }
            }

            @Override
            public ItemStack getItem() {
                return ItemCreator.of(CompMaterial.ENDER_PEARL, Message.MENU_RANDOM_TP.getString(), Message.MENU_RANDOM_TP_LORE.getStringList()).make();
            }
        };

    }

    // Team chest select menu
    private class TeamChestSelectMenu extends Menu {
        @Position(0)
        private final Button teamChest1;

        @Position(1)
        private final Button teamChest2;

        @Position(2)
        private final Button teamChest3;

        TeamChestSelectMenu() {
            super(GameMenu.this);

            setTitle(Message.MENU_TEAM_CHEST_SELECT_TITLE.getString());
            setSize(1 * 9);

            this.teamChest1 = new Button() {
                @Override
                public void onClickedInMenu(Player player, Menu menu, ClickType click) {
                    player.openInventory(redTeamChest1);
                }

                @Override
                public ItemStack getItem() {
                    return ItemCreator.of(CompMaterial.CHEST, Message.MENU_TEAM_CHEST_SELECT_CHEST1.getString()).make();
                }
            };

            this.teamChest2 = new Button() {
                @Override
                public void onClickedInMenu(Player player, Menu menu, ClickType click) {
                    player.openInventory(redTeamChest2);
                }

                @Override
                public ItemStack getItem() {
                    return ItemCreator.of(CompMaterial.CHEST, Message.MENU_TEAM_CHEST_SELECT_CHEST2.getString()).make();
                }
            };

            this.teamChest3 = new Button() {
                @Override
                public void onClickedInMenu(Player player, Menu menu, ClickType click) {
                    player.openInventory(redTeamChest3);
                }

                @Override
                public ItemStack getItem() {
                    return ItemCreator.of(CompMaterial.CHEST, Message.MENU_TEAM_CHEST_SELECT_CHEST3.getString()).make();
                }
            };
        }

    }

    // Waypoint menu
    private class WaypointMenu extends Menu {

        @Position(0)
        private final Button waypoint1;
        // TODO
//        @Position(1)
//        private final Button waypoint2;
//
//        @Position(2)
//        private final Button waypoint3;

        WaypointMenu() {
            super(GameMenu.this);

            setTitle(Message.MENU_WAYPOINT_TITLE.getString());
            setSize(1 * 9);

            this.waypoint1 = new Button() {
                @Override
                public void onClickedInMenu(Player player, Menu menu, ClickType click) {
                    // TODO
                }

                @Override
                public ItemStack getItem() {
                    // TODO
                    return NO_ITEM;
                }
            };
        }
    }
}
