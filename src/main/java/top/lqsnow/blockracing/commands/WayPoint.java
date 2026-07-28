package top.lqsnow.blockracing.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import top.lqsnow.blockracing.managers.Message;
import top.lqsnow.blockracing.managers.GameProgressStore;
import top.lqsnow.blockracing.managers.Setting;

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
        if (args.length != 2 || !args[0].equalsIgnoreCase("remove")) {
            player.sendMessage(Message.NOTICE_ERROR_COMMAND.getString(player));
            return true;
        }
        int index;
        try {
            index = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            player.sendMessage(Message.NOTICE_ERROR_COMMAND.getString(player));
            return true;
        }
        if (index < 1 || index > Setting.getMaxTeamWaypointNum()) {
            player.sendMessage(Message.NOTICE_ERROR_COMMAND.getString(player));
            return true;
        }
        if (args[0].equalsIgnoreCase("remove")) {
            if (redTeamPlayers.contains(player.getName())) {
                boolean flag = removeWaypoint("red", index);
                if (!flag) return true;
                sendRed(Message.NOTICE_RED_REMOVE_WAYPOINT, (viewer, text) -> text
                        .replace("%player%", player.getName()).replace("%index%", String.valueOf(index)));
            } else if (blueTeamPlayers.contains(player.getName())) {
                boolean flag = removeWaypoint("blue", index);
                if (!flag) return true;
                sendBlue(Message.NOTICE_BLUE_REMOVE_WAYPOINT, (viewer, text) -> text
                        .replace("%player%", player.getName()).replace("%index%", String.valueOf(index)));
                }
            }
        return true;
    }

    private boolean removeWaypoint(String team, int index) {
        if (team.equals("red")) {
            redWaypoint.remove(index);
        } else {
            blueWaypoint.remove(index);
        }
        GameProgressStore.saveNow();

        return true;
    }
}
