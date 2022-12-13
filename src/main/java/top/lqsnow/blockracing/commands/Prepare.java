package top.lqsnow.blockracing.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import top.lqsnow.blockracing.utils.ConsoleCommandHandler;

import java.util.ArrayList;
import java.util.List;

import static top.lqsnow.blockracing.commands.Start.canStart;
import static top.lqsnow.blockracing.managers.GameManager.gameStart;

public class Prepare implements CommandExecutor {
    public static List<Player> prepareList = new ArrayList<>();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (gameStart) {
            sender.sendMessage(ChatColor.DARK_RED + "游戏已开始！无法使用该指令！");
            return true;
        }
        if (!prepareList.contains((Player) sender)) {
            prepareList.add((Player) sender);
            ConsoleCommandHandler.send("tellraw @a \"\u00a7b" + sender.getName() + "已准备！\"");
            if (prepareList.size() > 1 && prepareList.size() == Bukkit.getOnlinePlayers().size()) {
                canStart = true;
                ConsoleCommandHandler.send("tellraw @a \"\u00a7b所有人都准备好了，输入/start开始游戏吧！\"");
            }
        } else {
            prepareList.remove((Player) sender);
            ConsoleCommandHandler.send("tellraw @a \"\u00a7c" + sender.getName() + "已取消准备！\"");
        }
        return true;
    }
}
