package top.lqsnow.blockracing.listeners;

import org.bukkit.*;
import org.bukkit.event.player.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import top.lqsnow.blockracing.Main;
import top.lqsnow.blockracing.commands.Restart;
import top.lqsnow.blockracing.managers.BlockManager;
import top.lqsnow.blockracing.managers.GameManager;
import top.lqsnow.blockracing.managers.ScoreboardManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.*;

import static top.lqsnow.blockracing.commands.Restart.typeRestartPlayers;
import static top.lqsnow.blockracing.managers.BlockManager.*;
import static top.lqsnow.blockracing.managers.GameManager.*;
import static top.lqsnow.blockracing.managers.InventoryManager.*;
import static top.lqsnow.blockracing.utils.ConsoleCommandHandler.sendAll;


public class BasicEventListener implements IListener {

    public static int blockAmount = 50;
    public static List<Player> prepareList = new ArrayList<>();
    public static ArrayList<Player> editAmountPlayer = new ArrayList<>();
    boolean flag = false;


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        ScoreboardManager.setPlayerScoreboard(e.getPlayer());
        GameManager.playerLogin(e.getPlayer());
        e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, -1, 0, false, false));
        if (speedMode) e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, -1, 4, false, false));
        inGamePlayer.add(e.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        inGamePlayer.remove(e.getPlayer());
        prepareList.remove(e.getPlayer());

        typeRestartPlayers.remove(e.getPlayer());
        editAmountPlayer.remove(e.getPlayer());
        Restart.check();
    }

    @EventHandler
    public void onPlayerSwap(PlayerSwapHandItemsEvent e) {
        if (e.getPlayer().isSneaking()) {
            if (!gameStart) e.getPlayer().openInventory(settings);
            else e.getPlayer().openInventory(menu);
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onMessageSend(AsyncPlayerChatEvent e) {
        if (gameStart) return;
        if (editAmountPlayer.contains(e.getPlayer())) {
            if (e.getMessage().equals("quit")) {
                e.getPlayer().sendMessage(ChatColor.GREEN + "成功退出输入模式！");
                editAmountPlayer.remove(e.getPlayer());
                e.setCancelled(true);
                return;
            }
            try {
                blockAmount = Integer.parseInt(e.getMessage());
                flag = true;
            } catch (Exception ex) {
                e.getPlayer().sendMessage(ChatColor.RED + "请输入正确数字！如果您想退出输入模式，请发送quit");
                flag = false;
            } finally {
                e.setCancelled(true);
            }
            if (flag) {
                setBlockAmount(true);
                editAmountPlayer.remove(e.getPlayer());
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent e) {
        e.getPlayer().sendMessage(ChatColor.AQUA + "提示：如果出生点附近无法放置或破坏方块，是因为服务器带有出生点保护，离开出生点附近即可！");
        // 隔段时间才能给上效果，如果不延时效果给不上，不知道为啥
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
            e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, -1, 0, false, false));
            if (gameStart && speedMode) e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, -1, 4, false, false));
        }, 10L);
    }

    public static void setBlockAmount(Boolean sendMessage) {
        BlockManager.init();
        if (blockAmount < 10) {
            sendAll("&a需要收集的方块数量更改为10");
            blockAmount = 10;
        } else if (blockAmount > maxBlockAmount) {
            sendAll("&a需要收集的方块数量更改为" + maxBlockAmount);
            blockAmount = maxBlockAmount;
        } else {
            if (sendMessage) sendAll("&a需要收集的方块数量更改为" + blockAmount);
        }
        ScoreboardManager.update();
    }

    @Override
    public void register() {

    }

    @Override
    public void unregister() {
        PlayerJoinEvent.getHandlerList().unregister(Main.getInstance());
        PlayerQuitEvent.getHandlerList().unregister(Main.getInstance());
        PlayerSwapHandItemsEvent.getHandlerList().unregister(Main.getInstance());
        AsyncPlayerChatEvent.getHandlerList().unregister(Main.getInstance());
        PlayerRespawnEvent.getHandlerList().unregister(Main.getInstance());
    }
}
