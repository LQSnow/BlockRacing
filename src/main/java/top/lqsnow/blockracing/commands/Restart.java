package top.lqsnow.blockracing.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import top.lqsnow.blockracing.managers.Message;
import top.lqsnow.blockracing.managers.Game;
import top.lqsnow.blockracing.managers.GameProgressStore;
import top.lqsnow.blockracing.managers.WorldResetMarker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static top.lqsnow.blockracing.utils.CommandUtil.sendAll;

public class Restart implements CommandExecutor {
    private static final Set<UUID> restartVotes = new HashSet<>();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }
        if (restartVotes.remove(player.getUniqueId())) {
            sendAll(Message.NOTICE_RESTART_CANCEL,
                    (viewer, text) -> text.replace("%player%", player.getName()));
            return true;
        }
        restartVotes.add(player.getUniqueId());
        sendAll(Message.NOTICE_RESTART,
                (viewer, text) -> text.replace("%player%", player.getName()));
        check();
        return true;
    }

    public static void check() {
        if (Bukkit.getOnlinePlayers().isEmpty()) return;
        restartVotes.retainAll(Bukkit.getOnlinePlayers().stream().map(Player::getUniqueId).toList());
        if (restartVotes.size() == Bukkit.getOnlinePlayers().size()) {
            Game.setCurrentGameState(Game.GameState.END);
            GameProgressStore.clear();
            WorldResetMarker.request();
            Bukkit.getServer().shutdown();
        }
    }

    public static void removeVote(Player player) {
        restartVotes.remove(player.getUniqueId());
    }
}
