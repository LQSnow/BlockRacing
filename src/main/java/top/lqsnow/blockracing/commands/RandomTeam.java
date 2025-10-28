package top.lqsnow.blockracing.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import top.lqsnow.blockracing.managers.Game;
import top.lqsnow.blockracing.managers.Message;
import top.lqsnow.blockracing.managers.Team;
import top.lqsnow.blockracing.utils.CommandUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static top.lqsnow.blockracing.managers.Game.getCurrentGameState;

public class RandomTeam implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            Bukkit.getLogger().info("This command can only be run by a player.");
            return true;
        }
        if (!getCurrentGameState().equals(Game.GameState.PREGAME)) {
            sender.sendMessage(Message.NOTICE_GAME_HAS_START.getString());
            return true;
        }

        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        Collections.shuffle(players, new Random());
        boolean randomTeam = new Random().nextBoolean();
        int sizeA = players.size() / 2;
        for (int i = 0; i < players.size(); i++) {
            if (randomTeam) {
                if (i < sizeA) {
                    Team.joinTeam(players.get(i), Team.redTeam, false);
                } else {
                    Team.joinTeam(players.get(i), Team.blueTeam, false);
                }
            } else {
                if (i < sizeA) {
                    Team.joinTeam(players.get(i), Team.blueTeam, false);
                } else {
                    Team.joinTeam(players.get(i), Team.redTeam, false);
                }
            }
        }

        CommandUtil.sendAll(Message.NOTICE_TEAM_SHUFFLE.getString());









        return true;
    }
}
