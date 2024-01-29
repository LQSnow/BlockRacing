package top.lqsnow.blockracing.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import static top.lqsnow.blockracing.listeners.BasicListener.editAmountPlayer;
import static top.lqsnow.blockracing.managers.Block.*;
import static top.lqsnow.blockracing.managers.Team.blueTeamPlayers;
import static top.lqsnow.blockracing.managers.Team.redTeamPlayers;
import static top.lqsnow.blockracing.utils.CommandUtil.sendAll;

public class Game {
    public enum GameState {PREGAME, INGAME, END}
    public static GameState currentGameState =  GameState.PREGAME;
    public static List<String> readyPlayers = new ArrayList<>();

    public static void playerLogin(Player player) {
        Scoreboard.showScoreboard(player);
    }

    public static void playerReady(Player player) {
        if (!readyPlayers.contains(player.getName())) {
            readyPlayers.add(player.getName());
            sendAll(Message.NOTICE_READY.getString().replace("%player%", player.getName()));
            // Check if the game can start, just notice players
            if (readyPlayers.size() > 1 && readyPlayers.size() == Bukkit.getOnlinePlayers().size()) {
                sendAll(Message.NOTICE_ALL_READY.getString());
            }
        } else {
            readyPlayers.remove(player.getName());
            sendAll(Message.NOTICE_CANCEL_READY.getString().replace("%player%", player.getName()));
        }
    }

    // Check if the game can start. If not, send the reason to the player; if possible, start the game directly
    public static void checkStartDemands(Player player) {

        // Not enough players
        if (!(readyPlayers.size() > 1)) {
            player.sendMessage(Message.NOTICE_NOT_ENOUGH_PLAYERS.getString());
            return;
        }

        // Exist unready players
        if (!(readyPlayers.size() == Bukkit.getOnlinePlayers().size())) {
            player.sendMessage(Message.NOTICE_UNREADY_PLAYERS.getString());
            return;
        }

        // Exist empty team
        if (redTeamPlayers.size() == 0 || blueTeamPlayers.size() == 0) {
            player.sendMessage(Message.NOTICE_EMPTY_TEAM.getString());
            return;
        }

        // Blocks have problems
        if (!checkBlock()) {
            return;
        }

        // Start the game
        sendAll(Message.NOTICE_START.getString());
        startGame();
    }

    public static void startGame() {
        // init
        editAmountPlayer.clear();
        setupBlocks();


    }

    public static GameState getCurrentGameState() {
        return currentGameState;
    }

    public static void setCurrentGameState(GameState currentGameState) {
        Game.currentGameState = currentGameState;
    }
}
