package top.lqsnow.blockracing;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.mineacademy.fo.plugin.SimplePlugin;
import top.lqsnow.blockracing.commands.*;
import top.lqsnow.blockracing.listeners.BasicListener;
import top.lqsnow.blockracing.managers.*;

import static org.bukkit.Bukkit.getPluginCommand;
import static org.bukkit.Bukkit.getPluginManager;


public class Main extends SimplePlugin {
    @Getter
    private static Main instance;

    @Override
    protected void onPluginStart() {
        instance = this;

        // Register events
        getPluginManager().registerEvents(new BasicListener(), this);

        // Register commands
        getPluginCommand("debug").setExecutor(new Debug());
        getPluginCommand("debug").setTabCompleter(new Debug());
        getPluginCommand("menu").setExecutor(new Menu());
        getPluginCommand("menu").setTabCompleter(new Menu());
        getPluginCommand("waypoint").setExecutor(new WayPoint());
        getPluginCommand("locatestructure").setExecutor(new LocateStructure());
        getPluginCommand("locatestructure").setTabCompleter(new LocateStructure());
        getPluginCommand("locatebiome").setExecutor(new LocateBiome());
        getPluginCommand("locatebiome").setTabCompleter(new LocateBiome());
        getPluginCommand("restartgame").setExecutor(new Restart());
        getPluginCommand("tp").setExecutor(new Teleport());
        getPluginCommand("block").setExecutor(new GetBlock());
        getPluginCommand("block").setTabCompleter(new GetBlock());

        // Save resources
        this.saveResource("EasyBlocks.txt", false);
        this.saveResource("MediumBlocks.txt", false);
        this.saveResource("HardBlocks.txt", false);
        this.saveResource("DyedBlocks.txt", false);
        this.saveResource("EndBlocks.txt", false);
        this.saveResource("zh_cn.json", false);
        this.saveResource("en_us.json", false);

        // Load managers
        Config.saveDefaultConfig();
        Config.load();
        Message.saveDefaultConfig();
        Message.load();
        Setting.getSettings();
        Team.createTeam();
        Scoreboard.createScoreboard();
        Scoreboard.setPreGameScoreboard();
        new Block();
        new Game.runPer2Tick().runTaskTimer(this, 0L, 2L);

        // Init world settings
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
            World world = Bukkit.getWorlds().get(0);
            world.setDifficulty(Difficulty.PEACEFUL);
            // overworld
            world.setGameRule(GameRule.KEEP_INVENTORY, true);
            // nether
            Bukkit.getWorlds().get(1).setGameRule(GameRule.KEEP_INVENTORY, true);
            // end
            Bukkit.getWorlds().get(2).setGameRule(GameRule.KEEP_INVENTORY, true);
            world.setTime(1000);
        }, 5);

        // Set world border
        World world = Bukkit.getWorlds().get(0);
        world.getWorldBorder().setCenter(world.getSpawnLocation());
        world.getWorldBorder().setSize(32);

        // Complete
        Bukkit.getLogger().info("[BlockRacing] Load Complete!");
    }

    @Override
    protected void onPluginStop() {
        super.onPluginStop();
        Config.saveConfig();
    }
}
