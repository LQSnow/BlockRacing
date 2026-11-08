package top.lqsnow.blockracing.managers;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import top.lqsnow.blockracing.Main;
import top.lqsnow.blockracing.menus.*;
import top.lqsnow.blockracing.toolkit.menu.*;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;

public final class MenuMemory {
    private static MenuHistory history = new MenuHistory();
    private static File file;
    private MenuMemory() {}

    public static void load() {
        history = new MenuHistory();
        file = new File(Main.getInstance().getDataFolder(), "menu-preferences.yml");
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        for (String id : yaml.getKeys(false)) {
            try { history.enable(UUID.fromString(id), yaml.getBoolean(id)); }
            catch (IllegalArgumentException ignored) { /* Ignore invalid preference IDs. */ }
        }
    }

    public static boolean enabled(Player player) { return history.enabled(player.getUniqueId()); }
    public static void toggle(Player player) {
        history.enable(player.getUniqueId(), !enabled(player));
        YamlConfiguration yaml = new YamlConfiguration();
        history.enabledPlayers().forEach(id -> yaml.set(id.toString(), true));
        try { yaml.save(file); }
        catch (IOException ex) { Main.getInstance().getLogger().log(Level.WARNING, "Unable to save menu preferences", ex); }
    }

    public static void resetDestinations() { history.resetDestinations(); }
    private static String team(Player player) {
        return Team.redTeamPlayers.contains(player.getName()) ? "red"
                : Team.blueTeamPlayers.contains(player.getName()) ? "blue" : "";
    }

    public static void record(Player player, MenuView menu) {
        if (Game.getCurrentGameState() != Game.GameState.INGAME) return;
        MenuHistory.Kind kind;
        if (menu instanceof GameMenu) kind = MenuHistory.Kind.MAIN;
        else if (menu instanceof GameMenu.TeamChestSelectMenu) kind = MenuHistory.Kind.CHEST_SELECT;
        else if (menu instanceof GameMenu.WayPointMenu) kind = MenuHistory.Kind.WAYPOINTS;
        else if (menu instanceof GameMenu.TeammateTeleportMenu) kind = MenuHistory.Kind.TEAMMATES;
        else if (menu instanceof LanguageMenu) kind = MenuHistory.Kind.LANGUAGE;
        else return;
        history.record(player.getUniqueId(), new MenuHistory.Destination(kind,
                kind == MenuHistory.Kind.MAIN || kind == MenuHistory.Kind.LANGUAGE ? "" : team(player), 0));
    }

    public static void recordChest(Player player, int index) {
        if (Game.getCurrentGameState() == Game.GameState.INGAME) {
            history.record(player.getUniqueId(), new MenuHistory.Destination(MenuHistory.Kind.CHEST, team(player), index));
        }
    }

    public static boolean reopen(Player player) {
        if (!enabled(player)) return false;
        var remembered = history.last(player.getUniqueId());
        if (remembered.isEmpty()) return false;
        var destination = remembered.get();
        if (!destination.team().isEmpty() && !destination.team().equals(team(player))) return false;
        switch (destination.kind()) {
            case MAIN -> { return false; }
            case LANGUAGE -> new LanguageMenu().open(player);
            case CHEST_SELECT -> {
                if (team(player).isEmpty()) return false;
                new GameMenu.TeamChestSelectMenu().open(player);
            }
            case TEAMMATES -> {
                if (team(player).isEmpty()) return false;
                new GameMenu.TeammateTeleportMenu(player).open(player);
            }
            case WAYPOINTS -> {
                if (team(player).isEmpty()) return false;
                boolean red = team(player).equals("red");
                new GameMenu.WayPointMenu(red ? Game.redWaypoint : Game.blueWaypoint,
                        red ? Game.redWaypointIconCache : Game.blueWaypointIconCache).open(player);
            }
            case CHEST -> {
                if (team(player).isEmpty() || destination.index() >= (team(player).equals("red") ? Game.redTeamChest : Game.blueTeamChest).size()) return false;
                Gui.openTeamChest(player, destination.index());
            }
        }
        return true;
    }
}
