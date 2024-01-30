package top.lqsnow.blockracing.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import top.lqsnow.blockracing.managers.Message;

import static top.lqsnow.blockracing.managers.Game.*;
import static top.lqsnow.blockracing.managers.Team.*;
import static top.lqsnow.blockracing.utils.CommandUtil.sendBlue;
import static top.lqsnow.blockracing.utils.CommandUtil.sendRed;

public class WayPoint implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            Bukkit.getLogger().info("This command can only be run by a player.");
            return true;
        }
        if (args[0].equals("remove")) {
            int index = Integer.parseInt(args[1]);
            if (redTeamPlayers.contains(player.getName())) {
                boolean flag = removeWaypoint("red", index);
                if (!flag) return true;
                sendRed(Message.NOTICE_RED_REMOVE_WAYPOINT.getString().replace("%player%", player.getName()).replace("%index%", String.valueOf(index)));
            } else if (blueTeamPlayers.contains(player.getName())) {
                boolean flag = removeWaypoint("blue", index);
                if (!flag) return true;
                sendBlue(Message.NOTICE_BLUE_REMOVE_WAYPOINT.getString().replace("%player%", player.getName()).replace("%index%", String.valueOf(index)));
                }
            }
        return true;
    }

    private boolean removeWaypoint(String team, int index) {
        if (team.equals("red")) {
            switch (index) {
                case 1 -> {
                    if (redWaypoint1 == null) return false;
                    redWaypoint1 = null;
                    return true;
                }
                case 2 -> {
                    if (redWaypoint2 == null) return false;
                    redWaypoint2 = null;
                    return true;
                }
                case 3 -> {
                    if (redWaypoint3 == null) return false;
                    redWaypoint3 = null;
                    return true;
                }
            }
        } else {
            switch (index) {
                case 1 -> {
                    if (blueWaypoint1 == null) return false;
                    blueWaypoint1 = null;
                    return true;
                }
                case 2 -> {
                    if (blueWaypoint2 == null) return false;
                    blueWaypoint2 = null;
                    return true;
                }
                case 3 -> {
                    if (blueWaypoint3 == null) return false;
                    blueWaypoint3 = null;
                    return true;
                }
            }
        }

        return true;
    }
}
