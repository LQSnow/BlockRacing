package top.lqsnow.blockracing.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.lqsnow.blockracing.managers.LanguageManager;
import top.lqsnow.blockracing.menus.LanguageMenu;

import java.util.List;

public final class Language implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by a player.");
            return true;
        }
        if (args.length == 0) {
            new LanguageMenu().open(player);
            return true;
        }
        LanguageManager.Preference preference = switch (args[0].toLowerCase()) {
            case "auto" -> LanguageManager.Preference.AUTO;
            case "zh_cn", "zh", "chinese" -> LanguageManager.Preference.ZH_CN;
            case "en_us", "en", "english" -> LanguageManager.Preference.EN_US;
            default -> null;
        };
        if (preference == null) {
            player.sendMessage("Usage: /language [auto|zh_cn|en_us]");
            return true;
        }
        LanguageMenu.apply(player, preference);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                 @NotNull String label, @NotNull String[] args) {
        if (args.length != 1) {
            return List.of();
        }
        String prefix = args[0].toLowerCase();
        return List.of("auto", "en_us", "zh_cn").stream()
                .filter(option -> option.startsWith(prefix))
                .toList();
    }
}
