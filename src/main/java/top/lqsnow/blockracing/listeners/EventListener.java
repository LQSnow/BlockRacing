package top.lqsnow.blockracing.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import top.lqsnow.blockracing.Main;
import top.lqsnow.blockracing.managers.GameManager;
import top.lqsnow.blockracing.managers.ScoreboardManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import top.lqsnow.blockracing.utils.ConsoleCommandHandler;
import top.lqsnow.blockracing.utils.ItemBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import static top.lqsnow.blockracing.commands.Prepare.prepareList;
import static top.lqsnow.blockracing.managers.BlockManager.blocks;
import static top.lqsnow.blockracing.managers.GameManager.*;
import static top.lqsnow.blockracing.managers.InventoryManager.*;
import static top.lqsnow.blockracing.managers.ScoreboardManager.*;


public class EventListener implements Listener {
    public static ArrayList<Player> findCommandPermission = new ArrayList<Player>();
    public static boolean enableNormalBlock = false;
    public static boolean enableHardBlock = false;
    public static boolean enableDyedBlock = false;
    public static boolean enableEndBlock = false;
    public static ArrayList<String> redTeamPlayerString = new ArrayList<>();
    public static ArrayList<String> blueTeamPlayerString = new ArrayList<>();
    private boolean redIsRolled = false;
    private boolean blueIsRolled = false;
    public static HashMap<String, Location> point1 = new HashMap<>();
    public static HashMap<String, Location> point2 = new HashMap<>();
    public static HashMap<String, Location> point3 = new HashMap<>();
    Random r = new Random();

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent e) {
        // 设置记分板
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
            ScoreboardManager.setPlayerScoreboard(e.getPlayer());
        }, 5);
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
            GameManager.playerLogin(e.getPlayer());
        }, 5);

        inGamePlayer.add(e.getPlayer());

    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        inGamePlayer.remove(e.getPlayer());
        if (prepareList.contains(e.getPlayer())) prepareList.remove(e.getPlayer());
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        InventoryView inv = player.getOpenInventory();
        if (inv.getTitle().equals(TITLE_MENU) || inv.getTitle().equals(TITLE_TEAM_CHEST_SWITCH) || inv.getTitle().equals(TITLE_WAYPOINTS) || inv.getTitle().equals(TITLE_SETTINGS)) {
            e.setCancelled(true);
        }
        if (e.getRawSlot() < 0 || e.getRawSlot() > e.getInventory().getSize()) {
            return;
        }
        ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null) {
            return;
        }

        // settings
        if (clickedItem.getItemMeta().getDisplayName().equals("中等难度方块：" + ChatColor.RED + "已禁用")) {
            setItem("GREEN_CONCRETE", 1,
                    "中等难度方块：" + ChatColor.GREEN + "已启用",
                    ChatColor.RED + "点击以禁用中等难度方块", 1, "settings");
            enableNormalBlock = true;
            ScoreboardManager.update();
        } else if (clickedItem.getItemMeta().getDisplayName().equals("中等难度方块：" + ChatColor.GREEN + "已启用")) {
            setItem("RED_CONCRETE", 1,
                    "中等难度方块：" + ChatColor.RED + "已禁用",
                    ChatColor.GREEN + "点击以启用中等难度方块", 1, "settings");
            enableNormalBlock =false;
            ScoreboardManager.update();
        } else if (clickedItem.getItemMeta().getDisplayName().equals("困难难度方块：" + ChatColor.RED + "已禁用")){
            setItem("GREEN_CONCRETE", 1,
                    "困难难度方块：" + ChatColor.GREEN + "已启用",
                    ChatColor.RED + "点击以禁用困难难度方块", 3, "settings");
            enableHardBlock = true;
            ScoreboardManager.update();
        } else if (clickedItem.getItemMeta().getDisplayName().equals("困难难度方块：" + ChatColor.GREEN + "已启用")) {
            setItem("RED_CONCRETE", 1,
                    "困难难度方块：" + ChatColor.RED + "已禁用",
                    ChatColor.GREEN + "点击以启用困难难度方块", 3, "settings");
            enableHardBlock = false;
            ScoreboardManager.update();
        } else if (clickedItem.getItemMeta().getDisplayName().equals("染色方块：" + ChatColor.RED + "已禁用")) {
            setItem("GREEN_CONCRETE", 1,
                    "染色方块：" + ChatColor.GREEN + "已启用",
                    ChatColor.RED + "点击以禁用染色方块", 5, "settings");
            enableDyedBlock = true;
            ScoreboardManager.update();
        } else if (clickedItem.getItemMeta().getDisplayName().equals("染色方块：" + ChatColor.GREEN + "已启用")) {
            setItem("RED_CONCRETE", 1,
                    "染色方块：" + ChatColor.RED + "已禁用",
                    ChatColor.GREEN + "点击以启用染色方块", 5, "settings");
            enableDyedBlock = false;
            ScoreboardManager.update();
        } else if (clickedItem.getItemMeta().getDisplayName().equals("末地方块：" + ChatColor.RED + "已禁用")) {
            setItem("GREEN_CONCRETE", 1,
                    "末地方块：" + ChatColor.GREEN + "已启用",
                    ChatColor.RED + "点击以禁用染色方块", 7, "settings");
            enableEndBlock = true;
            ScoreboardManager.update();
        } else if (clickedItem.getItemMeta().getDisplayName().equals("末地方块：" + ChatColor.GREEN + "已启用")) {
            setItem("RED_CONCRETE", 1,
                    "末地方块：" + ChatColor.RED + "已禁用",
                    ChatColor.GREEN + "点击以启用染色方块", 7, "settings");
            enableEndBlock = false;
            ScoreboardManager.update();
        }

        // menu
        if (clickedItem.getItemMeta().getDisplayName().equals(TEAM_CHEST)) {
            Bukkit.getPlayer(e.getWhoClicked().getName()).openInventory(chestSwitch);
            return;
        }
        if (clickedItem.getItemMeta().getDisplayName().equals(ROLL)) {
            if (redTeamPlayer.contains((Player) e.getWhoClicked()) & !redIsRolled) {
                for (int i = 0; i < 4; i++) {
                    if (redCurrentBlocks.size() != 0) {
                        redCurrentBlocks.remove(0);
                        redCurrentBlocks.add(blocks[r.nextInt(blocks.length)]);
                    } else break;
                }

                ConsoleCommandHandler.send("tellraw @a \"\\u00a7c红队Roll掉了所需方块！\"");
                redIsRolled = true;
                redTeamScore = 0;
                ScoreboardManager.update();
            } else if (redTeamPlayer.contains((Player) e.getWhoClicked()) & redIsRolled) {
                e.getWhoClicked().sendMessage(ChatColor.DARK_RED + "您的队伍已经使用过Roll了！");
            }
            if (blueTeamPlayer.contains((Player) e.getWhoClicked()) & !blueIsRolled) {
                for (int i = 0; i < 4; i++) {
                    if (blueCurrentBlocks.size() != 0) {
                        blueCurrentBlocks.remove(0);
                        blueCurrentBlocks.add(blocks[r.nextInt(blocks.length)]);
                    } else break;
                }
                ConsoleCommandHandler.send("tellraw @a \"\\u00a79蓝队Roll掉了所需方块！\"");
                blueIsRolled = true;
                blueTeamScore = 0;
                ScoreboardManager.update();
            } else if (blueTeamPlayer.contains((Player) e.getWhoClicked()) & blueIsRolled) {
                e.getWhoClicked().sendMessage(ChatColor.DARK_RED + "您的队伍已经使用过Roll了！");
            }
            return;
        }
        if (clickedItem.getItemMeta().getDisplayName().equals(WAYPOINTS)) {
            if (redTeamPlayer.contains((Player) e.getWhoClicked())) {
                Bukkit.getPlayer(e.getWhoClicked().getName()).openInventory(redWayPoints);
            }else if (blueTeamPlayer.contains((Player) e.getWhoClicked())) {
                Bukkit.getPlayer(e.getWhoClicked().getName()).openInventory(blueWayPoints);
            }
            return;
        }

        if (clickedItem.getItemMeta().getDisplayName().equals(FIND)) {
            if (findCommandPermission.contains((Player) e.getWhoClicked())) {
                e.getWhoClicked().sendMessage(ChatColor.DARK_RED + "您还没有使用/find命令！无法再次购买！");
                return;
            }
            if (redTeamPlayer.contains((Player) e.getWhoClicked())) {
                if (redTeamScore >= 10) {
                    redTeamScore -= 10;
                    ScoreboardManager.update();
                    findCommandPermission.add((Player) e.getWhoClicked());
                    ConsoleCommandHandler.send("tellraw @a \"\\u00a7a" + e.getWhoClicked().getName() + "购买了定位命令使用权限！\"");
                } else {
                    e.getWhoClicked().sendMessage(ChatColor.DARK_RED + "积分不足！");
                }
            } else if (blueTeamPlayer.contains((Player) e.getWhoClicked())) {
                if (blueTeamScore >= 10) {
                    blueTeamScore -= 10;
                    ScoreboardManager.update();
                    findCommandPermission.add((Player) e.getWhoClicked());
                    ConsoleCommandHandler.send("tellraw @a \"\\u00a7a" + e.getWhoClicked().getName() + "购买了定位命令使用权限！\"");
                } else {
                    e.getWhoClicked().sendMessage(ChatColor.DARK_RED + "积分不足！");
                }
            }
        }

        // 箱子选择界面
        if (clickedItem.getItemMeta().getDisplayName().equals(TEAM_CHEST1)) {
            if (redTeamPlayer.contains((Player) e.getWhoClicked())) {
                Bukkit.getPlayer(e.getWhoClicked().getName()).openInventory(redTeamChest1);
            } else if (blueTeamPlayer.contains((Player) e.getWhoClicked())) {
                Bukkit.getPlayer(e.getWhoClicked().getName()).openInventory(blueTeamChest1);
            }
            return;
        }
        if (clickedItem.getItemMeta().getDisplayName().equals(TEAM_CHEST2)) {
            if (redTeamPlayer.contains((Player) e.getWhoClicked())) {
                Bukkit.getPlayer(e.getWhoClicked().getName()).openInventory(redTeamChest2);
            } else if (blueTeamPlayer.contains((Player) e.getWhoClicked())) {
                Bukkit.getPlayer(e.getWhoClicked().getName()).openInventory(blueTeamChest2);
            }
            return;
        }
        if (clickedItem.getItemMeta().getDisplayName().equals(TEAM_CHEST3)) {
            if (redTeamPlayer.contains((Player) e.getWhoClicked())) {
                Bukkit.getPlayer(e.getWhoClicked().getName()).openInventory(redTeamChest3);
            } else if (blueTeamPlayer.contains((Player) e.getWhoClicked())) {
                Bukkit.getPlayer(e.getWhoClicked().getName()).openInventory(blueTeamChest3);
            }
            return;
        }

        // 记录点界面
        if (clickedItem.getItemMeta().getDisplayName().equals(WAYPOINTS1)) {
            if (redTeamPlayer.contains((Player) e.getWhoClicked())) {
                ItemStack Waypoints1 = new ItemStack(Material.FILLED_MAP);
                ItemBuilder Waypoints1Builder = new ItemBuilder(Waypoints1);
                Waypoints1Builder.setAmount(1);
                Waypoints1Builder.setDisplayName(ACTIVATED_WAYPOINTS1);
                Waypoints1Builder.setLore(ChatColor.GREEN + "维度：" + e.getWhoClicked().getWorld().getName(), ChatColor.GREEN + "坐标：" + e.getWhoClicked().getLocation().getBlockX() + ", " + e.getWhoClicked().getLocation().getBlockY() + ", " + e.getWhoClicked().getLocation().getBlockZ(), ChatColor.AQUA + "左键点击传送，右键点击删除");
                Waypoints1Builder.toItemStack();
                redWayPoints.setItem(0, Waypoints1);
                point1.put("red", e.getWhoClicked().getLocation());
            }else if (blueTeamPlayer.contains((Player) e.getWhoClicked())) {
                ItemStack Waypoints1 = new ItemStack(Material.FILLED_MAP);
                ItemBuilder Waypoints1Builder = new ItemBuilder(Waypoints1);
                Waypoints1Builder.setAmount(1);
                Waypoints1Builder.setDisplayName(ACTIVATED_WAYPOINTS1);
                Waypoints1Builder.setLore(ChatColor.GREEN + "维度：" + e.getWhoClicked().getWorld().getName(), ChatColor.GREEN + "坐标：" + e.getWhoClicked().getLocation().getBlockX() + ", " + e.getWhoClicked().getLocation().getBlockY() + ", " + e.getWhoClicked().getLocation().getBlockZ(), ChatColor.AQUA + "左键点击传送，右键点击删除");
                Waypoints1Builder.toItemStack();
                blueWayPoints.setItem(0, Waypoints1);
                point1.put("blue", e.getWhoClicked().getLocation());
            }
        } else if (clickedItem.getItemMeta().getDisplayName().equals(WAYPOINTS2)) {
            if (redTeamPlayer.contains((Player) e.getWhoClicked())) {
                ItemStack Waypoints2 = new ItemStack(Material.FILLED_MAP);
                ItemBuilder Waypoints2Builder = new ItemBuilder(Waypoints2);
                Waypoints2Builder.setAmount(1);
                Waypoints2Builder.setDisplayName(ACTIVATED_WAYPOINTS2);
                Waypoints2Builder.setLore(ChatColor.GREEN + "维度：" + e.getWhoClicked().getWorld().getName(), ChatColor.GREEN + "坐标：" + e.getWhoClicked().getLocation().getBlockX() + ", " + e.getWhoClicked().getLocation().getBlockY() + ", " + e.getWhoClicked().getLocation().getBlockZ(), ChatColor.AQUA + "左键点击传送，右键点击删除");
                Waypoints2Builder.toItemStack();
                redWayPoints.setItem(1, Waypoints2);
                point2.put("red", e.getWhoClicked().getLocation());
            }else if (blueTeamPlayer.contains((Player) e.getWhoClicked())) {
                ItemStack Waypoints2 = new ItemStack(Material.FILLED_MAP);
                ItemBuilder Waypoints2Builder = new ItemBuilder(Waypoints2);
                Waypoints2Builder.setAmount(1);
                Waypoints2Builder.setDisplayName(ACTIVATED_WAYPOINTS2);
                Waypoints2Builder.setLore(ChatColor.GREEN + "维度：" + e.getWhoClicked().getWorld().getName(), ChatColor.GREEN + "坐标：" + e.getWhoClicked().getLocation().getBlockX() + ", " + e.getWhoClicked().getLocation().getBlockY() + ", " + e.getWhoClicked().getLocation().getBlockZ(), ChatColor.AQUA + "左键点击传送，右键点击删除");
                Waypoints2Builder.toItemStack();
                blueWayPoints.setItem(1, Waypoints2);
                point2.put("blue", e.getWhoClicked().getLocation());
            }
        }else if (clickedItem.getItemMeta().getDisplayName().equals(WAYPOINTS3)) {
            if (redTeamPlayer.contains((Player) e.getWhoClicked())) {
                ItemStack Waypoints3 = new ItemStack(Material.FILLED_MAP);
                ItemBuilder Waypoints3Builder = new ItemBuilder(Waypoints3);
                Waypoints3Builder.setAmount(1);
                Waypoints3Builder.setDisplayName(ACTIVATED_WAYPOINTS3);
                Waypoints3Builder.setLore(ChatColor.GREEN + "维度：" + e.getWhoClicked().getWorld().getName(), ChatColor.GREEN + "坐标：" + e.getWhoClicked().getLocation().getBlockX() + ", " + e.getWhoClicked().getLocation().getBlockY() + ", " + e.getWhoClicked().getLocation().getBlockZ(), ChatColor.AQUA + "左键点击传送，右键点击删除");
                Waypoints3Builder.toItemStack();
                redWayPoints.setItem(2, Waypoints3);
                point3.put("red", e.getWhoClicked().getLocation());
            }else if (blueTeamPlayer.contains((Player) e.getWhoClicked())) {
                ItemStack Waypoints3 = new ItemStack(Material.FILLED_MAP);
                ItemBuilder Waypoints3Builder = new ItemBuilder(Waypoints3);
                Waypoints3Builder.setAmount(1);
                Waypoints3Builder.setDisplayName(ACTIVATED_WAYPOINTS3);
                Waypoints3Builder.setLore(ChatColor.GREEN + "维度：" + e.getWhoClicked().getWorld().getName(), ChatColor.GREEN + "坐标：" + e.getWhoClicked().getLocation().getBlockX() + ", " + e.getWhoClicked().getLocation().getBlockY() + ", " + e.getWhoClicked().getLocation().getBlockZ(), ChatColor.AQUA + "左键点击传送，右键点击删除");
                Waypoints3Builder.toItemStack();
                blueWayPoints.setItem(2, Waypoints3);
                point3.put("blue", e.getWhoClicked().getLocation());
            }
        }else if ((clickedItem.getItemMeta().getDisplayName().equals(ACTIVATED_WAYPOINTS1)) & e.isLeftClick()) {
            if (redTeamPlayer.contains((Player) e.getWhoClicked())) {
                ((Player) e.getWhoClicked()).teleport(point1.get("red"));
            }else if (blueTeamPlayer.contains((Player) e.getWhoClicked())) {
                ((Player) e.getWhoClicked()).teleport(point1.get("blue"));
            }
        }else if ((clickedItem.getItemMeta().getDisplayName().equals(ACTIVATED_WAYPOINTS2)) & e.isLeftClick()) {
            if (redTeamPlayer.contains((Player) e.getWhoClicked())) {
                ((Player) e.getWhoClicked()).teleport(point2.get("red"));
            } else if (blueTeamPlayer.contains((Player) e.getWhoClicked())) {
                ((Player) e.getWhoClicked()).teleport(point2.get("blue"));
            }
        }else if ((clickedItem.getItemMeta().getDisplayName().equals(ACTIVATED_WAYPOINTS3)) & e.isLeftClick()) {
            if (redTeamPlayer.contains((Player) e.getWhoClicked())) {
                ((Player) e.getWhoClicked()).teleport(point3.get("red"));
            } else if (blueTeamPlayer.contains((Player) e.getWhoClicked())) {
                ((Player) e.getWhoClicked()).teleport(point3.get("blue"));
            }
        }else if ((clickedItem.getItemMeta().getDisplayName().equals(ACTIVATED_WAYPOINTS1)) & e.isRightClick()) {
            if (redTeamPlayer.contains((Player) e.getWhoClicked())) {
                ItemStack Waypoints1 = new ItemStack(Material.MAP);
                ItemBuilder Waypoints1Builder = new ItemBuilder(Waypoints1);
                Waypoints1Builder.setAmount(1);
                Waypoints1Builder.setDisplayName(WAYPOINTS1);
                Waypoints1Builder.setLore(ChatColor.AQUA + "左键点击创建记录点");
                Waypoints1Builder.toItemStack();
                redWayPoints.setItem(0, Waypoints1);
                point1.remove("red");
            }else if (blueTeamPlayer.contains((Player) e.getWhoClicked())) {
                ItemStack bWayPoints1 = new ItemStack(Material.MAP);
                ItemBuilder bWayPoints1Builder = new ItemBuilder(bWayPoints1);
                bWayPoints1Builder.setAmount(1);
                bWayPoints1Builder.setDisplayName(WAYPOINTS1);
                bWayPoints1Builder.setLore(ChatColor.AQUA + "左键点击创建记录点");
                bWayPoints1Builder.toItemStack();
                blueWayPoints.setItem(0, bWayPoints1);
                point1.remove("blue");
            }
        }else if ((clickedItem.getItemMeta().getDisplayName().equals(ACTIVATED_WAYPOINTS2)) & e.isRightClick()) {
            if (redTeamPlayer.contains((Player) e.getWhoClicked())) {
                ItemStack Waypoints2 = new ItemStack(Material.MAP);
                ItemBuilder Waypoints2Builder = new ItemBuilder(Waypoints2);
                Waypoints2Builder.setAmount(1);
                Waypoints2Builder.setDisplayName(WAYPOINTS2);
                Waypoints2Builder.setLore(ChatColor.AQUA + "左键点击创建记录点");
                Waypoints2Builder.toItemStack();
                redWayPoints.setItem(1, Waypoints2);
                point2.remove("red");
            }else if (blueTeamPlayer.contains((Player) e.getWhoClicked())) {
                ItemStack bWayPoints2 = new ItemStack(Material.MAP);
                ItemBuilder bWayPoints2Builder = new ItemBuilder(bWayPoints2);
                bWayPoints2Builder.setAmount(1);
                bWayPoints2Builder.setDisplayName(WAYPOINTS2);
                bWayPoints2Builder.setLore(ChatColor.AQUA + "左键点击创建记录点");
                bWayPoints2Builder.toItemStack();
                blueWayPoints.setItem(1, bWayPoints2);
                point2.remove("blue");
            }
        }else if ((clickedItem.getItemMeta().getDisplayName().equals(ACTIVATED_WAYPOINTS3)) & e.isRightClick()) {
            if (redTeamPlayer.contains((Player) e.getWhoClicked())) {
                ItemStack Waypoints3 = new ItemStack(Material.MAP);
                ItemBuilder Waypoints3Builder = new ItemBuilder(Waypoints3);
                Waypoints3Builder.setAmount(1);
                Waypoints3Builder.setDisplayName(WAYPOINTS3);
                Waypoints3Builder.setLore(ChatColor.AQUA + "左键点击创建记录点");
                Waypoints3Builder.toItemStack();
                redWayPoints.setItem(2, Waypoints3);
                point3.remove("red");
            }else if (blueTeamPlayer.contains((Player) e.getWhoClicked())) {
                ItemStack bWayPoints3 = new ItemStack(Material.MAP);
                ItemBuilder bWayPoints3Builder = new ItemBuilder(bWayPoints3);
                bWayPoints3Builder.setAmount(1);
                bWayPoints3Builder.setDisplayName(WAYPOINTS3);
                bWayPoints3Builder.setLore(ChatColor.AQUA + "左键点击创建记录点");
                bWayPoints3Builder.toItemStack();
                blueWayPoints.setItem(2, bWayPoints3);
                point3.remove("blue");
            }
        }
    }
}
