package top.lqsnow.blockracing.menus;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import top.lqsnow.blockracing.managers.Game;
import top.lqsnow.blockracing.managers.GameProgressStore;
import top.lqsnow.blockracing.managers.LanguageManager;
import top.lqsnow.blockracing.managers.Message;
import top.lqsnow.blockracing.managers.Scoreboard;
import top.lqsnow.blockracing.managers.Setting;
import top.lqsnow.blockracing.toolkit.item.ItemBuilder;
import top.lqsnow.blockracing.toolkit.menu.MenuButton;
import top.lqsnow.blockracing.toolkit.menu.MenuView;
import top.lqsnow.blockracing.utils.TranslationUtil;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static top.lqsnow.blockracing.managers.Game.*;
import static top.lqsnow.blockracing.managers.Gui.openTeamChest;
import static top.lqsnow.blockracing.managers.Gui.updateMenu;
import static top.lqsnow.blockracing.managers.Team.blueTeamPlayers;
import static top.lqsnow.blockracing.managers.Team.redTeamPlayers;
import static top.lqsnow.blockracing.utils.CommandUtil.sendAll;

public final class GameMenu extends MenuView {
    public GameMenu() {
        super(36, player -> Message.MENU_GAME_TITLE.getString(player));

        setButton(10, MenuButton.of(
                player -> ItemBuilder.of(Material.CHEST)
                        .name(Message.MENU_TEAM_CHEST.getString(player))
                        .lore(Message.MENU_TEAM_CHEST_LORE.getStringList(player))
                        .build(),
                (player, click) -> new TeamChestSelectMenu().open(player)
        ));
        setButton(12, MenuButton.of(
                player -> ItemBuilder.of(Material.TOTEM_OF_UNDYING)
                        .name(Message.MENU_ROLL.getString(player))
                        .lore(List.of(rollLore(player)))
                        .build(),
                (player, click) -> {
                    Game.roll(player);
                    player.closeInventory();
                }
        ));
        setButton(14, MenuButton.of(
                player -> ItemBuilder.of(Material.COMPASS)
                        .name(Message.MENU_LOCATE.getString(player))
                        .lore(replaceScorePlaceholder(Message.MENU_LOCATE_LORE.getStringList(player)))
                        .build(),
                (player, click) -> {
                    Game.locate(player);
                    player.closeInventory();
                }
        ));
        setButton(16, MenuButton.of(
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
        setButton(21, MenuButton.of(
                player -> ItemBuilder.of(Material.ENDER_PEARL)
                        .name(Message.MENU_RANDOM_TP.getString(player))
                        .lore(Message.MENU_RANDOM_TP_LORE.getStringList(player))
                        .build(),
                (player, click) -> handleRandomTeleport(player)
        ));
        setButton(19, MenuButton.of(
                player -> ItemBuilder.of(Material.PLAYER_HEAD)
                        .name(Message.MENU_TEAMMATE_TELEPORT.getString(player))
                        .lore(Message.MENU_TEAMMATE_TELEPORT_LORE.getStringList(player))
                        .build(),
                (player, click) -> new TeammateTeleportMenu(player).open(player)
        ));
        setButton(23, MenuButton.of(
                player -> ItemBuilder.of(Material.WRITABLE_BOOK)
                        .name(Message.MENU_CURRENT_BLOCKS.getString(player))
                        .lore(Message.MENU_CURRENT_BLOCKS_LORE.getStringList(player))
                        .build(),
                (player, click) -> showCurrentBlocks(player)
        ));
        setButton(25, MenuButton.of(
                () -> ItemBuilder.of(Material.KNOWLEDGE_BOOK)
                        .name("§bLanguage / 语言")
                        .lore(List.of("§7Change display language / 切换显示语言"))
                        .build(),
                (player, click) -> new LanguageMenu().open(player)
        ));
        if (Setting.isSpeedMode()) {
            setButton(31, MenuButton.of(
                    player -> ItemBuilder.of(Material.FIREWORK_ROCKET)
                            .name(Message.MENU_SUPPLY.getString(player))
                            .lore(Message.MENU_SUPPLY_LORE.getStringList(player))
                            .flags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP)
                            .build(),
                    (player, click) -> {
                        Game.buySupply(player);
                        player.closeInventory();
                    }
            ));
        }
    }

    @Override
    protected ItemStack getBackgroundItem(int slot, Player player) {
        Material material = slot < 9 || slot >= 27
                ? Material.BLACK_STAINED_GLASS_PANE
                : Material.GRAY_STAINED_GLASS_PANE;
        return ItemBuilder.of(material).name(" ").build();
    }

    private static void showCurrentBlocks(Player player) {
        player.closeInventory();
        player.sendMessage(Message.NOTICE_BLOCK_OVERVIEW_DIVIDER.getString(player));
        player.sendMessage(Message.NOTICE_BLOCK_OVERVIEW_TITLE.getString(player));
        player.sendMessage(Message.NOTICE_BLOCK_OVERVIEW_RED.getString(player));
        sendBlockSection(player, getCurrentBlocks("red"));
        player.sendMessage(Message.NOTICE_BLOCK_OVERVIEW_DIVIDER.getString(player));
        player.sendMessage(Message.NOTICE_BLOCK_OVERVIEW_BLUE.getString(player));
        sendBlockSection(player, getCurrentBlocks("blue"));
        player.sendMessage(Message.NOTICE_BLOCK_OVERVIEW_DIVIDER.getString(player));
    }

    private static void sendBlockSection(Player player, List<String> blocks) {
        for (int index = 0; index < blocks.size(); index++) {
            String block = blocks.get(index);
            player.sendMessage(Message.NOTICE_BLOCK_OVERVIEW_ENTRY.getString(player)
                    .replace("%index%", String.valueOf(index + 1))
                    .replace("%block%", TranslationUtil.getValue(block, player)));
        }
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
        GameProgressStore.saveNow();
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

    public static final class TeammateTeleportMenu extends MenuView {
        public TeammateTeleportMenu(Player viewer) {
            this(getOnlineTeammates(viewer));
        }

        private TeammateTeleportMenu(List<Player> teammates) {
            super(menuSize(Math.max(1, teammates.size())),
                    player -> Message.MENU_TEAMMATE_TELEPORT_TITLE.getString(player));

            if (teammates.isEmpty()) {
                setButton(4, MenuButton.of(
                        player -> ItemBuilder.of(Material.BARRIER)
                                .name(Message.MENU_TEAMMATE_TELEPORT_EMPTY.getString(player))
                                .lore(Message.MENU_TEAMMATE_TELEPORT_EMPTY_LORE.getStringList(player))
                                .build(),
                        (player, click) -> {
                        }
                ));
            } else {
                for (int slot = 0; slot < teammates.size() && slot < 53; slot++) {
                    Player teammate = teammates.get(slot);
                    setButton(slot, MenuButton.of(
                            player -> teammateHead(teammate, player),
                            (player, click) -> teleportToTeammate(player, teammate.getName())
                    ));
                }
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

    private static List<Player> getOnlineTeammates(Player player) {
        List<String> team = redTeamPlayers.contains(player.getName())
                ? redTeamPlayers
                : blueTeamPlayers.contains(player.getName()) ? blueTeamPlayers : List.of();
        List<Player> teammates = new ArrayList<>();
        for (String name : team) {
            Player teammate = org.bukkit.Bukkit.getPlayerExact(name);
            if (teammate != null && teammate.isOnline() && !teammate.equals(player)) {
                teammates.add(teammate);
            }
        }
        teammates.sort(Comparator.comparing(Player::getName, String.CASE_INSENSITIVE_ORDER));
        return teammates;
    }

    private static ItemStack teammateHead(Player teammate, Player viewer) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        if (head.getItemMeta() instanceof SkullMeta meta) {
            meta.setOwningPlayer(teammate);
            head.setItemMeta(meta);
        }
        return ItemBuilder.of(head)
                .name("§f" + teammate.getName())
                .lore(Message.MENU_TEAMMATE_TELEPORT_PLAYER_LORE.getStringList(viewer))
                .build();
    }

    private static void teleportToTeammate(Player player, String targetName) {
        Player target = org.bukkit.Bukkit.getPlayerExact(targetName);
        if (target == null || !target.isOnline()) {
            player.sendMessage(Message.NOTICE_PLAYER_NOT_EXIST.getString(player));
            player.closeInventory();
            return;
        }
        boolean sameTeam = redTeamPlayers.contains(player.getName()) && redTeamPlayers.contains(targetName)
                || blueTeamPlayers.contains(player.getName()) && blueTeamPlayers.contains(targetName);
        if (!sameTeam) {
            player.sendMessage(Message.NOTICE_PLAYER_NOT_IN_SAME_TEAM.getString(player));
            player.closeInventory();
            return;
        }
        player.teleport(target);
        player.sendMessage(Message.NOTICE_TP_PLAYER_SUCCESS.getString(player)
                .replace("%player%", target.getName()));
        player.closeInventory();
    }

    private static int menuSize(int contentSlots) {
        return Math.min(54, ((contentSlots / 9) + 1) * 9);
    }

    private static List<String> replaceScorePlaceholder(List<String> lore) {
        return lore.stream()
                .map(line -> line.replace("%score%", String.valueOf(locateCost)))
                .toList();
    }

    private static String rollLore(Player player) {
        String lore = Message.MENU_ROLL_LORE.getString(player);
        if (!lore.contains("%count%")) {
            lore = LanguageManager.usesChinese(player)
                    ? "§b替换当前目标方块（每局每队最多 %count% 次）"
                    : "§bReplace current targets (up to %count% times per team)";
        }
        return lore.replace("%count%", String.valueOf(Setting.getMaxRollCount()));
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
