package top.lqsnow.blockracing.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import top.lqsnow.blockracing.Main;
import top.lqsnow.blockracing.utils.ItemBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.Random;

import static org.bukkit.Bukkit.getServer;
import static top.lqsnow.blockracing.listeners.BasicEventListener.*;
import static top.lqsnow.blockracing.managers.BlockManager.blocks;
import static top.lqsnow.blockracing.managers.InventoryManager.*;
import static top.lqsnow.blockracing.managers.ScoreboardManager.*;
import static top.lqsnow.blockracing.listeners.InventoryEventListener.*;
import static top.lqsnow.blockracing.utils.ColorUtil.t;
import static top.lqsnow.blockracing.utils.ConsoleCommandHandler.playSound;
import static top.lqsnow.blockracing.utils.ConsoleCommandHandler.sendAll;

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
    public static int locateCost;
    public static boolean extremeMode = false;

    // 玩家登录时的设置
    public static void playerLogin(Player player) {
        // ArrayList里装Player对象时，如果该玩家退出重进，ArrayList里的Player就不是游戏里的Player了，所以需要重新添加
        if (redTeamPlayerString.contains(player.getName())) {
            redTeamPlayer.removeIf(p -> p.getName().equals(player.getName()));
            redTeamPlayer.add(player);
        } else if (blueTeamPlayerString.contains(player.getName())) {
            blueTeamPlayer.removeIf(p -> p.getName().equals(player.getName()));
            blueTeamPlayer.add(player);
        }

        if (!gameStart) {
            player.setGameMode(GameMode.ADVENTURE);
            player.sendMessage(t("&b&l欢迎来到方块竞速！按Shift+F打开菜单进行选队和准备！如果没反应，请检查潜行和切换副手的快捷键！"));
            player.getWorld().setDifficulty(Difficulty.PEACEFUL);
        } else {
            if (!redTeamPlayerString.contains(player.getName()) && !blueTeamPlayerString.contains(player.getName())) {
                player.setGameMode(GameMode.SPECTATOR);
                player.sendMessage(ChatColor.RED + "游戏已开始，您现在为旁观者！");
            }
        }
    }

    // 游戏开始时的设置
    public static void gameStart() {
        gameStart = true;
        editAmountPlayer.clear();
        if (!extremeMode) setBlocks();
        else setExtremeBlocks();

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
                sendAll("&4名为 " + s + " 的物品不存在！请检查配置文件！");
                flag = true;
            }
        }
        if (flag) {
            getServer().getPluginManager().disablePlugin(Main.getInstance());
            return;
        }

        if (blockAmount <= 20) locateCost = 2;
        else if (blockAmount <= 50) locateCost = 3;
        else if (blockAmount <= 100) locateCost = 5;
        else if (blockAmount <= 200) locateCost = 8;
        else locateCost = 10;
        InventoryManager.setLocateItem();

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

        for (Player player : var) {
            randomTeleport(player, true);
            player.setHealth(20);
            player.setExp(0);
            player.setLevel(0);
            player.setFoodLevel(20);
            player.setSaturation(10);
            player.setGameMode(GameMode.SURVIVAL);
            for (PotionEffect effect : player.getActivePotionEffects()) {
                player.removePotionEffect(effect.getType());
            }
            player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 60, 0, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 60, 0, false, false));
            player.getInventory().clear();

        }

        Bukkit.getWorlds().get(0).setTime(1000);
        Bukkit.getWorlds().get(0).setDifficulty(Difficulty.EASY);

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

        Bukkit.getLogger().info("红队本局全部方块：");
        Bukkit.getLogger().info(redCurrentBlocks.toString() + redTeamBlocks.toString());
        Bukkit.getLogger().info("蓝队本局全部方块：");
        Bukkit.getLogger().info(blueCurrentBlocks.toString() + blueTeamBlocks.toString());
        Bukkit.getLogger().info("红队成员：" + redTeamPlayerString.toString());
        Bukkit.getLogger().info("蓝队成员：" + blueTeamPlayerString.toString());
    }


    // 随机传送
    public static void randomTeleport(Player player, boolean avoidOcean) {
        World playerWorld = player.getWorld();
        double randX = r.nextInt(20000) - 10000;
        double randZ = r.nextInt(20000) - 10000;
        Location offset = new Location(playerWorld, randX, 0, randZ).toHighestLocation();
        double Y = offset.getY() + 1;
        offset.setY(Y);
        player.teleport(offset);
        player.sendMessage(ChatColor.GREEN + "已传送到 " + offset.getX() + " " + offset.getY() + " " + offset.getZ());
        if (avoidOcean) {
            Biome biome = player.getLocation().getBlock().getBiome();
            if (biome == Biome.OCEAN || biome == Biome.DEEP_OCEAN || biome == Biome.DEEP_COLD_OCEAN || biome == Biome.LUKEWARM_OCEAN || biome == Biome.DEEP_FROZEN_OCEAN || biome == Biome.COLD_OCEAN || biome == Biome.WARM_OCEAN || biome == Biome.DEEP_LUKEWARM_OCEAN || biome == Biome.FROZEN_OCEAN) {
                player.sendMessage(ChatColor.YELLOW + "检测您传送到了海洋，正在重新传送");
                randomTeleport(player, true);
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

    private static void setExtremeBlocks() {
        ArrayList<String> blocks_temp = new ArrayList<>();
        Collections.addAll(blocks_temp, blocks);
        for (int i = 0; i < blockAmount; i++) {
            int a = r.nextInt(blocks.length - i);
            redTeamBlocks.add(blocks_temp.get(a));
            blueTeamBlocks.add(blocks_temp.get(a));
            blocks_temp.remove(a);
        }
        setCurrentBlocks();
    }

    // 胜利检测
    public static void redWin() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.closeInventory();
            player.showTitle(Title.title(Component.text(t("&c&l红队获胜！")), Component.empty()));
            player.setGameMode(GameMode.SPECTATOR);
        }
        sendAll("&c&l红队获胜！");
        playSound(Sound.UI_TOAST_CHALLENGE_COMPLETE);
    }

    public static void blueWin() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.closeInventory();
            player.showTitle(Title.title(Component.text(t("&9&l蓝队获胜！")), Component.empty()));
            player.setGameMode(GameMode.SPECTATOR);
        }
        sendAll("&9&l蓝队获胜！");
        playSound(Sound.UI_TOAST_CHALLENGE_COMPLETE);
    }
}
