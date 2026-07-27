package top.lqsnow.blockracing.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Team;
import top.lqsnow.blockracing.utils.TranslationUtil;

import static top.lqsnow.blockracing.managers.Game.*;
import static top.lqsnow.blockracing.managers.Block.*;


public class Scoreboard {
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection();
    public static org.bukkit.scoreboard.Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
    public static Objective sidebar;

    public static void createScoreboard() {
        sidebar = scoreboard.registerNewObjective("sidebar", Criteria.DUMMY, Component.empty());
        sidebar.setDisplaySlot(DisplaySlot.SIDEBAR);
        for (int i = 1; i <= 15; i++) {
            Team team = scoreboard.registerNewTeam("SLOT_" + i);
            team.addEntry(genEntry(i));
        }
    }

    public static void setPreGameScoreboard() {
        // Generate displayed game mode
        String displayedGameMode = null;
        if (Setting.getCurrentGameMode().equals(Setting.GameMode.NORMAL)) {
            displayedGameMode = Setting.isSpeedMode() ? String.format("%s + %s", Message.SCOREBOARD_MODE_NORMAL.getString(), Message.SCOREBOARD_MODE_SPEED.getString()) : Message.SCOREBOARD_MODE_NORMAL.getString();
        } else if (Setting.getCurrentGameMode().equals(Setting.GameMode.RACING)) {
            displayedGameMode = Setting.isSpeedMode() ? String.format("%s + %s", Message.SCOREBOARD_MODE_RACING.getString(), Message.SCOREBOARD_MODE_SPEED.getString()) : Message.SCOREBOARD_MODE_RACING.getString();
        }

        // Generate blocks
        String blocks = String.format("%s%s%s%s%s", Message.SCOREBOARD_BLOCKS_EASY.getString(), (Setting.isEnableMediumBlock() ? " " + Message.SCOREBOARD_BLOCKS_MEDIUM.getString() : ""), (Setting.isEnableHardBlock() ? " " + Message.SCOREBOARD_BLOCKS_HARD.getString() : ""), (Setting.isEnableDyedBlock() ? " " + Message.SCOREBOARD_BLOCKS_DYED.getString() : ""), (Setting.isEnableEndBlock() ? " " + Message.SCOREBOARD_BLOCKS_END.getString() : ""));

        // Generate scoreboard
        setTitle(Message.SCOREBOARD_PREGAME_TITLE.getString());
        for (int slot = 11; slot >= 1; slot--) {
            String messageKey = "SCOREBOARD_PREGAME_SLOT" + slot;
            String originalMessage = Message.valueOf(messageKey).getString();
            if (originalMessage.equals("")) continue;
            String formattedMessage = originalMessage
                    .replace("%game_mode%", displayedGameMode)
                    .replace("%block_amount%", String.valueOf(Setting.getBlockAmount()))
                    .replace("%blocks%", blocks);

            setSlot(slot, formattedMessage);
        }
    }

    public static void setInGameScoreboard() {
        // Generate scoreboard
        // Set title
        setTitle(Message.SCOREBOARD_INGAME_TITLE.getString());
        // Set red team score display
        setSlot(12, Message.SCOREBOARD_RED_SCORE.getString().replace("%score%", String.valueOf(redTeamScore)).replace("%current_block%", String.valueOf(redTeamCurrentBlockAmount)).replace("%total_block%", String.valueOf(redTeamTotalBlockAmount)));
        // Clean red team blocks display
        for (int i = getCurrentBlocks("red").size(); i < 4; i++) {
            int slotIndex = 11 - i;
            setSlot(slotIndex, "");
        }
        // Set red team blocks display
        for (int i = 0; i < getCurrentBlocks("red").size(); i++) {
            int slotIndex = 11 - i;
            setSlot(slotIndex, getBlockDisplay(redTeamRemainingBlocks.get(i)));
        }
        // Set dividing line
        setSlot(7, Message.SCOREBOARD_DIVIDING_LINE.getString());
        // Set blue team score display
        setSlot(6, Message.SCOREBOARD_BLUE_SCORE.getString().replace("%score%", String.valueOf(blueTeamScore)).replace("%current_block%", String.valueOf(blueTeamCurrentBlockAmount)).replace("%total_block%", String.valueOf(blueTeamTotalBlockAmount)));
        // Clean blue team blocks display
        for (int i = getCurrentBlocks("blue").size(); i < 4; i++) {
            int slotIndex = 5 - i;
            setSlot(slotIndex, "");
        }
        // Set blue team blocks display
        for (int i = 0; i < getCurrentBlocks("blue").size(); i++) {
            int slotIndex = 5 - i;
            setSlot(slotIndex, getBlockDisplay(blueTeamRemainingBlocks.get(i)));
        }
        // Set bottom display
        setSlot(1, Message.SCOREBOARD_BOTTOM_SLOT.getString());
    }

    public static String getBlockDisplay(String block) {
        if (easyBlocks.contains(block)) {
            return String.format(Message.SCOREBOARD_BLOCK_FORMAT.getString().replace("%difficulty%", Message.SCOREBOARD_BLOCK_DIFFICULTY_EASY.getString()).replace("%block%", TranslationUtil.getValue(block)));
        } else if (mediumBlocks.contains(block)) {
            return String.format(Message.SCOREBOARD_BLOCK_FORMAT.getString().replace("%difficulty%", Message.SCOREBOARD_BLOCK_DIFFICULTY_MEDIUM.getString()).replace("%block%", TranslationUtil.getValue(block)));
        } else if (hardBlocks.contains(block)) {
            return String.format(Message.SCOREBOARD_BLOCK_FORMAT.getString().replace("%difficulty%", Message.SCOREBOARD_BLOCK_DIFFICULTY_HARD.getString()).replace("%block%", TranslationUtil.getValue(block)));
        } else if (dyedBlocks.contains(block)) {
            return String.format(Message.SCOREBOARD_BLOCK_FORMAT.getString().replace("%difficulty%", Message.SCOREBOARD_BLOCK_DIFFICULTY_DYED.getString()).replace("%block%", TranslationUtil.getValue(block)));
        } else if (endBlocks.contains(block)) {
            return String.format(Message.SCOREBOARD_BLOCK_FORMAT.getString().replace("%difficulty%", Message.SCOREBOARD_BLOCK_DIFFICULTY_END.getString()).replace("%block%", TranslationUtil.getValue(block)));
        }
        return null;
    }

    public static void showScoreboard(Player player) {
        player.setScoreboard(scoreboard);
    }

    public static void updateScoreboard() {
        if (getCurrentGameState().equals(GameState.PREGAME)) {
            setPreGameScoreboard();
        } else if (getCurrentGameState().equals(GameState.INGAME)) {
            setInGameScoreboard();
        }
    }


    /**
     * Generates an invisible, unique legacy entry for each scoreboard line.
     */
    private static String genEntry(int slot) {
        return "\u00A7" + Integer.toHexString(slot);
    }

    private static void setTitle(String title) {
        sidebar.displayName(LEGACY_SERIALIZER.deserialize(title));
    }

    private static void setSlot(int slot, String text) {
        Team team = scoreboard.getTeam("SLOT_" + slot);
        String entry = genEntry(slot);
        if (!scoreboard.getEntries().contains(entry)) {
            sidebar.getScore(entry).setScore(slot);
        }

        if (team == null) {
            return;
        }
        team.prefix(LEGACY_SERIALIZER.deserialize(text));
        team.suffix(Component.empty());
    }

}
