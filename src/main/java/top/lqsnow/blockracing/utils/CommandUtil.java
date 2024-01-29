package top.lqsnow.blockracing.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import static top.lqsnow.blockracing.utils.ColorUtil.t;

public class CommandUtil {
    public static void sendAll(String message) {
        Bukkit.getOnlinePlayers().forEach((Player player) -> {
            player.sendMessage(t(message));
        });
    }
}
