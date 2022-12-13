package top.lqsnow.blockracing.managers;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import top.lqsnow.blockracing.Main;
import top.lqsnow.blockracing.utils.ConsoleCommandHandler;
import top.lqsnow.blockracing.utils.ItemBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.Random;

import static org.bukkit.Bukkit.getServer;
import static top.lqsnow.blockracing.commands.BlockRacing.blockAmount;
import static top.lqsnow.blockracing.listeners.EventListener.blueTeamPlayerString;
import static top.lqsnow.blockracing.listeners.EventListener.redTeamPlayerString;
import static top.lqsnow.blockracing.managers.BlockManager.blocks;
import static top.lqsnow.blockracing.managers.InventoryManager.*;
import static top.lqsnow.blockracing.managers.ScoreboardManager.*;

public class GameManager {
    public static ArrayList<String> redCurrentBlocks = new ArrayList<>();
    public static ArrayList<String> blueCurrentBlocks = new ArrayList<>();
    public static ArrayList<String> redTeamBlocks = new ArrayList<>();
    public static ArrayList<String> blueTeamBlocks = new ArrayList<>();
    static Random r = new Random();
    public static ArrayList<Player> redTeamPlayer = new ArrayList<>();
    public static ArrayList<Player> blueTeamPlayer = new ArrayList<>();
    public static ArrayList<Player> inGamePlayer = new ArrayList<>();
    public static ArrayList<Player> var = new ArrayList<>();
    public static boolean gameStart = false;

    // 玩家登录时的设置
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
            }else if (blueTeamPlayerString.contains(player.getName())) {
                if (!blueTeamPlayer.contains(player)) {
                    blueTeamPlayer.add(player);
                }
            }else {
                player.setGameMode(GameMode.SPECTATOR);
                player.sendMessage(ChatColor.RED + "游戏已开始，您现在为旁观者！");
            }
        }
    }

    // 游戏开始时的设置
    public static void gameStart() {
        gameStart = true;
        setBlocks();

        // 检查方块库是否正常
        boolean flag = false;
        for (String s : blocks) {
            try {
                ItemStack stack = new ItemStack(Material.valueOf(s));
                ItemBuilder TeamChestBuilder = new ItemBuilder(stack);
                TeamChestBuilder.setAmount(64);
                TeamChestBuilder.toItemStack();
                settings.setItem(0, stack);
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

        // 未选队玩家（旁观者）处理
        var.addAll(Bukkit.getOnlinePlayers());
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!redTeamPlayer.contains(player) && !blueTeamPlayer.contains(player)) {
                player.setGameMode(GameMode.SPECTATOR);
                player.sendMessage(ChatColor.RED + "游戏已开始，由于您未选择队伍，您现在为旁观者！");
                var.remove(player);
            }
        }



        // 随机传送
        World playerWorld = Bukkit.getWorld("world");
        for (Player player : var) {
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
            for (Player p : var) {
                ConsoleCommandHandler.send("gamemode survival " + p.getName());
            }
        }

        for (Player p : redTeamPlayer) {
            if (!inGamePlayer.contains(p)) {
                redTeamPlayer.remove(p.getPlayer());
                redTeamPlayerString.remove(Objects.requireNonNull(p.getPlayer()).getName());
                red.removeEntry(p.getName());
            }
        }
        for (Player p : blueTeamPlayer) {
            if (!inGamePlayer.contains(p)) {
                blueTeamPlayer.remove(p.getPlayer());
                blueTeamPlayerString.remove(Objects.requireNonNull(p.getPlayer()).getName());
                blue.removeEntry(p.getName());
            }
        }
    }

    // 设置两个队伍的目标方块
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

    // 胜利检测
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
