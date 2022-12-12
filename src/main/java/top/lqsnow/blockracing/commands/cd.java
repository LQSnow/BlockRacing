package top.lqsnow.blockracing.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;


import static top.lqsnow.blockracing.managers.GameManager.gameStart;
import static top.lqsnow.blockracing.managers.InventoryManager.menu;


public class cd implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!gameStart) {
            sender.sendMessage(ChatColor.DARK_RED + "游戏未开始！");
            return true;
        }
        Bukkit.getPlayer(sender.getName()).openInventory(menu);
        return true;

    }
}
