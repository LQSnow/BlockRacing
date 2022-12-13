package top.lqsnow.blockracing.managers;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.parser.ParseException;
import top.lqsnow.blockracing.utils.ConsoleCommandHandler;
import top.lqsnow.blockracing.utils.ItemBuilder;

import java.io.IOException;

import static top.lqsnow.blockracing.managers.GameManager.*;
import static top.lqsnow.blockracing.managers.InventoryManager.*;
import static top.lqsnow.blockracing.managers.ScoreboardManager.*;

public class GameTick extends BukkitRunnable {
    public static int redCompleteAmount;
    public static int blueCompleteAmount;

    // 每2t执行一次
    @Override
    public void run() {
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
    private void redTaskComplete(String block) throws Exception {
        ConsoleCommandHandler.send("tellraw @a {\"text\": \"\\u00a7c红队\\u00a7a收集了\u00a7b" + TranslationManager.getValue(block) + "\"}");
        ConsoleCommandHandler.send("execute as @a at @s run playsound minecraft:entity.experience_orb.pickup player @s");
        redCompleteAmount += 1;
        redCurrentBlocks.remove(block);
        if (redTeamBlocks.size() >= 1) {
            redCurrentBlocks.add(redTeamBlocks.get(0));
            redTeamBlocks.remove(0);
        }
        redTeamScore += 1;
        ScoreboardManager.update();
        // 将一组该物品放到蓝队队伍箱子
        if (blueTeamChest1.firstEmpty() != -1) {
            ItemStack stack = new ItemStack(Material.valueOf(block));
            ItemBuilder TeamChestBuilder = new ItemBuilder(stack);
            TeamChestBuilder.setAmount(64);
            TeamChestBuilder.toItemStack();
            blueTeamChest1.setItem(blueTeamChest1.firstEmpty(), stack);
        }else if (blueTeamChest2.firstEmpty() != -1) {
            ItemStack stack = new ItemStack(Material.valueOf(block));
            ItemBuilder TeamChestBuilder = new ItemBuilder(stack);
            TeamChestBuilder.setAmount(64);
            TeamChestBuilder.toItemStack();
            blueTeamChest2.setItem(blueTeamChest2.firstEmpty(), stack);
        }else if (blueTeamChest3.firstEmpty() != -1) {
            ItemStack stack = new ItemStack(Material.valueOf(block));
            ItemBuilder TeamChestBuilder = new ItemBuilder(stack);
            TeamChestBuilder.setAmount(64);
            TeamChestBuilder.toItemStack();
            blueTeamChest3.setItem(blueTeamChest3.firstEmpty(), stack);
        }
        else {
            ConsoleCommandHandler.send("tellraw @a \"\\u00a74蓝队队伍箱子已满！" + TranslationManager.getValue(block) + "无法放入队伍箱子！\"");
        }
    }

    // 蓝队获得目标方块
    private void blueTaskComplete(String block) throws Exception {
        ConsoleCommandHandler.send("tellraw @a {\"text\": \"\\u00a79蓝队\\u00a7a收集了\u00a7b" + TranslationManager.getValue(block) + "\"}");
        ConsoleCommandHandler.send("execute as @a at @s run playsound minecraft:entity.experience_orb.pickup player @s");
        blueCompleteAmount += 1;
        blueCurrentBlocks.remove(block);
        if (blueTeamBlocks.size() >= 1) {
            blueCurrentBlocks.add(blueTeamBlocks.get(0));
            blueTeamBlocks.remove(0);
        }
        blueTeamScore += 1;
        ScoreboardManager.update();
        // 将一组该物品放到红队队伍箱子
        if (redTeamChest1.firstEmpty() != -1) {
            ItemStack stack = new ItemStack(Material.valueOf(block));
            ItemBuilder TeamChestBuilder = new ItemBuilder(stack);
            TeamChestBuilder.setAmount(64);
            TeamChestBuilder.toItemStack();
            redTeamChest1.setItem(redTeamChest1.firstEmpty(), stack);
        }else if (redTeamChest2.firstEmpty() != -1) {
            ItemStack stack = new ItemStack(Material.valueOf(block));
            ItemBuilder TeamChestBuilder = new ItemBuilder(stack);
            TeamChestBuilder.setAmount(64);
            TeamChestBuilder.toItemStack();
            redTeamChest2.setItem(redTeamChest2.firstEmpty(), stack);
        }else if (redTeamChest3.firstEmpty() != -1) {
            ItemStack stack = new ItemStack(Material.valueOf(block));
            ItemBuilder TeamChestBuilder = new ItemBuilder(stack);
            TeamChestBuilder.setAmount(64);
            TeamChestBuilder.toItemStack();
            redTeamChest3.setItem(redTeamChest3.firstEmpty(), stack);
        } else {
            ConsoleCommandHandler.send("tellraw @a \"\\u00a74红队队伍箱子已满！" + TranslationManager.getValue(block) + "无法放入队伍箱子！\"");
        }
    }
}

