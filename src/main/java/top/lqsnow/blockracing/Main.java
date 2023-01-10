package top.lqsnow.blockracing;

import com.destroystokyo.paper.profile.PlayerProfile;
import top.lqsnow.blockracing.commands.*;
import top.lqsnow.blockracing.listeners.EventListener;
import top.lqsnow.blockracing.managers.ConfigManager;
import top.lqsnow.blockracing.managers.InventoryManager;
import top.lqsnow.blockracing.managers.ScoreboardManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import top.lqsnow.blockracing.utils.ConsoleCommandHandler;

import java.io.IOException;
import java.util.Objects;

public class Main extends JavaPlugin {
    private static Main instance;
    ConfigManager configManager;

    @Override
    public void onEnable() {
        instance = this;
        // 注册事件监听器
        Bukkit.getPluginManager().registerEvents(new EventListener(), this);

        // 注册命令处理器
        Objects.requireNonNull(Bukkit.getPluginCommand("locate")).setExecutor(new Locate());
        Objects.requireNonNull(Bukkit.getPluginCommand("locate")).setTabCompleter(new Locate());
        Objects.requireNonNull(Bukkit.getPluginCommand("tp")).setExecutor(new TP());
        Objects.requireNonNull(Bukkit.getPluginCommand("restartgame")).setExecutor(new Restart());


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
        PlayerProfile redProfile = Bukkit.createProfile("InventoryHolder");
        InventoryManager.init();

        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
            ConsoleCommandHandler.send("difficulty peaceful");
            ConsoleCommandHandler.send("gamerule keepInventory true");
            ConsoleCommandHandler.send("gamerule sendCommandFeedback false");
            ConsoleCommandHandler.send("time set day");
        }, 5);
    }

    public static Main getInstance() {return instance;}

}
