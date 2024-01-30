package top.lqsnow.blockracing.managers;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import top.lqsnow.blockracing.Main;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

public enum Message {
    // scoreboard
    SCOREBOARD_MODE_NORMAL("scoreboard.game-mode.normal"),
    SCOREBOARD_MODE_RACING("scoreboard.game-mode.racing"),
    SCOREBOARD_MODE_SPEED("scoreboard.game-mode.speed"),
    SCOREBOARD_BLOCKS_EASY("scoreboard.blocks.easy"),
    SCOREBOARD_BLOCKS_MEDIUM("scoreboard.blocks.medium"),
    SCOREBOARD_BLOCKS_HARD("scoreboard.blocks.hard"),
    SCOREBOARD_BLOCKS_DYED("scoreboard.blocks.dyed"),
    SCOREBOARD_BLOCKS_END("scoreboard.blocks.end"),
    SCOREBOARD_PREGAME_TITLE("scoreboard.pregame.title"),
    SCOREBOARD_PREGAME_SLOT11("scoreboard.pregame.slot11"),
    SCOREBOARD_PREGAME_SLOT10("scoreboard.pregame.slot10"),
    SCOREBOARD_PREGAME_SLOT9("scoreboard.pregame.slot9"),
    SCOREBOARD_PREGAME_SLOT8("scoreboard.pregame.slot8"),
    SCOREBOARD_PREGAME_SLOT7("scoreboard.pregame.slot7"),
    SCOREBOARD_PREGAME_SLOT6("scoreboard.pregame.slot6"),
    SCOREBOARD_PREGAME_SLOT5("scoreboard.pregame.slot5"),
    SCOREBOARD_PREGAME_SLOT4("scoreboard.pregame.slot4"),
    SCOREBOARD_PREGAME_SLOT3("scoreboard.pregame.slot3"),
    SCOREBOARD_PREGAME_SLOT2("scoreboard.pregame.slot2"),
    SCOREBOARD_PREGAME_SLOT1("scoreboard.pregame.slot1"),
    SCOREBOARD_INGAME_TITLE("scoreboard.ingame.title"),
    SCOREBOARD_RED_SCORE("scoreboard.ingame.red-score"),
    SCOREBOARD_BLUE_SCORE("scoreboard.ingame.blue-score"),
    SCOREBOARD_BLOCK_FORMAT("scoreboard.ingame.block-format"),
    SCOREBOARD_DIVIDING_LINE("scoreboard.ingame.dividing-line"),
    SCOREBOARD_BOTTOM_SLOT("scoreboard.ingame.bottom-slot"),
    SCOREBOARD_BLOCK_DIFFICULTY_EASY("scoreboard.ingame.block-difficulty.easy"),
    SCOREBOARD_BLOCK_DIFFICULTY_MEDIUM("scoreboard.ingame.block-difficulty.medium"),
    SCOREBOARD_BLOCK_DIFFICULTY_HARD("scoreboard.ingame.block-difficulty.hard"),
    SCOREBOARD_BLOCK_DIFFICULTY_DYED("scoreboard.ingame.block-difficulty.dyed"),
    SCOREBOARD_BLOCK_DIFFICULTY_END("scoreboard.ingame.block-difficulty.end"),

    // team
    TEAM_RED_NAME("team.red.name"),
    TEAM_RED_PREFIX("team.red.prefix"),
    TEAM_RED_CHAT("team.red.chat"),
    TEAM_RED_COLOR("team.red.color"),
    TEAM_BLUE_NAME("team.blue.name"),
    TEAM_BLUE_PREFIX("team.blue.prefix"),
    TEAM_BLUE_CHAT("team.blue.chat"),
    TEAM_BLUE_COLOR("team.blue.color"),

    // menu
    MENU_PREGAME_TITLE("menu.pregame-menu.title"),
    MENU_JOIN_RED("menu.pregame-menu.join-red"),
    MENU_JOIN_RED_LORE("menu.pregame-menu.join-red-lore"),
    MENU_JOIN_BLUE("menu.pregame-menu.join-blue"),
    MENU_JOIN_BLUE_LORE("menu.pregame-menu.join-blue-lore"),
    MENU_READY("menu.pregame-menu.ready"),
    MENU_READY_LORE("menu.pregame-menu.ready-lore"),
    MENU_START("menu.pregame-menu.start"),
    MENU_START_LORE("menu.pregame-menu.start-lore"),
    MENU_BLOCK_AMOUNT("menu.pregame-menu.block-amount"),
    MENU_BLOCK_AMOUNT_LORE("menu.pregame-menu.block-amount-lore"),
    MENU_MEDIUM_BLOCKS("menu.pregame-menu.medium-blocks"),
    MENU_HARD_BLOCKS("menu.pregame-menu.hard-blocks"),
    MENU_DYED_BLOCKS("menu.pregame-menu.dyed-blocks"),
    MENU_END_BLOCKS("menu.pregame-menu.end-blocks"),
    MENU_DISABLED("menu.pregame-menu.disabled"),
    MENU_ENABLED("menu.pregame-menu.enabled"),
    MENU_CURRENT_MODE("menu.pregame-menu.current-mode"),
    MENU_SWITCH_TO("menu.pregame-menu.switch-to"),
    MENU_NORMAL_MODE("menu.pregame-menu.normal-mode"),
    MENU_RACING_MODE("menu.pregame-menu.racing-mode"),
    MENU_SPEED_MODE_ENABLED("menu.pregame-menu.speed-mode-enabled"),
    MENU_SPEED_MODE_DISABLED("menu.pregame-menu.speed-mode-disabled"),
    MENU_NORMAL_MODE_LORE("menu.pregame-menu.normal-mode-lore"),
    MENU_RACING_MODE_LORE("menu.pregame-menu.racing-mode-lore"),
    MENU_SPEED_MODE_LORE("menu.pregame-menu.speed-mode-lore"),
    MENU_SELECT_TEAM("menu.pregame-menu.select-team"),
    MENU_BLOCK_SETTING("menu.pregame-menu.block-setting"),
    MENU_SELECT_MODE("menu.pregame-menu.select-mode"),
    MENU_READY_AND_START("menu.pregame-menu.ready-and-start"),
    MENU_GAME_TITLE("menu.game-menu.title"),
    MENU_TEAM_CHEST("menu.game-menu.team-chest"),
    MENU_TEAM_CHEST_LORE("menu.game-menu.team-chest-lore"),
    MENU_ROLL("menu.game-menu.roll"),
    MENU_ROLL_LORE("menu.game-menu.roll-lore"),
    MENU_LOCATE("menu.game-menu.locate"),
    MENU_LOCATE_LORE("menu.game-menu.locate-lore"),
    MENU_WAYPOINTS("menu.game-menu.waypoints"),
    MENU_WAYPOINTS_LORE("menu.game-menu.waypoints-lore"),
    MENU_RANDOM_TP("menu.game-menu.random-tp"),
    MENU_RANDOM_TP_LORE("menu.game-menu.random-tp-lore"),
    MENU_TEAM_CHEST_SELECT_TITLE("menu.team-chest-select-menu.title"),
    MENU_TEAM_CHEST_SELECT_CHEST1("menu.team-chest-select-menu.chest1"),
    MENU_TEAM_CHEST_SELECT_CHEST2("menu.team-chest-select-menu.chest2"),
    MENU_TEAM_CHEST_SELECT_CHEST3("menu.team-chest-select-menu.chest3"),
    MENU_RED_CHEST1("menu.team-chest.red-chest1"),
    MENU_RED_CHEST2("menu.team-chest.red-chest2"),
    MENU_RED_CHEST3("menu.team-chest.red-chest3"),
    MENU_BLUE_CHEST1("menu.team-chest.blue-chest1"),
    MENU_BLUE_CHEST2("menu.team-chest.blue-chest2"),
    MENU_BLUE_CHEST3("menu.team-chest.blue-chest3"),
    MENU_WAYPOINT_TITLE("menu.way-point.title"),
    MENU_WAYPOINT_EMPTY_1("menu.way-point.empty.waypoint1"),
    MENU_WAYPOINT_EMPTY_2("menu.way-point.empty.waypoint2"),
    MENU_WAYPOINT_EMPTY_3("menu.way-point.empty.waypoint3"),
    MENU_WAYPOINT_EMPTY_LORE("menu.way-point.empty.lore"),
    MENU_WAYPOINT_FILLED_1("menu.way-point.filled.waypoint1"),
    MENU_WAYPOINT_FILLED_2("menu.way-point.filled.waypoint2"),
    MENU_WAYPOINT_FILLED_3("menu.way-point.filled.waypoint3"),
    MENU_WAYPOINT_FILLED_LORE("menu.way-point.filled.lore"),
    MENU_ALL_RETURN_BACK("menu.all.return-back"),

    // notice
    NOTICE_WELCOME("notice.welcome"),
    NOTICE_JOIN_RED("notice.join-red"),
    NOTICE_JOIN_BLUE("notice.join-blue"),
    NOTICE_ALREADY_IN_RED("notice.already-in-red"),
    NOTICE_ALREADY_IN_BLUE("notice.already-in-blue"),
    NOTICE_READY("notice.ready"),
    NOTICE_CANCEL_READY("notice.cancel-ready"),
    NOTICE_ALL_READY("notice.all-ready"),
    NOTICE_START("notice.start"),
    NOTICE_EXIST_UNREADY("notice.exist-unready"),
    NOTICE_UNREADY_PLAYERS("notice.unready-players"),
    NOTICE_NOT_ENOUGH_PLAYERS("notice.not-enough-players"),
    NOTICE_EMPTY_TEAM("notice.empty-team"),
    NOTICE_TOO_MUCH_BLOCKS("notice.too-much-blocks"),
    NOTICE_SET_BLOCKS("notice.set-blocks"),
    NOTICE_SET_BLOCKS_QUIT("notice.set-blocks-quit"),
    NOTICE_SET_BLOCKS_ERROR("notice.set-blocks-error"),
    NOTICE_SET_BLOCKS_SUCCESS("notice.set-blocks-success"),
    NOTICE_SPAWN_PROTECT("notice.spawn-protect-notice"),
    NOTICE_ERROR_BLOCK("notice.exist-error-block"),
    NOTICE_NOT_ENOUGH_SCORE("notice.not-enough-score"),
    NOTICE_RANDOM_TP("notice.random-tp"),
    NOTICE_RED_COLLECT("notice.red-collect"),
    NOTICE_BLUE_COLLECT("notice.blue-collect"),
    NOTICE_TEAM_CHEST_FULL("notice.team-chest-full"),
    NOTICE_RED_WIN("notice.red-win"),
    NOTICE_BLUE_WIN("notice.blue-win"),
    NOTICE_RED_TEAM_CHEST("notice.red-team-chest"),
    NOTICE_BLUE_TEAM_CHEST("notice.blue-team-chest"),
    NOTICE_REMOVE_WAYPOINT("notice.remove-waypoint"),
    NOTICE_TP_SUCCESS("notice.tp-success"),
    NOTICE_TP_OCEAN("notice.tp-ocean"),
    NOTICE_RED_REMOVE_WAYPOINT("notice.red-remove-waypoint"),
    NOTICE_BLUE_REMOVE_WAYPOINT("notice.blue-remove-waypoint"),
    NOTICE_CANNOT_ROLL("notice.cannot-roll"),
    NOTICE_ROLL_REQUEST("notice.roll-request"),
    NOTICE_ROLL_REQUEST_CANCEL("notice.roll-request-cancel"),
    NOTICE_RED_ROLL_SUCCESS("notice.red-roll-success"),
    NOTICE_BLUE_ROLL_SUCCESS("notice.blue-roll-success"),
    NOTICE_SPECTATOR("notice.spectator"),
    NOTICE_SPECTATOR_JOIN("notice.spectator-join"),
    NOTICE_LOCATE_ALREADY_BOUGHT("notice.locate-already-bought"),
    NOTICE_BUY_LOCATE("notice.buy-locate"),
    NOTICE_ERROR_COMMAND("notice.error-command"),
    NOTICE_LOCATE_NO_PERMISSION("notice.locate-no-permission"),
    NOTICE_RESTART("notice.restart"),
    NOTICE_RESTART_CANCEL("notice.restart-cancel"),
    NOTICE_GAME_NOT_START("notice.game-not-start"),
    NOTICE_TP_PLAYER_SUCCESS("notice.tp-player-success"),
    NOTICE_SPECTATOR_TP_PLAYER_SUCCESS("notice.spectator-tp-player-success"),
    NOTICE_PLAYER_NOT_EXIST("notice.player-not-exist"),
    NOTICE_PLAYER_NOT_IN_SAME_TEAM("notice.player-not-in-same-team"),

    // other
    MESSAGE_LANG("lang"),
    MESSAGE_VERSION("lang-version");

    private static File file;
    private String path;
    private String cacheString;
    private List<String> cacheStringList;

    Message(String path) {
        this.path = path;
    }

    public static void saveDefaultConfig() {
        File messageFile = new File(Main.getInstance().getDataFolder(), "lang.yml");
        if (!messageFile.exists()) {
            Main.getInstance().saveResource("lang.yml", false);
        }
    }

    public static void load() {
        if (file == null) {
            file = new File(Main.getInstance().getDataFolder(), "lang.yml");
        }

        for (Message m : values()) {
            m.cacheString = null;
            m.cacheStringList = null;
        }
    }

    private static FileConfiguration getMessageConfig() {
        FileConfiguration messageConfig = YamlConfiguration.loadConfiguration(new File(Main.getInstance().getDataFolder(), "lang.yml"));

        try (Reader reader = new InputStreamReader(Main.getInstance().getResource("lang.yml"), StandardCharsets.UTF_8)) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(reader);
            messageConfig.setDefaults(defConfig);
        } catch (IOException e) {
            Main.getInstance().getLogger().log(Level.SEVERE, "Error reading lang.yml!", e);
        }

        return messageConfig;
    }

    public String getString() {
        return cacheString != null ? cacheString : (cacheString = ChatColor.translateAlternateColorCodes('&', getMessageConfig().getString(path)));
    }

    public List<String> getStringList() {
        return cacheStringList != null ? cacheStringList : (cacheStringList = Collections.unmodifiableList(
                getMessageConfig().getStringList(path).stream()
                        .map(msg -> ChatColor.translateAlternateColorCodes('&', msg))
                        .collect(Collectors.toList())
        ));
    }
}
