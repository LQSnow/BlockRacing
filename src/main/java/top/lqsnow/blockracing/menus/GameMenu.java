package top.lqsnow.blockracing.menus;

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static top.lqsnow.blockracing.managers.Game.*;
import static top.lqsnow.blockracing.managers.Gui.*;
import static top.lqsnow.blockracing.managers.Team.*;
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
                return ItemCreator.of(CompMaterial.TOTEM_OF_UNDYING, Message.MENU_ROLL.getString(), Message.MENU_ROLL_LORE.getString()).make();
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
                return ItemCreator.of(CompMaterial.COMPASS, Message.MENU_LOCATE.getString(), replacePlaceholders(Message.MENU_LOCATE_LORE.getStringList())).make();
            }
        };

        // Open waypoint menu
        this.waypoint = new Button() {
            @Override
            public void onClickedInMenu(Player player, Menu menu, ClickType click) {
                if (redTeamPlayers.contains(player.getName())) new RedWaypointMenu().displayTo(player);
                else if (blueTeamPlayers.contains(player.getName())) new BlueWaypointMenu().displayTo(player);
            }

            @Override
            public ItemStack getItem() {
                return ItemCreator.of(CompMaterial.PAPER, Message.MENU_WAYPOINTS.getString(), Message.MENU_WAYPOINTS_LORE.getStringList()).make();
            }
        };

        // Random tp
        this.randomTP = new Button() {
            @Override
            public void onClickedInMenu(Player player, Menu menu, ClickType click) {
                if (freeRandomTPList.contains(player.getName())) {
                    Game.randomTeleport(player, false);
                    freeRandomTPList.remove(player.getName());
                } else {
                    if (redTeamScore < 2) {
                        player.sendMessage(Message.NOTICE_NOT_ENOUGH_SCORE.getString());
                        return;
                    }
                    player.closeInventory();
                    randomTeleport(player, false);
                    sendAll(Message.NOTICE_RANDOM_TP.getString().replace("%player%", Message.TEAM_RED_COLOR.getString() + player.getName()));
                    redTeamScore -= 2;
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
    public class TeamChestSelectMenu extends Menu {
        @Position(0)
        private final Button teamChest1;

        @Position(1)
        private final Button teamChest2;

        @Position(2)
        private final Button teamChest3;

        @Position(8)
        private final Button back;

        public TeamChestSelectMenu() {
            super(GameMenu.this);

            setTitle(Message.MENU_TEAM_CHEST_SELECT_TITLE.getString());
            setSize(1 * 9);

            this.teamChest1 = new Button() {
                @Override
                public void onClickedInMenu(Player player, Menu menu, ClickType click) {
                    openTeamChest(player, 1);
                }

                @Override
                public ItemStack getItem() {
                    return ItemCreator.of(CompMaterial.CHEST, Message.MENU_TEAM_CHEST_SELECT_CHEST1.getString()).make();
                }
            };

            this.teamChest2 = new Button() {
                @Override
                public void onClickedInMenu(Player player, Menu menu, ClickType click) {
                    openTeamChest(player, 2);
                }

                @Override
                public ItemStack getItem() {
                    return ItemCreator.of(CompMaterial.CHEST, Message.MENU_TEAM_CHEST_SELECT_CHEST2.getString()).make();
                }
            };

            this.teamChest3 = new Button() {
                @Override
                public void onClickedInMenu(Player player, Menu menu, ClickType click) {
                    openTeamChest(player, 3);
                }

                @Override
                public ItemStack getItem() {
                    return ItemCreator.of(CompMaterial.CHEST, Message.MENU_TEAM_CHEST_SELECT_CHEST3.getString()).make();
                }
            };

            this.back = new Button() {
                @Override
                public void onClickedInMenu(Player player, Menu menu, ClickType click) {
                    new GameMenu().displayTo(player);
                }

                @Override
                public ItemStack getItem() {
                    return ItemCreator.of(CompMaterial.ARROW, Message.MENU_ALL_RETURN_BACK.getString()).make();
                }
            };

        }

        @Override
        protected boolean addReturnButton() {
            return false;
        }
    }

    // Waypoint menu
    public class RedWaypointMenu extends Menu {

        @Position(0)
        private final Button waypoint1;

        @Position(1)
        private final Button waypoint2;

        @Position(2)
        private final Button waypoint3;

        @Position(8)
        private final Button back;

        public RedWaypointMenu() {
            super(GameMenu.this);

            setTitle(Message.MENU_WAYPOINT_TITLE.getString());
            setSize(1 * 9);

            this.waypoint1 = new Button() {
                @Override
                public void onClickedInMenu(Player player, Menu menu, ClickType click) {
                    boolean isChanged = waypoint(player, 1, click);
                    if (isChanged) {
                        updateMenu(RedWaypointMenu.this);
                    }
                }

                @Override
                public ItemStack getItem() {
                    if (redWaypoint1 != null)
                        return ItemCreator.of(CompMaterial.FILLED_MAP, Message.MENU_WAYPOINT_FILLED_1.getString(), replacePlaceholders(Message.MENU_WAYPOINT_FILLED_LORE.getStringList(), redWaypoint1.getWorld().getName(), getCoords(redWaypoint1))).make();
                    else
                        return ItemCreator.of(CompMaterial.MAP, Message.MENU_WAYPOINT_EMPTY_1.getString(), Message.MENU_WAYPOINT_EMPTY_LORE.getStringList()).make();
                }
            };

            this.waypoint2 = new Button() {
                @Override
                public void onClickedInMenu(Player player, Menu menu, ClickType click) {
                    boolean isChanged = waypoint(player, 2, click);
                    if (isChanged) {
                        updateMenu(RedWaypointMenu.this);
                    }
                }

                @Override
                public ItemStack getItem() {
                    if (redWaypoint2 != null)
                        return ItemCreator.of(CompMaterial.FILLED_MAP, Message.MENU_WAYPOINT_FILLED_2.getString(), replacePlaceholders(Message.MENU_WAYPOINT_FILLED_LORE.getStringList(), redWaypoint2.getWorld().getName(), getCoords(redWaypoint2))).make();
                    else
                        return ItemCreator.of(CompMaterial.MAP, Message.MENU_WAYPOINT_EMPTY_2.getString(), Message.MENU_WAYPOINT_EMPTY_LORE.getStringList()).make();
                }
            };

            this.waypoint3 = new Button() {
                @Override
                public void onClickedInMenu(Player player, Menu menu, ClickType click) {
                    boolean isChanged = waypoint(player, 3, click);
                    if (isChanged) {
                        updateMenu(RedWaypointMenu.this);
                    }
                }

                @Override
                public ItemStack getItem() {
                    if (redWaypoint3 != null)
                        return ItemCreator.of(CompMaterial.FILLED_MAP, Message.MENU_WAYPOINT_FILLED_3.getString(), replacePlaceholders(Message.MENU_WAYPOINT_FILLED_LORE.getStringList(), redWaypoint3.getWorld().getName(), getCoords(redWaypoint3))).make();
                    else
                        return ItemCreator.of(CompMaterial.MAP, Message.MENU_WAYPOINT_EMPTY_3.getString(), Message.MENU_WAYPOINT_EMPTY_LORE.getStringList()).make();
                }
            };

            this.back = new Button() {
                @Override
                public void onClickedInMenu(Player player, Menu menu, ClickType click) {
                    new GameMenu().displayTo(player);
                }

                @Override
                public ItemStack getItem() {
                    return ItemCreator.of(CompMaterial.ARROW, Message.MENU_ALL_RETURN_BACK.getString()).make();
                }
            };
        }

        @Override
        protected boolean addReturnButton() {
            return false;
        }

    }

    public class BlueWaypointMenu extends Menu {

        @Position(0)
        private final Button waypoint1;

        @Position(1)
        private final Button waypoint2;

        @Position(2)
        private final Button waypoint3;

        @Position(8)
        private final Button back;

        public BlueWaypointMenu() {
            super(GameMenu.this);

            setTitle(Message.MENU_WAYPOINT_TITLE.getString());
            setSize(1 * 9);

            this.waypoint1 = new Button() {
                @Override
                public void onClickedInMenu(Player player, Menu menu, ClickType click) {
                    boolean isChanged = waypoint(player, 1, click);
                    if (isChanged) {
                        updateMenu(BlueWaypointMenu.this);
                    }
                }

                @Override
                public ItemStack getItem() {
                    if (blueWaypoint1 != null)
                        return ItemCreator.of(CompMaterial.FILLED_MAP, Message.MENU_WAYPOINT_FILLED_1.getString(), replacePlaceholders(Message.MENU_WAYPOINT_FILLED_LORE.getStringList(), blueWaypoint1.getWorld().getName(), getCoords(blueWaypoint1))).make();
                    else
                        return ItemCreator.of(CompMaterial.MAP, Message.MENU_WAYPOINT_EMPTY_1.getString(), Message.MENU_WAYPOINT_EMPTY_LORE.getStringList()).make();
                }
            };

            this.waypoint2 = new Button() {
                @Override
                public void onClickedInMenu(Player player, Menu menu, ClickType click) {
                    boolean isChanged = waypoint(player, 2, click);
                    if (isChanged) {
                        updateMenu(BlueWaypointMenu.this);
                    }
                }

                @Override
                public ItemStack getItem() {
                    if (blueWaypoint2 != null)
                        return ItemCreator.of(CompMaterial.FILLED_MAP, Message.MENU_WAYPOINT_FILLED_2.getString(), replacePlaceholders(Message.MENU_WAYPOINT_FILLED_LORE.getStringList(), blueWaypoint2.getWorld().getName(), getCoords(blueWaypoint2))).make();
                    else
                        return ItemCreator.of(CompMaterial.MAP, Message.MENU_WAYPOINT_EMPTY_2.getString(), Message.MENU_WAYPOINT_EMPTY_LORE.getStringList()).make();
                }
            };

            this.waypoint3 = new Button() {
                @Override
                public void onClickedInMenu(Player player, Menu menu, ClickType click) {
                    boolean isChanged = waypoint(player, 3, click);
                    if (isChanged) {
                        updateMenu(BlueWaypointMenu.this);
                    }
                }

                @Override
                public ItemStack getItem() {
                    if (blueWaypoint3 != null)
                        return ItemCreator.of(CompMaterial.FILLED_MAP, Message.MENU_WAYPOINT_FILLED_3.getString(), replacePlaceholders(Message.MENU_WAYPOINT_FILLED_LORE.getStringList(), blueWaypoint3.getWorld().getName(), getCoords(blueWaypoint3))).make();
                    else
                        return ItemCreator.of(CompMaterial.MAP, Message.MENU_WAYPOINT_EMPTY_3.getString(), Message.MENU_WAYPOINT_EMPTY_LORE.getStringList()).make();
                }
            };

            this.back = new Button() {
                @Override
                public void onClickedInMenu(Player player, Menu menu, ClickType click) {
                    new GameMenu().displayTo(player);
                }

                @Override
                public ItemStack getItem() {
                    return ItemCreator.of(CompMaterial.ARROW, Message.MENU_ALL_RETURN_BACK.getString()).make();
                }
            };
        }

        @Override
        protected boolean addReturnButton() {
            return false;
        }
    }

    private Collection<String> replacePlaceholders(Collection<String> lore) {
        List<String> modifiedLore = new ArrayList<>();

        for (String line : lore) {
            line = line.replace("%score%", String.valueOf(locateCost));

            modifiedLore.add(line);
        }
        return modifiedLore;
    }

    private Collection<String> replacePlaceholders(Collection<String> lore, String dimension, String coords) {
        List<String> modifiedLore = new ArrayList<>();

        for (String line : lore) {
            line = line.replace("%dimension%", dimension).replace("%coords%", coords);

            modifiedLore.add(line);
        }
        return modifiedLore;
    }




}
