package top.lqsnow.blockracing;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameRules;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import top.lqsnow.blockracing.commands.*;
import top.lqsnow.blockracing.listeners.BasicListener;
import top.lqsnow.blockracing.managers.*;
import top.lqsnow.blockracing.toolkit.menu.MenuListener;

import static org.bukkit.Bukkit.getPluginCommand;
import static org.bukkit.Bukkit.getPluginManager;


public class Main extends JavaPlugin {
    private static Main instance;

    public static Main getInstance() {
        return instance;
    }

    public static String getVersion() {
        return instance.getPluginMeta().getVersion();
    }

    @Override
    public void onEnable() {
        instance = this;

        // Register events
        getPluginManager().registerEvents(new BasicListener(), this);
        getPluginManager().registerEvents(new MenuListener(), this);

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
        getPluginCommand("randomteam").setExecutor(new RandomTeam());

        // Save resources
        saveIfAbsent(
                "EasyBlocks.txt",
                "MediumBlocks.txt",
                "HardBlocks.txt",
                "DyedBlocks.txt",
                "EndBlocks.txt",
                "zh_cn.json",
                "en_us.json"
        );


        // Load managers
        Config.saveDefaultConfig();
        Config.load();
        Message.saveDefaultConfig();
        Message.load();
        Setting.getSettings();
        Game.initChest();
        Team.createTeam();
        Scoreboard.createScoreboard();
        new Block();
        Block.checkBlock();
        Setting.setBlockAmount(Math.max(10, Math.min(Setting.getBlockAmount(), Block.maxBlockAmount)));
        Scoreboard.setPreGameScoreboard();
        new Game.runPer2Tick().runTaskTimer(this, 0L, 2L);

        // Init world settings
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
            World world = getPrimaryWorld();
            world.setDifficulty(Difficulty.PEACEFUL);
            Bukkit.getWorlds().forEach(loadedWorld -> loadedWorld.setGameRule(GameRules.KEEP_INVENTORY, true));
            world.setTime(1000);
        }, 5);

        // Set world border
        World world = getPrimaryWorld();
        world.getWorldBorder().setCenter(world.getSpawnLocation());
        world.getWorldBorder().setSize(32);

        // Complete
        Bukkit.getLogger().info("[BlockRacing] Load Complete!");
    }

    @Override
    public void onDisable() {
        Config.saveConfig();
    }

    private void saveIfAbsent(String... paths) {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        for (String path : paths) {
            java.io.File out = new java.io.File(getDataFolder(), path);
            if (!out.exists()) {
                saveResource(path, false); // 只在缺失时复制，避免 WARNING
            }
        }
    }

    private World getPrimaryWorld() {
        return Bukkit.getWorlds().stream()
                .filter(world -> world.getEnvironment() == World.Environment.NORMAL)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No overworld is loaded"));
    }
}
