package top.lqsnow.blockracing.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import top.lqsnow.blockracing.utils.ConsoleCommandHandler;

import java.util.ArrayList;
import java.util.List;

public class Restart implements CommandExecutor {
    public static List<Player> typeRestartPlayers = new ArrayList<>();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        if (typeRestartPlayers.contains(player)) {
            typeRestartPlayers.remove(player);
            ConsoleCommandHandler.send("tellraw @a \"\\u00a74" + player.getName() + "已取消重启申请！\"");
        } else {
            typeRestartPlayers.add(player);
            ConsoleCommandHandler.send("tellraw @a \"\\u00a7b" + player.getName() + "申请重启！服务器中所有玩家申请即可重启游戏！使用/restartgame命令即可申请！\"");
            check();
        }
        return true;
    }

    public static void check() {
        if (Bukkit.getOnlinePlayers().size() == 0) return;
        if (typeRestartPlayers.size() == Bukkit.getOnlinePlayers().size()) {
            ConsoleCommandHandler.send("stop");
        }
    }
}
