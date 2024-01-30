package top.lqsnow.blockracing.managers;

import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.remain.CompMaterial;
import top.lqsnow.blockracing.Main;
import top.lqsnow.blockracing.utils.TranslationUtil;

import java.util.*;

import static top.lqsnow.blockracing.listeners.BasicListener.editAmountPlayer;
import static top.lqsnow.blockracing.managers.Block.*;
import static top.lqsnow.blockracing.managers.Gui.*;
import static top.lqsnow.blockracing.managers.Scoreboard.updateScoreboard;
import static top.lqsnow.blockracing.managers.Team.*;
import static top.lqsnow.blockracing.utils.ColorUtil.t;
import static top.lqsnow.blockracing.utils.CommandUtil.*;

public class Game {
    public enum GameState {PREGAME, INGAME, END}

    @Getter
    @Setter
    public static GameState currentGameState = GameState.PREGAME;
    public static List<String> readyPlayers = new ArrayList<>();
    public static int redTeamScore = 0;
    public static int blueTeamScore = 0;
    public static int redTeamCurrentBlockAmount = 0;
    public static int blueTeamCurrentBlockAmount = 0;
    public static int redTeamTotalBlockAmount = 0;
    public static int blueTeamTotalBlockAmount = 0;
    public static List<String> freeRandomTPList = new ArrayList<>();
    public static Location redWaypoint1;
    public static Location redWaypoint2;
    public static Location redWaypoint3;
    public static Location blueWaypoint1;
    public static Location blueWaypoint2;
    public static Location blueWaypoint3;
    public static int redTeamRollCount;
    public static int blueTeamRollCount;
    public static List<String> redRollPlayers = new ArrayList<>();
    public static List<String> blueRollPlayers = new ArrayList<>();
    public static List<String> inGamePlayers = new ArrayList<>();
    public static ArrayList<String> locateCommandPermission = new ArrayList<>();
    public static int locateCost;

    public static void playerLogin(Player player) {
        Scoreboard.showScoreboard(player);

        if (getCurrentGameState().equals(GameState.PREGAME)) {
            player.setGameMode(GameMode.ADVENTURE);
            player.sendMessage(Message.NOTICE_WELCOME.getString());
            player.sendMessage(t("&eNot your language? Please follow the tutorial to change the language: https://github.com/LQSnow/BlockRacing/blob/3.0/docs/en/README-en.md"));
            player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
        } else if (getCurrentGameState().equals(GameState.INGAME)) {
            if (!redTeamPlayers.contains(player.getName()) && !blueTeamPlayers.contains(player.getName())) {
                player.setGameMode(GameMode.SPECTATOR);
                player.sendMessage(Message.NOTICE_SPECTATOR_JOIN.getString());
            }
        }

        // The permissions will disappear when the player exits and re-enters, permissions need to be given again.
        if (locateCommandPermission.contains(player.getName())) {
            player.addAttachment(Main.getInstance(), "minecraft.command.locate", true);
        }
    }

    public static void playerQuit(Player player) {
        redRollPlayers.remove(player.getName());
        blueRollPlayers.remove(player.getName());
        readyPlayers.remove(player.getName());
        editAmountPlayer.remove(player.getName());
    }

    public static void playerReady(Player player) {
        if (!readyPlayers.contains(player.getName())) {
            readyPlayers.add(player.getName());
            sendAll(Message.NOTICE_READY.getString().replace("%player%", player.getName()));
            // Check if the game can start, just notice players
            if (readyPlayers.size() > 1 && readyPlayers.size() == Bukkit.getOnlinePlayers().size()) {
                sendAll(Message.NOTICE_ALL_READY.getString());
            }
        } else {
            readyPlayers.remove(player.getName());
            sendAll(Message.NOTICE_CANCEL_READY.getString().replace("%player%", player.getName()));
        }
    }

    // Check if the game can start. If not, send the reason to the player; if possible, start the game directly
    public static void checkStartDemands(Player player) {

        // Game already start
        if (getCurrentGameState().equals(GameState.INGAME)) return;

        // Not enough players
        if (!(Bukkit.getOnlinePlayers().size() > 1)) {
            player.sendMessage(Message.NOTICE_NOT_ENOUGH_PLAYERS.getString());
            return;
        }

        // Exist unready players
        if (!(readyPlayers.size() == Bukkit.getOnlinePlayers().size())) {
            List<String> unreadyPlayers = getOnlinePlayersString();
            unreadyPlayers.removeAll(readyPlayers);
            player.sendMessage(Message.NOTICE_EXIST_UNREADY.getString());
            player.sendMessage(Message.NOTICE_UNREADY_PLAYERS.getString() + unreadyPlayers);
            return;
        }

        // Exist empty team
        if (redTeamPlayers.isEmpty() || blueTeamPlayers.isEmpty()) {
            player.sendMessage(Message.NOTICE_EMPTY_TEAM.getString());
            return;
        }

        // Blocks have problems
        if (!checkBlock()) {
            return;
        }

        // Start the game
        sendAll(Message.NOTICE_START.getString());
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
        World world = Bukkit.getWorlds().get(0);
        world.setDifficulty(Difficulty.EASY);
        world.setTime(1000);

        // World border
        world.getWorldBorder().setCenter(world.getSpawnLocation());
        world.getWorldBorder().setSize(59999968);

        // Processing of unselected team players (spectators)
        inGamePlayers.addAll(getOnlinePlayersString());
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!redTeamPlayers.contains(player.getName()) && !blueTeamPlayers.contains(player.getName())) {
                player.setGameMode(GameMode.SPECTATOR);
                player.sendMessage(Message.NOTICE_SPECTATOR.getString());
                inGamePlayers.remove(player.getName());
            }
        }

        // Settings for each player
        for (String p : inGamePlayers) {
            // General
            Player player = Bukkit.getPlayer(p);
            player.getInventory().clear();
            RandomTeleport(player, true);
            player.setHealth(20);
            player.setExp(0);
            player.setLevel(0);
            player.setFoodLevel(20);
            player.setSaturation(10);
            player.setGameMode(GameMode.SURVIVAL);
            for (PotionEffect effect : player.getActivePotionEffects()) {
                player.removePotionEffect(effect.getType());
            }
            player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 1200, 4, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 1200, 4, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 1200, 4, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, -1, 0, false, false));

            // Speed mode
            if (Setting.isSpeedMode()) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, -1, 4, false, false));
                player.getInventory().addItem(ItemCreator.of(CompMaterial.IRON_PICKAXE).enchant(Enchantment.SILK_TOUCH, 1).make());
                player.getInventory().addItem(ItemCreator.of(CompMaterial.COOKED_BEEF).amount(64).make());

                ItemStack damagedElytra = new ItemStack(Material.ELYTRA);
                damagedElytra.setDurability((short) (damagedElytra.getType().getMaxDurability() - 1));
                ItemMeta elytraMeta = damagedElytra.getItemMeta();
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

        // Remove players from the team who are not online when starting the game
        for (String p : redTeamPlayers) {
            if (!getOnlinePlayersString().contains(p)) {
                redTeamPlayers.remove(p);
                inGamePlayers.remove(p);
                redTeam.removeEntry(p);
            }
        }
        for (String p : blueTeamPlayers) {
            if (!getOnlinePlayersString().contains(p)) {
                blueTeamPlayers.remove(p);
                inGamePlayers.remove(p);
                blueTeam.removeEntry(p);
            }
        }

        Bukkit.getLogger().info("Red team players: " + redTeamPlayers.toString());
        Bukkit.getLogger().info("Blue team players: " + blueTeamPlayers.toString());
        if (Setting.getCurrentGameMode().equals(Setting.GameMode.NORMAL)) Bukkit.getLogger().info("Game mode: Normal");
        else if (Setting.getCurrentGameMode().equals(Setting.GameMode.RACING)) Bukkit.getLogger().info("Game mode: Racing");
        Bukkit.getLogger().info(Setting.isSpeedMode() ? "Speed mode: On" : "Speed mode: Off");
    }

    // Roll
    public static void roll(Player player) {
        if (redTeamPlayers.contains(player.getName())) {
            if (redTeamRollCount >= 3) {
                player.sendMessage(Message.NOTICE_CANNOT_ROLL.getString());
                return;
            }
            if (!redRollPlayers.contains(player.getName())) {
                redRollPlayers.add(player.getName());
                sendRed(Message.NOTICE_ROLL_REQUEST.getString().replace("%player%", player.getName()));
            } else {
                redRollPlayers.remove(player.getName());
                sendRed(Message.NOTICE_ROLL_REQUEST_CANCEL.getString().replace("%player%", player.getName()));
            }
        } else if (blueTeamPlayers.contains(player.getName())) {
            if (blueTeamRollCount >= 3) {
                player.sendMessage(Message.NOTICE_CANNOT_ROLL.getString());
                return;
            }
            if (!blueRollPlayers.contains(player.getName())) {
                blueRollPlayers.add(player.getName());
                sendBlue(Message.NOTICE_ROLL_REQUEST.getString().replace("%player%", player.getName()));
            } else {
                blueRollPlayers.remove(player.getName());
                sendBlue(Message.NOTICE_ROLL_REQUEST_CANCEL.getString().replace("%player%", player.getName()));
            }
        }
    }

    public static void locate(Player player) {
        if (locateCommandPermission.contains(player.getName())) {
            player.sendMessage(Message.NOTICE_LOCATE_ALREADY_BOUGHT.getString());
            return;
        }
        if (redTeamPlayers.contains(player.getName())) {
            if (redTeamScore >= locateCost) {
                redTeamScore -= locateCost;
                updateScoreboard();
                locateCommandPermission.add(player.getName());
                sendAll(Message.NOTICE_BUY_LOCATE.getString().replace("%player%", player.getName()));
                player.addAttachment(Main.getInstance(), "minecraft.command.locate", true);
            } else {
                player.sendMessage(Message.NOTICE_NOT_ENOUGH_SCORE.getString());
            }
        } else if (blueTeamPlayers.contains(player.getName())) {
            if (blueTeamScore >= locateCost) {
                blueTeamScore -= locateCost;
                updateScoreboard();
                locateCommandPermission.add(player.getName());
                sendAll(Message.NOTICE_BUY_LOCATE.getString().replace("%player%", player.getName()));
                player.addAttachment(Main.getInstance(), "minecraft.command.locate", true);
            } else {
                player.sendMessage(Message.NOTICE_NOT_ENOUGH_SCORE.getString());
            }
        }
    }

    // Random Teleport
    public static void RandomTeleport(Player player, boolean avoidOcean) {
        Random random = new Random();
        World playerWorld = Bukkit.getWorlds().get(0);
        double randX = random.nextInt(20000) - 10000;
        double randZ = random.nextInt(20000) - 10000;
        Location offset = playerWorld.getHighestBlockAt(new Location(playerWorld, randX, 0, randZ)).getLocation();
        double Y = offset.getY() + 1;
        offset.setY(Y);
        player.teleport(offset);
        player.sendMessage(Message.NOTICE_TP_SUCCESS.getString().replace("%x%", String.valueOf(offset.getX())).replace("%y%", String.valueOf(offset.getY())).replace("%z%", String.valueOf(offset.getZ())));
        if (avoidOcean) {
            Biome biome = player.getLocation().getBlock().getBiome();
            if (biome == Biome.OCEAN || biome == Biome.DEEP_OCEAN || biome == Biome.DEEP_COLD_OCEAN || biome == Biome.LUKEWARM_OCEAN || biome == Biome.DEEP_FROZEN_OCEAN || biome == Biome.COLD_OCEAN || biome == Biome.WARM_OCEAN || biome == Biome.DEEP_LUKEWARM_OCEAN || biome == Biome.FROZEN_OCEAN) {
                player.sendMessage(Message.NOTICE_TP_OCEAN.getString());
                RandomTeleport(player, true);
            }
        }
    }

    // Waypoints
    // Return value: true -> waypoint changed, false -> waypoint doesn't change
    public static boolean waypoint(Player player, int index, ClickType clickType) {
        String team = redTeamPlayers.contains(player.getName()) ? "red" : (blueTeamPlayers.contains(player.getName()) ? "blue" : "");

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
                        player.sendMessage(Message.NOTICE_TP_SUCCESS.getString().replace("%x%", String.valueOf(waypoint.getX())).replace("%y%", String.valueOf(waypoint.getY())).replace("%z%", String.valueOf(waypoint.getZ())));
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
            case "red" -> switch (index) {
                case 1 -> redWaypoint1;
                case 2 -> redWaypoint2;
                case 3 -> redWaypoint3;
                default -> null;
            };
            case "blue" -> switch (index) {
                case 1 -> blueWaypoint1;
                case 2 -> blueWaypoint2;
                case 3 -> blueWaypoint3;
                default -> null;
            };
            default -> null;
        };
    }

    public static void setWaypoint(Player player, String team, int index) {
        Location waypoint = player.getLocation();
        switch (team) {
            case "red" -> {
                switch (index) {
                    case 1 -> redWaypoint1 = waypoint;
                    case 2 -> redWaypoint2 = waypoint;
                    case 3 -> redWaypoint3 = waypoint;
                }
            }
            case "blue" -> {
                switch (index) {
                    case 1 -> blueWaypoint1 = waypoint;
                    case 2 -> blueWaypoint2 = waypoint;
                    case 3 -> blueWaypoint3 = waypoint;
                }
            }
        }
    }

    private static void removeWaypoint(Player player, int index) {
        TextComponent message = new TextComponent(Message.NOTICE_REMOVE_WAYPOINT.getString().replace("%index%", String.valueOf(index)));
        message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/waypoint remove " + index));
        player.spigot().sendMessage(message);
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
            if (getCurrentGameState().equals(GameState.INGAME)) this.cancel();
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
                this.cancel();
            }
            if (blueTeamRemainingBlocks.isEmpty()) {
                blueWin();
                this.cancel();
            }

            // Roll check
            if (!redRollPlayers.isEmpty()) checkRedRoll();
            if (!blueRollPlayers.isEmpty()) checkBlueRoll();

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
            sendAll(Message.NOTICE_RED_ROLL_SUCCESS.getString());
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
            sendAll(Message.NOTICE_BLUE_ROLL_SUCCESS.getString());
            blueTeamRollCount += 1;
            blueRollPlayers.clear();
            updateScoreboard();
        }
    }

    private static void setLocateScore() {
        if (Setting.getBlockAmount() <= 20) locateCost = 2;
        else if (Setting.getBlockAmount() <= 50) locateCost = 3;
        else if (Setting.getBlockAmount() <= 100) locateCost = 5;
        else if (Setting.getBlockAmount() <= 200) locateCost = 8;
        else locateCost = 10;
    }

    private static void checkRedInventory() {
        // Complete from player
        for (String player : redTeamPlayers) {
            for (String block : getCurrentBlocks("red")) {
                Player p = Bukkit.getPlayer(player);
                if (p == null) continue;
                if (p.getInventory().contains(Material.valueOf(block))) {
                    redTaskComplete(block, player);
                    return;
                }
            }
        }
        // Complete from team chest
        for (String block : getCurrentBlocks("red")) {
            if (redTeamChest1.contains(Material.valueOf(block)) || redTeamChest2.contains(Material.valueOf(block)) || redTeamChest3.contains(Material.valueOf(block))) {
                redTaskComplete(block, Message.NOTICE_RED_TEAM_CHEST.getString());
                return;
            }
        }
    }

    private static void checkBlueInventory() {
        // Complete from player
        for (String player : blueTeamPlayers) {
            for (String block : getCurrentBlocks("blue")) {
                Player p = Bukkit.getPlayer(player);
                if (p == null) continue;
                if (p.getInventory().contains(Material.valueOf(block))) {
                    blueTaskComplete(block, player);
                    return;
                }
            }
        }
        // Complete from team chest
        for (String block : getCurrentBlocks("blue")) {
            if (blueTeamChest1.contains(Material.valueOf(block)) || blueTeamChest2.contains(Material.valueOf(block)) || blueTeamChest3.contains(Material.valueOf(block))) {
                blueTaskComplete(block, Message.NOTICE_BLUE_TEAM_CHEST.getString());
                return;
            }
        }
    }

    public static void redTaskComplete(String block, String player) {
        sendAll(Message.NOTICE_RED_COLLECT.getString().replace("%block%", TranslationUtil.getValue(block)).replace("%player%", player));
        Bukkit.getLogger().info(Message.NOTICE_RED_COLLECT.getString().replace("%block%", TranslationUtil.getValue(block)).replace("%player%", player).replaceAll("§.", ""));
        playSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
        redTeamRemainingBlocks.remove(block);
        if (Setting.isSpeedMode()) redTeamScore += 3;
        else redTeamScore += 1;
        redTeamCurrentBlockAmount += 1;
        updateScoreboard();
        // Put items into the opponent's team chest
        if (Setting.getCurrentGameMode().equals(Setting.GameMode.NORMAL)) {
            if (blueTeamChest1.firstEmpty() != -1)
                blueTeamChest1.setItem(blueTeamChest1.firstEmpty(), ItemCreator.of(CompMaterial.valueOf(block)).amount(64).make());
            else if (blueTeamChest2.firstEmpty() != -1)
                blueTeamChest2.setItem(blueTeamChest2.firstEmpty(), ItemCreator.of(CompMaterial.valueOf(block)).amount(64).make());
            else if (blueTeamChest3.firstEmpty() != -1)
                blueTeamChest3.setItem(blueTeamChest3.firstEmpty(), ItemCreator.of(CompMaterial.valueOf(block)).amount(64).make());
            else
                sendAll(Message.NOTICE_TEAM_CHEST_FULL.getString().replace("%team%", Message.TEAM_BLUE_NAME.getString()).replace("%block%", TranslationUtil.getValue(block)));
        }
    }

    public static void blueTaskComplete(String block, String player) {
        sendAll(Message.NOTICE_BLUE_COLLECT.getString().replace("%block%", TranslationUtil.getValue(block)).replace("%player%", player));
        Bukkit.getLogger().info(Message.NOTICE_BLUE_COLLECT.getString().replace("%block%", TranslationUtil.getValue(block)).replace("%player%", player).replaceAll("§.", ""));
        playSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
        blueTeamRemainingBlocks.remove(block);
        if (Setting.isSpeedMode()) blueTeamScore += 3;
        else blueTeamScore += 1;
        blueTeamCurrentBlockAmount += 1;
        updateScoreboard();
        // Put items into the opponent's team chest
        if (Setting.getCurrentGameMode().equals(Setting.GameMode.NORMAL)) {
            if (redTeamChest1.firstEmpty() != -1)
                redTeamChest1.setItem(redTeamChest1.firstEmpty(), ItemCreator.of(CompMaterial.valueOf(block)).amount(64).make());
            else if (redTeamChest2.firstEmpty() != -1)
                redTeamChest2.setItem(redTeamChest2.firstEmpty(), ItemCreator.of(CompMaterial.valueOf(block)).amount(64).make());
            else if (redTeamChest3.firstEmpty() != -1)
                redTeamChest3.setItem(redTeamChest3.firstEmpty(), ItemCreator.of(CompMaterial.valueOf(block)).amount(64).make());
            else
                sendAll(Message.NOTICE_TEAM_CHEST_FULL.getString().replace("%team%", Message.TEAM_RED_NAME.getString()).replace("%block%", TranslationUtil.getValue(block)));
        }
    }

    public static void redWin() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.closeInventory();
            player.sendTitle(Message.NOTICE_RED_WIN.getString(), null);
            player.setGameMode(GameMode.SPECTATOR);
        }
        sendAll(Message.NOTICE_RED_WIN.getString());
        playSound(Sound.UI_TOAST_CHALLENGE_COMPLETE);
    }

    public static void blueWin() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.closeInventory();
            player.sendTitle(Message.NOTICE_BLUE_WIN.getString(), null);
            player.setGameMode(GameMode.SPECTATOR);
        }
        sendAll(Message.NOTICE_BLUE_WIN.getString());
        playSound(Sound.UI_TOAST_CHALLENGE_COMPLETE);
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
}
