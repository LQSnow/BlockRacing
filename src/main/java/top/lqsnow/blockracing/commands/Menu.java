package top.lqsnow.blockracing.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static top.lqsnow.blockracing.listeners.InventoryEventListener.blueTeamPlayerString;
import static top.lqsnow.blockracing.listeners.InventoryEventListener.redTeamPlayerString;
import static top.lqsnow.blockracing.managers.GameManager.*;
import static top.lqsnow.blockracing.managers.InventoryManager.*;
import static top.lqsnow.blockracing.utils.ColorUtil.t;

public class Menu implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length > 2) sender.sendMessage(t("&4指令输入错误！"));
        Player player = (Player) sender;

        if (args.length == 0 || args[0].equalsIgnoreCase("main")) {
            if (!gameStart) player.openInventory(settings);
            else player.openInventory(menu);
            return true;
        }

        if (args[0].equalsIgnoreCase("chest")) {
            if (!gameStart) {
                sender.sendMessage(t("&4游戏还没开始！"));
                return true;
            }
            if (redTeamPlayerString.contains(player.getName())) {
                if (args.length == 1) {
                    player.openInventory(chestSwitch);
                    return true;
                }
                if (args[1].equalsIgnoreCase("1")) player.openInventory(redTeamChest1);
                else if (args[1].equalsIgnoreCase("2")) player.openInventory(redTeamChest2);
                else if (args[1].equalsIgnoreCase("3")) player.openInventory(redTeamChest3);
            } else if (blueTeamPlayerString.contains(player.getName())) {
                if (args.length == 1) {
                    player.openInventory(chestSwitch);
                    return true;
                }
                if (args[1].equalsIgnoreCase("1")) player.openInventory(blueTeamChest1);
                else if (args[1].equalsIgnoreCase("2")) player.openInventory(blueTeamChest2);
                else if (args[1].equalsIgnoreCase("3")) player.openInventory(blueTeamChest3);
            }

        }

        if (args[0].equalsIgnoreCase("waypoints")) {
            if (!gameStart) {
                sender.sendMessage(t("&4游戏还没开始！"));
                return true;
            }
            if (redTeamPlayer.contains(player)) {
                Bukkit.getPlayer(player.getName()).openInventory(redWayPoints);
            } else if (blueTeamPlayer.contains(player)) {
                Bukkit.getPlayer(player.getName()).openInventory(blueWayPoints);
            }
        }
        return true;
    }


    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("main");
            completions.add("chest");
            completions.add("waypoints");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("chest")) {
            completions.add("1");
            completions.add("2");
            completions.add("3");
        }

        String prefix = args[args.length - 1].toLowerCase();
        completions.removeIf(s -> !s.toLowerCase().startsWith(prefix));

        return completions;
    }
}
