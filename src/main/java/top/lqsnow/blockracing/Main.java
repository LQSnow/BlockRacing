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
    public void onLoad() {
        instance = this;
        WorldResetMarker.processPendingReset(this);
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
        getPluginCommand("shout").setExecutor(new Shout());
        Language language = new Language();
        getPluginCommand("language").setExecutor(language);
        getPluginCommand("language").setTabCompleter(language);

        // Save resources
        saveIfAbsent(
                "EasyBlocks.txt",
                "MediumBlocks.txt",
                "HardBlocks.txt",
                "DyedBlocks.txt",
                "EndBlocks.txt",
                "zh_cn.json",
                "en_us.json",
                "languages/en_us/lang.yml"
        );


        // Load managers
        Config.saveDefaultConfig();
        Config.load();
        Message.saveDefaultConfig();
        Message.load();
        LanguageManager.load();
        Setting.getSettings();
        Game.initChest();
        Team.createTeam();
        Scoreboard.createScoreboard();
        new Block();
        Block.checkBlock();
        Block.refreshAvailableBlocksAndClampAmount();
        boolean recoveredGame = GameProgressStore.load();
        if (recoveredGame) {
            Game.resumeRecoveredGame();
            Scoreboard.setInGameScoreboard();
        } else {
            Scoreboard.setPreGameScoreboard();
        }
        GameProgressStore.startAutosave();
        new Game.runPer2Tick().runTaskTimer(this, 0L, 2L);

        if (!recoveredGame) {
            // Only initialize a fresh pregame world. A recovered game must keep
            // its world, player positions and border exactly as they were saved.
            Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
                World world = getPrimaryWorld();
                world.setDifficulty(Difficulty.PEACEFUL);
                Bukkit.getWorlds().forEach(loadedWorld ->
                        loadedWorld.setGameRule(GameRules.KEEP_INVENTORY, true));
                world.setTime(1000);
                world.getWorldBorder().setCenter(world.getSpawnLocation());
                world.getWorldBorder().setSize(32);
            }, 5);
        }

        // Complete
        Bukkit.getLogger().info("[BlockRacing] Load Complete!");
    }

    @Override
    public void onDisable() {
        if (Game.getCurrentGameState() == Game.GameState.INGAME) {
            GameProgressStore.saveNow();
        } else {
            GameProgressStore.clear();
        }
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
