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
        if (currentGameState == Game.GameState.INGAME && MenuMemory.reopen(player)) return;
        openMainMenu(player);
    }

    public static void openMainMenu(Player player) {
        if (currentGameState.equals(Game.GameState.PREGAME)) new PreGameMenu().open(player);
        if (currentGameState.equals(Game.GameState.INGAME)) new GameMenu().open(player);
    }

    public static void openTeamChestSelection(Player player) {
        if (currentGameState == Game.GameState.INGAME
                && (redTeamPlayers.contains(player.getName()) || blueTeamPlayers.contains(player.getName()))) {
            new GameMenu.TeamChestSelectMenu().open(player);
        } else openMainMenu(player);
    }

    @SuppressWarnings("deprecation") // InventoryView has no component-based title setter in Paper 26.2.
    public static void openTeamChest(Player player, int index) {
        if (redTeamPlayers.contains(player.getName())) {
            if (index < 0 || index >= redTeamChest.size()) { openMainMenu(player); return; }
            TeamChestNavigation.prepare(redTeamChest.get(index));
            player.openInventory(redTeamChest.get(index));
            player.getOpenInventory().setTitle(Message.MENU_RED_CHEST.getString(player) + (index + 1));
        } else if (blueTeamPlayers.contains(player.getName())) {
            if (index < 0 || index >= blueTeamChest.size()) { openMainMenu(player); return; }
            TeamChestNavigation.prepare(blueTeamChest.get(index));
            player.openInventory(blueTeamChest.get(index));
            player.getOpenInventory().setTitle(Message.MENU_BLUE_CHEST.getString(player) + (index + 1));
        }
        if (redTeamPlayers.contains(player.getName()) || blueTeamPlayers.contains(player.getName())) MenuMemory.recordChest(player, index);
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
