package top.lqsnow.blockracing.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import static top.lqsnow.blockracing.managers.GameManager.gameStart;
import static top.lqsnow.blockracing.managers.InventoryManager.*;

public class Settings implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!gameStart) Bukkit.getPlayer(sender.getName()).openInventory(settings);
        else sender.sendMessage(ChatColor.DARK_RED + "游戏已开始！无法修改设置！");
        return true;
    }
}
