package top.lqsnow.blockracing.managers;

import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.remain.CompMaterial;
import top.lqsnow.blockracing.utils.TranslationUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static top.lqsnow.blockracing.listeners.BasicListener.editAmountPlayer;
import static top.lqsnow.blockracing.managers.Block.*;
import static top.lqsnow.blockracing.managers.Gui.*;
import static top.lqsnow.blockracing.managers.Scoreboard.updateScoreboard;
import static top.lqsnow.blockracing.managers.Team.*;
import static top.lqsnow.blockracing.utils.CommandUtil.sendAll;

public class Game {
    public enum GameState {PREGAME, INGAME, END}

    public static GameState currentGameState = GameState.PREGAME;
    public static List<String> readyPlayers = new ArrayList<>();
    public static int redTeamScore = 0;
    public static int blueTeamScore = 0;
    public static int redTeamCurrentBlockAmount = 0;
    public static int blueTeamCurrentBlockAmount = 0;
    public static int redTeamTotalBlockAmount = 0;
    public static int blueTeamTotalBlockAmount = 0;
    public static List<String> freeRandomTPList;

    public static void playerLogin(Player player) {
        Scoreboard.showScoreboard(player);
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
            player.sendMessage(Message.NOTICE_UNREADY_PLAYERS.getString());
            return;
        }

        // Exist empty team
        if (redTeamPlayers.size() == 0 || blueTeamPlayers.size() == 0) {
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
        // init
        setCurrentGameState(GameState.INGAME);
        closeAllPlayersMenu();
        editAmountPlayer.clear();
        setupBlocks();
        redTeamTotalBlockAmount = redTeamBlocks.size();
        blueTeamTotalBlockAmount = blueTeamBlocks.size();
        updateScoreboard();
        Bukkit.getOnlinePlayers().forEach((Player player) -> freeRandomTPList.add(player.getName()));

    }

    public static void roll(Player player) {

    }

    public static void locate(Player player) {

    }

    public static void randomTP(Player player, boolean avoidOcean) {
        Random random = new Random();
        World playerWorld = Bukkit.getWorlds().get(0);
        double randX = random.nextInt(20000) - 10000;
        double randZ = random.nextInt(20000) - 10000;
        Location offset = playerWorld.getHighestBlockAt(new Location(playerWorld, randX, 0, randZ)).getLocation();
        double Y = offset.getY() + 1;
        offset.setY(Y);
        player.teleport(offset);
        player.sendMessage(ChatColor.GREEN + "已传送到 " + offset.getX() + " " + offset.getY() + " " + offset.getZ());
        if (avoidOcean) {
            Biome biome = player.getLocation().getBlock().getBiome();
            if (biome == Biome.OCEAN || biome == Biome.DEEP_OCEAN || biome == Biome.DEEP_COLD_OCEAN || biome == Biome.LUKEWARM_OCEAN || biome == Biome.DEEP_FROZEN_OCEAN || biome == Biome.COLD_OCEAN || biome == Biome.WARM_OCEAN || biome == Biome.DEEP_LUKEWARM_OCEAN || biome == Biome.FROZEN_OCEAN) {
                player.sendMessage(ChatColor.YELLOW + "检测您传送到了海洋，正在重新传送");
                randomTP(player, true);
            }
        }
    }

    // Run per 2t
    public static class runTick extends BukkitRunnable {

        @Override
        public void run() {

            // Before the game
            // Provide regeneration and saturation effects
            if (getCurrentGameState().equals(GameState.PREGAME)) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 5, 255));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 5, 255));
                }
            }

            // During the game
            if (getCurrentGameState().equals(GameState.INGAME)) {

                // Inventory check
                checkRedInventory();
                checkBlueInventory();

                // Win check
                if (redTeamRemainingBlocks.size() == 0) {
                    redWin();
                    this.cancel();
                }
                if (redTeamRemainingBlocks.size() == 0) {
                    blueWin();
                    this.cancel();
                }
            }
        }
    }

    private static void checkRedInventory() {
        for (String player : redTeamPlayers) {
            for (String block : getCurrentBlocks("red")) {
                Player p = Bukkit.getPlayer(player);
                if (p == null) continue;
                if (p.getInventory().contains(Material.valueOf(block))) {
                    redTaskComplete(block);
                    return;
                }
            }
        }
        for (String block : getCurrentBlocks("red")) {
            if (redTeamChest1.contains(Material.valueOf(block)) || redTeamChest2.contains(Material.valueOf(block)) || redTeamChest3.contains(Material.valueOf(block))) {
                redTaskComplete(block);
                return;
            }
        }
    }

    private static void checkBlueInventory() {
        for (String player : blueTeamPlayers) {
            for (String block : getCurrentBlocks("blue")) {
                Player p = Bukkit.getPlayer(player);
                if (p == null) continue;
                if (p.getInventory().contains(Material.valueOf(block))) {
                    blueTaskComplete(block);
                    return;
                }
            }
        }
        for (String block : getCurrentBlocks("blue")) {
            if (blueTeamChest1.contains(Material.valueOf(block)) || blueTeamChest2.contains(Material.valueOf(block)) || blueTeamChest3.contains(Material.valueOf(block))) {
                blueTaskComplete(block);
                return;
            }
        }
    }
    
    private static void redTaskComplete(String block) {
        sendAll(Message.NOTICE_RED_COLLECT.getString());
        Bukkit.getLogger().info(Message.NOTICE_RED_COLLECT.getString());
        playSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
        redTeamRemainingBlocks.remove(block);
        if (Setting.isSpeedMode()) redTeamScore += 3;
        else redTeamScore += 1;
        updateScoreboard();
        // Put items into the opponent's team chest
        if (Setting.getCurrentGameMode().equals(Setting.GameMode.NORMAL)) {
            if (blueTeamChest1.firstEmpty() != -1) blueTeamChest1.setItem(blueTeamChest1.firstEmpty(), ItemCreator.of(CompMaterial.valueOf(block)).amount(64).make());
            else if (blueTeamChest2.firstEmpty() != -1) blueTeamChest2.setItem(blueTeamChest2.firstEmpty(), ItemCreator.of(CompMaterial.valueOf(block)).amount(64).make());
            else if (blueTeamChest3.firstEmpty() != -1)blueTeamChest3.setItem(blueTeamChest3.firstEmpty(), ItemCreator.of(CompMaterial.valueOf(block)).amount(64).make());
            else sendAll(Message.NOTICE_TEAM_CHEST_FULL.getString().replace("%team%", Message.TEAM_BLUE_NAME.getString()).replace("%block%", TranslationUtil.getValue(block)));
        }
    }

    private static void blueTaskComplete(String block) {
        sendAll(Message.NOTICE_BLUE_COLLECT.getString());
        Bukkit.getLogger().info(Message.NOTICE_BLUE_COLLECT.getString());
        playSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
        blueTeamRemainingBlocks.remove(block);
        if (Setting.isSpeedMode()) blueTeamScore += 3;
        else blueTeamScore += 1;
        updateScoreboard();
        // Put items into the opponent's team chest
        if (Setting.getCurrentGameMode().equals(Setting.GameMode.NORMAL)) {
            if (redTeamChest1.firstEmpty() != -1) redTeamChest1.setItem(redTeamChest1.firstEmpty(), ItemCreator.of(CompMaterial.valueOf(block)).amount(64).make());
            else if (redTeamChest2.firstEmpty() != -1) redTeamChest2.setItem(redTeamChest2.firstEmpty(), ItemCreator.of(CompMaterial.valueOf(block)).amount(64).make());
            else if (redTeamChest3.firstEmpty() != -1)redTeamChest3.setItem(redTeamChest3.firstEmpty(), ItemCreator.of(CompMaterial.valueOf(block)).amount(64).make());
            else sendAll(Message.NOTICE_TEAM_CHEST_FULL.getString().replace("%team%", Message.TEAM_RED_NAME.getString()).replace("%block%", TranslationUtil.getValue(block)));
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

    public static void playSound(Sound sound) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player, sound, 1F, 1F);
        }
    }

    public static GameState getCurrentGameState() {
        return currentGameState;
    }

    public static void setCurrentGameState(GameState currentGameState) {
        Game.currentGameState = currentGameState;
    }
}
