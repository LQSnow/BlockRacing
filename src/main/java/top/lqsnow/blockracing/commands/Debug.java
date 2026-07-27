package top.lqsnow.blockracing.commands;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.lqsnow.blockracing.managers.LanguageManager;
import top.lqsnow.blockracing.managers.Message;
import top.lqsnow.blockracing.utils.TranslationUtil;

import java.util.ArrayList;
import java.util.List;

import static top.lqsnow.blockracing.managers.Block.*;
import static top.lqsnow.blockracing.managers.Game.*;
import static top.lqsnow.blockracing.managers.Scoreboard.updateScoreboard;
import static top.lqsnow.blockracing.managers.Team.*;
import static top.lqsnow.blockracing.utils.ColorUtil.t;


public class Debug implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            Bukkit.getLogger().info("This command can only be run by a player.");
            return true;
        }

        if (!hasValidArguments(args)) {
            player.sendMessage(Message.NOTICE_ERROR_COMMAND.getString(player));
            return true;
        }

        // Reload
        if (args[0].equalsIgnoreCase("reload")) {
            Message.load();
            LanguageManager.load();
            if (getCurrentGameState().equals(GameState.PREGAME)) {
                reloadBlock();
            }
            updateScoreboard();
        }

        // Skip block
        if (args[0].equalsIgnoreCase("skip")) {
            if (getCurrentGameState().equals(GameState.PREGAME)) {
                player.sendMessage("&cThis command can only be used after the start of the game!");
                return true;
            }
            if (args[1].equalsIgnoreCase("red")) {
                if (args[2].equalsIgnoreCase("all") || args[2].isEmpty()) {
                    int amount = getCurrentBlocks("red").size();
                    for (int i = 0; i < amount; i++) redTaskComplete(redTeamRemainingBlocks.get(0), "Debug");
                    return true;
                }
                redTaskComplete(redTeamRemainingBlocks.get(Integer.parseInt(args[2]) - 1), "Debug");
            } else if (args[1].equalsIgnoreCase("blue")) {
                if (args[2].equalsIgnoreCase("all") || args[2].isEmpty()) {
                    int amount = getCurrentBlocks("blue").size();
                    for (int i = 0; i < amount; i++) blueTaskComplete(blueTeamRemainingBlocks.get(0), "Debug");
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
                sender.sendMessage(String.format("The translation of %s is: %s, key: %s", block, TranslationUtil.getValue(block), material.translationKey()));
            } else if (args[1].equalsIgnoreCase("blue")) {
                String block = getCurrentBlocks("blue").get(Integer.parseInt(args[2]) - 1);
                Material material = Material.getMaterial(block);
                sender.sendMessage(String.format("The translation of %s is: %s, key: %s", block, TranslationUtil.getValue(block), material.translationKey()));
            }
        }

        // Get team situation
        if (args[0].equalsIgnoreCase("getteam")) {
            player.sendMessage("Red team Players: " + redTeamPlayers.toString());
            player.sendMessage("Blue team Players: " + blueTeamPlayers.toString());
        }

        // Set team member
        if (args[0].equalsIgnoreCase("setteam")) {
            if (args[1].equalsIgnoreCase("red") || args[1].equalsIgnoreCase("blue")) {
                if (args[2].equalsIgnoreCase("add")) {
                    Player p = Bukkit.getPlayerExact(args[3]);
                    if (p == null) {
                        player.sendMessage(t("&cThe player does not exist!"));
                        return true;
                    }
                    boolean result;
                    if (args[1].equalsIgnoreCase("red")) {
                        result = joinTeam(p, redTeam, false);
                    } else {
                        result = joinTeam(p, blueTeam, false);
                    }
                    if (result) {
                        player.sendMessage(t(String.format("&aSuccessfully added %s to the %s team", p.getName(), args[1].toLowerCase())));
                        if (p.getGameMode().equals(GameMode.SPECTATOR)) {
                            player.sendMessage(t("&eDetected that the player is in spectator mode. If you want him to join the game, please ask him to rejoin the server!"));
                        }
                    } else {
                        player.sendMessage(t(String.format("&cThe player has already joined the %s team!", args[1].toLowerCase())));
                    }
                } else if (args[2].equalsIgnoreCase("remove")) {
                    String targetName = args[3];
                    boolean result = false;
                    if (args[1].equalsIgnoreCase("red")) {
                        result = redTeam.removeEntry(targetName);
                        redTeamPlayers.remove(targetName);
                    } else if (args[1].equalsIgnoreCase("blue")) {
                        result = blueTeam.removeEntry(targetName);
                        blueTeamPlayers.remove(targetName);
                    }
                    if (result) {
                        player.sendMessage(t("&aSuccessfully removed player from team"));
                    } else {
                        player.sendMessage(t("&cThe player does not exist!"));
                    }
                }
            }
        }

        return true;
    }

    private boolean hasValidArguments(String[] args) {
        if (args.length == 0) return false;
        String action = args[0].toLowerCase();
        if (action.equals("reload") || action.equals("getteam")) return args.length == 1;
        if (action.equals("skip") || action.equals("setscore") || action.equals("getblock")
                || action.equals("gettranslation")) {
            if (args.length != 3 || !isTeam(args[1])) return false;
            return switch (action) {
                case "skip" -> args[2].equalsIgnoreCase("all") || isCurrentBlockIndex(args[1], args[2]);
                case "setscore" -> isInteger(args[2]);
                case "getblock" -> args[2].equalsIgnoreCase("remain") || args[2].equalsIgnoreCase("all");
                case "gettranslation" -> isCurrentBlockIndex(args[1], args[2]);
                default -> false;
            };
        }
        return action.equals("setteam") && args.length == 4 && isTeam(args[1])
                && (args[2].equalsIgnoreCase("add") || args[2].equalsIgnoreCase("remove"));
    }

    private boolean isTeam(String value) {
        return value.equalsIgnoreCase("red") || value.equalsIgnoreCase("blue");
    }

    private boolean isInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private boolean isCurrentBlockIndex(String team, String value) {
        if (!isInteger(value)) return false;
        int index = Integer.parseInt(value);
        return index >= 1 && index <= getCurrentBlocks(team.toLowerCase()).size();
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
            completions.add("getteam");
            completions.add("setteam");
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("skip") || args[0].equalsIgnoreCase("setscore") || args[0].equalsIgnoreCase("getblock") || args[0].equalsIgnoreCase("gettranslation") || args[0].equalsIgnoreCase("setteam")) {
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
            } else if (args[0].equalsIgnoreCase("setteam")) {
                completions.add("add");
                completions.add("remove");
            }
        } else if (args.length == 4) {
            if (args[0].equalsIgnoreCase("setteam")) {
                if (args[2].equalsIgnoreCase("add")) {
                    return getOnlinePlayersString();
                } else if (args[2].equalsIgnoreCase("remove")) {
                    if (args[1].equalsIgnoreCase("red")) return redTeamPlayers;
                    else if (args[1].equalsIgnoreCase("blue")) return blueTeamPlayers;
                }
            }
        }

        String prefix = args[args.length - 1].toLowerCase();
        completions.removeIf(s -> !s.toLowerCase().startsWith(prefix));

        return completions;
    }
}
