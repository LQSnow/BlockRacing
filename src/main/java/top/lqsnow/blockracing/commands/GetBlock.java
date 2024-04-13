package top.lqsnow.blockracing.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.lqsnow.blockracing.managers.Game;
import top.lqsnow.blockracing.utils.TranslationUtil;

import java.util.ArrayList;
import java.util.List;

import static top.lqsnow.blockracing.managers.Game.getCurrentBlocks;
import static top.lqsnow.blockracing.managers.Game.getCurrentGameState;
import static top.lqsnow.blockracing.utils.ColorUtil.t;

public class GetBlock implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player player)) {
            Bukkit.getLogger().info("This command can only be run by a player.");
            return true;
        }

        if (getCurrentGameState().equals(Game.GameState.PREGAME)) {
            player.sendMessage(t("&cThis command can only be used after the start of the game!"));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(t("&cMissing parameters"));
            return true;
        }

        if (args[1].isEmpty()) {
            player.sendMessage(t("&cMissing parameters"));
            return true;
        } else if (Integer.parseInt(args[1]) > 4) {
            player.sendMessage(t("&cParameters error"));
            return true;
        }

        if (args[0].equalsIgnoreCase("red")) {
            String block = getCurrentBlocks("red").get(Integer.parseInt(args[1]) - 1);
            sender.sendMessage(String.format(TranslationUtil.getValue(block)));
        } else if (args[0].equalsIgnoreCase("blue")) {
            String block = getCurrentBlocks("blue").get(Integer.parseInt(args[1]) - 1);
            sender.sendMessage(String.format(TranslationUtil.getValue(block)));
        }
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("red");
            completions.add("blue");
        } else if (args.length == 2) {
            completions.add("1");
            completions.add("2");
            completions.add("3");
            completions.add("4");
        }

        String prefix = args[args.length - 1].toLowerCase();
        completions.removeIf(s -> !s.toLowerCase().startsWith(prefix));
        return completions;
    }
}
