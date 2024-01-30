package top.lqsnow.blockracing.commands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.lqsnow.blockracing.managers.Block;
import top.lqsnow.blockracing.managers.Game;
import top.lqsnow.blockracing.managers.Message;
import top.lqsnow.blockracing.utils.TranslationUtil;

import java.util.ArrayList;
import java.util.List;

import static top.lqsnow.blockracing.managers.Block.*;
import static top.lqsnow.blockracing.managers.Game.*;
import static top.lqsnow.blockracing.managers.Scoreboard.updateScoreboard;


public class Debug implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            Bukkit.getLogger().info("This command can only be run by a player.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("&cMissing parameters");
            return true;
        }

        // Reload
        if (args[0].equalsIgnoreCase("reload")) {
            Message.load();
            if (Game.getCurrentGameState().equals(Game.GameState.PREGAME)) {
                Block.reloadBlock();
            }
            updateScoreboard();
        }

        // Skip block
        if (args[0].equalsIgnoreCase("skip")) {
            if (Game.getCurrentGameState().equals(GameState.PREGAME)) {
                player.sendMessage("&cThis command can only be used after the start of the game!");
                return true;
            }
            if (args[1].equalsIgnoreCase("red")) {
                if (args[2].equalsIgnoreCase("all") || args[2].isEmpty()) {
                    for (int i = 0; i < getCurrentBlocks("red").size(); i++) redTaskComplete(redTeamRemainingBlocks.get(0), "Debug");
                    return true;
                }
                redTaskComplete(redTeamRemainingBlocks.get(Integer.parseInt(args[2]) - 1), "Debug");
            } else if (args[1].equalsIgnoreCase("blue")) {
                if (args[2].equalsIgnoreCase("all") || args[2].isEmpty()) {
                    for (int i = 0; i < getCurrentBlocks("blue").size(); i++) blueTaskComplete(blueTeamRemainingBlocks.get(0), "Debug");
                    return true;
                }
                blueTaskComplete(blueTeamRemainingBlocks.get(Integer.parseInt(args[2]) - 1), "Debug");
            }
        }

        // Set score
        if (args[0].equalsIgnoreCase("setscore")) {
            if (args[1].equalsIgnoreCase("red")) {
                redTeamScore = Integer.parseInt(args[2]);
            } else if (args[1].equalsIgnoreCase("blue")) {
                blueTeamScore = Integer.parseInt(args[2]);
            }
            updateScoreboard();
        }

        // Query block list
        if (args[0].equalsIgnoreCase("getblock")) {
            if (args[1].equalsIgnoreCase("red")) {
                if (args[2].equalsIgnoreCase("remain")) {
                    sender.sendMessage("Red remaining blocks：" + redTeamRemainingBlocks.toString());
                } else if (args[2].equalsIgnoreCase("all")) {
                    sender.sendMessage("Red all blocks：" + redTeamBlocks.toString());
                }
            } else if (args[1].equalsIgnoreCase("blue")) {
                if (args[2].equalsIgnoreCase("remain")) {
                    sender.sendMessage("Blue remaining blocks：" + blueTeamRemainingBlocks.toString());
                } else if (args[2].equalsIgnoreCase("all")) {
                    sender.sendMessage("Blue all blocks：" + blueTeamBlocks.toString());
                }
            }
        }

        // Get translation
        if (args[0].equalsIgnoreCase("gettranslation")) {
            if (args[1].equalsIgnoreCase("red")) {
                String block = getCurrentBlocks("red").get(Integer.parseInt(args[2]) - 1);
                Material material = Material.getMaterial(block);
                sender.sendMessage(String.format("The translation of %s is: %s, key: %s"), block, TranslationUtil.getValue(block), material.getTranslationKey());
            } else if (args[1].equalsIgnoreCase("blue")) {
                String block = getCurrentBlocks("blue").get(Integer.parseInt(args[2]) - 1);
                Material material = Material.getMaterial(block);
                sender.sendMessage(String.format("The translation of %s is: %s, key: %s"), block, TranslationUtil.getValue(block), material.getTranslationKey());
            }
        }

        return true;
    }


    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.isOp()) return null;

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("reload");
            completions.add("skip");
            completions.add("setscore");
            completions.add("getblock");
            completions.add("gettranslation");
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("skip") || args[0].equalsIgnoreCase("setscore") || args[0].equalsIgnoreCase("getblock") || args[0].equalsIgnoreCase("gettranslation")) {
                completions.add("red");
                completions.add("blue");
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("skip")) {
                completions.add("all");
                completions.add("1");
                completions.add("2");
                completions.add("3");
                completions.add("4");
            } else if (args[0].equalsIgnoreCase("gettranslation")) {
                completions.add("1");
                completions.add("2");
                completions.add("3");
                completions.add("4");
            } else if (args[0].equalsIgnoreCase("getblock")) {
                completions.add("remain");
                completions.add("all");
            }
        }

        return completions;
    }
}
