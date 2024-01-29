package top.lqsnow.blockracing.managers;

import org.bukkit.entity.Player;
import top.lqsnow.blockracing.menus.PreGameMenu;

import static top.lqsnow.blockracing.managers.Game.currentGameState;

public class Gui {
    public static void openMenu(Player player) {
        if (currentGameState.equals(Game.GameState.PREGAME)) new PreGameMenu().displayTo(player);
    }
}
