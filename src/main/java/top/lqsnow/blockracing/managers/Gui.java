package top.lqsnow.blockracing.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import top.lqsnow.blockracing.menus.GameMenu;
import top.lqsnow.blockracing.menus.PreGameMenu;
import top.lqsnow.blockracing.toolkit.menu.MenuManager;
import top.lqsnow.blockracing.toolkit.menu.MenuView;

import static top.lqsnow.blockracing.managers.Game.redTeamChest;
import static top.lqsnow.blockracing.managers.Game.blueTeamChest;
import static top.lqsnow.blockracing.managers.Game.currentGameState;
import static top.lqsnow.blockracing.managers.Team.blueTeamPlayers;
import static top.lqsnow.blockracing.managers.Team.redTeamPlayers;

public class Gui {
    public static void openMenu(Player player) {
        if (currentGameState.equals(Game.GameState.PREGAME)) new PreGameMenu().open(player);
        if (currentGameState.equals(Game.GameState.INGAME)) new GameMenu().open(player);
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
    public static void updateMenu(MenuView menu) {
        MenuManager.refreshOpenMenus(menu.getClass());
    }
}
