package top.lqsnow.blockracing.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import top.lqsnow.blockracing.managers.ScoreboardManager;
import top.lqsnow.blockracing.utils.ConsoleCommandHandler;


import static top.lqsnow.blockracing.managers.ScoreboardManager.*;
import static top.lqsnow.blockracing.listeners.EventListener.blueTeamPlayerString;
import static top.lqsnow.blockracing.listeners.EventListener.redTeamPlayerString;
import static top.lqsnow.blockracing.managers.GameManager.*;

public class BlockRacing implements CommandExecutor {
    public static int blockAmount = 50;
    boolean flag = false;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (gameStart) {
            sender.sendMessage(ChatColor.RED + "游戏已开始！无法使用该指令！");
            return true;
        }

        // 游戏介绍
        if (args.length == 0) {
            sender.sendMessage(ChatColor.AQUA + "欢迎来到方块竞速！本插件由洛奇亚LQ_Snow制作。");
            sender.sendMessage(ChatColor.GREEN + "游戏介绍：lqsnow.top");
            return true;
        }

        // 加入队伍
        if (args[0].equals("teamjoinred")) {
            red.addEntry(sender.getName());
            ConsoleCommandHandler.send("tellraw @a {\"text\": \"\\u00a7c" + sender.getName() + "加入了红队！\"}");
            redTeamPlayer.add((Player) sender);
            redTeamPlayerString.add(sender.getName());
            if (blueTeamPlayer.contains(sender)) {
                blueTeamPlayer.remove(sender);
                blueTeamPlayerString.remove(sender.getName());
                blue.removeEntry(sender.getName());
            }
        } else if (args[0].equals("teamjoinblue")) {
            blue.addEntry(sender.getName());
            ConsoleCommandHandler.send("tellraw @a {\"text\": \"\\u00a79" + sender.getName() + "加入了蓝队！\"}");
            blueTeamPlayer.add((Player) sender);
            blueTeamPlayerString.add(sender.getName());
            if (redTeamPlayer.contains(sender)) {
                redTeamPlayer.remove(sender);
                redTeamPlayerString.remove(sender.getName());
                red.removeEntry(sender.getName());
            }
        } else {
            // 设置目标方块数量
            try {
                blockAmount = Integer.parseInt(args[0]);
                flag = true;
            }catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "请输入正确数字！");
                flag = false;
            }
            if (flag) {
                if (blockAmount < 10) {
                    ConsoleCommandHandler.send("tellraw @a {\"color\": \"green\",\"text\": \"需要收集的方块数量更改为10\"}");
                    blockAmount = 10;
                } else {
                    ConsoleCommandHandler.send("tellraw @a {\"color\": \"green\",\"text\": \"需要收集的方块数量更改为" + blockAmount + "\"}");
                }
            }
        }
        ScoreboardManager.update();
        return true;
    }
}
