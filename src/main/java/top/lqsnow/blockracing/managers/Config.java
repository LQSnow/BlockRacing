package top.lqsnow.blockracing.managers;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import top.lqsnow.blockracing.Main;

public enum Config {
    MEDIUM_BLOCK("medium-block"),
    HARD_BLOCK("hard-block"),
    DYED_BLOCK("dyed-block"),
    END_BLOCK("end-block"),
    BLOCK_AMOUNT("block-amount"),
    GAME_MODE("game-mode"),
    SPEED_MODE("speed-mode"),
    CONFIG_VERSION("config-version");

    private static File file;
    private static FileConfiguration config;
    private String path;

    private Config(String path) {
        this.path = path;
    }

    public static void saveDefaultConfig() {
        File messageFile = new File(Main.getInstance().getDataFolder(), "config.yml");
        if (!messageFile.exists()) {
            Main.getInstance().saveResource("config.yml", false);
        }
    }

    public static void load() {
        if (file == null) {
            file = new File(Main.getInstance().getDataFolder(), "config.yml");
        }
        config = YamlConfiguration.loadConfiguration(file);
        try (Reader reader = new InputStreamReader(Main.getInstance().getResource("config.yml"), StandardCharsets.UTF_8)) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(reader);
            config.setDefaults(defConfig);
        } catch (IOException ioe) {
            Main.getInstance().getLogger().log(Level.SEVERE, "[BlockRacing] Error reading config.yml!", ioe);
        }
    }

    public String getString() {
        return config.getString(path);
    }

    public List<String> getStringList() {
        return config.getStringList(path);
    }

    public boolean getBoolean() {
        return config.getBoolean(path);
    }

    public int getInt() {
        return config.getInt(path);
    }

    public void setString(String value) {
        config.set(path, value);
    }

    public void setStringList(List<String> value) {
        config.set(path, value);
    }

    public void setBoolean(boolean value) {
        config.set(path, value);
    }

    public void setInt(int value) {
        config.set(path, value);
    }

    public static void saveConfig() {
        try {
            config.save(file);
            Bukkit.getLogger().info("[BlockRacing] Config.yml saved!");
        } catch (IOException e) {
            Main.getInstance().getLogger().log(Level.SEVERE, "[BlockRacing] Error saving config.yml!", e);
        }
    }
}
