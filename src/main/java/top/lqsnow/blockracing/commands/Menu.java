package top.lqsnow.blockracing.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.lqsnow.blockracing.Main;

import java.util.ArrayList;
import java.util.List;

import static top.lqsnow.blockracing.listeners.InventoryEventListener.*;
import static top.lqsnow.blockracing.managers.GameManager.*;
import static top.lqsnow.blockracing.managers.InventoryManager.*;
import static top.lqsnow.blockracing.utils.ColorUtil.t;

public class Menu implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
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

            if (args.length == 1) {
                if (redTeamPlayer.contains(player)) {
                    Bukkit.getPlayer(player.getName()).openInventory(redWayPoints);
                } else if (blueTeamPlayer.contains(player)) {
                    Bukkit.getPlayer(player.getName()).openInventory(blueWayPoints);
                }
            }

            if (args[1].equalsIgnoreCase("use")) {
                if (blueTeamPlayer.contains(player)) {
                    if (args[2].equalsIgnoreCase("1")) {
                        operateWaypoints(blueWayPoints.getItem(0), player, true, false);
                    } else if (args[2].equalsIgnoreCase("2")) {
                        operateWaypoints(blueWayPoints.getItem(1), player, true, false);
                    } else if (args[2].equalsIgnoreCase("3")) {
                        operateWaypoints(blueWayPoints.getItem(2), player, true, false);
                    }
                } else if (redTeamPlayer.contains(player)) {
                    if (args[2].equalsIgnoreCase("1")) {
                        operateWaypoints(redWayPoints.getItem(0), player, true, false);
                    } else if (args[2].equalsIgnoreCase("2")) {
                        operateWaypoints(redWayPoints.getItem(1), player, true, false);
                    } else if (args[2].equalsIgnoreCase("3")) {
                        operateWaypoints(redWayPoints.getItem(2), player, true, false);
                    }
                }
            } else if (args[1].equalsIgnoreCase("remove")) {
                if (redTeamPlayer.contains(player)) {
                    if (args[2].equalsIgnoreCase("1")) {
                        operateWaypoints(redWayPoints.getItem(0), player, false, true);
                    } else if (args[2].equalsIgnoreCase("2")) {
                        operateWaypoints(redWayPoints.getItem(1), player, false, true);
                    } else if (args[2].equalsIgnoreCase("3")) {
                        operateWaypoints(redWayPoints.getItem(2), player, false, true);
                    }
                } else if (blueTeamPlayer.contains(player)) {
                    if (args[2].equalsIgnoreCase("1")) {
                        operateWaypoints(blueWayPoints.getItem(0), player, false, true);
                    } else if (args[2].equalsIgnoreCase("2")) {
                        operateWaypoints(blueWayPoints.getItem(1), player, false, true);
                    } else if (args[2].equalsIgnoreCase("3")) {
                        operateWaypoints(blueWayPoints.getItem(2), player, false, true);
                    }
                }

            }
        }

        if (args[0].equalsIgnoreCase("roll")) {
            if (!gameStart) {
                sender.sendMessage(t("&4游戏还没开始！"));
                return true;
            }
            roll(player);
        }

        if (args[0].equalsIgnoreCase("locate")) {
            if (!gameStart) {
                sender.sendMessage(t("&4游戏还没开始！"));
                return true;
            }
            buyLocate(player);
        }

        if (args[0].equalsIgnoreCase("randomTP")) {
            if (!gameStart) {
                sender.sendMessage(t("&4游戏还没开始！"));
                return true;
            }
            randomTP(player);
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
            completions.add("remove");
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
