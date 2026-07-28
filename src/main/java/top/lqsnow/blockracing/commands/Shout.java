package top.lqsnow.blockracing.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import top.lqsnow.blockracing.managers.Game;
import top.lqsnow.blockracing.managers.Message;
import top.lqsnow.blockracing.managers.TeamChat;

public final class Shout implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }
        if (Game.getCurrentGameState() != Game.GameState.INGAME) {
            player.sendMessage(Message.NOTICE_GAME_NOT_START.getString(player));
            return true;
        }
        if (args.length == 0) {
            player.sendMessage(Message.NOTICE_ERROR_COMMAND.getString(player));
            return true;
        }
        TeamChat.shout(player, String.join(" ", args));
        return true;
    }
}
