package top.lqsnow.blockracing.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import top.lqsnow.blockracing.managers.BlockManager;
import top.lqsnow.blockracing.managers.GameManager;
import top.lqsnow.blockracing.utils.ConsoleCommandHandler;

import java.util.ArrayList;

import static top.lqsnow.blockracing.commands.Prepare.prepareList;
import static top.lqsnow.blockracing.managers.BlockManager.blockAmountCheckout;
import static top.lqsnow.blockracing.managers.BlockManager.maxBlockAmount;
import static top.lqsnow.blockracing.managers.GameManager.blueTeamPlayer;
import static top.lqsnow.blockracing.managers.GameManager.redTeamPlayer;

public class Start implements CommandExecutor {
    public static boolean canStart = false;
    ArrayList<Player> unPreparePlayer = new ArrayList<>();
    ArrayList<String> unPrepareList = new ArrayList<>();

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!canStart) {
            // 人数检测
            if (Bukkit.getOnlinePlayers().size() == 1) {
                commandSender.sendMessage(ChatColor.DARK_RED + "游戏无法开始！至少需要两个人！");
                return true;
            }
            // 存在未准备玩家
            unPreparePlayer.clear();
            unPrepareList.clear();
            unPreparePlayer.addAll(Bukkit.getOnlinePlayers());
            for (Player p : prepareList) unPreparePlayer.remove(p);
            for (Player p : unPreparePlayer) unPrepareList.add(p.getName());
            commandSender.sendMessage(ChatColor.DARK_RED + "游戏无法开始！还有玩家未准备！");
            commandSender.sendMessage(ChatColor.DARK_RED + "未准备的玩家：" + ChatColor.YELLOW + unPrepareList);
            return true;
        } else {
            // 队伍人数检测
            if (redTeamPlayer.size() == 0 || blueTeamPlayer.size() == 0) {
                commandSender.sendMessage(ChatColor.DARK_RED + "游戏无法开始！有队伍空无一人！");
                return true;
            }
            // 目标方块数量检测
            BlockManager.init();
            if (!blockAmountCheckout) {
                commandSender.sendMessage(ChatColor.RED + "目标方块数量过多！最多只能设置为" + maxBlockAmount);
                return true;
            }
            ConsoleCommandHandler.send("tellraw @a \"\u00a7b游戏开始！\"");
            GameManager.gameStart();
        }
        return true;
    }
}
