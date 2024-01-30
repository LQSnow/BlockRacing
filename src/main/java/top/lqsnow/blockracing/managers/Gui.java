package top.lqsnow.blockracing.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.mineacademy.fo.menu.Menu;
import top.lqsnow.blockracing.menus.GameMenu;
import top.lqsnow.blockracing.menus.PreGameMenu;

import static top.lqsnow.blockracing.managers.Game.currentGameState;
import static top.lqsnow.blockracing.managers.Team.blueTeamPlayers;
import static top.lqsnow.blockracing.managers.Team.redTeamPlayers;

public class Gui {
    public static Inventory checkBlockInventory = Bukkit.createInventory(null, 9, "Check Block Inventory");
    public static Inventory redTeamChest1 = Bukkit.createInventory(null, 6 * 9, Message.MENU_RED_CHEST1.getString());
    public static Inventory redTeamChest2 = Bukkit.createInventory(null, 6 * 9, Message.MENU_RED_CHEST2.getString());
    public static Inventory redTeamChest3 = Bukkit.createInventory(null, 6 * 9, Message.MENU_RED_CHEST3.getString());
    public static Inventory blueTeamChest1 = Bukkit.createInventory(null, 6 * 9, Message.MENU_BLUE_CHEST1.getString());
    public static Inventory blueTeamChest2 = Bukkit.createInventory(null, 6 * 9, Message.MENU_BLUE_CHEST2.getString());
    public static Inventory blueTeamChest3 = Bukkit.createInventory(null, 6 * 9, Message.MENU_BLUE_CHEST3.getString());

    public static void openMenu(Player player) {
        if (currentGameState.equals(Game.GameState.PREGAME)) new PreGameMenu().displayTo(player);
        if (currentGameState.equals(Game.GameState.INGAME)) new GameMenu().displayTo(player);
    }

    public static void openTeamChest(Player player, int index) {
        if (redTeamPlayers.contains(player.getName())) {
            player.openInventory(switch (index) {
                case 1 -> redTeamChest1;
                case 2 -> redTeamChest2;
                case 3 -> redTeamChest3;
                default -> throw new IllegalArgumentException();
            });
        } else if (blueTeamPlayers.contains(player.getName())) {
            player.openInventory(switch (index) {
                case 1 -> blueTeamChest1;
                case 2 -> blueTeamChest2;
                case 3 -> blueTeamChest3;
                default -> throw new IllegalArgumentException();
            });
        }
    }

    public static void closeAllPlayersMenu() {
        Bukkit.getOnlinePlayers().forEach((Player player) -> {
            player.closeInventory();
        });
    }

    // Update menu
    public static void updateMenu(Menu menu) {
        Bukkit.getOnlinePlayers().forEach((Player player) -> {
            try {
                Menu playerMenu = Menu.getMenu(player);
                if (playerMenu.getClass().equals(menu.getClass())) {
                    playerMenu.restartMenu();
                }
            } catch (NullPointerException ignored) {}
        });
    }
}
