package top.lqsnow.blockracing.utils;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import top.lqsnow.blockracing.Main;
import top.lqsnow.blockracing.managers.Message;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

public class TranslationUtil {
    private static volatile Map<String, String> translations = Map.of();
    private static volatile String loadedLanguage;
    private static final Map<String, Map<String, String>> localizedTranslations = new HashMap<>();

    public static String getValue(String block) {
        try {
            String key = Objects.requireNonNull(Material.getMaterial(block)).translationKey();
            if (block.equalsIgnoreCase("NETHER_WART")) {
                key = "block.minecraft.nether_wart";
            }
            String language = Message.MESSAGE_LANG.getString();
            if (!language.equals(loadedLanguage)) {
                reload();
            }
            return translations.getOrDefault(key, block);
        } catch (Exception e) {
            Main.getInstance().getLogger().log(Level.SEVERE, "[BlockRacing] Error getting value of blocks!", e);
        }
        return block;
    }

    public static String getValue(String block, Player player) {
        String language = top.lqsnow.blockracing.managers.LanguageManager.usesChinese(player)
                ? "zh_cn"
                : "en_us";
        try {
            String key = Objects.requireNonNull(Material.getMaterial(block)).translationKey();
            if (block.equalsIgnoreCase("NETHER_WART")) {
                key = "block.minecraft.nether_wart";
            }
            return loadTranslations(language).getOrDefault(key, block);
        } catch (Exception ex) {
            Main.getInstance().getLogger().log(Level.SEVERE, "[BlockRacing] Error getting localized block value!", ex);
            return block;
        }
    }

    public static synchronized void reload() {
        localizedTranslations.clear();
        String language = Message.MESSAGE_LANG.getString();
        File file = new File(Main.getInstance().getDataFolder(), language + ".json");
        try (Reader reader = new FileReader(file)) {
            JSONObject jsonObject = (JSONObject) new JSONParser().parse(reader);
            Map<String, String> loaded = new HashMap<>();
            for (Object key : jsonObject.keySet()) {
                Object value = jsonObject.get(key);
                if (key instanceof String && value instanceof String) {
                    loaded.put((String) key, (String) value);
                }
            }
            translations = Map.copyOf(loaded);
            loadedLanguage = language;
        } catch (Exception e) {
            Main.getInstance().getLogger().log(Level.SEVERE, "[BlockRacing] Error loading block translations!", e);
            translations = Map.of();
            loadedLanguage = language;
        }
    }

    private static synchronized Map<String, String> loadTranslations(String language) {
        Map<String, String> cached = localizedTranslations.get(language);
        if (cached != null) {
            return cached;
        }
        File file = new File(Main.getInstance().getDataFolder(), language + ".json");
        try (Reader reader = new FileReader(file)) {
            JSONObject jsonObject = (JSONObject) new JSONParser().parse(reader);
            Map<String, String> loaded = new HashMap<>();
            for (Object key : jsonObject.keySet()) {
                Object value = jsonObject.get(key);
                if (key instanceof String && value instanceof String) {
                    loaded.put((String) key, (String) value);
                }
            }
            Map<String, String> result = Map.copyOf(loaded);
            localizedTranslations.put(language, result);
            return result;
        } catch (Exception ex) {
            Main.getInstance().getLogger().log(Level.SEVERE, "Error loading " + language + " block translations!", ex);
            return Map.of();
        }
    }
}
