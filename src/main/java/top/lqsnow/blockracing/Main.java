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
    private boolean initialized;

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
        getPluginManager().registerEvents(new TeamChestNavigation(), this);

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
        MenuMemory.load();
        if (!getVersion().equals(Config.CONFIG_VERSION.getString()) || !getVersion().equals(Message.MESSAGE_VERSION.getString())
                || LanguageManager.hasOutdatedLanguageFiles()) {
            getLogger().warning("Configuration version mismatch. Stop the server, back up plugins/BlockRacing (including unfinished matches and custom files), delete that folder, and restart to regenerate it. Review old custom settings before restoring them; do not copy old config/lang files back wholesale.");
        }
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
        if (!GameProgressStore.hasRecoveryFailure()) new Game.runPer2Tick().runTaskTimer(this, 0L, 2L);

        if (!recoveredGame && !GameProgressStore.hasRecoveryFailure()) {
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
        initialized = true;
        new top.lqsnow.blockracing.updates.UpdateChecker(this).start();
        Bukkit.getLogger().info("[BlockRacing] Load Complete!");
    }

    @Override
    public void onDisable() {
        // A malformed new preset/catalogue must not delete a valid match that was never loaded.
        if (!initialized) return;
        if (Game.getCurrentGameState() == Game.GameState.INGAME) {
            GameProgressStore.saveNow();
        } else if (!GameProgressStore.hasRecoveryFailure()) {
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
