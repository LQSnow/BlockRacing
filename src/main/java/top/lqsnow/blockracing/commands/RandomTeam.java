package top.lqsnow.blockracing.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import top.lqsnow.blockracing.managers.Game;
import top.lqsnow.blockracing.managers.Gui;
import top.lqsnow.blockracing.managers.Message;
import top.lqsnow.blockracing.managers.Scoreboard;
import top.lqsnow.blockracing.managers.Team;
import top.lqsnow.blockracing.menus.PreGameMenu;
import top.lqsnow.blockracing.toolkit.text.Texts;
import top.lqsnow.blockracing.utils.CommandUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static top.lqsnow.blockracing.managers.Game.getCurrentGameState;

public class RandomTeam implements CommandExecutor {
    private static final long CONFIRM_TIMEOUT_MILLIS = 30_000;
    private static final ConcurrentMap<UUID, Long> PENDING_CONFIRMATIONS = new ConcurrentHashMap<>();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            Bukkit.getLogger().info("This command can only be run by a player.");
            return true;
        }
        if (!getCurrentGameState().equals(Game.GameState.PREGAME)) {
            sender.sendMessage(Message.NOTICE_GAME_HAS_START.getString());
            return true;
        }

        if (args.length == 0) {
            requestConfirmation(player);
            return true;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("confirm")) {
            confirm(player);
            return true;
        }

        player.sendMessage(Message.NOTICE_ERROR_COMMAND.getString());
        return true;
    }

    public static void requestConfirmation(Player player) {
        if (!getCurrentGameState().equals(Game.GameState.PREGAME)) {
            player.sendMessage(Message.NOTICE_GAME_HAS_START.getString());
            return;
        }

        long now = System.currentTimeMillis();
        PENDING_CONFIRMATIONS.entrySet().removeIf(entry -> entry.getValue() < now);
        PENDING_CONFIRMATIONS.put(player.getUniqueId(), now + CONFIRM_TIMEOUT_MILLIS);
        player.closeInventory();

        Component button = Texts.component(Message.NOTICE_TEAM_SHUFFLE_CONFIRM_BUTTON.getString())
                .clickEvent(ClickEvent.runCommand("/randomteam confirm"))
                .hoverEvent(HoverEvent.showText(
                        Texts.component(Message.NOTICE_TEAM_SHUFFLE_CONFIRM_HOVER.getString())));
        player.sendMessage(Texts.component(Message.NOTICE_TEAM_SHUFFLE_CONFIRM.getString())
                .append(Component.space())
                .append(button));
    }

    private static void confirm(Player player) {
        Long expiresAt = PENDING_CONFIRMATIONS.remove(player.getUniqueId());
        if (expiresAt == null || expiresAt < System.currentTimeMillis()) {
            player.sendMessage(Message.NOTICE_TEAM_SHUFFLE_CONFIRM_EXPIRED.getString());
            return;
        }

        List<List<Player>> teams = splitPlayers(Bukkit.getOnlinePlayers(), new Random());
        Team.clearTeams();
        teams.get(0).forEach(member -> Team.joinTeam(member, Team.redTeam, false));
        teams.get(1).forEach(member -> Team.joinTeam(member, Team.blueTeam, false));

        CommandUtil.sendAll(Message.NOTICE_TEAM_SHUFFLE.getString());
        Gui.updateMenu(new PreGameMenu());
        Scoreboard.updateScoreboard();
    }

    static <T> List<List<T>> splitPlayers(Collection<? extends T> players, Random random) {
        List<T> shuffled = new ArrayList<>(players);
        Collections.shuffle(shuffled, random);
        int splitIndex = shuffled.size() / 2;
        List<T> first = new ArrayList<>(shuffled.subList(0, splitIndex));
        List<T> second = new ArrayList<>(shuffled.subList(splitIndex, shuffled.size()));

        return random.nextBoolean()
                ? List.of(first, second)
                : List.of(second, first);
    }
}
