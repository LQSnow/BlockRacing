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
        redTeamPlayers.forEach(playerName -> {
            Player player = Bukkit.getPlayerExact(playerName);
            if (player != null) player.sendMessage(t(message));
        });
    }

    public static void sendBlue(String message) {
        blueTeamPlayers.forEach(playerName -> {
            Player player = Bukkit.getPlayerExact(playerName);
            if (player != null) player.sendMessage(t(message));
        });
    }
}
