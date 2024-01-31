package top.lqsnow.blockracing.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.lqsnow.blockracing.managers.Game;
import top.lqsnow.blockracing.managers.Gui;
import top.lqsnow.blockracing.managers.Message;
import top.lqsnow.blockracing.menus.GameMenu;

import java.util.ArrayList;
import java.util.List;

import static top.lqsnow.blockracing.managers.Game.*;
import static top.lqsnow.blockracing.managers.Gui.*;
import static top.lqsnow.blockracing.managers.Team.*;

public class Menu implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            Bukkit.getLogger().info("This command can only be run by a player.");
            return true;
        }

        if (Game.getCurrentGameState().equals(Game.GameState.END)) return true;

        if (args.length == 0 || args[0].equalsIgnoreCase("main")) {
            Gui.openMenu(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("chest")) {
            if (Game.getCurrentGameState().equals(Game.GameState.PREGAME)) {
                sender.sendMessage(Message.NOTICE_GAME_NOT_START.getString());
                return true;
            }
            if (redTeamPlayers.contains(player.getName())) {
                if (args.length == 1) {
                    new GameMenu().new TeamChestSelectMenu().displayTo(player);
                    return true;
                }
                if (args[1].equalsIgnoreCase("1")) player.openInventory(redTeamChest1);
                else if (args[1].equalsIgnoreCase("2")) player.openInventory(redTeamChest2);
                else if (args[1].equalsIgnoreCase("3")) player.openInventory(redTeamChest3);
            } else if (blueTeamPlayers.contains(player.getName())) {
                if (args.length == 1) {
                    new GameMenu().new TeamChestSelectMenu().displayTo(player);
                    return true;
                }
                if (args[1].equalsIgnoreCase("1")) player.openInventory(blueTeamChest1);
                else if (args[1].equalsIgnoreCase("2")) player.openInventory(blueTeamChest2);
                else if (args[1].equalsIgnoreCase("3")) player.openInventory(blueTeamChest3);
            }

        }

        if (args[0].equalsIgnoreCase("waypoints")) {
            if (Game.getCurrentGameState().equals(Game.GameState.PREGAME)) {
                sender.sendMessage(Message.NOTICE_GAME_NOT_START.getString());
                return true;
            }

            if (args.length == 1) {
                if (redTeamPlayers.contains(player.getName())) {
                    new GameMenu().new RedWaypointMenu().displayTo(player);
                } else if (blueTeamPlayers.contains(player.getName())) {
                    new GameMenu().new BlueWaypointMenu().displayTo(player);
                }
                return true;
            }

            if (args[1].equalsIgnoreCase("use")) {
                int index = Integer.parseInt(args[2]);
                waypoint(player, index, ClickType.LEFT);
            }
        }

        if (args[0].equalsIgnoreCase("roll")) {
            if (Game.getCurrentGameState().equals(Game.GameState.PREGAME)) {
                sender.sendMessage(Message.NOTICE_GAME_NOT_START.getString());
                return true;
            }
            roll(player);
        }

        if (args[0].equalsIgnoreCase("locate")) {
            if (Game.getCurrentGameState().equals(Game.GameState.PREGAME)) {
                sender.sendMessage(Message.NOTICE_GAME_NOT_START.getString());
                return true;
            }
            locate(player);
        }

        if (args[0].equalsIgnoreCase("randomTP")) {
            if (Game.getCurrentGameState().equals(Game.GameState.PREGAME)) {
                sender.sendMessage(Message.NOTICE_GAME_NOT_START.getString());
                return true;
            }
            randomTeleport(player, false);
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
            completions.add("roll");
            completions.add("locate");
            completions.add("randomTP");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("chest")) {
            completions.add("1");
            completions.add("2");
            completions.add("3");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("waypoints")) {
            completions.add("use");
        } else if (args.length == 3 && args[0].equalsIgnoreCase("waypoints")) {
            completions.add("1");
            completions.add("2");
            completions.add("3");
        }

        String prefix = args[args.length - 1].toLowerCase();
        completions.removeIf(s -> !s.toLowerCase().startsWith(prefix));

        return completions;
    }
}
