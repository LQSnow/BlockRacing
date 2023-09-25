package top.lqsnow.blockracing.commands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.lqsnow.blockracing.managers.TranslationManager;

import java.util.ArrayList;
import java.util.List;

import static top.lqsnow.blockracing.managers.GameManager.*;
import static top.lqsnow.blockracing.managers.GameTick.skipBlock;
import static top.lqsnow.blockracing.managers.ScoreboardManager.*;

public class Debug implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage("§c你没有权限执行此命令");
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage("§c请输入参数");
            return true;
        }

        // 设置分数
        if (args[0].equalsIgnoreCase("setScore")) {
            if (args[1].equalsIgnoreCase("red")) {
                redTeamScore = Integer.parseInt(args[2]);
            } else if (args[1].equalsIgnoreCase("blue")) {
                blueTeamScore = Integer.parseInt(args[2]);
            }
            update();
        }

        // 查询方块列表
        if (args[0].equalsIgnoreCase("getBlock")) {
            if (args[1].equalsIgnoreCase("red")) {
                sender.sendMessage("红队当前方块：" + redCurrentBlocks.toString());
                sender.sendMessage("红队剩余方块：" + redTeamBlocks.toString());
            } else if (args[1].equalsIgnoreCase("blue")) {
                sender.sendMessage("蓝队当前方块：" + blueCurrentBlocks.toString());
                sender.sendMessage("蓝队剩余方块：" + blueTeamBlocks.toString());
            }
        }

        // 跳过方块
        try {
            if (args[0].equalsIgnoreCase("skipBlock")) {
                if (args[1].equalsIgnoreCase("red")) {
                    if (args[2].equalsIgnoreCase("all")) {
                        skipBlock("red", -1);
                        update();
                        return true;
                    }
                    skipBlock("red", Integer.parseInt(args[2]));
                } else if (args[1].equalsIgnoreCase("blue")) {
                    if (args[2].equalsIgnoreCase("all")) {
                        skipBlock("blue", -1);
                        update();
                        return true;
                    }
                    skipBlock("blue", Integer.parseInt(args[2]));
                }
                update();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 刷新记分板
        if (args[0].equalsIgnoreCase("updateScoreboard")) {
            update();
        }

        // 获取翻译
        try {
            if (args[0].equalsIgnoreCase("getTranslation")) {
                if (args[1].equalsIgnoreCase("red")) {
                    String block = redCurrentBlocks.get(Integer.parseInt(args[2]) - 1);
                    Material material = Material.getMaterial(block);
                    sender.sendMessage(block + "对应的翻译是：" + TranslationManager.getValue(block), "，Key：" + material.translationKey());
                } else if (args[1].equalsIgnoreCase("blue")) {
                    String block = blueCurrentBlocks.get(Integer.parseInt(args[2]) - 1);
                    Material material = Material.getMaterial(block);
                    sender.sendMessage(block + "对应的翻译是：" + TranslationManager.getValue(block), "，Key：" + material.translationKey());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!sender.isOp()) return null;

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // 第一个参数补全选项
            completions.add("setScore");
            completions.add("getBlock");
            completions.add("skipBlock");
            completions.add("updateScoreboard");
            completions.add("getTranslation");
        } else if (args.length == 2) {
            // 第二个参数补全选项
            if (args[0].equalsIgnoreCase("setScore") || args[0].equalsIgnoreCase("getBlock") || args[0].equalsIgnoreCase("skipBlock") || args[0].equalsIgnoreCase("getTranslation")) {
                completions.add("red");
                completions.add("blue");
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("setScore")) {
            // 第三个参数补全选项（只有在setScore命令下）
            completions.add("<分数值>");
        } else if (args.length == 3 && args[0].equalsIgnoreCase("skipBlock")) {
            // 第三个参数补全选项（只有在skipBlock命令下）
            completions.add("all");
            completions.add("<方块索引>");
        } else if (args.length == 3 && args[0].equalsIgnoreCase("getTranslation")) {
            // 第三个参数补全选项（只有在getTranslation命令下）
            completions.add("<方块索引>");
        }

        return completions;
    }
}
