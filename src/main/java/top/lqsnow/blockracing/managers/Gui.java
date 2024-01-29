package top.lqsnow.blockracing.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.mineacademy.fo.menu.button.ButtonReturnBack;
import org.mineacademy.fo.remain.CompMaterial;
import top.lqsnow.blockracing.menus.PreGameMenu;

import static top.lqsnow.blockracing.managers.Game.currentGameState;

public class Gui {
    public static Inventory checkBlockInventory = Bukkit.createInventory(null, 9, "Check Block Inventory");
    public static Inventory redTeamChest1 = Bukkit.createInventory(null, 6 * 9, Message.MENU_RED_CHEST1.getString());
    public static Inventory redTeamChest2 = Bukkit.createInventory(null, 6 * 9, Message.MENU_RED_CHEST2.getString());
    public static Inventory redTeamChest3 = Bukkit.createInventory(null, 6 * 9, Message.MENU_RED_CHEST3.getString());
    public static Inventory blueTeamChest1 = Bukkit.createInventory(null, 6 * 9, Message.MENU_BLUE_CHEST1.getString());
    public static Inventory blueTeamChest2 = Bukkit.createInventory(null, 6 * 9, Message.MENU_BLUE_CHEST2.getString());
    public static Inventory blueTeamChest3 = Bukkit.createInventory(null, 6 * 9, Message.MENU_BLUE_CHEST3.getString());

    public Gui() {
        ButtonReturnBack.setMaterial(CompMaterial.ARROW);
    }

    public static void openMenu(Player player) {
        if (currentGameState.equals(Game.GameState.PREGAME)) new PreGameMenu().displayTo(player);
    }

    public static void closeAllPlayersMenu() {
        Bukkit.getOnlinePlayers().forEach((Player player) -> {
            player.closeInventory();
        });
    }
}
