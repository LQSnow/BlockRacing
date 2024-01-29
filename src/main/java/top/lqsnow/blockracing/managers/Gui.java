package top.lqsnow.blockracing.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import top.lqsnow.blockracing.menus.PreGameMenu;

import static top.lqsnow.blockracing.managers.Game.currentGameState;

public class Gui {
    public static Inventory checkBlockInventory = Bukkit.createInventory(null, 9, "Check Block Inventory");

    public static void openMenu(Player player) {
        if (currentGameState.equals(Game.GameState.PREGAME)) new PreGameMenu().displayTo(player);
    }
}
