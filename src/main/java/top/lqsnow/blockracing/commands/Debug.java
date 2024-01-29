package top.lqsnow.blockracing.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import top.lqsnow.blockracing.managers.Message;
import top.lqsnow.blockracing.managers.Scoreboard;


public class Debug implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            Bukkit.getLogger().info("This command can only be run by a player.");
            return true;
        }
        Player player = (Player) sender;
        if (args[0].equalsIgnoreCase("reload")) {
            Message.load();
            Scoreboard.updateScoreboard();
        }
//        String joinedArgs = String.join(" ", args);
//        Test.test(joinedArgs);
        return true;
    }
}
