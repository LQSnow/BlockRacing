package top.lqsnow.blockracing.managers;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import top.lqsnow.blockracing.Main;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.logging.Level;

import static top.lqsnow.blockracing.utils.ColorUtil.t;

public final class LanguageManager {
    public enum Preference {
        AUTO, ZH_CN, EN_US
    }

    private static YamlConfiguration chinese;
    private static YamlConfiguration english;
    private static YamlConfiguration preferences;
    private static File preferencesFile;

    private LanguageManager() {
    }

    public static void load() {
        chinese = loadLanguage(new File(Main.getInstance().getDataFolder(), "lang.yml"), "lang.yml");
        english = loadLanguage(
                new File(Main.getInstance().getDataFolder(), "languages/en_us/lang.yml"),
                "languages/en_us/lang.yml"
        );
        preferencesFile = new File(Main.getInstance().getDataFolder(), "language-preferences.yml");
        preferences = YamlConfiguration.loadConfiguration(preferencesFile);
    }

    public static synchronized boolean registerFirstJoin(Player player) {
        String path = player.getUniqueId().toString();
        if (preferences.contains(path)) {
            return false;
        }
        preferences.set(path, Preference.AUTO.name());
        savePreferences();
        return true;
    }

    public static synchronized void setPreference(Player player, Preference preference) {
        preferences.set(player.getUniqueId().toString(), preference.name());
        savePreferences();
    }

    public static synchronized Preference getPreference(Player player) {
        String value = preferences.getString(player.getUniqueId().toString(), Preference.AUTO.name());
        try {
            return Preference.valueOf(value);
        } catch (IllegalArgumentException ex) {
            return Preference.AUTO;
        }
    }

    public static String getString(Message message, Player player) {
        String value = configuration(player).getString(message.getPath());
        if (value == null) {
            value = chinese.getString(message.getPath(), message.getPath());
        }
        return t(value);
    }

    public static List<String> getStringList(Message message, Player player) {
        List<String> values = configuration(player).getStringList(message.getPath());
        if (values.isEmpty()) {
            values = chinese.getStringList(message.getPath());
        }
        return values.stream().map(value -> t(value)).toList();
    }

    public static boolean usesChinese(Player player) {
        return usesChinese(getPreference(player), player.locale());
    }

    static boolean usesChinese(Preference preference, Locale clientLocale) {
        return switch (preference) {
            case ZH_CN -> true;
            case EN_US -> false;
            case AUTO -> clientLocale.getLanguage().equalsIgnoreCase(Locale.CHINESE.getLanguage());
        };
    }

    private static YamlConfiguration configuration(Player player) {
        return usesChinese(player) ? chinese : english;
    }

    private static YamlConfiguration loadLanguage(File file, String resourcePath) {
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
        try (InputStreamReader reader = new InputStreamReader(
                Main.getInstance().getResource(resourcePath),
                StandardCharsets.UTF_8
        )) {
            configuration.setDefaults(YamlConfiguration.loadConfiguration(reader));
        } catch (Exception ex) {
            Main.getInstance().getLogger().log(Level.SEVERE, "Unable to load language " + resourcePath, ex);
        }
        return configuration;
    }

    private static void savePreferences() {
        try {
            preferences.save(preferencesFile);
        } catch (IOException ex) {
            Main.getInstance().getLogger().log(Level.SEVERE, "Unable to save language preferences", ex);
        }
    }
}
