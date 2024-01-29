package top.lqsnow.blockracing.managers;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Team;


public class Scoreboard {
    public static org.bukkit.scoreboard.Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
    public static Objective sidebar;

    public static void createScoreboard() {
        sidebar = scoreboard.registerNewObjective("sidebar", "dummy");
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

    }

    public static void showScoreboard(Player player) {
        player.setScoreboard(scoreboard);
    }

    public static void updateScoreboard() {
        setPreGameScoreboard();
        setInGameScoreboard();
    }


    /**
     * https://github.com/Andy-K-Sparklight/PluginDiaryCode/blob/master/RarityCommons/src/main/java/rarityeg/commons/ScoreHelper.java
     * Help build up a scoreboard.
     * Considering RarityCommons isn't designed for Paper only,
     * we won't make migrations before Bukkit and Spigot support Kyori Powered Adventure.
     *
     * @author crisdev333
     * @author RarityEG
     */
    private static String genEntry(int slot) {
        return ChatColor.values()[slot].toString();
    }

    private static void setTitle(String title) {
        title = ChatColor.translateAlternateColorCodes('&', title);
        sidebar.setDisplayName(title.length() > 32 ? title.substring(0, 32) : title);
    }

    private static void setSlot(int slot, String text) {
        Team team = scoreboard.getTeam("SLOT_" + slot);
        String entry = genEntry(slot);
        if (!scoreboard.getEntries().contains(entry)) {
            sidebar.getScore(entry).setScore(slot);
        }

        text = ChatColor.translateAlternateColorCodes('&', text);
        String pre = getFirstSplit(text);
        String suf = getFirstSplit(ChatColor.getLastColors(pre) + getSecondSplit(text));

        // Edited
        if (pre.endsWith("§")) {
            pre = pre.substring(0, pre.length() - 1);
            if (suf.startsWith("§")) {
                suf = suf.substring(0, 2) + "§" + suf.substring(2);
            } else {
                suf = "§" + suf;
            }
        }

        if (team == null) {
            return;
        }
        team.setPrefix(pre);
        team.setSuffix(suf);
    }

    private static String getFirstSplit(String s) {
        return s.length() > 16 ? s.substring(0, 16) : s;
    }

    private static String getSecondSplit(String s) {
        if (s.length() > 32) {
            s = s.substring(0, 32);
        }
        return s.length() > 16 ? s.substring(16) : "";
    }

}
