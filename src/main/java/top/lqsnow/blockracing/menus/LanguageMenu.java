package top.lqsnow.blockracing.menus;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import top.lqsnow.blockracing.managers.Gui;
import top.lqsnow.blockracing.managers.Game;
import top.lqsnow.blockracing.managers.LanguageManager;
import top.lqsnow.blockracing.managers.Scoreboard;
import top.lqsnow.blockracing.toolkit.item.ItemBuilder;
import top.lqsnow.blockracing.toolkit.menu.MenuButton;
import top.lqsnow.blockracing.toolkit.menu.MenuView;
import top.lqsnow.blockracing.toolkit.text.Texts;

import java.util.List;

public final class LanguageMenu extends MenuView {
    public LanguageMenu() {
        super(9, "Language / 语言");
        setButton(2, button(Material.RED_BANNER, "简体中文", "使用中文", LanguageManager.Preference.ZH_CN));
        setButton(4, button(Material.COMPASS, "自动 / Auto", "跟随客户端语言 / Follow client language",
                LanguageManager.Preference.AUTO));
        setButton(6, button(Material.BLUE_BANNER, "English", "Use English", LanguageManager.Preference.EN_US));
    }

    public static void sendFirstJoinPrompt(Player player) {
        Component openMenu = Texts.component("§a[Open / 打开]")
                .clickEvent(ClickEvent.runCommand("/language"))
                .hoverEvent(HoverEvent.showText(Texts.component("§7/language")));
        player.sendMessage(Texts.component("§7Language / 语言  ")
                .append(openMenu));
    }

    public static void apply(Player player, LanguageManager.Preference preference) {
        LanguageManager.setPreference(player, preference);
        Scoreboard.refreshPlayer(player);
        if (Game.getCurrentGameState() == Game.GameState.PREGAME) {
            Game.resetPregameInventory(player);
        }
        player.sendMessage(preference == LanguageManager.Preference.ZH_CN
                ? "§a语言已切换为简体中文。"
                : preference == LanguageManager.Preference.EN_US
                ? "§aLanguage changed to English."
                : "§aLanguage set to Auto. / 语言已设为自动。");
        player.closeInventory();
        Gui.openMenu(player);
    }

    private static MenuButton button(Material material, String name, String lore,
                                     LanguageManager.Preference preference) {
        return MenuButton.of(
                () -> ItemBuilder.of(material).name("§f" + name).lore(List.of("§7" + lore)).build(),
                (player, click) -> apply(player, preference)
        );
    }
}
