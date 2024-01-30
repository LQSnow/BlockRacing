package top.lqsnow.blockracing.managers;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import static top.lqsnow.blockracing.managers.Scoreboard.scoreboard;
import static top.lqsnow.blockracing.utils.CommandUtil.sendAll;

public class Team {
    public static org.bukkit.scoreboard.Team redTeam = scoreboard.registerNewTeam("red");
    public static org.bukkit.scoreboard.Team blueTeam = scoreboard.registerNewTeam("blue");
    public static List<String> redTeamPlayers = new ArrayList<>();
    public static List<String> blueTeamPlayers = new ArrayList<>();

    public static void createTeam() {
        redTeam.setDisplayName(Message.TEAM_RED_NAME.getString());
        redTeam.setPrefix(Message.TEAM_RED_PREFIX.getString());
        redTeam.setColor(ChatColor.RED);
        blueTeam.setDisplayName(Message.TEAM_BLUE_NAME.getString());
        blueTeam.setPrefix(Message.TEAM_BLUE_PREFIX.getString());
        blueTeam.setColor(ChatColor.BLUE);
    }

    public static void joinTeam(Player player, org.bukkit.scoreboard.Team team) {
        if (team.equals(redTeam)) {
            if (redTeamPlayers.contains(player.getName())) {
                player.sendMessage(Message.NOTICE_ALREADY_IN_RED.getString());
                return;
            }
            if (blueTeamPlayers.contains(player.getName())) {
                blueTeam.removeEntry(player.getName());
                blueTeamPlayers.remove(player.getName());
            }
            redTeam.addEntry(player.getName());
            redTeamPlayers.add(player.getName());
            sendAll(Message.NOTICE_JOIN_RED.getString().replace("%player%", player.getName()));
        }
        else if (team.equals(blueTeam)) {
            if (blueTeamPlayers.contains(player.getName())) {
                player.sendMessage(Message.NOTICE_ALREADY_IN_BLUE.getString());
                return;
            }
            if (redTeamPlayers.contains(player.getName())) {
                redTeam.removeEntry(player.getName());
                redTeamPlayers.remove(player.getName());
            }
            blueTeam.addEntry(player.getName());
            blueTeamPlayers.add(player.getName());
            sendAll(Message.NOTICE_JOIN_BLUE.getString().replace("%player%", player.getName()));
        }
    }

    public static boolean isPlayerInRedTeam(Player player) {
        return redTeamPlayers.contains(player.getName());
    }

    public static boolean isPlayerInBlueTeam(Player player) {
        return blueTeamPlayers.contains(player.getName());
    }

}
