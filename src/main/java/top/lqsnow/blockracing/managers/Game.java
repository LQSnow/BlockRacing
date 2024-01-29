package top.lqsnow.blockracing.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

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
            // Check if the game can start
            if (readyPlayers.size() > 1 && readyPlayers.size() == Bukkit.getOnlinePlayers().size()) {
                sendAll(Message.NOTICE_ALL_READY.getString());
            }
        } else {
            readyPlayers.remove(player.getName());
            sendAll(Message.NOTICE_CANCEL_READY.getString().replace("%player%", player.getName()));
        }
    }

    public static void startGame() {

    }

    public static GameState getCurrentGameState() {
        return currentGameState;
    }

    public static void setCurrentGameState(GameState currentGameState) {
        Game.currentGameState = currentGameState;
    }
}
