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

public class LocateStructure implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            Bukkit.getLogger().info("This command can only be run by a player.");
            return true;
        }

        if (locateCommandPermission.contains(player.getName())) {
            player.performCommand("locate structure " + args[0]);
            locateCommandPermission.remove(player.getName());
            player.addAttachment(Main.getInstance(), "minecraft.command.locate", false);
        } else player.sendMessage(Message.NOTICE_LOCATE_NO_PERMISSION.getString());

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return Arrays.asList("ancient_city", "buried_treasure", "end_city", "fortress", "mansion", "mineshaft", "mineshaft_mesa", "monument", "ocean_ruin_cold", "ocean_ruin_warm", "shipwreck", "shipwreck_beached", "stronghold", "desert_pyramid", "igloo", "jungle_pyramid", "swamp_hut", "village_desert", "village_plains", "village_savanna", "village_snowy", "village_taiga", "pillager_outpost", "nether_fossil", "bastion_remnant", "ruined_portal", "ruined_portal_desert", "ruined_portal_jungle", "ruined_portal_mountain", "ruined_portal_ocean", "ruined_portal_swamp", "ruined_portal_nether", "trial_chambers");
    }
}
