package top.lqsnow.blockracing.commands;

import top.lqsnow.blockracing.utils.ConsoleCommandHandler;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import static top.lqsnow.blockracing.listeners.InventoryEventListener.locateCommandPermission;

import java.util.Arrays;
import java.util.List;

public class Locate implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length != 2) {
            sender.sendMessage(ChatColor.DARK_RED + "指令输入错误！");
            return true;
        }

        // 检测是否拥有权限
        if (locateCommandPermission.contains((Player) sender)) {
            ConsoleCommandHandler.sendLocateCommand((Player) sender, args[0] + " " + args[1]);
        } else sender.sendMessage(ChatColor.DARK_RED + "您没有该命令的使用权限！请在/cd中购买！");
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return Arrays.asList("structure", "biome");
        }
        if (args[0].equals("structure") & args.length == 2) {
            return Arrays.asList("ancient_city", "buried_treasure", "end_city", "fortress", "mansion", "mineshaft", "mineshaft_mesa", "monument", "ocean_ruin_cold", "ocean_ruin_warm", "shipwreck", "shipwreck_beached", "stronghold", "desert_pyramid", "igloo", "jungle_pyramid", "swamp_hut", "village_desert", "village_plains", "village_savanna", "village_snowy", "village_taiga", "pillager_outpost", "nether_fossil", "bastion_remnant", "ruined_portal", "ruined_portal_desert", "ruined_portal_jungle", "ruined_portal_mountain", "ruined_portal_ocean", "ruined_portal_swamp", "ruined_portal_nether");
        }
        if (args[0].equals("biome") & args.length == 2) {
            return Arrays.asList("plains", "sunflower_plains", "snowy_plains", "ice_spikes", "desert", "swamp", "mangrove_swamp", "forest", "flower_forest", "birch_forest", "dark_forest", "old_growth_birch_forest", "old_growth_pine_taiga", "old_growth_spruce_taiga", "taiga", "snowy_taiga", "savanna", "savanna_plateau", "windswept_hills", "windswept_gravelly_hills", "windswept_forest", "windswept_savanna", "jungle", "sparse_jungle", "bamboo_jungle", "badlands", "eroded_badlands", "wooded_badlands", "meadow", "grove", "snowy_slopes", "frozen_peaks", "jagged_peaks", "stony_peaks", "river", "frozen_river", "beach", "snowy_beach", "stony_shore", "warm_ocean", "lukewarm_ocean", "deep_lukewarm_ocean", "ocean", "deep_ocean", "cold_ocean", "deep_cold_ocean", "frozen_ocean", "deep_frozen_ocean", "mushroom_fields", "dripstone_caves", "lush_caves", "deep_dark", "nether_wastes", "warped_forest", "crimson_forest", "soul_sand_valley", "basalt_deltas", "the_end", "end_highlands", "end_midlands", "small_end_islands", "end_barrens");
        }
        return null;
    }
}
