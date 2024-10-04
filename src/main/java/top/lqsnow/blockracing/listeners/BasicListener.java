package top.lqsnow.blockracing.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import top.lqsnow.blockracing.Main;
import top.lqsnow.blockracing.managers.*;
import top.lqsnow.blockracing.menus.PreGameMenu;

import java.util.ArrayList;
import java.util.List;

import static top.lqsnow.blockracing.managers.Gui.updateMenu;
import static top.lqsnow.blockracing.managers.Scoreboard.updateScoreboard;
import static top.lqsnow.blockracing.managers.Team.isPlayerInBlueTeam;
import static top.lqsnow.blockracing.managers.Team.isPlayerInRedTeam;
import static top.lqsnow.blockracing.utils.ColorUtil.t;
import static top.lqsnow.blockracing.managers.Block.*;
import static top.lqsnow.blockracing.utils.CommandUtil.sendAll;

public class BasicListener implements Listener {
    public static List<String> editAmountPlayer = new ArrayList<>();

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        Game.playerLogin(event.getPlayer());
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        Game.playerQuit(event.getPlayer());
    }

    @EventHandler
    private void onPlayerSwapHand(PlayerSwapHandItemsEvent event) {
        // Open menu
        if (event.getPlayer().isSneaking()) {
            event.setCancelled(true);
            Gui.openMenu(event.getPlayer());
        }
    }

    @EventHandler
    private void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        // Change block amount
        if (editAmountPlayer.contains(player.getName())) {
            if (!Game.getCurrentGameState().equals(Game.GameState.PREGAME)) return;
            if (event.getMessage().equals("quit")) {
                player.sendMessage(Message.NOTICE_SET_BLOCKS_QUIT.getString());
                editAmountPlayer.remove(player.getName());
                event.setCancelled(true);
                return;
            }
            boolean flag;
            int blockAmount = 0;
            try {
                blockAmount = Integer.parseInt(event.getMessage());
                flag = true;
            } catch (Exception ex) {
                player.sendMessage(Message.NOTICE_SET_BLOCKS_ERROR.getString());
                flag = false;
            } finally {
                event.setCancelled(true);
            }
            if (flag) {
                setBlockAmount(blockAmount, true);
                editAmountPlayer.remove(player.getName());
            }
        }

        // Change chat format
        if (isPlayerInRedTeam(player)) {
            event.setFormat(t(Message.TEAM_RED_CHAT.getString()));
        } else if (isPlayerInBlueTeam(player)) {
            event.setFormat(t(Message.TEAM_BLUE_CHAT.getString()));
        }
    }

    @EventHandler
    private void onPlayerRespawn(PlayerRespawnEvent event) {
        event.getPlayer().sendMessage(Message.NOTICE_SPAWN_PROTECT.getString());
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
            event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, -1, 0, false, false));
            if (Game.getCurrentGameState().equals(Game.GameState.INGAME) && Setting.isSpeedMode()) event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.HASTE, -1, 4, false, false));
        }, 10L);
    }

    public static void setBlockAmount(int blockAmount, Boolean sendMessage) {
        Block.addUpBlocks();
        if (blockAmount < 10) {
            if (sendMessage) sendAll(Message.NOTICE_SET_BLOCKS_SUCCESS.getString() + 10);
            blockAmount = 10;
        } else if (blockAmount > maxBlockAmount) {
            if (sendMessage) sendAll(Message.NOTICE_SET_BLOCKS_SUCCESS.getString() + maxBlockAmount);
            blockAmount = maxBlockAmount;
        } else {
            if (sendMessage) sendAll(Message.NOTICE_SET_BLOCKS_SUCCESS.getString() + blockAmount);
        }
        Setting.setBlockAmount(blockAmount);
        updateMenu(new PreGameMenu());
        updateScoreboard();
    }
}
