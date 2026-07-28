package top.lqsnow.blockracing.managers;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static top.lqsnow.blockracing.managers.Scoreboard.scoreboard;
import static top.lqsnow.blockracing.utils.CommandUtil.sendAll;

public class Team {
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection();
    public static org.bukkit.scoreboard.Team redTeam = scoreboard.registerNewTeam("red");
    public static org.bukkit.scoreboard.Team blueTeam = scoreboard.registerNewTeam("blue");
    public static List<String> redTeamPlayers = new CopyOnWriteArrayList<>();
    public static List<String> blueTeamPlayers = new CopyOnWriteArrayList<>();

    public static void createTeam() {
        redTeam.displayName(LEGACY_SERIALIZER.deserialize(Message.TEAM_RED_NAME.getString()));
        redTeam.prefix(LEGACY_SERIALIZER.deserialize(Message.TEAM_RED_PREFIX.getString()));
        redTeam.color(NamedTextColor.RED);
        blueTeam.displayName(LEGACY_SERIALIZER.deserialize(Message.TEAM_BLUE_NAME.getString()));
        blueTeam.prefix(LEGACY_SERIALIZER.deserialize(Message.TEAM_BLUE_PREFIX.getString()));
        blueTeam.color(NamedTextColor.BLUE);
        Scoreboard.syncPlayerTeams();
    }

    public static boolean joinTeam(Player player, org.bukkit.scoreboard.Team team, boolean sendMessage) {
        if (team.equals(redTeam)) {
            if (redTeamPlayers.contains(player.getName())) {
                if (sendMessage) {
                    player.sendMessage(Message.NOTICE_ALREADY_IN_RED.getString(player));
                }
                return false;
            }
            if (blueTeamPlayers.contains(player.getName())) {
                blueTeam.removeEntry(player.getName());
                blueTeamPlayers.remove(player.getName());
            }
            redTeam.addEntry(player.getName());
            redTeamPlayers.add(player.getName());
            Scoreboard.syncPlayerTeams();
            if (sendMessage) {
                sendAll(Message.NOTICE_JOIN_RED,
                        (viewer, text) -> text.replace("%player%", player.getName()));
            }
        }
        else if (team.equals(blueTeam)) {
            if (blueTeamPlayers.contains(player.getName())) {
                if (sendMessage) {
                    player.sendMessage(Message.NOTICE_ALREADY_IN_BLUE.getString(player));
                }
                return false;
            }
            if (redTeamPlayers.contains(player.getName())) {
                redTeam.removeEntry(player.getName());
                redTeamPlayers.remove(player.getName());
            }
            blueTeam.addEntry(player.getName());
            blueTeamPlayers.add(player.getName());
            Scoreboard.syncPlayerTeams();
            if (sendMessage) {
                sendAll(Message.NOTICE_JOIN_BLUE,
                        (viewer, text) -> text.replace("%player%", player.getName()));
            }
        }
        return true;
    }

    public static boolean isPlayerInRedTeam(Player player) {
        return redTeamPlayers.contains(player.getName());
    }

    public static boolean isPlayerInBlueTeam(Player player) {
        return blueTeamPlayers.contains(player.getName());
    }

    public static void clearTeams() {
        new HashSet<>(redTeam.getEntries()).forEach(redTeam::removeEntry);
        new HashSet<>(blueTeam.getEntries()).forEach(blueTeam::removeEntry);
        redTeamPlayers.clear();
        blueTeamPlayers.clear();
        Scoreboard.syncPlayerTeams();
    }

    public static void restoreTeams(List<String> redPlayers, List<String> bluePlayers) {
        new HashSet<>(redTeam.getEntries()).forEach(redTeam::removeEntry);
        new HashSet<>(blueTeam.getEntries()).forEach(blueTeam::removeEntry);
        redTeamPlayers.clear();
        blueTeamPlayers.clear();
        redTeamPlayers.addAll(redPlayers);
        blueTeamPlayers.addAll(bluePlayers);
        redPlayers.forEach(redTeam::addEntry);
        bluePlayers.forEach(blueTeam::addEntry);
        Scoreboard.syncPlayerTeams();
    }

}
