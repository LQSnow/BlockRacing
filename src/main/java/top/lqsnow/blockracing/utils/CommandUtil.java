package top.lqsnow.blockracing.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import static top.lqsnow.blockracing.managers.Team.*;
import static top.lqsnow.blockracing.utils.ColorUtil.t;

public class CommandUtil {
    public static void sendAll(String message) {
        Bukkit.getOnlinePlayers().forEach((Player player) -> {
            player.sendMessage(t(message));
        });
    }

    public static void sendRed(String message) {
        try {
            redTeamPlayers.forEach((String player) -> Bukkit.getPlayer(player).sendMessage(t(message)));
        } catch (NullPointerException ignored) {
        }
    }

    public static void sendBlue(String message) {
        try {
            blueTeamPlayers.forEach((String player) -> Bukkit.getPlayer(player).sendMessage(t(message)));
        } catch (NullPointerException ignored) {
        }
    }
}
