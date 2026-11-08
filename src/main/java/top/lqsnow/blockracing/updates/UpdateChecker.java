package top.lqsnow.blockracing.updates;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import top.lqsnow.blockracing.Main;
import top.lqsnow.blockracing.managers.Config;
import top.lqsnow.blockracing.managers.Message;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** HTTP runs asynchronously; cached notifications and all Bukkit access run on the main thread. */
public final class UpdateChecker implements Listener {
    private final Main plugin;
    private final Map<UUID, String> notified = new HashMap<>();
    private GitHubReleaseClient.Release available;

    public UpdateChecker(Main plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (!Config.CHECK_UPDATES.getBoolean()) return;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        String currentVersion = Main.getVersion();
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            var result = new GitHubReleaseClient().check(currentVersion);
            if (result.isEmpty() || !plugin.isEnabled()) return;
            try {
                Bukkit.getScheduler().runTask(plugin, () -> publish(result.get()));
            } catch (org.bukkit.plugin.IllegalPluginAccessException ignored) {
                // The plugin may be disabled while a request is completing.
            }
        }, 20L, 6L * 60 * 60 * 20);
    }

    private void publish(GitHubReleaseClient.Release release) {
        if (available != null && available.version().equals(release.version())) return;
        available = release;
        Bukkit.getConsoleSender().sendMessage(format(Message.UPDATE_AVAILABLE.getString()));
        Bukkit.getOnlinePlayers().forEach(this::notifyPlayer);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) notifyPlayer(player);
        }, 40L);
    }

    private void notifyPlayer(Player player) {
        if (available == null || !player.hasPermission("blockracing.update-notify")
                || available.version().equals(notified.get(player.getUniqueId()))) return;
        player.sendMessage(format(Message.UPDATE_AVAILABLE.getString(player)));
        notified.put(player.getUniqueId(), available.version());
    }

    private String format(String message) {
        return message.replace("%current%", Main.getVersion())
                .replace("%latest%", available.version()).replace("%url%", GitHubReleaseClient.RELEASE_URL);
    }
}
