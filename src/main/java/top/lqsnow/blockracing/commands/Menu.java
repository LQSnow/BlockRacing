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
import top.lqsnow.blockracing.managers.Scoreboard;
import top.lqsnow.blockracing.managers.Setting;
import top.lqsnow.blockracing.menus.GameMenu;

import java.util.ArrayList;
import java.util.List;

import static top.lqsnow.blockracing.managers.Game.*;
import static top.lqsnow.blockracing.managers.Team.*;
import static top.lqsnow.blockracing.utils.CommandUtil.sendAll;

public class Menu implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            Bukkit.getLogger().info("This command can only be run by a player.");
            return true;
        }

        if (Game.getCurrentGameState().equals(Game.GameState.END)) return true;

        if (args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("main"))) {
            Gui.openMenu(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("chest")) {
            if (args.length > 2) {
                player.sendMessage(Message.NOTICE_ERROR_COMMAND.getString(player));
                return true;
            }
            if (Game.getCurrentGameState().equals(Game.GameState.PREGAME)) {
                player.sendMessage(Message.NOTICE_GAME_NOT_START.getString(player));
                return true;
            }
            if (redTeamPlayers.contains(player.getName())) {
                if (args.length == 1) {
                    new GameMenu.TeamChestSelectMenu().open(player);
                    return true;
                }
                Integer ith = parseIndex(args[1], redTeamChest.size());
                if (ith == null) {
                    player.sendMessage(Message.NOTICE_ERROR_COMMAND.getString(player));
                    return true;
                }
                Gui.openTeamChest(player, ith - 1);
            } else if (blueTeamPlayers.contains(player.getName())) {
                if (args.length == 1) {
                    new GameMenu.TeamChestSelectMenu().open(player);
                    return true;
                }
                Integer ith = parseIndex(args[1], blueTeamChest.size());
                if (ith == null) {
                    player.sendMessage(Message.NOTICE_ERROR_COMMAND.getString(player));
                    return true;
                }
                Gui.openTeamChest(player, ith - 1);
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("waypoints")) {
            if (Game.getCurrentGameState().equals(Game.GameState.PREGAME)) {
                player.sendMessage(Message.NOTICE_GAME_NOT_START.getString(player));
                return true;
            }

            if (args.length == 1) {
                if (redTeamPlayers.contains(player.getName())) {
                    new GameMenu.WayPointMenu(redWaypoint, redWaypointIconCache).open(player);
                } else if (blueTeamPlayers.contains(player.getName())) {
                    new GameMenu.WayPointMenu(blueWaypoint, blueWaypointIconCache).open(player);
                }
                return true;
            }

            if (args.length == 3 && args[1].equalsIgnoreCase("use")) {
                Integer index = parseIndex(args[2], Setting.getMaxTeamWaypointNum());
                if (index == null) {
                    player.sendMessage(Message.NOTICE_ERROR_COMMAND.getString(player));
                    return true;
                }
                waypoint(player, index, ClickType.LEFT);
            } else {
                player.sendMessage(Message.NOTICE_ERROR_COMMAND.getString(player));
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("roll") && args.length == 1) {
            if (Game.getCurrentGameState().equals(Game.GameState.PREGAME)) {
                player.sendMessage(Message.NOTICE_GAME_NOT_START.getString(player));
                return true;
            }
            roll(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("locate") && args.length == 1) {
            if (Game.getCurrentGameState().equals(Game.GameState.PREGAME)) {
                player.sendMessage(Message.NOTICE_GAME_NOT_START.getString(player));
                return true;
            }
            locate(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("randomTP") && args.length == 1) {
            if (Game.getCurrentGameState().equals(Game.GameState.PREGAME)) {
                player.sendMessage(Message.NOTICE_GAME_NOT_START.getString(player));
                return true;
            }
            if (freeRandomTPList.contains(player.getName())) {
                Game.randomTeleport(player, false);
                freeRandomTPList.remove(player.getName());
            } else {
                if (redTeamPlayers.contains(player.getName())) {
                    if (redTeamScore < 2) {
                        player.sendMessage(Message.NOTICE_NOT_ENOUGH_SCORE.getString(player));
                        return true;
                    }
                } else if (blueTeamPlayers.contains(player.getName())) {
                    if (blueTeamScore < 2) {
                        player.sendMessage(Message.NOTICE_NOT_ENOUGH_SCORE.getString(player));
                        return true;
                    }
                }
                player.closeInventory();
                randomTeleport(player, false);
                if (redTeamPlayers.contains(player.getName())) {
                    redTeamScore -= 2;
                    sendAll(Message.NOTICE_RANDOM_TP, (viewer, text) -> text.replace("%player%",
                            Message.TEAM_RED_COLOR.getString(viewer) + player.getName()));
                } else if (blueTeamPlayers.contains(player.getName())) {
                    blueTeamScore -= 2;
                    sendAll(Message.NOTICE_RANDOM_TP, (viewer, text) -> text.replace("%player%",
                            Message.TEAM_BLUE_COLOR.getString(viewer) + player.getName()));
                }
                Scoreboard.updateScoreboard();
            }
            return true;
        }

        player.sendMessage(Message.NOTICE_ERROR_COMMAND.getString(player));
        return true;
    }

    private static Integer parseIndex(String value, int maximum) {
        try {
            int index = Integer.parseInt(value);
            return index >= 1 && index <= maximum ? index : null;
        } catch (NumberFormatException ex) {
            return null;
        }
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
            for(int i = 1; i <= Setting.getMaxTeamChestNum(); i++){
                completions.add(Integer.toString(i));
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("waypoints")) {
            completions.add("use");
        } else if (args.length == 3 && args[0].equalsIgnoreCase("waypoints")) {
            for(int i = 1; i <= Setting.getMaxTeamWaypointNum(); i++){
                completions.add(Integer.toString(i));
            }
        }

        String prefix = args[args.length - 1].toLowerCase();
        completions.removeIf(s -> !s.toLowerCase().startsWith(prefix));

        return completions;
    }
}
