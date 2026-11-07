package top.lqsnow.blockracing.utils;

import top.lqsnow.blockracing.toolkit.material.Materials;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import top.lqsnow.blockracing.Main;
import top.lqsnow.blockracing.managers.Message;

import java.io.File;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class TranslationUtil {
    private static volatile Map<String, String> translations = Map.of();
    private static volatile String loadedLanguage;
    private static final Map<String, Map<String, String>> localizedTranslations = new HashMap<>();

    public static String getValue(String block) {
        try {
            String key = Materials.require(block).translationKey();
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
            String key = Materials.require(block).translationKey();
            return loadTranslations(language).getOrDefault(key, block);
        } catch (Exception ex) {
            Main.getInstance().getLogger().log(Level.SEVERE, "[BlockRacing] Error getting localized block value!", ex);
            return block;
        }
    }

    public static synchronized void reload() {
        localizedTranslations.clear();
        String language = Message.MESSAGE_LANG.getString();
        try {
            translations = readLanguage(language);
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
        try {
            Map<String, String> result = readLanguage(language);
            localizedTranslations.put(language, result);
            return result;
        } catch (Exception ex) {
            Main.getInstance().getLogger().log(Level.SEVERE, "Error loading " + language + " block translations!", ex);
            return Map.of();
        }
    }

    private static Map<String, String> readLanguage(String language) throws Exception {
        Map<String, String> loaded = new HashMap<>();
        try (var input = Main.getInstance().getResource(language + ".json")) {
            if (input != null) {
                try (Reader reader = new java.io.InputStreamReader(input, java.nio.charset.StandardCharsets.UTF_8)) {
                    merge(loaded, (JSONObject) new JSONParser().parse(reader));
                }
            }
        }
        File file = new File(Main.getInstance().getDataFolder(), language + ".json");
        if (file.isFile()) {
            try (Reader reader = java.nio.file.Files.newBufferedReader(file.toPath(), java.nio.charset.StandardCharsets.UTF_8)) {
                merge(loaded, (JSONObject) new JSONParser().parse(reader));
            } catch (Exception ex) {
                Main.getInstance().getLogger().log(Level.WARNING, "Invalid custom translations; using bundled " + language, ex);
            }
        }
        return Map.copyOf(loaded);
    }

    private static void merge(Map<String, String> target, JSONObject json) {
        for (Object key : json.keySet()) {
            Object value = json.get(key);
            if (key instanceof String k && value instanceof String v) target.put(k, v);
        }
    }
}
