package top.lqsnow.blockracing.listeners;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import top.lqsnow.blockracing.Main;
import top.lqsnow.blockracing.managers.BlockManager;
import top.lqsnow.blockracing.managers.GameManager;
import top.lqsnow.blockracing.managers.ScoreboardManager;
import top.lqsnow.blockracing.utils.ConsoleCommandHandler;
import top.lqsnow.blockracing.utils.ItemBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

import static top.lqsnow.blockracing.listeners.BasicEventListener.editAmountPlayer;
import static top.lqsnow.blockracing.listeners.BasicEventListener.prepareList;
import static top.lqsnow.blockracing.managers.BlockManager.*;
import static top.lqsnow.blockracing.managers.BlockManager.easyBlocks;
import static top.lqsnow.blockracing.managers.GameManager.*;
import static top.lqsnow.blockracing.managers.InventoryManager.*;
import static top.lqsnow.blockracing.managers.InventoryManager.blueWayPoints;
import static top.lqsnow.blockracing.managers.ScoreboardManager.*;
import static top.lqsnow.blockracing.managers.ScoreboardManager.blueTeamScore;
import static top.lqsnow.blockracing.utils.ConsoleCommandHandler.sendAll;

public class InventoryEventListener implements IListener{

    private boolean redIsRolled = false;
    private boolean blueIsRolled = false;
    public static HashMap<String, Location> point1 = new HashMap<>();
    public static HashMap<String, Location> point2 = new HashMap<>();
    public static HashMap<String, Location> point3 = new HashMap<>();
    public static HashMap<Player, Boolean> randomTP = new HashMap<>();
    Random r = new Random();
    public static boolean canStart = false;
    public static boolean enableNormalBlock = false;
    public static boolean enableHardBlock = false;
    public static boolean enableDyedBlock = false;
    public static boolean enableEndBlock = false;
    public static ArrayList<Player> locateCommandPermission = new ArrayList<Player>();
    public static ArrayList<String> redTeamPlayerString = new ArrayList<>();
    public static ArrayList<String> blueTeamPlayerString = new ArrayList<>();

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        InventoryView inv = player.getOpenInventory();

        if (inv.getTitle().equals(TITLE_MENU) || inv.getTitle().equals(TITLE_TEAM_CHEST_SWITCH) || inv.getTitle().equals(TITLE_WAYPOINTS) || inv.getTitle().equals(TITLE_SETTINGS)) {
            e.setCancelled(true);
            // 防误触处理
            if (e.getRawSlot() < 0 || e.getRawSlot() > e.getInventory().getSize()) {
                return;
            }
            ItemStack clickedItem = e.getCurrentItem();
            if (clickedItem == null) {
                return;
            }

            // settings 设置界面
            // 难度选择
            if (clickedItem.getItemMeta().getDisplayName().equals("中等难度方块：" + ChatColor.RED + "已禁用")) {
                setItem("GREEN_CONCRETE", 1,
                        "中等难度方块：" + ChatColor.GREEN + "已启用",
                        ChatColor.RED + "点击以禁用中等难度方块", 28, "settings");
                enableNormalBlock = true;
                ScoreboardManager.update();
                return;
            } else if (clickedItem.getItemMeta().getDisplayName().equals("中等难度方块：" + ChatColor.GREEN + "已启用")) {
                setItem("RED_CONCRETE", 1,
                        "中等难度方块：" + ChatColor.RED + "已禁用",
                        ChatColor.GREEN + "点击以启用中等难度方块", 28, "settings");
                enableNormalBlock = false;
                ScoreboardManager.update();
                return;
            } else if (clickedItem.getItemMeta().getDisplayName().equals("困难难度方块：" + ChatColor.RED + "已禁用")) {
                setItem("GREEN_CONCRETE", 1,
                        "困难难度方块：" + ChatColor.GREEN + "已启用",
                        ChatColor.RED + "点击以禁用困难难度方块", 29, "settings");
                enableHardBlock = true;
                ScoreboardManager.update();
                return;
            } else if (clickedItem.getItemMeta().getDisplayName().equals("困难难度方块：" + ChatColor.GREEN + "已启用")) {
                setItem("RED_CONCRETE", 1,
                        "困难难度方块：" + ChatColor.RED + "已禁用",
                        ChatColor.GREEN + "点击以启用困难难度方块", 29, "settings");
                enableHardBlock = false;
                ScoreboardManager.update();
                return;
            } else if (clickedItem.getItemMeta().getDisplayName().equals("染色方块：" + ChatColor.RED + "已禁用")) {
                setItem("GREEN_CONCRETE", 1,
                        "染色方块：" + ChatColor.GREEN + "已启用",
                        ChatColor.RED + "点击以禁用染色方块", 30, "settings");
                enableDyedBlock = true;
                ScoreboardManager.update();
                return;
            } else if (clickedItem.getItemMeta().getDisplayName().equals("染色方块：" + ChatColor.GREEN + "已启用")) {
                setItem("RED_CONCRETE", 1,
                        "染色方块：" + ChatColor.RED + "已禁用",
                        ChatColor.GREEN + "点击以启用染色方块", 30, "settings");
                enableDyedBlock = false;
                ScoreboardManager.update();
                return;
            } else if (clickedItem.getItemMeta().getDisplayName().equals("末地方块：" + ChatColor.RED + "已禁用")) {
                setItem("GREEN_CONCRETE", 1,
                        "末地方块：" + ChatColor.GREEN + "已启用",
                        ChatColor.RED + "点击以禁用末地方块", 31, "settings");
                enableEndBlock = true;
                ScoreboardManager.update();
                return;
            } else if (clickedItem.getItemMeta().getDisplayName().equals("末地方块：" + ChatColor.GREEN + "已启用")) {
                setItem("RED_CONCRETE", 1,
                        "末地方块：" + ChatColor.RED + "已禁用",
                        ChatColor.GREEN + "点击以启用末地方块", 31, "settings");
                enableEndBlock = false;
                ScoreboardManager.update();
                return;
            }

            // 队伍选择
            if (clickedItem.getItemMeta().getDisplayName().equals(ChatColor.RED + "加入红队")) {
                red.addEntry(player.getName());
                sendAll("&c" +  player.getName() + "加入了红队！");
                redTeamPlayer.add(player);
                redTeamPlayerString.add(player.getName());
                if (blueTeamPlayer.contains(player)) {
                    blueTeamPlayer.remove(player);
                    blueTeamPlayerString.remove(player.getName());
                    blue.removeEntry(player.getName());
                }
                return;
            } else if (clickedItem.getItemMeta().getDisplayName().equals(ChatColor.BLUE + "加入蓝队")) {
                blue.addEntry(player.getName());
                sendAll("&9" +  player.getName() + "加入了蓝队！");
                blueTeamPlayer.add(player);
                blueTeamPlayerString.add(player.getName());
                if (redTeamPlayer.contains(player)) {
                    redTeamPlayer.remove(player);
                    redTeamPlayerString.remove(player.getName());
                    red.removeEntry(player.getName());
                }
                return;
            }

            // 准备
            if (clickedItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "准备")) {
                if (!prepareList.contains(player)) {
                    prepareList.add(player);
                    sendAll("&b" +  player.getName() + "已准备！");
                    if (prepareList.size() > 1 && prepareList.size() == Bukkit.getOnlinePlayers().size()) {
                        canStart = true;
                        sendAll("&b所有人都准备好了，可以开始游戏了！");
                    }
                    return;
                } else {
                    player.sendMessage("&c您已经准备过了！");
                    return;
                }
            }

            // 开始游戏
            ArrayList<Player> unPreparePlayer = new ArrayList<>();
            ArrayList<String> unPrepareList = new ArrayList<>();
            if (clickedItem.getItemMeta().getDisplayName().equals(ChatColor.AQUA + "开始游戏")) {
                if (!canStart) {
                    // 人数检测
                    if (Bukkit.getOnlinePlayers().size() == 1) {
                        player.sendMessage(ChatColor.DARK_RED + "游戏无法开始！至少需要两个人！");
                        return;
                    }
                    // 存在未准备玩家
                    unPreparePlayer.clear();
                    unPrepareList.clear();
                    unPreparePlayer.addAll(Bukkit.getOnlinePlayers());
                    for (Player p : prepareList) unPreparePlayer.remove(p);
                    for (Player p : unPreparePlayer) unPrepareList.add(p.getName());
                    player.sendMessage(ChatColor.DARK_RED + "游戏无法开始！还有玩家未准备！");
                    player.sendMessage(ChatColor.DARK_RED + "未准备的玩家：" + ChatColor.YELLOW + unPrepareList);
                    return;
                } else {
                    // 队伍人数检测
                    if (redTeamPlayer.size() == 0 || blueTeamPlayer.size() == 0) {
                        player.sendMessage(ChatColor.DARK_RED + "游戏无法开始！有队伍空无一人！");
                        return;
                    }
                    // 目标方块数量检测
                    BlockManager.init();
                    if (!blockAmountCheckout) {
                        player.sendMessage(ChatColor.RED + "目标方块数量过多！最多只能设置为" + maxBlockAmount);
                        return;
                    }
                    sendAll("&b游戏开始！");
                    player.closeInventory();
                    GameManager.gameStart();
                }
            }

            // 更改方块数量
            if (clickedItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "目标方块数量")) {
                player.closeInventory();
                editAmountPlayer.add(player);
                player.sendMessage(ChatColor.YELLOW + "请直接发送目标方块数量，发送quit取消输入");
                return;
            }

            // 切换模式
            if (clickedItem.getItemMeta().getDisplayName().equals("点击此处切换至" + ChatColor.YELLOW + "极限竞速模式")) {
                extremeMode = true;
                ItemStack stack = new ItemStack(Objects.requireNonNull(Material.GREEN_CONCRETE));
                ItemBuilder builder = new ItemBuilder(stack);
                builder.setAmount(1);
                builder.setDisplayName("点击此处切换至" + ChatColor.GREEN + "普通模式");
                builder.setLore(ChatColor.WHITE + "当前模式：" + ChatColor.YELLOW + "极限竞速模式", ChatColor.GREEN + "普通模式：", ChatColor.GREEN + "两个队伍需要收集的方块及顺序完全随机，", ChatColor.GREEN + "收集方块会将一组该方块给到对方的队伍箱子里！", ChatColor.YELLOW + "极限竞速模式：", ChatColor.YELLOW + "两个队伍需要收集的方块及顺序完全相同，", ChatColor.YELLOW + "收集方块将不会给予一组方块到对方队伍！");
                builder.toItemStack();
                settings.setItem(34, stack);
                ScoreboardManager.update();
            } else if (clickedItem.getItemMeta().getDisplayName().equals("点击此处切换至" + ChatColor.GREEN + "普通模式")) {
                extremeMode = false;
                ItemStack stack = new ItemStack(Objects.requireNonNull(Material.RED_CONCRETE));
                ItemBuilder builder = new ItemBuilder(stack);
                builder.setAmount(1);
                builder.setDisplayName("点击此处切换至" + ChatColor.YELLOW + "极限竞速模式");
                builder.setLore(ChatColor.WHITE + "当前模式：" + ChatColor.GREEN + "普通模式", ChatColor.GREEN + "普通模式：", ChatColor.GREEN + "两个队伍需要收集的方块及顺序完全随机，", ChatColor.GREEN + "收集方块会将一组该方块给到对方的队伍箱子里！", ChatColor.YELLOW + "极限竞速模式：", ChatColor.YELLOW + "两个队伍需要收集的方块及顺序完全相同，", ChatColor.YELLOW + "收集方块将不会给予一组方块到对方队伍！");
                builder.toItemStack();
                settings.setItem(34, stack);
                ScoreboardManager.update();
            }

            // menu 菜单界面
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

                    sendAll("&c红队Roll掉了所需方块！");
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
                            blueCurrentBlocks.add(easyBlocks[r.nextInt(easyBlocks.length)]);
                        } else break;
                    }
                    sendAll("&9蓝队Roll掉了所需方块！");
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
                } else if (blueTeamPlayer.contains((Player) e.getWhoClicked())) {
                    Bukkit.getPlayer(e.getWhoClicked().getName()).openInventory(blueWayPoints);
                }
                return;
            }

            if (clickedItem.getItemMeta().getDisplayName().equals(LOCATE)) {
                if (locateCommandPermission.contains((Player) e.getWhoClicked())) {
                    e.getWhoClicked().sendMessage(ChatColor.DARK_RED + "您还没有使用locate命令！无法再次购买！");
                    return;
                }
                if (redTeamPlayer.contains((Player) e.getWhoClicked())) {
                    if (redTeamScore >= locateCost) {
                        redTeamScore -= locateCost;
                        ScoreboardManager.update();
                        locateCommandPermission.add((Player) e.getWhoClicked());
                        sendAll("&a" + e.getWhoClicked().getName() + "购买了定位命令使用权限！");
                    } else {
                        e.getWhoClicked().sendMessage(ChatColor.DARK_RED + "积分不足！");
                    }
                } else if (blueTeamPlayer.contains((Player) e.getWhoClicked())) {
                    if (blueTeamScore >= locateCost) {
                        blueTeamScore -= locateCost;
                        ScoreboardManager.update();
                        locateCommandPermission.add((Player) e.getWhoClicked());
                        sendAll("&a" + e.getWhoClicked().getName() + "购买了定位命令使用权限！");
                    } else {
                        e.getWhoClicked().sendMessage(ChatColor.DARK_RED + "积分不足！");
                    }
                }
            }

            if (clickedItem.getItemMeta().getDisplayName().equals(RANDOMTP)) {
                randomTP.putIfAbsent(player, false);
                if (randomTP.get(player)) {
                    if (redTeamPlayer.contains(player)) {
                        if (redTeamScore < 2) {
                            player.sendMessage(ChatColor.DARK_RED + "积分不足！");
                            return;
                        }
                    } else {
                        if (blueTeamScore < 2) {
                            player.sendMessage(ChatColor.DARK_RED + "积分不足！");
                            return;
                        }
                    }
                }
                Player p = (Player) e.getWhoClicked();
                e.getWhoClicked().closeInventory();
                randomTeleport(p, false);
                sendAll("&a玩家" +  p.getName() + "使用了随机传送！");
                if (randomTP.get(player)) {
                    if (redTeamPlayer.contains(player)) redTeamScore -= 2;
                    else blueTeamScore -= 2;
                } else randomTP.put(player, true);
                ScoreboardManager.update();
            }

            // chestSwitch 箱子选择界面
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

            // wayPoint 记录点界面
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
                } else if (blueTeamPlayer.contains((Player) e.getWhoClicked())) {
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
                } else if (blueTeamPlayer.contains((Player) e.getWhoClicked())) {
                    ItemStack Waypoints2 = new ItemStack(Material.FILLED_MAP);
                    ItemBuilder Waypoints2Builder = new ItemBuilder(Waypoints2);
                    Waypoints2Builder.setAmount(1);
                    Waypoints2Builder.setDisplayName(ACTIVATED_WAYPOINTS2);
                    Waypoints2Builder.setLore(ChatColor.GREEN + "维度：" + e.getWhoClicked().getWorld().getName(), ChatColor.GREEN + "坐标：" + e.getWhoClicked().getLocation().getBlockX() + ", " + e.getWhoClicked().getLocation().getBlockY() + ", " + e.getWhoClicked().getLocation().getBlockZ(), ChatColor.AQUA + "左键点击传送，右键点击删除");
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
                    Waypoints3Builder.setLore(ChatColor.GREEN + "维度：" + e.getWhoClicked().getWorld().getName(), ChatColor.GREEN + "坐标：" + e.getWhoClicked().getLocation().getBlockX() + ", " + e.getWhoClicked().getLocation().getBlockY() + ", " + e.getWhoClicked().getLocation().getBlockZ(), ChatColor.AQUA + "左键点击传送，右键点击删除");
                    Waypoints3Builder.toItemStack();
                    redWayPoints.setItem(2, Waypoints3);
                    point3.put("red", e.getWhoClicked().getLocation());
                } else if (blueTeamPlayer.contains((Player) e.getWhoClicked())) {
                    ItemStack Waypoints3 = new ItemStack(Material.FILLED_MAP);
                    ItemBuilder Waypoints3Builder = new ItemBuilder(Waypoints3);
                    Waypoints3Builder.setAmount(1);
                    Waypoints3Builder.setDisplayName(ACTIVATED_WAYPOINTS3);
                    Waypoints3Builder.setLore(ChatColor.GREEN + "维度：" + e.getWhoClicked().getWorld().getName(), ChatColor.GREEN + "坐标：" + e.getWhoClicked().getLocation().getBlockX() + ", " + e.getWhoClicked().getLocation().getBlockY() + ", " + e.getWhoClicked().getLocation().getBlockZ(), ChatColor.AQUA + "左键点击传送，右键点击删除");
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
                    Waypoints1Builder.setLore(ChatColor.AQUA + "左键点击创建记录点");
                    Waypoints1Builder.toItemStack();
                    redWayPoints.setItem(0, Waypoints1);
                    point1.remove("red");
                } else if (blueTeamPlayer.contains((Player) e.getWhoClicked())) {
                    ItemStack bWayPoints1 = new ItemStack(Material.MAP);
                    ItemBuilder bWayPoints1Builder = new ItemBuilder(bWayPoints1);
                    bWayPoints1Builder.setAmount(1);
                    bWayPoints1Builder.setDisplayName(WAYPOINTS1);
                    bWayPoints1Builder.setLore(ChatColor.AQUA + "左键点击创建记录点");
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
                    Waypoints2Builder.setLore(ChatColor.AQUA + "左键点击创建记录点");
                    Waypoints2Builder.toItemStack();
                    redWayPoints.setItem(1, Waypoints2);
                    point2.remove("red");
                } else if (blueTeamPlayer.contains((Player) e.getWhoClicked())) {
                    ItemStack bWayPoints2 = new ItemStack(Material.MAP);
                    ItemBuilder bWayPoints2Builder = new ItemBuilder(bWayPoints2);
                    bWayPoints2Builder.setAmount(1);
                    bWayPoints2Builder.setDisplayName(WAYPOINTS2);
                    bWayPoints2Builder.setLore(ChatColor.AQUA + "左键点击创建记录点");
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
                    Waypoints3Builder.setLore(ChatColor.AQUA + "左键点击创建记录点");
                    Waypoints3Builder.toItemStack();
                    redWayPoints.setItem(2, Waypoints3);
                    point3.remove("red");
                } else if (blueTeamPlayer.contains((Player) e.getWhoClicked())) {
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


    @Override
    public void register() {

    }

    @Override
    public void unregister() {
        InventoryClickEvent.getHandlerList().unregister(Main.getInstance());
    }
}
