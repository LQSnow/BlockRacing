package top.lqsnow.blockracing.menus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
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
import static top.lqsnow.blockracing.managers.Game.blueTeamScore;
import static top.lqsnow.blockracing.managers.Game.blueWaypoint;
import static top.lqsnow.blockracing.managers.Game.blueWaypointIconCache;
import static top.lqsnow.blockracing.managers.Game.freeRandomTPList;
import static top.lqsnow.blockracing.managers.Game.getCoords;
import static top.lqsnow.blockracing.managers.Game.locateCost;
import static top.lqsnow.blockracing.managers.Game.randomTeleport;
import static top.lqsnow.blockracing.managers.Game.redTeamScore;
import static top.lqsnow.blockracing.managers.Game.redWaypoint;
import static top.lqsnow.blockracing.managers.Game.redWaypointIconCache;
import static top.lqsnow.blockracing.managers.Game.waypoint;
import static top.lqsnow.blockracing.managers.Gui.openTeamChest;
import static top.lqsnow.blockracing.managers.Gui.updateMenu;
import top.lqsnow.blockracing.managers.Message;
import top.lqsnow.blockracing.managers.Scoreboard;
import top.lqsnow.blockracing.managers.Setting;
import static top.lqsnow.blockracing.managers.Team.blueTeamPlayers;
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
                if (redTeamPlayers.contains(player.getName())) new WayPointMenu(redWaypoint,redWaypointIconCache).displayTo(player);
                else if (blueTeamPlayers.contains(player.getName())) new WayPointMenu(blueWaypoint,blueWaypointIconCache).displayTo(player);
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
                    if (redTeamPlayers.contains(player.getName())) {
                        if (redTeamScore < 2) {
                            player.sendMessage(Message.NOTICE_NOT_ENOUGH_SCORE.getString());
                            return;
                        }
                    } else if (blueTeamPlayers.contains(player.getName())) {
                        if (blueTeamScore < 2) {
                            player.sendMessage(Message.NOTICE_NOT_ENOUGH_SCORE.getString());
                            return;
                        }
                    }
                    player.closeInventory();
                    randomTeleport(player, false);
                    if (redTeamPlayers.contains(player.getName())) {
                        redTeamScore -= 2;
                        sendAll(Message.NOTICE_RANDOM_TP.getString().replace("%player%", Message.TEAM_RED_COLOR.getString() + player.getName()));
                    } else if (blueTeamPlayers.contains(player.getName())) {
                        blueTeamScore -= 2;
                        sendAll(Message.NOTICE_RANDOM_TP.getString().replace("%player%", Message.TEAM_BLUE_COLOR.getString() + player.getName()));
                    }
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

        public TeamChestSelectMenu() {
            super(GameMenu.this);

            setTitle(Message.MENU_TEAM_CHEST_SELECT_TITLE.getString());

            int teamChestNum = Setting.getMaxTeamChestNum();
            int teamChestMenuSize = ((teamChestNum) / 9 + 1) * 9;
            setSize(teamChestMenuSize);

            for (int i = 0; i < teamChestNum; i++) {
                Button button = new Button(i) {
                    @Override
                    public void onClickedInMenu(Player player, Menu menu, ClickType click) {
                        openTeamChest(player, this.getSlot());
                    }

                    @Override
                    public ItemStack getItem() {
                        return ItemCreator.of(CompMaterial.CHEST, Message.MENU_TEAM_CHEST_SELECT_CHEST.getString() + (this.getSlot() + 1)).make();
                    }
                };

                this.registerButton(button);
            }

            Button back = new Button(teamChestMenuSize - 1) {
                @Override
                public void onClickedInMenu(Player player, Menu menu, ClickType click) {
                    new GameMenu().displayTo(player);
                }

                @Override
                public ItemStack getItem() {
                    return ItemCreator.of(CompMaterial.ARROW, Message.MENU_ALL_RETURN_BACK.getString()).make();
                }
            };

            this.registerButton(back);

        }

        @Override
        protected boolean addReturnButton() {
            return false;
        }
    }

    public class WayPointMenu extends Menu {
        public WayPointMenu(HashMap<Integer, Location> wayPointMap, HashMap<Integer, Material> wayPointIconCache) {
            super(GameMenu.this);

            setTitle(Message.MENU_WAYPOINT_TITLE.getString());

            int teamWaypointNum = Setting.getMaxTeamWaypointNum();
            int teamWaypointMenuNum = ((teamWaypointNum) / 9 + 1) * 9;
            setSize(teamWaypointMenuNum);

            for (int i = 1; i <= teamWaypointNum; i++) {
                Button button = new Button(i-1) {
                    @Override
                    public void onClickedInMenu(Player player, Menu menu, ClickType click) {
                        boolean isChanged = waypoint(player, this.getSlot() + 1, click); 
                        if (isChanged) {
                            updateMenu(WayPointMenu.this);
                        }
                    }

                    @Override
                    public ItemStack getItem() {
                        int ith = this.getSlot() + 1;
                        Location wayPoint = wayPointMap.get(ith);

                        if (wayPoint != null) {
                            Material icon = wayPointIconCache.get(ith);
                            
                            if(icon == null){
                                Block block = wayPoint.getBlock();
                                while (block.isEmpty()&&block.getY()>-64) {
                                    block = block.getRelative(0, -1, 0);
                                }
                                icon = block.getType();
                                if(block.isEmpty()){
                                    switch(block.getWorld().getEnvironment()){
                                        case NORMAL:icon = Material.GRASS_BLOCK;break;
                                        case NETHER:icon = Material.NETHERRACK;break;
                                        case THE_END:icon = Material.END_STONE;break;
                                        default:icon = Material.FILLED_MAP;break;
                                    }
                                }
                                wayPointIconCache.put(ith,icon);
                            }
                            
                            ItemStack itemStack;
                            try {
                                itemStack = ItemCreator.of(new ItemStack(icon))
                                        .name(Message.MENU_WAYPOINT_FILLED.getString() + ith)
                                        .lore(new ArrayList<>(replacePlaceholders(Message.MENU_WAYPOINT_FILLED_LORE.getStringList(), wayPoint.getWorld().getName(), getCoords(wayPoint), wayPoint.getBlock().getBiome().getKey().getKey())))
                                        .make();
                            } catch (Exception e) {
                                itemStack = ItemCreator.of(CompMaterial.FILLED_MAP, Message.MENU_WAYPOINT_FILLED.getString() + ith, replacePlaceholders(Message.MENU_WAYPOINT_FILLED_LORE.getStringList(), wayPoint.getWorld().getName(), getCoords(wayPoint), wayPoint.getBlock().getBiome().getKey().getKey())).make();
                            }
                            return itemStack;
                        } else {
                            wayPointIconCache.remove(ith);
                            return ItemCreator.of(CompMaterial.MAP, Message.MENU_WAYPOINT_EMPTY.getString() + ith, Message.MENU_WAYPOINT_EMPTY_LORE.getStringList()).make();
                        }
                    }

                };

                this.registerButton(button);
            }

            Button back = new Button(teamWaypointMenuNum - 1) {
                @Override
                public void onClickedInMenu(Player player, Menu menu, ClickType click) {
                    new GameMenu().displayTo(player);
                }

                @Override
                public ItemStack getItem() {
                    return ItemCreator.of(CompMaterial.ARROW, Message.MENU_ALL_RETURN_BACK.getString()).make();
                }
            };
            this.registerButton(back);
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

    private Collection<String> replacePlaceholders(Collection<String> lore, String dimension, String coords, String biome) {
        List<String> modifiedLore = new ArrayList<>();

        for (String line : lore) {
            line = line
                    .replace("%dimension%", dimension)
                    .replace("%coords%", coords)
                    .replace("%biome%", biome);

            modifiedLore.add(line);
        }
        return modifiedLore;
    }


}
