package top.lqsnow.blockracing.listeners;

import org.bukkit.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import top.lqsnow.blockracing.Main;
import top.lqsnow.blockracing.commands.Restart;
import top.lqsnow.blockracing.managers.BlockManager;
import top.lqsnow.blockracing.managers.GameManager;
import top.lqsnow.blockracing.managers.ScoreboardManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import top.lqsnow.blockracing.utils.ConsoleCommandHandler;
import top.lqsnow.blockracing.utils.ItemBuilder;

import java.util.*;

import static top.lqsnow.blockracing.commands.Restart.typeRestartPlayers;
import static top.lqsnow.blockracing.managers.BlockManager.*;
import static top.lqsnow.blockracing.managers.GameManager.*;
import static top.lqsnow.blockracing.managers.InventoryManager.*;
import static top.lqsnow.blockracing.managers.ScoreboardManager.*;


public class EventListener implements Listener {
    public static ArrayList<Player> locateCommandPermission = new ArrayList<Player>();
    public static boolean enableNormalBlock = false;
    public static boolean enableHardBlock = false;
    public static boolean enableDyedBlock = false;
    public static boolean enableEndBlock = false;
    public static ArrayList<String> redTeamPlayerString = new ArrayList<>();
    public static ArrayList<String> blueTeamPlayerString = new ArrayList<>();
    public static int blockAmount = 50;
    public static List<Player> prepareList = new ArrayList<>();
    private boolean redIsRolled = false;
    private boolean blueIsRolled = false;
    public static HashMap<String, Location> point1 = new HashMap<>();
    public static HashMap<String, Location> point2 = new HashMap<>();
    public static HashMap<String, Location> point3 = new HashMap<>();
    public static HashMap<Player, Boolean> randomTP = new HashMap<>();
    Random r = new Random();
    public static boolean canStart = false;
    public static ArrayList<Player> editAmountPlayer = new ArrayList<>();
    boolean flag = false;


    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent e) {
        // ???????????????
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
            ScoreboardManager.setPlayerScoreboard(e.getPlayer());
        }, 40);
        // ?????????
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
            GameManager.playerLogin(e.getPlayer());
        }, 40);
        // ????????????????????????
        inGamePlayer.add(e.getPlayer());

    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        // ????????????????????????
        inGamePlayer.remove(e.getPlayer());
        prepareList.remove(e.getPlayer());

        typeRestartPlayers.remove(e.getPlayer());
        editAmountPlayer.remove(e.getPlayer());
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), Restart::check, 5);
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
                e.getPlayer().sendMessage(ChatColor.GREEN + "???????????????????????????");
                editAmountPlayer.remove(e.getPlayer());
                e.setCancelled(true);
                return;
            }
            try {
                blockAmount = Integer.parseInt(e.getMessage());
                flag = true;
            } catch (Exception ex) {
                e.getPlayer().sendMessage(ChatColor.RED + "??????????????????????????????????????????????????????????????????quit");
                flag = false;
            } finally {
                e.setCancelled(true);
            }
            if (flag) {
                if (blockAmount < 10) {
                    Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
                        ConsoleCommandHandler.send("tellraw @a {\"color\": \"green\",\"text\": \"????????????????????????????????????10\"}");
                    }, 1L);
                    blockAmount = 10;
                } else {
                    Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
                        ConsoleCommandHandler.send("tellraw @a {\"color\": \"green\",\"text\": \"????????????????????????????????????" + blockAmount + "\"}");
                    }, 1L);

                }
                editAmountPlayer.remove(e.getPlayer());
                ScoreboardManager.update();
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent e) {
        e.getPlayer().sendMessage(ChatColor.AQUA + "????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????");
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        InventoryView inv = player.getOpenInventory();

        if (inv.getTitle().equals(TITLE_MENU) || inv.getTitle().equals(TITLE_TEAM_CHEST_SWITCH) || inv.getTitle().equals(TITLE_WAYPOINTS) || inv.getTitle().equals(TITLE_SETTINGS)) {
            e.setCancelled(true);
            // ???????????????
            if (e.getRawSlot() < 0 || e.getRawSlot() > e.getInventory().getSize()) {
                return;
            }
            ItemStack clickedItem = e.getCurrentItem();
            if (clickedItem == null) {
                return;
            }

            // settings ????????????
            // ????????????
            if (clickedItem.getItemMeta().getDisplayName().equals("?????????????????????" + ChatColor.RED + "?????????")) {
                setItem("GREEN_CONCRETE", 1,
                        "?????????????????????" + ChatColor.GREEN + "?????????",
                        ChatColor.RED + "?????????????????????????????????", 28, "settings");
                enableNormalBlock = true;
                ScoreboardManager.update();
                return;
            } else if (clickedItem.getItemMeta().getDisplayName().equals("?????????????????????" + ChatColor.GREEN + "?????????")) {
                setItem("RED_CONCRETE", 1,
                        "?????????????????????" + ChatColor.RED + "?????????",
                        ChatColor.GREEN + "?????????????????????????????????", 28, "settings");
                enableNormalBlock = false;
                ScoreboardManager.update();
                return;
            } else if (clickedItem.getItemMeta().getDisplayName().equals("?????????????????????" + ChatColor.RED + "?????????")) {
                setItem("GREEN_CONCRETE", 1,
                        "?????????????????????" + ChatColor.GREEN + "?????????",
                        ChatColor.RED + "?????????????????????????????????", 29, "settings");
                enableHardBlock = true;
                ScoreboardManager.update();
                return;
            } else if (clickedItem.getItemMeta().getDisplayName().equals("?????????????????????" + ChatColor.GREEN + "?????????")) {
                setItem("RED_CONCRETE", 1,
                        "?????????????????????" + ChatColor.RED + "?????????",
                        ChatColor.GREEN + "?????????????????????????????????", 29, "settings");
                enableHardBlock = false;
                ScoreboardManager.update();
                return;
            } else if (clickedItem.getItemMeta().getDisplayName().equals("???????????????" + ChatColor.RED + "?????????")) {
                setItem("GREEN_CONCRETE", 1,
                        "???????????????" + ChatColor.GREEN + "?????????",
                        ChatColor.RED + "???????????????????????????", 30, "settings");
                enableDyedBlock = true;
                ScoreboardManager.update();
                return;
            } else if (clickedItem.getItemMeta().getDisplayName().equals("???????????????" + ChatColor.GREEN + "?????????")) {
                setItem("RED_CONCRETE", 1,
                        "???????????????" + ChatColor.RED + "?????????",
                        ChatColor.GREEN + "???????????????????????????", 30, "settings");
                enableDyedBlock = false;
                ScoreboardManager.update();
                return;
            } else if (clickedItem.getItemMeta().getDisplayName().equals("???????????????" + ChatColor.RED + "?????????")) {
                setItem("GREEN_CONCRETE", 1,
                        "???????????????" + ChatColor.GREEN + "?????????",
                        ChatColor.RED + "???????????????????????????", 31, "settings");
                enableEndBlock = true;
                ScoreboardManager.update();
                return;
            } else if (clickedItem.getItemMeta().getDisplayName().equals("???????????????" + ChatColor.GREEN + "?????????")) {
                setItem("RED_CONCRETE", 1,
                        "???????????????" + ChatColor.RED + "?????????",
                        ChatColor.GREEN + "???????????????????????????", 31, "settings");
                enableEndBlock = false;
                ScoreboardManager.update();
                return;
            }

            // ????????????
            if (clickedItem.getItemMeta().getDisplayName().equals(ChatColor.RED + "????????????")) {
                red.addEntry(player.getName());
                ConsoleCommandHandler.send("tellraw @a {\"text\": \"\\u00a7c" + player.getName() + "??????????????????\"}");
                redTeamPlayer.add((Player) player);
                redTeamPlayerString.add(player.getName());
                if (blueTeamPlayer.contains(player)) {
                    blueTeamPlayer.remove(player);
                    blueTeamPlayerString.remove(player.getName());
                    blue.removeEntry(player.getName());
                }
                return;
            } else if (clickedItem.getItemMeta().getDisplayName().equals(ChatColor.BLUE + "????????????")) {
                blue.addEntry(player.getName());
                ConsoleCommandHandler.send("tellraw @a {\"text\": \"\\u00a79" + player.getName() + "??????????????????\"}");
                blueTeamPlayer.add((Player) player);
                blueTeamPlayerString.add(player.getName());
                if (redTeamPlayer.contains(player)) {
                    redTeamPlayer.remove(player);
                    redTeamPlayerString.remove(player.getName());
                    red.removeEntry(player.getName());
                }
                return;
            }

            // ??????
            if (clickedItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "??????")) {
                if (!prepareList.contains(player)) {
                    prepareList.add(player);
                    ConsoleCommandHandler.send("tellraw @a \"\u00a7b" + player.getName() + "????????????\"");
                    if (prepareList.size() > 1 && prepareList.size() == Bukkit.getOnlinePlayers().size()) {
                        canStart = true;
                        ConsoleCommandHandler.send("tellraw @a \"\u00a7b???????????????????????????????????????????????????\"");
                    }
                    return;
                } else {
                    ConsoleCommandHandler.send("tellraw @a \"\u00a7c????????????????????????\"");
                    return;
                }
            }

            // ????????????
            ArrayList<Player> unPreparePlayer = new ArrayList<>();
            ArrayList<String> unPrepareList = new ArrayList<>();
            if (clickedItem.getItemMeta().getDisplayName().equals(ChatColor.AQUA + "????????????")) {
                if (!canStart) {
                    // ????????????
                    if (Bukkit.getOnlinePlayers().size() == 1) {
                        player.sendMessage(ChatColor.DARK_RED + "?????????????????????????????????????????????");
                        return;
                    }
                    // ?????????????????????
                    unPreparePlayer.clear();
                    unPrepareList.clear();
                    unPreparePlayer.addAll(Bukkit.getOnlinePlayers());
                    for (Player p : prepareList) unPreparePlayer.remove(p);
                    for (Player p : unPreparePlayer) unPrepareList.add(p.getName());
                    player.sendMessage(ChatColor.DARK_RED + "?????????????????????????????????????????????");
                    player.sendMessage(ChatColor.DARK_RED + "?????????????????????" + ChatColor.YELLOW + unPrepareList);
                    return;
                } else {
                    // ??????????????????
                    if (redTeamPlayer.size() == 0 || blueTeamPlayer.size() == 0) {
                        player.sendMessage(ChatColor.DARK_RED + "?????????????????????????????????????????????");
                        return;
                    }
                    // ????????????????????????
                    BlockManager.init();
                    if (!blockAmountCheckout) {
                        player.sendMessage(ChatColor.RED + "????????????????????????????????????????????????" + maxBlockAmount);
                        return;
                    }
                    ConsoleCommandHandler.send("tellraw @a \"\u00a7b???????????????\"");
                    player.closeInventory();
                    GameManager.gameStart();
                }
            }

            // ??????????????????
            if (clickedItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "??????????????????")) {
                player.closeInventory();
                editAmountPlayer.add(player);
                player.sendMessage(ChatColor.YELLOW + "??????????????????????????????????????????quit????????????");
                return;
            }

            // ????????????
            if (clickedItem.getItemMeta().getDisplayName().equals("?????????????????????" + ChatColor.YELLOW + "??????????????????")) {
                extremeMode = true;
                ItemStack stack = new ItemStack(Objects.requireNonNull(Material.GREEN_CONCRETE));
                ItemBuilder builder = new ItemBuilder(stack);
                builder.setAmount(1);
                builder.setDisplayName("?????????????????????" + ChatColor.GREEN + "????????????");
                builder.setLore(ChatColor.GREEN + "???????????????", ChatColor.GREEN + "?????????????????????????????????????????????????????????", ChatColor.GREEN + "??????????????????????????????????????????????????????????????????", ChatColor.YELLOW + "?????????????????????", ChatColor.YELLOW + "?????????????????????????????????????????????????????????", ChatColor.YELLOW + "?????????????????????????????????????????????????????????");
                builder.toItemStack();
                settings.setItem(34, stack);
                ScoreboardManager.update();
            } else if (clickedItem.getItemMeta().getDisplayName().equals("?????????????????????" + ChatColor.GREEN + "????????????")) {
                extremeMode = false;
                ItemStack stack = new ItemStack(Objects.requireNonNull(Material.RED_CONCRETE));
                ItemBuilder builder = new ItemBuilder(stack);
                builder.setAmount(1);
                builder.setDisplayName("?????????????????????" + ChatColor.YELLOW + "??????????????????");
                builder.setLore(ChatColor.GREEN + "???????????????", ChatColor.GREEN + "?????????????????????????????????????????????????????????", ChatColor.GREEN + "??????????????????????????????????????????????????????????????????", ChatColor.YELLOW + "?????????????????????", ChatColor.YELLOW + "?????????????????????????????????????????????????????????", ChatColor.YELLOW + "?????????????????????????????????????????????????????????");
                builder.toItemStack();
                settings.setItem(34, stack);
                ScoreboardManager.update();
            }

            // menu ????????????
            if (clickedItem.getItemMeta().getDisplayName().equals(TEAM_CHEST)) {
                Bukkit.getPlayer(e.getWhoClicked().getName()).openInventory(chestSwitch);
                return;
            }
            if (clickedItem.getItemMeta().getDisplayName().equals(ROLL)) {
                if (redTeamPlayer.contains((Player) e.getWhoClicked()) & !redIsRolled) {
                    for (int i = 0; i < 4; i++) {
                        if (redCurrentBlocks.size() != 0) {
                            redCurrentBlocks.remove(0);
                            redCurrentBlocks.add(easyBlocks[r.nextInt(easyBlocks.length)]);
                        } else break;
                    }

                    ConsoleCommandHandler.send("tellraw @a \"\\u00a7c??????Roll?????????????????????\"");
                    redIsRolled = true;
                    redTeamScore = 0;
                    ScoreboardManager.update();
                } else if (redTeamPlayer.contains((Player) e.getWhoClicked()) & redIsRolled) {
                    e.getWhoClicked().sendMessage(ChatColor.DARK_RED + "???????????????????????????Roll??????");
                }
                if (blueTeamPlayer.contains((Player) e.getWhoClicked()) & !blueIsRolled) {
                    for (int i = 0; i < 4; i++) {
                        if (blueCurrentBlocks.size() != 0) {
                            blueCurrentBlocks.remove(0);
                            blueCurrentBlocks.add(easyBlocks[r.nextInt(easyBlocks.length)]);
                        } else break;
                    }
                    ConsoleCommandHandler.send("tellraw @a \"\\u00a79??????Roll?????????????????????\"");
                    blueIsRolled = true;
                    blueTeamScore = 0;
                    ScoreboardManager.update();
                } else if (blueTeamPlayer.contains((Player) e.getWhoClicked()) & blueIsRolled) {
                    e.getWhoClicked().sendMessage(ChatColor.DARK_RED + "???????????????????????????Roll??????");
                }
                return;
            }
            if (clickedItem.getItemMeta().getDisplayName().equals(WAYPOINTS)) {
                if (redTeamPlayer.contains((Player) e.getWhoClicked())) {
                    Bukkit.getPlayer(e.getWhoClicked().getName()).openInventory(redWayPoints);
                } else if (blueTeamPlayer.contains((Player) e.getWhoClicked())) {
                    Bukkit.getPlayer(e.getWhoClicked().getName()).openInventory(blueWayPoints);
                }
                return;
            }

            if (clickedItem.getItemMeta().getDisplayName().equals(LOCATE)) {
                if (locateCommandPermission.contains((Player) e.getWhoClicked())) {
                    e.getWhoClicked().sendMessage(ChatColor.DARK_RED + "??????????????????locate??????????????????????????????");
                    return;
                }
                if (redTeamPlayer.contains((Player) e.getWhoClicked())) {
                    if (redTeamScore >= locateCost) {
                        redTeamScore -= locateCost;
                        ScoreboardManager.update();
                        locateCommandPermission.add((Player) e.getWhoClicked());
                        ConsoleCommandHandler.send("tellraw @a \"\\u00a7a" + e.getWhoClicked().getName() + "????????????????????????????????????\"");
                    } else {
                        e.getWhoClicked().sendMessage(ChatColor.DARK_RED + "???????????????");
                    }
                } else if (blueTeamPlayer.contains((Player) e.getWhoClicked())) {
                    if (blueTeamScore >= locateCost) {
                        blueTeamScore -= locateCost;
                        ScoreboardManager.update();
                        locateCommandPermission.add((Player) e.getWhoClicked());
                        ConsoleCommandHandler.send("tellraw @a \"\\u00a7a" + e.getWhoClicked().getName() + "????????????????????????????????????\"");
                    } else {
                        e.getWhoClicked().sendMessage(ChatColor.DARK_RED + "???????????????");
                    }
                }
            }

            if (clickedItem.getItemMeta().getDisplayName().equals(RANDOMTP)) {
                randomTP.putIfAbsent(player, false);
                if (randomTP.get(player)) {
                    if (redTeamPlayer.contains(player)) {
                        if (redTeamScore < 2) {
                            player.sendMessage(ChatColor.DARK_RED + "???????????????");
                            return;
                        }
                    } else {
                        if (blueTeamScore < 2) {
                            player.sendMessage(ChatColor.DARK_RED + "???????????????");
                            return;
                        }
                    }
                }
                Player p = (Player) e.getWhoClicked();
                e.getWhoClicked().closeInventory();
                World playerWorld = Bukkit.getWorld("world");
                double randX = r.nextInt(20000) - 10000;
                double randZ = r.nextInt(20000) - 10000;
                Location offset = new Location(playerWorld, randX, 0, randZ).toHighestLocation();
                double Y = offset.getY() + 1;
                offset.setY(Y);
                p.teleport(offset);
                ConsoleCommandHandler.send("tellraw @a \"\\u00a7a??????" + player.getName() + "????????????????????????\"");
                p.sendMessage(ChatColor.GREEN + "???????????? " + offset.getX() + " " + offset.getY() + " " + offset.getZ());
                if (randomTP.get(player)) {
                    if (redTeamPlayer.contains(player)) redTeamScore -= 2;
                    else blueTeamScore -= 2;
                } else randomTP.put(player, true);
                ScoreboardManager.update();
            }

            // chestSwitch ??????????????????
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

            // wayPoint ???????????????
            if (clickedItem.getItemMeta().getDisplayName().equals(WAYPOINTS1)) {
                if (redTeamPlayer.contains((Player) e.getWhoClicked())) {
                    ItemStack Waypoints1 = new ItemStack(Material.FILLED_MAP);
                    ItemBuilder Waypoints1Builder = new ItemBuilder(Waypoints1);
                    Waypoints1Builder.setAmount(1);
                    Waypoints1Builder.setDisplayName(ACTIVATED_WAYPOINTS1);
                    Waypoints1Builder.setLore(ChatColor.GREEN + "?????????" + e.getWhoClicked().getWorld().getName(), ChatColor.GREEN + "?????????" + e.getWhoClicked().getLocation().getBlockX() + ", " + e.getWhoClicked().getLocation().getBlockY() + ", " + e.getWhoClicked().getLocation().getBlockZ(), ChatColor.AQUA + "???????????????????????????????????????");
                    Waypoints1Builder.toItemStack();
                    redWayPoints.setItem(0, Waypoints1);
                    point1.put("red", e.getWhoClicked().getLocation());
                } else if (blueTeamPlayer.contains((Player) e.getWhoClicked())) {
                    ItemStack Waypoints1 = new ItemStack(Material.FILLED_MAP);
                    ItemBuilder Waypoints1Builder = new ItemBuilder(Waypoints1);
                    Waypoints1Builder.setAmount(1);
                    Waypoints1Builder.setDisplayName(ACTIVATED_WAYPOINTS1);
                    Waypoints1Builder.setLore(ChatColor.GREEN + "?????????" + e.getWhoClicked().getWorld().getName(), ChatColor.GREEN + "?????????" + e.getWhoClicked().getLocation().getBlockX() + ", " + e.getWhoClicked().getLocation().getBlockY() + ", " + e.getWhoClicked().getLocation().getBlockZ(), ChatColor.AQUA + "???????????????????????????????????????");
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
                    Waypoints2Builder.setLore(ChatColor.GREEN + "?????????" + e.getWhoClicked().getWorld().getName(), ChatColor.GREEN + "?????????" + e.getWhoClicked().getLocation().getBlockX() + ", " + e.getWhoClicked().getLocation().getBlockY() + ", " + e.getWhoClicked().getLocation().getBlockZ(), ChatColor.AQUA + "???????????????????????????????????????");
                    Waypoints2Builder.toItemStack();
                    redWayPoints.setItem(1, Waypoints2);
                    point2.put("red", e.getWhoClicked().getLocation());
                } else if (blueTeamPlayer.contains((Player) e.getWhoClicked())) {
                    ItemStack Waypoints2 = new ItemStack(Material.FILLED_MAP);
                    ItemBuilder Waypoints2Builder = new ItemBuilder(Waypoints2);
                    Waypoints2Builder.setAmount(1);
                    Waypoints2Builder.setDisplayName(ACTIVATED_WAYPOINTS2);
                    Waypoints2Builder.setLore(ChatColor.GREEN + "?????????" + e.getWhoClicked().getWorld().getName(), ChatColor.GREEN + "?????????" + e.getWhoClicked().getLocation().getBlockX() + ", " + e.getWhoClicked().getLocation().getBlockY() + ", " + e.getWhoClicked().getLocation().getBlockZ(), ChatColor.AQUA + "???????????????????????????????????????");
                    Waypoints2Builder.toItemStack();
                    blueWayPoints.setItem(1, Waypoints2);
                    point2.put("blue", e.getWhoClicked().getLocation());
                }
            } else if (clickedItem.getItemMeta().getDisplayName().equals(WAYPOINTS3)) {
                if (redTeamPlayer.contains((Player) e.getWhoClicked())) {
                    ItemStack Waypoints3 = new ItemStack(Material.FILLED_MAP);
                    ItemBuilder Waypoints3Builder = new ItemBuilder(Waypoints3);
                    Waypoints3Builder.setAmount(1);
                    Waypoints3Builder.setDisplayName(ACTIVATED_WAYPOINTS3);
                    Waypoints3Builder.setLore(ChatColor.GREEN + "?????????" + e.getWhoClicked().getWorld().getName(), ChatColor.GREEN + "?????????" + e.getWhoClicked().getLocation().getBlockX() + ", " + e.getWhoClicked().getLocation().getBlockY() + ", " + e.getWhoClicked().getLocation().getBlockZ(), ChatColor.AQUA + "???????????????????????????????????????");
                    Waypoints3Builder.toItemStack();
                    redWayPoints.setItem(2, Waypoints3);
                    point3.put("red", e.getWhoClicked().getLocation());
                } else if (blueTeamPlayer.contains((Player) e.getWhoClicked())) {
                    ItemStack Waypoints3 = new ItemStack(Material.FILLED_MAP);
                    ItemBuilder Waypoints3Builder = new ItemBuilder(Waypoints3);
                    Waypoints3Builder.setAmount(1);
                    Waypoints3Builder.setDisplayName(ACTIVATED_WAYPOINTS3);
                    Waypoints3Builder.setLore(ChatColor.GREEN + "?????????" + e.getWhoClicked().getWorld().getName(), ChatColor.GREEN + "?????????" + e.getWhoClicked().getLocation().getBlockX() + ", " + e.getWhoClicked().getLocation().getBlockY() + ", " + e.getWhoClicked().getLocation().getBlockZ(), ChatColor.AQUA + "???????????????????????????????????????");
                    Waypoints3Builder.toItemStack();
                    blueWayPoints.setItem(2, Waypoints3);
                    point3.put("blue", e.getWhoClicked().getLocation());
                }
            } else if ((clickedItem.getItemMeta().getDisplayName().equals(ACTIVATED_WAYPOINTS1)) & e.isLeftClick()) {
                if (redTeamPlayer.contains((Player) e.getWhoClicked())) {
                    ((Player) e.getWhoClicked()).teleport(point1.get("red"));
                } else if (blueTeamPlayer.contains((Player) e.getWhoClicked())) {
                    ((Player) e.getWhoClicked()).teleport(point1.get("blue"));
                }
            } else if ((clickedItem.getItemMeta().getDisplayName().equals(ACTIVATED_WAYPOINTS2)) & e.isLeftClick()) {
                if (redTeamPlayer.contains((Player) e.getWhoClicked())) {
                    ((Player) e.getWhoClicked()).teleport(point2.get("red"));
                } else if (blueTeamPlayer.contains((Player) e.getWhoClicked())) {
                    ((Player) e.getWhoClicked()).teleport(point2.get("blue"));
                }
            } else if ((clickedItem.getItemMeta().getDisplayName().equals(ACTIVATED_WAYPOINTS3)) & e.isLeftClick()) {
                if (redTeamPlayer.contains((Player) e.getWhoClicked())) {
                    ((Player) e.getWhoClicked()).teleport(point3.get("red"));
                } else if (blueTeamPlayer.contains((Player) e.getWhoClicked())) {
                    ((Player) e.getWhoClicked()).teleport(point3.get("blue"));
                }
            } else if ((clickedItem.getItemMeta().getDisplayName().equals(ACTIVATED_WAYPOINTS1)) & e.isRightClick()) {
                if (redTeamPlayer.contains((Player) e.getWhoClicked())) {
                    ItemStack Waypoints1 = new ItemStack(Material.MAP);
                    ItemBuilder Waypoints1Builder = new ItemBuilder(Waypoints1);
                    Waypoints1Builder.setAmount(1);
                    Waypoints1Builder.setDisplayName(WAYPOINTS1);
                    Waypoints1Builder.setLore(ChatColor.AQUA + "???????????????????????????");
                    Waypoints1Builder.toItemStack();
                    redWayPoints.setItem(0, Waypoints1);
                    point1.remove("red");
                } else if (blueTeamPlayer.contains((Player) e.getWhoClicked())) {
                    ItemStack bWayPoints1 = new ItemStack(Material.MAP);
                    ItemBuilder bWayPoints1Builder = new ItemBuilder(bWayPoints1);
                    bWayPoints1Builder.setAmount(1);
                    bWayPoints1Builder.setDisplayName(WAYPOINTS1);
                    bWayPoints1Builder.setLore(ChatColor.AQUA + "???????????????????????????");
                    bWayPoints1Builder.toItemStack();
                    blueWayPoints.setItem(0, bWayPoints1);
                    point1.remove("blue");
                }
            } else if ((clickedItem.getItemMeta().getDisplayName().equals(ACTIVATED_WAYPOINTS2)) & e.isRightClick()) {
                if (redTeamPlayer.contains((Player) e.getWhoClicked())) {
                    ItemStack Waypoints2 = new ItemStack(Material.MAP);
                    ItemBuilder Waypoints2Builder = new ItemBuilder(Waypoints2);
                    Waypoints2Builder.setAmount(1);
                    Waypoints2Builder.setDisplayName(WAYPOINTS2);
                    Waypoints2Builder.setLore(ChatColor.AQUA + "???????????????????????????");
                    Waypoints2Builder.toItemStack();
                    redWayPoints.setItem(1, Waypoints2);
                    point2.remove("red");
                } else if (blueTeamPlayer.contains((Player) e.getWhoClicked())) {
                    ItemStack bWayPoints2 = new ItemStack(Material.MAP);
                    ItemBuilder bWayPoints2Builder = new ItemBuilder(bWayPoints2);
                    bWayPoints2Builder.setAmount(1);
                    bWayPoints2Builder.setDisplayName(WAYPOINTS2);
                    bWayPoints2Builder.setLore(ChatColor.AQUA + "???????????????????????????");
                    bWayPoints2Builder.toItemStack();
                    blueWayPoints.setItem(1, bWayPoints2);
                    point2.remove("blue");
                }
            } else if ((clickedItem.getItemMeta().getDisplayName().equals(ACTIVATED_WAYPOINTS3)) & e.isRightClick()) {
                if (redTeamPlayer.contains((Player) e.getWhoClicked())) {
                    ItemStack Waypoints3 = new ItemStack(Material.MAP);
                    ItemBuilder Waypoints3Builder = new ItemBuilder(Waypoints3);
                    Waypoints3Builder.setAmount(1);
                    Waypoints3Builder.setDisplayName(WAYPOINTS3);
                    Waypoints3Builder.setLore(ChatColor.AQUA + "???????????????????????????");
                    Waypoints3Builder.toItemStack();
                    redWayPoints.setItem(2, Waypoints3);
                    point3.remove("red");
                } else if (blueTeamPlayer.contains((Player) e.getWhoClicked())) {
                    ItemStack bWayPoints3 = new ItemStack(Material.MAP);
                    ItemBuilder bWayPoints3Builder = new ItemBuilder(bWayPoints3);
                    bWayPoints3Builder.setAmount(1);
                    bWayPoints3Builder.setDisplayName(WAYPOINTS3);
                    bWayPoints3Builder.setLore(ChatColor.AQUA + "???????????????????????????");
                    bWayPoints3Builder.toItemStack();
                    blueWayPoints.setItem(2, bWayPoints3);
                    point3.remove("blue");
                }
            }
        }
    }
}
