package top.lqsnow.blockracing.managers;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import top.lqsnow.blockracing.Main;
import top.lqsnow.blockracing.utils.ConsoleCommandHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.Random;

import static org.bukkit.Bukkit.getServer;
import static top.lqsnow.blockracing.commands.BlockRacing.blockAmount;
import static top.lqsnow.blockracing.listeners.EventListener.blueTeamPlayerString;
import static top.lqsnow.blockracing.listeners.EventListener.redTeamPlayerString;
import static top.lqsnow.blockracing.managers.BlockManager.blocks;
import static top.lqsnow.blockracing.managers.InventoryManager.setItem;

public class GameManager {
    public static ArrayList<String> redCurrentBlocks = new ArrayList<>();
    public static ArrayList<String> blueCurrentBlocks = new ArrayList<>();
    public static ArrayList<String> redTeamBlocks = new ArrayList<>();
    public static ArrayList<String> blueTeamBlocks = new ArrayList<>();
    static Random r = new Random();
    public static ArrayList<Player> redTeamPlayer = new ArrayList<>();
    public static ArrayList<Player> blueTeamPlayer = new ArrayList<>();
    public static ArrayList<Player> inGamePlayer = new ArrayList<>();
    public static boolean gameStart = false;

    
    public static void playerLogin(Player player) {
        if (!gameStart) {
            ConsoleCommandHandler.send("gamemode adventure @a");
            ConsoleCommandHandler.send("tellraw " + player.getName() + " {\"text\": \"\\u00a7b\\u00a7l请加入选队！\"}");
            ConsoleCommandHandler.send("tellraw " + player.getName() + " {\"text\": \"\\u00a7c[加入红队]\",\"clickEvent\": {\"action\": \"run_command\",\"value\": \"/blockracing teamjoinred\"},\"extra\": [{\"text\": \"\\u00a79[加入蓝队]\",\"clickEvent\": {\"action\": \"run_command\",\"value\": \"/blockracing teamjoinblue\"}}]}");
        } else {
            if (redTeamPlayerString.contains(player.getName())) {
                if (!redTeamPlayer.contains(player)) {
                    redTeamPlayer.add(player);
                }
                return;
            }else if (blueTeamPlayerString.contains(player.getName())) {
                if (!blueTeamPlayer.contains(player)) {
                    blueTeamPlayer.add(player);
                }
                return;
            }else {
                player.setGameMode(GameMode.SPECTATOR);
                player.sendMessage(ChatColor.RED + "游戏已开始，您现在为旁观者！");
            }
        }
    }
    
    public static void gameStart() {
        gameStart = true;
        setBlocks();
        boolean flag = false;
        for (String s : blocks) {
            try {
                setItem(s, 1, "1", "1", 0, "settings");
            } catch (Exception e) {
                Bukkit.getLogger().severe("名为 " + s + " 的物品不存在！请检查配置文件！");
                ConsoleCommandHandler.send("tellraw @a \"\u00a74" + "名为 " + s + " 的物品不存在！请检查配置文件！" + "\"");
                flag = true;
            }
        }

        if (flag) {
            getServer().getPluginManager().disablePlugin(Main.getInstance());
            return;
        }
        BukkitTask gameTick = new GameTick().runTaskTimer(Main.getInstance(), 1L, 2L);
        ScoreboardManager.update();

        World playerWorld = Bukkit.getWorld("world");
        for (Player player : Bukkit.getOnlinePlayers()) {
            double randX = r.nextInt(20000) - 10000;
            double randZ = r.nextInt(20000) - 10000;
            Location offset = new Location(playerWorld, randX, 0, randZ).toHighestLocation();
            double Y = offset.getY() + 1;
            offset.setY(Y);
            player.teleport(offset);
            player.sendMessage(ChatColor.GREEN + "已传送到 " + offset.getX() + " " + offset.getY() + " " + offset.getZ());
            player.setHealth(20);
            player.setExp(0);
            player.setLevel(0);
            player.setFoodLevel(20);
            player.setSaturation(10);
            ConsoleCommandHandler.send("effect clear @a");
            ConsoleCommandHandler.send("effect give @a fire_resistance 60 0 true");
            ConsoleCommandHandler.send("effect give @a water_breathing 60 0 true");
            ConsoleCommandHandler.send("clear @a");
            ConsoleCommandHandler.send("time set day");
            ConsoleCommandHandler.send("difficulty easy");
            for (Player p : redTeamPlayer) {
                ConsoleCommandHandler.send("gamemode survival " + p.getName());
            }
            for (Player p : blueTeamPlayer) {
                ConsoleCommandHandler.send("gamemode survival " + p.getName());
            }
        }


        for (Player p : redTeamPlayer) {
            if (!inGamePlayer.contains(p)) {
                redTeamPlayer.remove(p.getPlayer());
                redTeamPlayerString.remove(Objects.requireNonNull(p.getPlayer()).getName());
            }
        }
        for (Player p : blueTeamPlayer) {
            if (!inGamePlayer.contains(p)) {
                blueTeamPlayer.remove(p.getPlayer());
                blueTeamPlayerString.remove(Objects.requireNonNull(p.getPlayer()).getName());
            }
        }
    }

    private static void setBlocks() {
        ArrayList<String> blocks_temp = new ArrayList<>();
        Collections.addAll(blocks_temp, blocks);
        for (int i = 0; i < blockAmount; i++) {
            int a = r.nextInt(blocks.length - i);
            redTeamBlocks.add(blocks_temp.get(a));
            blocks_temp.remove(a);
        }
        blocks_temp.clear();
        Collections.addAll(blocks_temp, blocks);
        for (int i = 0; i < blockAmount; i++) {
            int a = r.nextInt(blocks.length - i);
            blueTeamBlocks.add(blocks_temp.get(a));
            blocks_temp.remove(a);
        }
        setCurrentBlocks();
    }

    private static void setCurrentBlocks() {
        redCurrentBlocks.add(redTeamBlocks.get(0));
        redCurrentBlocks.add(redTeamBlocks.get(1));
        redCurrentBlocks.add(redTeamBlocks.get(2));
        redCurrentBlocks.add(redTeamBlocks.get(3));
        blueCurrentBlocks.add(blueTeamBlocks.get(0));
        blueCurrentBlocks.add(blueTeamBlocks.get(1));
        blueCurrentBlocks.add(blueTeamBlocks.get(2));
        blueCurrentBlocks.add(blueTeamBlocks.get(3));
        blueTeamBlocks.remove(0);
        blueTeamBlocks.remove(0);
        blueTeamBlocks.remove(0);
        blueTeamBlocks.remove(0);
        redTeamBlocks.remove(0);
        redTeamBlocks.remove(0);
        redTeamBlocks.remove(0);
        redTeamBlocks.remove(0);
    }

    public static void redWin() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.closeInventory();
        }
        ConsoleCommandHandler.send("tellraw @a \"\\u00a7c\\u00a7l红队获胜！\"");
        ConsoleCommandHandler.send("title @a title \"\\u00a7c\\u00a7l红队获胜！\"");
        ConsoleCommandHandler.send("gamemode spectator @a");
        ConsoleCommandHandler.send("execute as @a at @s run playsound minecraft:ui.toast.challenge_complete player @s");
    }

    public static void blueWin() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.closeInventory();
        }
        ConsoleCommandHandler.send("tellraw @a \"\\u00a79\\u00a7l蓝队获胜！\"");
        ConsoleCommandHandler.send("title @a title \"\\u00a79\\u00a7l蓝队获胜！\"");
        ConsoleCommandHandler.send("gamemode spectator @a");
        ConsoleCommandHandler.send("execute as @a at @s run playsound minecraft:ui.toast.challenge_complete player @s");
    }
}
