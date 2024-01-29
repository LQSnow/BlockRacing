package top.lqsnow.blockracing;

import org.bukkit.Bukkit;
import org.mineacademy.fo.plugin.SimplePlugin;
import top.lqsnow.blockracing.commands.Debug;
import top.lqsnow.blockracing.commands.Menu;
import top.lqsnow.blockracing.listeners.BasicListener;
import top.lqsnow.blockracing.managers.*;

import java.io.IOException;
import java.util.logging.Level;

import static org.bukkit.Bukkit.getPluginCommand;
import static org.bukkit.Bukkit.getPluginManager;


public class Main extends SimplePlugin {
    private static Main instance;

    @Override
    protected void onPluginStart() {
        instance = this;

        // Register events
        getPluginManager().registerEvents(new BasicListener(), this);

        // Register commands
        getPluginCommand("debug").setExecutor(new Debug());
        getPluginCommand("menu").setExecutor(new Menu());

        // Save resources
        if(!getDataFolder().exists()) {
            this.saveResource("EasyBlocks.txt", false);
            this.saveResource("MediumBlocks.txt", false);
            this.saveResource("HardBlocks.txt", false);
            this.saveResource("DyedBlocks.txt", false);
            this.saveResource("EndBlocks.txt", false);
            this.saveResource("zh_cn.json", false);
        }

        // Load managers
        Config.saveDefaultConfig();
        Config.load();
        Message.saveDefaultConfig();
        Message.load();
        Setting.getSettings();
        Team.createTeam();
        Scoreboard.createScoreboard();
        Scoreboard.setPreGameScoreboard();

        try {
            new Block();
        } catch (IOException e) {
            Main.getInstance().getLogger().log(Level.SEVERE, "[BlockRacing] Error reading the block files!", e);
        }

        Bukkit.getLogger().info("[BlockRacing] Load Complete!");
    }

    @Override
    protected void onPluginStop() {
        super.onPluginStop();
        Config.saveConfig();
    }

    public static Main getInstance() {
        return instance;
    }

}
