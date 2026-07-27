package top.lqsnow.blockracing.menus;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import top.lqsnow.blockracing.managers.Game;
import top.lqsnow.blockracing.managers.Message;
import top.lqsnow.blockracing.managers.Scoreboard;
import top.lqsnow.blockracing.managers.Setting;
import top.lqsnow.blockracing.toolkit.item.ItemBuilder;
import top.lqsnow.blockracing.toolkit.menu.MenuButton;
import top.lqsnow.blockracing.toolkit.menu.MenuView;

import java.util.HashMap;
import java.util.List;

import static top.lqsnow.blockracing.managers.Game.*;
import static top.lqsnow.blockracing.managers.Gui.openTeamChest;
import static top.lqsnow.blockracing.managers.Gui.updateMenu;
import static top.lqsnow.blockracing.managers.Team.blueTeamPlayers;
import static top.lqsnow.blockracing.managers.Team.redTeamPlayers;
import static top.lqsnow.blockracing.utils.CommandUtil.sendAll;

public final class GameMenu extends MenuView {
    public GameMenu() {
        super(9, player -> Message.MENU_GAME_TITLE.getString(player));

        setButton(0, MenuButton.of(
                player -> ItemBuilder.of(Material.CHEST)
                        .name(Message.MENU_TEAM_CHEST.getString(player))
                        .lore(Message.MENU_TEAM_CHEST_LORE.getStringList(player))
                        .build(),
                (player, click) -> new TeamChestSelectMenu().open(player)
        ));
        setButton(2, MenuButton.of(
                player -> ItemBuilder.of(Material.TOTEM_OF_UNDYING)
                        .name(Message.MENU_ROLL.getString(player))
                        .lore(List.of(Message.MENU_ROLL_LORE.getString(player)))
                        .build(),
                (player, click) -> {
                    Game.roll(player);
                    player.closeInventory();
                }
        ));
        setButton(4, MenuButton.of(
                player -> ItemBuilder.of(Material.COMPASS)
                        .name(Message.MENU_LOCATE.getString(player))
                        .lore(replaceScorePlaceholder(Message.MENU_LOCATE_LORE.getStringList(player)))
                        .build(),
                (player, click) -> {
                    Game.locate(player);
                    player.closeInventory();
                }
        ));
        setButton(6, MenuButton.of(
                player -> ItemBuilder.of(Material.PAPER)
                        .name(Message.MENU_WAYPOINTS.getString(player))
                        .lore(Message.MENU_WAYPOINTS_LORE.getStringList(player))
                        .build(),
                (player, click) -> {
                    if (redTeamPlayers.contains(player.getName())) {
                        new WayPointMenu(redWaypoint, redWaypointIconCache).open(player);
                    } else if (blueTeamPlayers.contains(player.getName())) {
                        new WayPointMenu(blueWaypoint, blueWaypointIconCache).open(player);
                    }
                }
        ));
        setButton(8, MenuButton.of(
                player -> ItemBuilder.of(Material.ENDER_PEARL)
                        .name(Message.MENU_RANDOM_TP.getString(player))
                        .lore(Message.MENU_RANDOM_TP_LORE.getStringList(player))
                        .build(),
                (player, click) -> handleRandomTeleport(player)
        ));
        setButton(1, MenuButton.of(
                () -> ItemBuilder.of(Material.KNOWLEDGE_BOOK).name("§bLanguage / 语言").build(),
                (player, click) -> new LanguageMenu().open(player)
        ));
    }

    private void handleRandomTeleport(Player player) {
        if (freeRandomTPList.remove(player.getName())) {
            Game.randomTeleport(player, false);
            return;
        }

        if (redTeamPlayers.contains(player.getName()) && redTeamScore < 2
                || blueTeamPlayers.contains(player.getName()) && blueTeamScore < 2) {
            player.sendMessage(Message.NOTICE_NOT_ENOUGH_SCORE.getString(player));
            return;
        }

        player.closeInventory();
        randomTeleport(player, false);
        if (redTeamPlayers.contains(player.getName())) {
            redTeamScore -= 2;
            sendAll(Message.NOTICE_RANDOM_TP,
                    (viewer, text) -> text.replace("%player%",
                            Message.TEAM_RED_COLOR.getString(viewer) + player.getName()));
        } else if (blueTeamPlayers.contains(player.getName())) {
            blueTeamScore -= 2;
            sendAll(Message.NOTICE_RANDOM_TP,
                    (viewer, text) -> text.replace("%player%",
                            Message.TEAM_BLUE_COLOR.getString(viewer) + player.getName()));
        }
        Scoreboard.updateScoreboard();
    }

    public static final class TeamChestSelectMenu extends MenuView {
        public TeamChestSelectMenu() {
            super(menuSize(Setting.getMaxTeamChestNum()),
                    player -> Message.MENU_TEAM_CHEST_SELECT_TITLE.getString(player));

            for (int slot = 0; slot < Setting.getMaxTeamChestNum(); slot++) {
                int chestIndex = slot;
                setButton(slot, MenuButton.of(
                        player -> ItemBuilder.of(Material.CHEST)
                                .name(Message.MENU_TEAM_CHEST_SELECT_CHEST.getString(player) + (chestIndex + 1))
                                .build(),
                        (player, click) -> openTeamChest(player, chestIndex)
                ));
            }
            setButton(getSize() - 1, backButton());
        }
    }

    public static final class WayPointMenu extends MenuView {
        private final HashMap<Integer, Location> waypoints;
        private final HashMap<Integer, Material> iconCache;

        public WayPointMenu(HashMap<Integer, Location> waypoints, HashMap<Integer, Material> iconCache) {
            super(menuSize(Setting.getMaxTeamWaypointNum()),
                    player -> Message.MENU_WAYPOINT_TITLE.getString(player));
            this.waypoints = waypoints;
            this.iconCache = iconCache;

            for (int slot = 0; slot < Setting.getMaxTeamWaypointNum(); slot++) {
                int index = slot + 1;
                setButton(slot, MenuButton.of(
                        player -> createWaypointItem(player, index),
                        (player, click) -> {
                            if (waypoint(player, index, click)) {
                                updateMenu(this);
                            }
                        }
                ));
            }
            setButton(getSize() - 1, backButton());
        }

        private ItemStack createWaypointItem(Player player, int index) {
            Location waypoint = waypoints.get(index);
            if (waypoint == null) {
                iconCache.remove(index);
                return ItemBuilder.of(Material.MAP)
                        .name(Message.MENU_WAYPOINT_EMPTY.getString(player) + index)
                        .lore(Message.MENU_WAYPOINT_EMPTY_LORE.getStringList(player))
                        .build();
            }

            Material icon = iconCache.computeIfAbsent(index, ignored -> findWaypointIcon(waypoint));
            try {
                return ItemBuilder.of(icon)
                        .name(Message.MENU_WAYPOINT_FILLED.getString(player) + index)
                        .lore(replaceWaypointPlaceholders(
                                Message.MENU_WAYPOINT_FILLED_LORE.getStringList(player),
                                waypoint.getWorld().getName(),
                                getCoords(waypoint),
                                waypoint.getBlock().getBiome().getKey().getKey()
                        ))
                        .build();
            } catch (IllegalArgumentException ex) {
                iconCache.put(index, Material.FILLED_MAP);
                return ItemBuilder.of(Material.FILLED_MAP)
                        .name(Message.MENU_WAYPOINT_FILLED.getString(player) + index)
                        .lore(replaceWaypointPlaceholders(
                                Message.MENU_WAYPOINT_FILLED_LORE.getStringList(player),
                                waypoint.getWorld().getName(),
                                getCoords(waypoint),
                                waypoint.getBlock().getBiome().getKey().getKey()
                        ))
                        .build();
            }
        }

        private Material findWaypointIcon(Location waypoint) {
            Block block = waypoint.getBlock();
            while (block.isEmpty() && block.getY() > block.getWorld().getMinHeight()) {
                block = block.getRelative(0, -1, 0);
            }
            if (!block.isEmpty() && block.getType().isItem()) {
                return block.getType();
            }
            return switch (block.getWorld().getEnvironment()) {
                case NORMAL -> Material.GRASS_BLOCK;
                case NETHER -> Material.NETHERRACK;
                case THE_END -> Material.END_STONE;
                default -> Material.FILLED_MAP;
            };
        }
    }

    private static MenuButton backButton() {
        return MenuButton.of(
                player -> ItemBuilder.of(Material.ARROW)
                        .name(Message.MENU_ALL_RETURN_BACK.getString(player))
                        .build(),
                (player, click) -> new GameMenu().open(player)
        );
    }

    private static int menuSize(int contentSlots) {
        return Math.min(54, ((contentSlots / 9) + 1) * 9);
    }

    private static List<String> replaceScorePlaceholder(List<String> lore) {
        return lore.stream()
                .map(line -> line.replace("%score%", String.valueOf(locateCost)))
                .toList();
    }

    private static List<String> replaceWaypointPlaceholders(List<String> lore, String dimension,
                                                             String coords, String biome) {
        return lore.stream()
                .map(line -> line
                        .replace("%dimension%", dimension)
                        .replace("%coords%", coords)
                        .replace("%biome%", biome))
                .toList();
    }
}
