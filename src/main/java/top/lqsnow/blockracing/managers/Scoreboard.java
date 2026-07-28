package top.lqsnow.blockracing.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Team;
import top.lqsnow.blockracing.utils.TranslationUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import static top.lqsnow.blockracing.managers.Block.*;
import static top.lqsnow.blockracing.managers.Game.*;

public final class Scoreboard {
    private static final LegacyComponentSerializer LEGACY_SERIALIZER =
            LegacyComponentSerializer.legacySection();
    private static final Map<UUID, PlayerBoard> PLAYER_BOARDS = new HashMap<>();

    /**
     * The canonical board owns the teams used by the game logic. Players receive
     * localized copies whose team entries are synchronized by {@link #syncPlayerTeams()}.
     */
    public static final org.bukkit.scoreboard.Scoreboard scoreboard =
            Bukkit.getScoreboardManager().getNewScoreboard();
    public static Objective sidebar;

    private Scoreboard() {
    }

    public static void createScoreboard() {
        sidebar = createSidebar(scoreboard);
    }

    public static void setPreGameScoreboard() {
        renderPreGame(scoreboard, sidebar, null);
        PLAYER_BOARDS.forEach((uuid, view) ->
                renderPreGame(view.scoreboard(), view.sidebar(), Bukkit.getPlayer(uuid)));
    }

    public static void setInGameScoreboard() {
        renderInGame(scoreboard, sidebar, null);
        PLAYER_BOARDS.forEach((uuid, view) ->
                renderInGame(view.scoreboard(), view.sidebar(), Bukkit.getPlayer(uuid)));
    }

    public static String getBlockDisplay(String block) {
        return getBlockDisplay(block, null);
    }

    public static String getBlockDisplay(String block, Player player) {
        Message difficulty;
        if (easyBlocks.contains(block)) {
            difficulty = Message.SCOREBOARD_BLOCK_DIFFICULTY_EASY;
        } else if (mediumBlocks.contains(block)) {
            difficulty = Message.SCOREBOARD_BLOCK_DIFFICULTY_MEDIUM;
        } else if (hardBlocks.contains(block)) {
            difficulty = Message.SCOREBOARD_BLOCK_DIFFICULTY_HARD;
        } else if (dyedBlocks.contains(block)) {
            difficulty = Message.SCOREBOARD_BLOCK_DIFFICULTY_DYED;
        } else if (endBlocks.contains(block)) {
            difficulty = Message.SCOREBOARD_BLOCK_DIFFICULTY_END;
        } else {
            return null;
        }

        String difficultyText = text(difficulty, player).stripTrailing();
        String blockFormat = text(Message.SCOREBOARD_BLOCK_FORMAT, player);
        if (blockFormat.equals("%difficulty% | %block%")) {
            blockFormat = "§8[%difficulty%§8] §f%block%";
        }

        return blockFormat
                .replace("%difficulty%", difficultyText)
                .replace("%block%", player == null
                        ? TranslationUtil.getValue(block)
                        : TranslationUtil.getValue(block, player));
    }

    public static void showScoreboard(Player player) {
        PlayerBoard view = createPlayerBoard(player);
        PLAYER_BOARDS.put(player.getUniqueId(), view);
        syncPlayerTeams(view, player);
        renderCurrent(view, player);
        player.setScoreboard(view.scoreboard());
    }

    public static void refreshPlayer(Player player) {
        PlayerBoard view = PLAYER_BOARDS.get(player.getUniqueId());
        if (view == null) {
            showScoreboard(player);
            return;
        }
        syncPlayerTeams(view, player);
        renderCurrent(view, player);
        player.setScoreboard(view.scoreboard());
    }

    public static void removePlayer(Player player) {
        PLAYER_BOARDS.remove(player.getUniqueId());
    }

    public static void syncPlayerTeams() {
        PLAYER_BOARDS.forEach((uuid, view) -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                syncPlayerTeams(view, player);
            }
        });
    }

    public static void updateScoreboard() {
        if (getCurrentGameState().equals(GameState.PREGAME)) {
            setPreGameScoreboard();
        } else if (getCurrentGameState().equals(GameState.INGAME)) {
            setInGameScoreboard();
        }
    }

    private static PlayerBoard createPlayerBoard(Player player) {
        org.bukkit.scoreboard.Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = createSidebar(board);
        Team red = board.registerNewTeam("red");
        Team blue = board.registerNewTeam("blue");
        localizeTeam(red, Message.TEAM_RED_NAME, Message.TEAM_RED_PREFIX, player);
        localizeTeam(blue, Message.TEAM_BLUE_NAME, Message.TEAM_BLUE_PREFIX, player);
        return new PlayerBoard(board, objective);
    }

    private static Objective createSidebar(org.bukkit.scoreboard.Scoreboard board) {
        Objective objective = board.registerNewObjective("sidebar", Criteria.DUMMY, Component.empty());
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        for (int i = 1; i <= 15; i++) {
            Team team = board.registerNewTeam("SLOT_" + i);
            team.addEntry(genEntry(i));
        }
        return objective;
    }

    private static void renderCurrent(PlayerBoard view, Player player) {
        if (getCurrentGameState().equals(GameState.INGAME)) {
            renderInGame(view.scoreboard(), view.sidebar(), player);
        } else {
            renderPreGame(view.scoreboard(), view.sidebar(), player);
        }
    }

    private static void renderPreGame(org.bukkit.scoreboard.Scoreboard board,
                                      Objective objective, Player player) {
        clearLines(board);

        String baseMode = Setting.getCurrentGameMode().equals(Setting.GameMode.NORMAL)
                ? text(Message.SCOREBOARD_MODE_NORMAL, player)
                : text(Message.SCOREBOARD_MODE_RACING, player);
        String displayedGameMode = Setting.isSpeedMode()
                ? baseMode + " + " + text(Message.SCOREBOARD_MODE_SPEED, player)
                : baseMode;

        String blocks = text(Message.SCOREBOARD_BLOCKS_EASY, player)
                + (Setting.isEnableMediumBlock() ? " " + text(Message.SCOREBOARD_BLOCKS_MEDIUM, player) : "")
                + (Setting.isEnableHardBlock() ? " " + text(Message.SCOREBOARD_BLOCKS_HARD, player) : "")
                + (Setting.isEnableDyedBlock() ? " " + text(Message.SCOREBOARD_BLOCKS_DYED, player) : "")
                + (Setting.isEnableEndBlock() ? " " + text(Message.SCOREBOARD_BLOCKS_END, player) : "");

        setTitle(objective, text(Message.SCOREBOARD_PREGAME_TITLE, player));
        for (int slot = 11; slot >= 1; slot--) {
            String originalMessage = text(Message.valueOf("SCOREBOARD_PREGAME_SLOT" + slot), player);
            if (originalMessage.isEmpty()) {
                continue;
            }
            setSlot(board, objective, slot, originalMessage
                    .replace("%game_mode%", displayedGameMode)
                    .replace("%block_amount%", String.valueOf(Setting.getBlockAmount()))
                    .replace("%blocks%", blocks));
        }
    }

    private static void renderInGame(org.bukkit.scoreboard.Scoreboard board,
                                     Objective objective, Player player) {
        clearLines(board);
        setTitle(objective, text(Message.SCOREBOARD_INGAME_TITLE, player));
        setSlot(board, objective, 12, text(Message.SCOREBOARD_RED_SCORE, player)
                .replace("%score%", String.valueOf(redTeamScore))
                .replace("%current_block%", String.valueOf(redTeamCurrentBlockAmount))
                .replace("%total_block%", String.valueOf(redTeamTotalBlockAmount)));

        for (int i = 0; i < getCurrentBlocks("red").size(); i++) {
            setSlot(board, objective, 11 - i,
                    getBlockDisplay(redTeamRemainingBlocks.get(i), player));
        }
        setSlot(board, objective, 7, text(Message.SCOREBOARD_DIVIDING_LINE, player));
        setSlot(board, objective, 6, text(Message.SCOREBOARD_BLUE_SCORE, player)
                .replace("%score%", String.valueOf(blueTeamScore))
                .replace("%current_block%", String.valueOf(blueTeamCurrentBlockAmount))
                .replace("%total_block%", String.valueOf(blueTeamTotalBlockAmount)));

        for (int i = 0; i < getCurrentBlocks("blue").size(); i++) {
            setSlot(board, objective, 5 - i,
                    getBlockDisplay(blueTeamRemainingBlocks.get(i), player));
        }
        setSlot(board, objective, 1, text(Message.SCOREBOARD_BOTTOM_SLOT, player));
    }

    private static void syncPlayerTeams(PlayerBoard view, Player player) {
        Team red = view.scoreboard().getTeam("red");
        Team blue = view.scoreboard().getTeam("blue");
        if (red == null || blue == null) {
            return;
        }

        new HashSet<>(red.getEntries()).forEach(red::removeEntry);
        new HashSet<>(blue.getEntries()).forEach(blue::removeEntry);
        top.lqsnow.blockracing.managers.Team.redTeamPlayers.forEach(red::addEntry);
        top.lqsnow.blockracing.managers.Team.blueTeamPlayers.forEach(blue::addEntry);
        localizeTeam(red, Message.TEAM_RED_NAME, Message.TEAM_RED_PREFIX, player);
        localizeTeam(blue, Message.TEAM_BLUE_NAME, Message.TEAM_BLUE_PREFIX, player);
    }

    private static void localizeTeam(Team team, Message name, Message prefix, Player player) {
        team.displayName(LEGACY_SERIALIZER.deserialize(text(name, player)));
        team.prefix(LEGACY_SERIALIZER.deserialize(text(prefix, player)));
        team.color(name == Message.TEAM_RED_NAME ? NamedTextColor.RED : NamedTextColor.BLUE);
    }

    private static String text(Message message, Player player) {
        return player == null ? message.getString() : message.getString(player);
    }

    private static void clearLines(org.bukkit.scoreboard.Scoreboard board) {
        for (int slot = 1; slot <= 15; slot++) {
            board.resetScores(genEntry(slot));
        }
    }

    private static String genEntry(int slot) {
        return "\u00A7" + Integer.toHexString(slot);
    }

    private static void setTitle(Objective objective, String title) {
        objective.displayName(LEGACY_SERIALIZER.deserialize(title));
    }

    private static void setSlot(org.bukkit.scoreboard.Scoreboard board,
                                Objective objective, int slot, String text) {
        Team team = board.getTeam("SLOT_" + slot);
        if (team == null || text == null) {
            return;
        }
        String entry = genEntry(slot);
        objective.getScore(entry).setScore(slot);
        team.prefix(LEGACY_SERIALIZER.deserialize(text));
        team.suffix(Component.empty());
    }

    private record PlayerBoard(org.bukkit.scoreboard.Scoreboard scoreboard, Objective sidebar) {
    }
}
