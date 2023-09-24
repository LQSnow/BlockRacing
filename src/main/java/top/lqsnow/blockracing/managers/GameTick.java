package top.lqsnow.blockracing.managers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import top.lqsnow.blockracing.utils.ConsoleCommandHandler;
import top.lqsnow.blockracing.utils.ItemBuilder;

import static top.lqsnow.blockracing.managers.GameManager.*;
import static top.lqsnow.blockracing.managers.InventoryManager.*;
import static top.lqsnow.blockracing.managers.ScoreboardManager.blueTeamScore;
import static top.lqsnow.blockracing.managers.ScoreboardManager.redTeamScore;
import static top.lqsnow.blockracing.utils.ConsoleCommandHandler.playSound;
import static top.lqsnow.blockracing.utils.ConsoleCommandHandler.sendAll;

public class GameTick extends BukkitRunnable {
    public static int redCompleteAmount;
    public static int blueCompleteAmount;

    // 每2t执行一次
    @Override
    public void run() {
        if (gameStart) {
            try {
                // 检查物品栏
                checkRedInventory();
                checkBlueInventory();
            } catch (Exception e) {
                e.printStackTrace();
            }
            // 胜利检测
            if (redCurrentBlocks.size() == 0) {
                GameManager.redWin();
                this.cancel();
            }
            if (blueCurrentBlocks.size() == 0) {
                GameManager.blueWin();
                this.cancel();
            }
        }

        //准备阶段提供生命恢复和饱腹效果
        if (!gameStart) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 5, 255));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 5, 255));
            }
        }
    }

    // 检查红队物品栏
    private void checkRedInventory() throws Exception {
        for (Player player : redTeamPlayer) {
            for (String block : redCurrentBlocks) {
                if (player.getInventory().contains(Material.valueOf(block))) {
                    redTaskComplete(block);
                    return;
                }
            }
        }
        for (String block : redCurrentBlocks) {
            if (redTeamChest1.contains(Material.valueOf(block)) || redTeamChest2.contains(Material.valueOf(block)) || redTeamChest3.contains(Material.valueOf(block))) {
                redTaskComplete(block);
                return;
            }
        }
    }

    // 检查蓝队物品栏
    private void checkBlueInventory() throws Exception {
        for (Player player : blueTeamPlayer) {
            for (String block : blueCurrentBlocks) {
                if (player.getInventory().contains(Material.valueOf(block))) {
                    blueTaskComplete(block);
                    return;
                }
            }
        }
        for (String block : blueCurrentBlocks) {
            if (blueTeamChest1.contains(Material.valueOf(block)) || blueTeamChest2.contains(Material.valueOf(block)) || blueTeamChest3.contains(Material.valueOf(block))) {
                blueTaskComplete(block);
                return;
            }
        }
    }

    // 红队获得目标方块
    private static void redTaskComplete(String block) throws Exception {
        sendAll("&c红队&a收集了&b" + TranslationManager.getValue(block));
        Bukkit.getLogger().info("红队收集了" + TranslationManager.getValue(block));
        playSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
        redCompleteAmount += 1;
        redCurrentBlocks.remove(block);
        if (redTeamBlocks.size() >= 1) {
            redCurrentBlocks.add(redTeamBlocks.get(0));
            redTeamBlocks.remove(0);
        }
        if (mode == 1) redTeamScore += 3;
        else redTeamScore += 1;
        ScoreboardManager.update();
        if (mode != 2) {
            // 将一组该物品放到蓝队队伍箱子
            if (blueTeamChest1.firstEmpty() != -1) {
                ItemStack stack = new ItemStack(Material.valueOf(block));
                ItemBuilder TeamChestBuilder = new ItemBuilder(stack);
                TeamChestBuilder.setAmount(64);
                TeamChestBuilder.toItemStack();
                blueTeamChest1.setItem(blueTeamChest1.firstEmpty(), stack);
            } else if (blueTeamChest2.firstEmpty() != -1) {
                ItemStack stack = new ItemStack(Material.valueOf(block));
                ItemBuilder TeamChestBuilder = new ItemBuilder(stack);
                TeamChestBuilder.setAmount(64);
                TeamChestBuilder.toItemStack();
                blueTeamChest2.setItem(blueTeamChest2.firstEmpty(), stack);
            } else if (blueTeamChest3.firstEmpty() != -1) {
                ItemStack stack = new ItemStack(Material.valueOf(block));
                ItemBuilder TeamChestBuilder = new ItemBuilder(stack);
                TeamChestBuilder.setAmount(64);
                TeamChestBuilder.toItemStack();
                blueTeamChest3.setItem(blueTeamChest3.firstEmpty(), stack);
            } else {
                sendAll("&4蓝队队伍箱子已满！" + TranslationManager.getValue(block) + "无法放入队伍箱子！");
            }
        }
    }

    // 蓝队获得目标方块
    private static void blueTaskComplete(String block) throws Exception {
        sendAll("&9蓝队&a收集了&b" + TranslationManager.getValue(block));
        Bukkit.getLogger().info("蓝队收集了" + TranslationManager.getValue(block));
        playSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
        blueCompleteAmount += 1;
        blueCurrentBlocks.remove(block);
        if (blueTeamBlocks.size() >= 1) {
            blueCurrentBlocks.add(blueTeamBlocks.get(0));
            blueTeamBlocks.remove(0);
        }
        if (mode == 1) blueTeamScore += 3;
        else blueTeamScore += 1;
        ScoreboardManager.update();
        if (mode != 2) {
            // 将一组该物品放到红队队伍箱子
            if (redTeamChest1.firstEmpty() != -1) {
                ItemStack stack = new ItemStack(Material.valueOf(block));
                ItemBuilder TeamChestBuilder = new ItemBuilder(stack);
                TeamChestBuilder.setAmount(64);
                TeamChestBuilder.toItemStack();
                redTeamChest1.setItem(redTeamChest1.firstEmpty(), stack);
            } else if (redTeamChest2.firstEmpty() != -1) {
                ItemStack stack = new ItemStack(Material.valueOf(block));
                ItemBuilder TeamChestBuilder = new ItemBuilder(stack);
                TeamChestBuilder.setAmount(64);
                TeamChestBuilder.toItemStack();
                redTeamChest2.setItem(redTeamChest2.firstEmpty(), stack);
            } else if (redTeamChest3.firstEmpty() != -1) {
                ItemStack stack = new ItemStack(Material.valueOf(block));
                ItemBuilder TeamChestBuilder = new ItemBuilder(stack);
                TeamChestBuilder.setAmount(64);
                TeamChestBuilder.toItemStack();
                redTeamChest3.setItem(redTeamChest3.firstEmpty(), stack);
            } else {
                sendAll("&4红队队伍箱子已满！" + TranslationManager.getValue(block) + "无法放入队伍箱子！");
            }
        }
    }

    public static void skipBlock(String team, int index) throws Exception {
        if (team.equalsIgnoreCase("red")) {
            if (index == -1) {
                for (int i = 0; i < redCurrentBlocks.size(); i++) {
                    redTaskComplete(redCurrentBlocks.get(0));
                }
                return;
            }
            redTaskComplete(redCurrentBlocks.get(index - 1));
        } else if (team.equalsIgnoreCase("blue")) {
            if (index == -1) {
                for (int i = 0; i < blueCurrentBlocks.size(); i++) {
                    blueTaskComplete(blueCurrentBlocks.get(0));
                }
                return;
            }
            blueTaskComplete(blueCurrentBlocks.get(index - 1));
        }
    }
}

