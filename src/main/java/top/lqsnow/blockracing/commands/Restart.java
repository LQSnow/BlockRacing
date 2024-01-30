package top.lqsnow.blockracing.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import top.lqsnow.blockracing.managers.Message;

import java.util.ArrayList;
import java.util.List;

import static top.lqsnow.blockracing.utils.CommandUtil.sendAll;

public class Restart implements CommandExecutor {
    public static List<Player> typeRestartPlayers = new ArrayList<>();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        if (typeRestartPlayers.contains(player)) {
            typeRestartPlayers.remove(player);
                sendAll(Message.NOTICE_RESTART_CANCEL.getString());
            return true;
        }
        typeRestartPlayers.add(player);
            sendAll(Message.NOTICE_RESTART.getString());
        check();
        return true;
    }

    public static void check() {
        if (Bukkit.getOnlinePlayers().size() == 0) return;
        if (typeRestartPlayers.size() == Bukkit.getOnlinePlayers().size()) {
            Bukkit.getServer().shutdown();
        }
    }
}
