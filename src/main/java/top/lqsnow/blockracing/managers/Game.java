package top.lqsnow.blockracing.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.ArrayDeque;
import java.util.Deque;
import java.time.Duration;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.GameRules;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import top.lqsnow.blockracing.Main;
import top.lqsnow.blockracing.commands.Restart;
import top.lqsnow.blockracing.toolkit.item.ItemBuilder;
import top.lqsnow.blockracing.toolkit.material.Materials;
import static top.lqsnow.blockracing.listeners.BasicListener.editAmountPlayer;
import static top.lqsnow.blockracing.managers.Block.blocks;
import static top.lqsnow.blockracing.managers.Block.blueTeamBlocks;
import static top.lqsnow.blockracing.managers.Block.blueTeamRemainingBlocks;
import static top.lqsnow.blockracing.managers.Block.checkBlock;
import static top.lqsnow.blockracing.managers.Block.redTeamBlocks;
import static top.lqsnow.blockracing.managers.Block.redTeamRemainingBlocks;
import static top.lqsnow.blockracing.managers.Block.setupBlocks;
import static top.lqsnow.blockracing.managers.Gui.closeAllPlayersMenu;
import static top.lqsnow.blockracing.managers.Scoreboard.updateScoreboard;
import static top.lqsnow.blockracing.managers.Team.blueTeamPlayers;
import static top.lqsnow.blockracing.managers.Team.redTeamPlayers;
import top.lqsnow.blockracing.utils.ColorUtil;
import static top.lqsnow.blockracing.utils.ColorUtil.t;
import static top.lqsnow.blockracing.utils.CommandUtil.sendAll;
import static top.lqsnow.blockracing.utils.CommandUtil.sendBlue;
import static top.lqsnow.blockracing.utils.CommandUtil.sendRed;
import top.lqsnow.blockracing.utils.TranslationUtil;

public class Game {
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection();
    public enum GameState {
        PREGAME, INGAME, END
    }

    public static GameState currentGameState = GameState.PREGAME;
    public static List<String> readyPlayers = new ArrayList<>();
    public static int redTeamScore = 0;
    public static int blueTeamScore = 0;
    public static int redTeamCurrentBlockAmount = 0;
    public static int blueTeamCurrentBlockAmount = 0;
    public static int redTeamTotalBlockAmount = 0;
    public static int blueTeamTotalBlockAmount = 0;
    public static List<String> freeRandomTPList = new ArrayList<>();

    public static ArrayList<Inventory> redTeamChest = new ArrayList<>();
    public static ArrayList<Inventory> blueTeamChest = new ArrayList<>();

    public static HashMap<Integer, Location> redWaypoint = new HashMap<>();
    public static HashMap<Integer, Location> blueWaypoint = new HashMap<>();

    public static HashMap<Integer, Material> redWaypointIconCache = new HashMap<>();
    public static HashMap<Integer, Material> blueWaypointIconCache = new HashMap<>();

    public static int redTeamRollCount;
    public static int blueTeamRollCount;
    public static List<String> redRollPlayers = new ArrayList<>();
    public static List<String> blueRollPlayers = new ArrayList<>();
    public static List<String> inGamePlayers = new ArrayList<>();
    public static ArrayList<String> locateCommandPermission = new ArrayList<>();
    public static int locateCost;
    public static Map<String, Integer> collectAmount = new HashMap<>();
    private static final Deque<Location> randomTpPool = new ArrayDeque<>();

    public static void initChest() {
        int teamChestNum = Setting.getMaxTeamChestNum();
        for (int i = 0; i < teamChestNum; i++) {
            redTeamChest.add(Bukkit.createInventory(null, 6 * 9,
                    LEGACY_SERIALIZER.deserialize(Message.MENU_RED_CHEST.getString() + (i + 1))));
            blueTeamChest.add(Bukkit.createInventory(null, 6 * 9,
                    LEGACY_SERIALIZER.deserialize(Message.MENU_BLUE_CHEST.getString() + (i + 1))));
        }
    }

    public static void playerLogin(Player player) {
        Scoreboard.showScoreboard(player);

        if (getCurrentGameState().equals(GameState.PREGAME)) {
            player.setGameMode(GameMode.ADVENTURE);
            player.sendMessage(Message.NOTICE_WELCOME.getString(player));
            player.teleport(getPrimaryWorld().getSpawnLocation());
        } else if (getCurrentGameState().equals(GameState.INGAME)) {
            // Spectator
            if (!redTeamPlayers.contains(player.getName()) && !blueTeamPlayers.contains(player.getName())) {
                player.setGameMode(GameMode.SPECTATOR);
                player.sendMessage(Message.NOTICE_SPECTATOR_JOIN.getString(player));
                return;
            }

            // Players who choose a team before the start of the game and exit, but enter
            // after the start of the game
            if (!inGamePlayers.contains(player.getName())) {
                initPlayer(player);
                freeRandomTPList.add(player.getName());
            }
        }

        // The permissions will disappear when the player exits and re-enters,
        // permissions need to be given again.
        if (locateCommandPermission.contains(player.getName())) {
            player.addAttachment(Main.getInstance(), "minecraft.command.locate", true);
        }

        checkUpdate(player);
    }

    private static void checkUpdate(Player player) {
        player.resetTitle();
        if (!Config.CONFIG_VERSION.getString().equals(Main.getVersion())
                || !Message.MESSAGE_VERSION.getString().equals(Main.getVersion())) {
            if (Message.NOTICE_VERSION_MISMATCH.getString() != null) {
                player.sendMessage(Message.NOTICE_VERSION_MISMATCH.getString(player));
                player.showTitle(Title.title(
                        LEGACY_SERIALIZER.deserialize(Message.NOTICE_VERSION_MISMATCH_TITLE.getString(player)),
                        LEGACY_SERIALIZER.deserialize(Message.NOTICE_VERSION_MISMATCH_SUBTITLE.getString(player)),
                        Title.Times.times(Duration.ZERO, Duration.ofSeconds(100), Duration.ZERO)
                ));
            } else {
                player.sendMessage(ColorUtil.t(
                        "&cWarning! The current file versions of your config.yml and lang.yml do not correspond to the plugin version! You may have updated the plugin, but did not update the configuration file! This may lead to some unexpected errors! You can delete the two configuration files in the \\plugins\\BlockRacing folder, and then restart the server, or download the latest version of the configuration file on GitHub to replace it!"));
                player.showTitle(Title.title(
                        LEGACY_SERIALIZER.deserialize(ColorUtil.t("&cWarning! Version Mismatch!")),
                        LEGACY_SERIALIZER.deserialize(ColorUtil.t("&cPlease check the specific information in the chat!")),
                        Title.Times.times(Duration.ofSeconds(1), Duration.ZERO, Duration.ZERO)
                ));
            }
            Bukkit.getLogger().severe(Message.NOTICE_VERSION_MISMATCH.getString());
        }
    }

    public static void playerQuit(Player player) {
        redRollPlayers.remove(player.getName());
        blueRollPlayers.remove(player.getName());
        readyPlayers.remove(player.getName());
        editAmountPlayer.remove(player.getName());
        Restart.removeVote(player);
    }

    public static void playerReady(Player player) {
        if (!readyPlayers.contains(player.getName())) {
            readyPlayers.add(player.getName());
            sendAll(Message.NOTICE_READY, (viewer, text) -> text.replace("%player%", player.getName()));
            // Check if the game can start, just notice players
            if (readyPlayers.size() > 1 && readyPlayers.size() == Bukkit.getOnlinePlayers().size()) {
                sendAll(Message.NOTICE_ALL_READY);
            }
        } else {
            readyPlayers.remove(player.getName());
            sendAll(Message.NOTICE_CANCEL_READY, (viewer, text) -> text.replace("%player%", player.getName()));
        }
    }

    // Check if the game can start. If not, send the reason to the player; if
    // possible, start the game directly
    public static void checkStartDemands(Player player) {

        // Game already start
        if (getCurrentGameState().equals(GameState.INGAME))
            return;

        // Not enough players
        if (!(Bukkit.getOnlinePlayers().size() > 1)) {
            player.sendMessage(Message.NOTICE_NOT_ENOUGH_PLAYERS.getString(player));
            return;
        }

        // Exist unready players
        if (!(readyPlayers.size() == Bukkit.getOnlinePlayers().size())) {
            List<String> unreadyPlayers = getOnlinePlayersString();
            unreadyPlayers.removeAll(readyPlayers);
            player.sendMessage(Message.NOTICE_EXIST_UNREADY.getString(player));
            player.sendMessage(Message.NOTICE_UNREADY_PLAYERS.getString(player) + unreadyPlayers);
            return;
        }

        // Exist empty team
        if (redTeamPlayers.isEmpty() || blueTeamPlayers.isEmpty()) {
            player.sendMessage(Message.NOTICE_EMPTY_TEAM.getString(player));
            return;
        }

        // Blocks have problems
        if (!checkBlock()) {
            return;
        }

        // Start the game
        sendAll(Message.NOTICE_START);
        startGame();
    }

    public static void startGame() {
        // Init
        setCurrentGameState(GameState.INGAME);
        closeAllPlayersMenu();
        editAmountPlayer.clear();
        setupBlocks();
        redTeamTotalBlockAmount = redTeamBlocks.size();
        blueTeamTotalBlockAmount = blueTeamBlocks.size();
        setLocateScore();
        updateScoreboard();
        Bukkit.getOnlinePlayers().forEach((Player player) -> freeRandomTPList.add(player.getName()));
        new runPer5Tick().runTaskTimer(Main.getInstance(), 0L, 5L);
        World world = getPrimaryWorld();
        world.setDifficulty(Difficulty.EASY);
        world.setTime(1000);
        world.setStorm(false);
        world.setThundering(false);
        world.getEntities().stream().filter(e -> e instanceof Item).forEach(Entity::remove);
        world.setGameRule(GameRules.LOCATOR_BAR, false);

        // World border
        world.getWorldBorder().setCenter(world.getSpawnLocation());
        world.getWorldBorder().setSize(59999968);

        // Processing of unselected team players (spectators)
        inGamePlayers.addAll(getOnlinePlayersString());
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!redTeamPlayers.contains(player.getName()) && !blueTeamPlayers.contains(player.getName())) {
                player.setGameMode(GameMode.SPECTATOR);
                player.sendMessage(Message.NOTICE_SPECTATOR.getString(player));
                inGamePlayers.remove(player.getName());
            }
        }

        // Settings for each player
        for (String p : inGamePlayers) {
            // General
            Player player = Bukkit.getPlayer(p);
            initPlayer(player);
        }

        Bukkit.getLogger().info("Red team players: " + redTeamPlayers.toString());
        Bukkit.getLogger().info("Blue team players: " + blueTeamPlayers.toString());
        if (Setting.getCurrentGameMode().equals(Setting.GameMode.NORMAL))
            Bukkit.getLogger().info("Game mode: Normal");
        else if (Setting.getCurrentGameMode().equals(Setting.GameMode.RACING))
            Bukkit.getLogger().info("Game mode: Racing");
        Bukkit.getLogger().info(Setting.isSpeedMode() ? "Speed mode: On" : "Speed mode: Off");
    }

    // Player init
    public static void initPlayer(Player player) {
        // General
        player.getInventory().clear();
        randomTeleport(player, true);
        player.setHealth(20);
        player.setExp(0);
        player.setLevel(0);
        player.setFoodLevel(20);
        player.setSaturation(10);
        player.setGameMode(GameMode.SURVIVAL);
        player.getInventory().addItem(ItemBuilder.of(Material.STONE_PICKAXE).build());
        player.getInventory().addItem(ItemBuilder.of(Material.STONE_AXE).build());
        player.getInventory().addItem(ItemBuilder.of(Material.STONE_SHOVEL).build());
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 1200, 4, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 1200, 4, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 1200, 4, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, -1, 0, false, false));

        // Speed mode
        if (Setting.isSpeedMode()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, -1, 4, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, -1, 1, false, false));
            Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, -1, 1, false, false));
            }, 1300L); // 延迟发放 避免冲突
            player.getInventory().addItem(ItemBuilder.of(Material.IRON_PICKAXE)
                    .enchant(Enchantment.SILK_TOUCH, 1)
                    .build());
            player.getInventory().addItem(ItemBuilder.of(Material.GOLDEN_CARROT).amount(64).build());

            ItemStack damagedElytra = new ItemStack(Material.ELYTRA);
            ItemMeta elytraMeta = damagedElytra.getItemMeta();
            Damageable damageable = (Damageable) elytraMeta;
            damageable.setDamage(damagedElytra.getType().getMaxDurability() - 1);
            Repairable repairable = (Repairable) elytraMeta;
            repairable.setRepairCost(15);
            damagedElytra.setItemMeta(elytraMeta);
            player.getInventory().addItem(damagedElytra);

            ItemStack xpBook = new ItemStack(Material.ENCHANTED_BOOK);
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) xpBook.getItemMeta();
            meta.addStoredEnchant(Enchantment.MENDING, 1, true);
            xpBook.setItemMeta(meta);
            player.getInventory().addItem(xpBook);
        }
    }

    // Roll
    public static void roll(Player player) {
        if (redTeamPlayers.contains(player.getName())) {
            if (redTeamRollCount >= 3) {
                player.sendMessage(Message.NOTICE_CANNOT_ROLL.getString(player));
                return;
            }
            if (!redRollPlayers.contains(player.getName())) {
                redRollPlayers.add(player.getName());
                sendRed(Message.NOTICE_ROLL_REQUEST, (viewer, text) -> text.replace("%player%", player.getName()));
            } else {
                redRollPlayers.remove(player.getName());
                sendRed(Message.NOTICE_ROLL_REQUEST_CANCEL, (viewer, text) -> text.replace("%player%", player.getName()));
            }
        } else if (blueTeamPlayers.contains(player.getName())) {
            if (blueTeamRollCount >= 3) {
                player.sendMessage(Message.NOTICE_CANNOT_ROLL.getString(player));
                return;
            }
            if (!blueRollPlayers.contains(player.getName())) {
                blueRollPlayers.add(player.getName());
                sendBlue(Message.NOTICE_ROLL_REQUEST, (viewer, text) -> text.replace("%player%", player.getName()));
            } else {
                blueRollPlayers.remove(player.getName());
                sendBlue(Message.NOTICE_ROLL_REQUEST_CANCEL, (viewer, text) -> text.replace("%player%", player.getName()));
            }
        }
    }

    public static void locate(Player player) {
        if (locateCommandPermission.contains(player.getName())) {
            player.sendMessage(Message.NOTICE_LOCATE_ALREADY_BOUGHT.getString(player));
            return;
        }
        if (redTeamPlayers.contains(player.getName())) {
            if (redTeamScore >= locateCost) {
                redTeamScore -= locateCost;
                updateScoreboard();
                locateCommandPermission.add(player.getName());
                sendAll(Message.NOTICE_BUY_LOCATE, (viewer, text) -> text.replace("%player%", player.getName()));
                player.addAttachment(Main.getInstance(), "minecraft.command.locate", true);
            } else {
                player.sendMessage(Message.NOTICE_NOT_ENOUGH_SCORE.getString(player));
            }
        } else if (blueTeamPlayers.contains(player.getName())) {
            if (blueTeamScore >= locateCost) {
                blueTeamScore -= locateCost;
                updateScoreboard();
                locateCommandPermission.add(player.getName());
                sendAll(Message.NOTICE_BUY_LOCATE, (viewer, text) -> text.replace("%player%", player.getName()));
                player.addAttachment(Main.getInstance(), "minecraft.command.locate", true);
            } else {
                player.sendMessage(Message.NOTICE_NOT_ENOUGH_SCORE.getString(player));
            }
        }
    }

    // Random Teleport
    public static void randomTeleport(Player player, boolean avoidOcean) {
        World playerWorld = getPrimaryWorld();
        int maxAttempts = avoidOcean ? 12 : 1;
        startAsyncRandomTeleport(player, playerWorld, avoidOcean, 1, maxAttempts);
    }

    private static void randomTeleportSynchronously(Player player, World playerWorld, boolean avoidOcean,
                                                    int maxAttempts) {
        Random random = new Random();
        Location offset = null;
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            Location candidate = pollRandomTeleportCandidate();
            double randX = candidate != null ? candidate.getX() : (random.nextInt(20000) - 10000);
            double randZ = candidate != null ? candidate.getZ() : (random.nextInt(20000) - 10000);
            Location current = playerWorld.getHighestBlockAt(new Location(playerWorld, randX, 0, randZ)).getLocation().add(0, 1, 0);
            offset = current;
            if (!avoidOcean || !isOcean(current.getBlock().getBiome())) {
                break;
            }
        }
        if (offset == null) return;
        completeRandomTeleport(player, offset, avoidOcean);
    }

    private static void startAsyncRandomTeleport(Player player, World world, boolean avoidOcean,
                                                 int attempt, int maxAttempts) {
        Random random = new Random();
        Location candidate = pollRandomTeleportCandidate();
        int blockX = candidate != null ? candidate.getBlockX() : random.nextInt(20000) - 10000;
        int blockZ = candidate != null ? candidate.getBlockZ() : random.nextInt(20000) - 10000;
        world.getChunkAtAsync(blockX >> 4, blockZ >> 4, true)
                .whenComplete((chunk, error) -> Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                if (!player.isOnline()) return;
                if (error != null) {
                    if (attempt < maxAttempts) {
                        startAsyncRandomTeleport(player, world, avoidOcean, attempt + 1, maxAttempts);
                    } else {
                        randomTeleportSynchronously(player, world, avoidOcean, 1);
                    }
                    return;
                }
                Location offset = world.getHighestBlockAt(blockX, blockZ).getLocation().add(0, 1, 0);
                if (avoidOcean && isOcean(offset.getBlock().getBiome()) && attempt < maxAttempts) {
                    startAsyncRandomTeleport(player, world, true, attempt + 1, maxAttempts);
                    return;
                }
                completeRandomTeleport(player, offset, avoidOcean);
                }));
    }

    private static void completeRandomTeleport(Player player, Location offset, boolean avoidOcean) {
        player.teleport(offset);

        String x = String.format("%.1f", offset.getX());
        String y = String.format("%.1f", offset.getY());
        String z = String.format("%.1f", offset.getZ());

        player.sendMessage(Message.NOTICE_TP_SUCCESS.getString(player).replace("%x%", x).replace("%y%", y).replace("%z%", z));
        if (avoidOcean && isOcean(offset.getBlock().getBiome())) {
            player.sendMessage(Message.NOTICE_TP_OCEAN.getString(player));
        }
    }

    private static boolean isOcean(Biome biome) {
        return biome == Biome.OCEAN || biome == Biome.DEEP_OCEAN || biome == Biome.DEEP_COLD_OCEAN
                || biome == Biome.LUKEWARM_OCEAN || biome == Biome.DEEP_FROZEN_OCEAN || biome == Biome.COLD_OCEAN
                || biome == Biome.WARM_OCEAN || biome == Biome.DEEP_LUKEWARM_OCEAN || biome == Biome.FROZEN_OCEAN;
    }

    public static synchronized void addRandomTeleportCandidate(Location location) {
        if (location != null) {
            randomTpPool.addLast(location);
        }
    }

    public static synchronized Location pollRandomTeleportCandidate() {
        return randomTpPool.pollFirst();
    }

    public static synchronized int getRandomTeleportPoolSize() {
        return randomTpPool.size();
    }

    public static synchronized List<Location> getRandomTeleportPoolSnapshot() {
        return List.copyOf(randomTpPool);
    }

    // Waypoints
    // Return value: true -> waypoint changed, false -> waypoint doesn't change
    public static boolean waypoint(Player player, int index, ClickType clickType) {
        String team = redTeamPlayers.contains(player.getName()) ? "red"
                : (blueTeamPlayers.contains(player.getName()) ? "blue" : "");

        if (!team.isEmpty()) {
            Location waypoint = getWaypoint(team, index);
            String action = "";
            if (clickType.equals(ClickType.LEFT)) {
                action = "left";
            } else if (clickType.equals(ClickType.RIGHT)) {
                action = "right";
            }

            switch (action) {
                case "left" -> {
                    if (waypoint == null) {
                        setWaypoint(player, team, index);
                        return true;
                    } else {
                        player.teleport(waypoint);
                        String x = String.format("%.1f", waypoint.getX());
                        String y = String.format("%.1f", waypoint.getY());
                        String z = String.format("%.1f", waypoint.getZ());

                        player.sendMessage(Message.NOTICE_TP_SUCCESS.getString(player).replace("%x%", x).replace("%y%", y)
                                .replace("%z%", z));
                        return false;
                    }
                }
                case "right" -> removeWaypoint(player, index);
            }
        }
        return false;
    }

    public static Location getWaypoint(String team, int index) {
        return switch (team) {
            case "red" -> redWaypoint.get(index);
            case "blue" -> blueWaypoint.get(index);
            default -> null;
        };
    }

    public static void setWaypoint(Player player, String team, int index) {
        Location waypoint = player.getLocation();
        switch (team) {
            case "red" -> {
                redWaypoint.put(index, waypoint);
            }
            case "blue" -> {
                blueWaypoint.put(index, waypoint);
            }
        }
    }

    private static void removeWaypoint(Player player, int index) {
        Component message = LEGACY_SERIALIZER.deserialize(
                        Message.NOTICE_REMOVE_WAYPOINT.getString(player).replace("%index%", String.valueOf(index)))
                .clickEvent(ClickEvent.runCommand("/waypoint remove " + index));
        player.sendMessage(message);
        player.closeInventory();
    }

    // Run per 2t
    // Before the game, provide regeneration and saturation effects
    public static class runPer2Tick extends BukkitRunnable {
        @Override
        public void run() {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 10, 255));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 10, 255));
            }
            if (getCurrentGameState().equals(GameState.INGAME))
                this.cancel();
        }
    }

    // Run per 5t
    // During the game
    public static class runPer5Tick extends BukkitRunnable {

        @Override
        public void run() {

            // Inventory check
            checkRedInventory();
            checkBlueInventory();

            // Win check
            if (redTeamRemainingBlocks.isEmpty()) {
                redWin();
                showRanking();
                this.cancel();
                return;
            }
            if (blueTeamRemainingBlocks.isEmpty()) {
                blueWin();
                showRanking();
                this.cancel();
                return;
            }

            // Roll check
            if (!redRollPlayers.isEmpty())
                checkRedRoll();
            if (!blueRollPlayers.isEmpty())
                checkBlueRoll();

        }
    }

    private static void showRanking() {
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(collectAmount.entrySet());
        entries.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        sendAll(Message.NOTICE_RANKING);
        for (Map.Entry<String, Integer> entry : entries) {
            if (redTeamPlayers.contains(entry.getKey())) {
                sendAll(Message.NOTICE_RANKING_RED, (viewer, text) -> text
                        .replace("%player%", entry.getKey()).replace("%amount%", entry.getValue().toString()));
            } else if (blueTeamPlayers.contains(entry.getKey())) {
                sendAll(Message.NOTICE_RANKING_BLUE, (viewer, text) -> text
                        .replace("%player%", entry.getKey()).replace("%amount%", entry.getValue().toString()));
            } else {
                sendAll(Message.NOTICE_RANKING_OFFLINE, (viewer, text) -> text
                        .replace("%player%", entry.getKey()).replace("%amount%", entry.getValue().toString()));
            }
        }
    }

    private static void checkRedRoll() {
        Set<String> redSet = new HashSet<>(redRollPlayers);
        Set<String> redOnlineSet = new HashSet<>(getOnlineTeamPlayers("red"));
        if (!getOnlineTeamPlayers("red").isEmpty() && redSet.containsAll(redOnlineSet)) {
            List<String> b = new ArrayList<>(blocks);
            b.removeAll(redTeamBlocks);
            Random random = new Random();
            int rollAmount = getCurrentBlocks("red").size();
            for (int i = 0; i < rollAmount; i++) {
                redTeamRemainingBlocks.set(i, b.get(random.nextInt(b.size())));
            }
            sendAll(Message.NOTICE_RED_ROLL_SUCCESS);
            redTeamRollCount += 1;
            redRollPlayers.clear();
            updateScoreboard();
        }
    }

    private static void checkBlueRoll() {
        Set<String> blueSet = new HashSet<>(blueRollPlayers);
        Set<String> blueOnlineSet = new HashSet<>(getOnlineTeamPlayers("blue"));
        if (!getOnlineTeamPlayers("blue").isEmpty() && blueSet.containsAll(blueOnlineSet)) {
            List<String> b = new ArrayList<>(blocks);
            b.removeAll(blueTeamBlocks);
            Random random = new Random();
            int rollAmount = getCurrentBlocks("blue").size();
            for (int i = 0; i < rollAmount; i++) {
                blueTeamRemainingBlocks.set(i, b.get(random.nextInt(b.size())));
            }
            sendAll(Message.NOTICE_BLUE_ROLL_SUCCESS);
            blueTeamRollCount += 1;
            blueRollPlayers.clear();
            updateScoreboard();
        }
    }

    private static void setLocateScore() {
        if (Setting.getBlockAmount() <= 20)
            locateCost = 2;
        else if (Setting.getBlockAmount() <= 50)
            locateCost = 3;
        else if (Setting.getBlockAmount() <= 100)
            locateCost = 5;
        else if (Setting.getBlockAmount() <= 200)
            locateCost = 8;
        else
            locateCost = 10;
    }

    private static void checkRedInventory() {
        // Complete from player
        for (String player : redTeamPlayers) {
            for (String block : getCurrentBlocks("red")) {
                Player p = Bukkit.getPlayer(player);
                if (p == null)
                    continue;
                if (p.getInventory().contains(Material.valueOf(block))) {
                    redTaskComplete(block, player);
                    return;
                }
            }
        }
        // Complete from team chest
        for (String block : getCurrentBlocks("red")) {
            for (Inventory chest : redTeamChest) {
                if (chest.contains(Material.valueOf(block))) {
                    redTaskComplete(block, Message.NOTICE_RED_TEAM_CHEST.getString());
                    return;
                }
            }
        }
    }

    private static void checkBlueInventory() {
        // Complete from player
        for (String player : blueTeamPlayers) {
            for (String block : getCurrentBlocks("blue")) {
                Player p = Bukkit.getPlayer(player);
                if (p == null)
                    continue;
                if (p.getInventory().contains(Material.valueOf(block))) {
                    blueTaskComplete(block, player);
                    return;
                }
            }
        }
        // Complete from team chest
        for (String block : getCurrentBlocks("blue")) {
            for (Inventory chest : blueTeamChest) {
                if (chest.contains(Material.valueOf(block))) {
                    blueTaskComplete(block, Message.NOTICE_BLUE_TEAM_CHEST.getString());
                    return;
                }
            }
        }
    }

    public static void redTaskComplete(String block, String player) {
        boolean collectedFromTeamChest = player.equals(Message.NOTICE_RED_TEAM_CHEST.getString());
        sendAll(Message.NOTICE_RED_COLLECT, (viewer, text) -> text
                .replace("%block%", TranslationUtil.getValue(block, viewer))
                .replace("%player%", collectedFromTeamChest
                        ? Message.NOTICE_RED_TEAM_CHEST.getString(viewer)
                        : player));
        Bukkit.getLogger().info(Message.NOTICE_RED_COLLECT.getString()
                .replace("%block%", TranslationUtil.getValue(block)).replace("%player%", player).replaceAll("§.", ""));
        playSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
        String newTarget = removeAndGetNewVisibleTarget(redTeamRemainingBlocks, block);
        if (Setting.isSpeedMode())
            redTeamScore += 3;
        else
            redTeamScore += 1;
        redTeamCurrentBlockAmount += 1;
        collect(player);
        updateScoreboard();
        if (newTarget != null) {
            sendRed(Message.NOTICE_NEW_TARGET_BLOCK, (viewer, text) -> text
                    .replace("%block%", TranslationUtil.getValue(newTarget, viewer)));
        }
        // Put items into the opponent's team chest
        if (Setting.getCurrentGameMode().equals(Setting.GameMode.NORMAL)) {
            for (int i = blueTeamChest.size() - 1; i >= 0; i--) {
                Inventory chest = blueTeamChest.get(i);
                int emptyPos = chest.firstEmpty();
                if (emptyPos == -1) {
                    continue;
                }
                chest.setItem(emptyPos, Materials.stack(block, 64));
                return;
            }
            sendAll(Message.NOTICE_TEAM_CHEST_FULL, (viewer, text) -> text
                    .replace("%team%", Message.TEAM_BLUE_NAME.getString(viewer))
                    .replace("%block%", TranslationUtil.getValue(block, viewer)));
        }
    }

    public static void blueTaskComplete(String block, String player) {
        boolean collectedFromTeamChest = player.equals(Message.NOTICE_BLUE_TEAM_CHEST.getString());
        sendAll(Message.NOTICE_BLUE_COLLECT, (viewer, text) -> text
                .replace("%block%", TranslationUtil.getValue(block, viewer))
                .replace("%player%", collectedFromTeamChest
                        ? Message.NOTICE_BLUE_TEAM_CHEST.getString(viewer)
                        : player));
        Bukkit.getLogger().info(Message.NOTICE_BLUE_COLLECT.getString()
                .replace("%block%", TranslationUtil.getValue(block)).replace("%player%", player).replaceAll("§.", ""));
        playSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
        String newTarget = removeAndGetNewVisibleTarget(blueTeamRemainingBlocks, block);
        if (Setting.isSpeedMode())
            blueTeamScore += 3;
        else
            blueTeamScore += 1;
        blueTeamCurrentBlockAmount += 1;
        collect(player);
        updateScoreboard();
        if (newTarget != null) {
            sendBlue(Message.NOTICE_NEW_TARGET_BLOCK, (viewer, text) -> text
                    .replace("%block%", TranslationUtil.getValue(newTarget, viewer)));
        }
        // Put items into the opponent's team chest
        if (Setting.getCurrentGameMode().equals(Setting.GameMode.NORMAL)) {
            for (int i = redTeamChest.size() - 1; i >= 0; i--) {
                Inventory chest = redTeamChest.get(i);
                int emptyPos = chest.firstEmpty();
                if (emptyPos == -1) {
                    continue;
                }
                chest.setItem(emptyPos, Materials.stack(block, 64));
                return;
            }
            sendAll(Message.NOTICE_TEAM_CHEST_FULL, (viewer, text) -> text
                    .replace("%team%", Message.TEAM_RED_NAME.getString(viewer))
                    .replace("%block%", TranslationUtil.getValue(block, viewer)));
        }
    }

    public static void redWin() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.closeInventory();
            player.showTitle(Title.title(
                    LEGACY_SERIALIZER.deserialize(Message.NOTICE_RED_WIN.getString(player)),
                    Component.empty()
            ));
            player.setGameMode(GameMode.SPECTATOR);
        }
        sendAll(Message.NOTICE_RED_WIN);
        playSound(Sound.UI_TOAST_CHALLENGE_COMPLETE);
        setCurrentGameState(GameState.END);
    }

    public static void blueWin() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.closeInventory();
            player.showTitle(Title.title(
                    LEGACY_SERIALIZER.deserialize(Message.NOTICE_BLUE_WIN.getString(player)),
                    Component.empty()
            ));
            player.setGameMode(GameMode.SPECTATOR);
        }
        sendAll(Message.NOTICE_BLUE_WIN);
        playSound(Sound.UI_TOAST_CHALLENGE_COMPLETE);
        setCurrentGameState(GameState.END);
    }

    public static List<String> getCurrentBlocks(String team) {
        return switch (team) {
            case "red" -> redTeamRemainingBlocks.subList(0, Math.min(redTeamRemainingBlocks.size(), 4));
            case "blue" -> blueTeamRemainingBlocks.subList(0, Math.min(blueTeamRemainingBlocks.size(), 4));
            default -> throw new IllegalStateException("Unexpected value: " + team);
        };
    }

    public static List<String> getOnlinePlayersString() {
        List<String> onlinePlayers = new ArrayList<>();
        Bukkit.getOnlinePlayers().forEach((Player player) -> onlinePlayers.add(player.getName()));
        return onlinePlayers;
    }

    public static List<String> getOnlineTeamPlayers(String team) {
        List<String> onlineTeamPlayers = new ArrayList<>();

        if (team.equals("red")) {
            for (String player : redTeamPlayers) {
                if (Bukkit.getPlayer(player) != null) {
                    onlineTeamPlayers.add(player);
                }
            }
        } else {
            for (String player : blueTeamPlayers) {
                if (Bukkit.getPlayer(player) != null) {
                    onlineTeamPlayers.add(player);
                }
            }
        }
        return onlineTeamPlayers;
    }

    public static void playSound(Sound sound) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player, sound, 1F, 1F);
        }
    }

    public static String getCoords(Location location) {
        return String.format("%.1f, %.1f, %.1f", location.getX(), location.getY(), location.getZ());
    }

    public static void collect(String p) {
        if (collectAmount.containsKey(p)) {
            int currentAmount = collectAmount.get(p);
            collectAmount.put(p, currentAmount + 1);
        } else {
            collectAmount.put(p, 1);
        }
    }

    public static GameState getCurrentGameState() {
        return currentGameState;
    }

    public static void setCurrentGameState(GameState currentGameState) {
        Game.currentGameState = currentGameState;
    }

    static String removeAndGetNewVisibleTarget(List<String> remainingBlocks, String completedBlock) {
        boolean hadHiddenTarget = remainingBlocks.size() > 4;
        if (!remainingBlocks.remove(completedBlock) || !hadHiddenTarget) {
            return null;
        }
        return remainingBlocks.get(3);
    }

    private static World getPrimaryWorld() {
        return Bukkit.getWorlds().stream()
                .filter(world -> world.getEnvironment() == World.Environment.NORMAL)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No overworld is loaded"));
    }
}
