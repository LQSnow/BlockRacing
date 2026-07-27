package top.lqsnow.blockracing.toolkit.menu;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class MenuManager {
    private MenuManager() {
    }

    public static void refreshOpenMenus(Class<? extends MenuView> menuType) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getOpenInventory().getTopInventory().getHolder() instanceof MenuView menu
                    && menu.getClass().equals(menuType)) {
                menu.refresh(player);
            }
        }
    }
}
