package top.lqsnow.blockracing.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.lqsnow.blockracing.Main;
import top.lqsnow.blockracing.managers.Message;

import java.util.Arrays;
import java.util.List;

import static top.lqsnow.blockracing.managers.Game.locateCommandPermission;

public class LocateBiome implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            Bukkit.getLogger().info("This command can only be run by a player.");
            return true;
        }

        if (locateCommandPermission.contains(player.getName())) {
            player.performCommand("locate biome " + args[0]);
            locateCommandPermission.remove(player.getName());
            player.addAttachment(Main.getInstance(), "minecraft.command.locate", false);
        } else player.sendMessage(Message.NOTICE_LOCATE_NO_PERMISSION.getString());

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return Arrays.asList("plains", "sunflower_plains", "snowy_plains", "ice_spikes", "desert", "swamp", "mangrove_swamp", "forest", "flower_forest", "birch_forest", "dark_forest", "old_growth_birch_forest", "old_growth_pine_taiga", "old_growth_spruce_taiga", "taiga", "snowy_taiga", "savanna", "savanna_plateau", "windswept_hills", "windswept_gravelly_hills", "windswept_forest", "windswept_savanna", "jungle", "sparse_jungle", "bamboo_jungle", "badlands", "eroded_badlands", "wooded_badlands", "meadow", "grove", "snowy_slopes", "frozen_peaks", "jagged_peaks", "stony_peaks", "river", "frozen_river", "beach", "snowy_beach", "stony_shore", "warm_ocean", "lukewarm_ocean", "deep_lukewarm_ocean", "ocean", "deep_ocean", "cold_ocean", "deep_cold_ocean", "frozen_ocean", "deep_frozen_ocean", "mushroom_fields", "dripstone_caves", "lush_caves", "deep_dark", "nether_wastes", "warped_forest", "crimson_forest", "soul_sand_valley", "basalt_deltas", "the_end", "end_highlands", "end_midlands", "small_end_islands", "end_barrens", "cherry_grove");
    }
}
