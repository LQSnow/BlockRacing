package top.lqsnow.blockracing.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static top.lqsnow.blockracing.managers.GameManager.*;


public class TP implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!gameStart) {
            sender.sendMessage(ChatColor.DARK_RED + "游戏未开始！");
            return true;
        } else if (!redTeamPlayer.contains((Player) sender) && !blueTeamPlayer.contains((Player) sender)) {
            // 旁观者可以自由tp
            if (Bukkit.getOnlinePlayers().contains(Bukkit.getPlayer(args[0]))) {
                ((Player) sender).teleport(Objects.requireNonNull(Bukkit.getPlayer(args[0])));
                sender.sendMessage(ChatColor.YELLOW + "[旁观者TP]" + ChatColor.WHITE + "已成功TP至" + Bukkit.getPlayer(args[0]).getName());
                return true;
            } else {
                sender.sendMessage(ChatColor.DARK_RED + "请正确输入指令！");
                return true;
            }
        }

        // 红队TP
        if (redTeamPlayer.contains((Player) sender)) {
            if (Bukkit.getOnlinePlayers().contains(Bukkit.getPlayer(args[0])) && redTeamPlayer.contains(Bukkit.getPlayer(args[0]))) {
                ((Player) sender).teleport(Objects.requireNonNull(Bukkit.getPlayer(args[0])));
                sender.sendMessage(ChatColor.RED + "[红队TP]" + ChatColor.WHITE + "已成功TP至" + Bukkit.getPlayer(args[0]).getName());
                return true;
            } else {
                sender.sendMessage(ChatColor.DARK_RED + "玩家不存在或不在同一队伍！");
                return true;
            }
        }

        // 蓝队TP
        if (blueTeamPlayer.contains((Player) sender)) {
            if (Bukkit.getOnlinePlayers().contains(Bukkit.getPlayer(args[0])) && blueTeamPlayer.contains(Bukkit.getPlayer(args[0]))) {
                ((Player) sender).teleport(Objects.requireNonNull(Bukkit.getPlayer(args[0])));
                sender.sendMessage(ChatColor.BLUE + "[蓝队TP]" + ChatColor.WHITE + "已成功TP至" + Bukkit.getPlayer(args[0]).getName());
                return true;
            } else {
                sender.sendMessage(ChatColor.DARK_RED + "玩家不存在或不在同一队伍！");
                return true;
            }
        }
        return true;
    }
}
