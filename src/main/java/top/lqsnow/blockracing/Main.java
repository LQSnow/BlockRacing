package top.lqsnow.blockracing;

import com.destroystokyo.paper.profile.PlayerProfile;
import org.bukkit.Difficulty;
import org.bukkit.GameRule;
import org.bukkit.World;
import top.lqsnow.blockracing.commands.*;
import top.lqsnow.blockracing.listeners.BasicEventListener;
import top.lqsnow.blockracing.listeners.ListenerManager;
import top.lqsnow.blockracing.managers.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.Objects;

public class Main extends JavaPlugin {
    private static Main instance;
    ConfigManager configManager;

    @Override
    public void onEnable() {
        instance = this;
        // 注册事件监听器
        ListenerManager.enable();

        // 注册命令处理器
        Objects.requireNonNull(Bukkit.getPluginCommand("locate")).setExecutor(new Locate());
        Objects.requireNonNull(Bukkit.getPluginCommand("locate")).setTabCompleter(new Locate());
        Objects.requireNonNull(Bukkit.getPluginCommand("tp")).setExecutor(new TP());
        Objects.requireNonNull(Bukkit.getPluginCommand("restartgame")).setExecutor(new Restart());
        Objects.requireNonNull(Bukkit.getPluginCommand("debug")).setExecutor(new Debug());
        Objects.requireNonNull(Bukkit.getPluginCommand("menu")).setExecutor(new Menu());

        // 初始化记分板
        ScoreboardManager.createScoreboard();
        ScoreboardManager.setPreScoreboard();

        // 创建资源文件
        if(!getDataFolder().exists()) {
            this.saveResource("EasyBlocks.txt", false);
            this.saveResource("NormalBlocks.txt", false);
            this.saveResource("HardBlocks.txt", false);
            this.saveResource("DyedBlocks.txt", false);
            this.saveResource("EndBlocks.txt", false);
            this.saveResource("zh_cn.json", false);
        }

        try {
            configManager = new ConfigManager();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 初始化菜单
        InventoryManager.init();

        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
            World world = Bukkit.getWorld("world");
            world.setDifficulty(Difficulty.EASY);
            world.setGameRule(GameRule.KEEP_INVENTORY, true);
            world.setGameRule(GameRule.SEND_COMMAND_FEEDBACK, false);
            world.setTime(1000);
        }, 5);

        // 初始化方块管理器
        BlockManager.init();

        new GameTick().runTaskTimer(Main.getInstance(), 0L, 2L);
    }

    @Override
    public void onDisable() {
        ListenerManager.disable();
    }

    public static Main getInstance() {return instance;}

}
