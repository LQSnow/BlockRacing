package top.lqsnow.blockracing.utils;

import top.lqsnow.blockracing.Main;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

import static top.lqsnow.blockracing.listeners.EventListener.locateCommandPermission;

public class ConsoleCommandHandler {
    private static String msg;

    /**
     * 向控制台发送指令
     *
     * @param command 指令
     */
    public static void send(String command) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    /**
     * 发送locate指令并将结果返回给玩家
     *
     * @param player  玩家
     * @param command 指令后半段，"/locate "之后的部分
     */
    public static void sendLocateCommand(Player player, String command) {
        command = "execute as " + player.getName() + " at @s run locate " + command;
        StringBuilder resBuilder = new StringBuilder();
        try {
            Consumer<ComponentLike> consumer = res -> resBuilder.append(PlainTextComponentSerializer.plainText().serialize(res.asComponent())).append("\n");
            CommandSender commandSender = Bukkit.createCommandSender(consumer);
            String finalCommand = command;
            Bukkit.getScheduler().runTask(Main.getInstance(), () -> Bukkit.dispatchCommand(commandSender, finalCommand));
            Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
                if (resBuilder.toString().trim().equals("")) {
                    player.sendMessage(ChatColor.DARK_RED + "无法获取位置！您可能处于错误的维度，或者与目标距离过远！");
                } else {
                    msg = resBuilder.toString().trim();
                    if (!msg.contains("The nearest")) {
                        player.sendMessage(ChatColor.DARK_RED + "指令输入错误！");
                        return;
                    }
                    msg = msg.replace("The nearest ", "最近的");
                    msg = msg.replace(" is at ", "位于" + ChatColor.GREEN);
                    msg = msg.replace("] (", "] " + ChatColor.WHITE + "(");
                    msg = msg.replace(" blocks away", "个方块外");
                    player.sendMessage(msg);
                    locateCommandPermission.remove(player);
                }
            }, 5);
        } catch (CommandException e) {
            player.sendMessage(ChatColor.DARK_RED + "指令输入错误！");
        }
    }
}
