package top.lqsnow.blockracing.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.mineacademy.fo.menu.Menu;
import top.lqsnow.blockracing.menus.GameMenu;
import top.lqsnow.blockracing.menus.PreGameMenu;

import static top.lqsnow.blockracing.managers.Game.redTeamChest;
import static top.lqsnow.blockracing.managers.Game.blueTeamChest;
import static top.lqsnow.blockracing.managers.Game.currentGameState;
import static top.lqsnow.blockracing.managers.Team.blueTeamPlayers;
import static top.lqsnow.blockracing.managers.Team.redTeamPlayers;

public class Gui {
    public static Inventory checkBlockInventory = Bukkit.createInventory(null, 9, "Check Block Inventory");

    public static void openMenu(Player player) {
        if (currentGameState.equals(Game.GameState.PREGAME)) new PreGameMenu().displayTo(player);
        if (currentGameState.equals(Game.GameState.INGAME)) new GameMenu().displayTo(player);
    }

    public static void openTeamChest(Player player, int index) {
        if (redTeamPlayers.contains(player.getName())) {
            player.openInventory(redTeamChest.get(index));
        } else if (blueTeamPlayers.contains(player.getName())) {
            player.openInventory(blueTeamChest.get(index));
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
