package top.lqsnow.blockracing.listeners;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import top.lqsnow.blockracing.Main;
import top.lqsnow.blockracing.managers.GameManager;
import top.lqsnow.blockracing.managers.ScoreboardManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import static top.lqsnow.blockracing.listeners.BasicEventListener.*;
import static top.lqsnow.blockracing.managers.BlockManager.*;
import static top.lqsnow.blockracing.managers.GameManager.*;
import static top.lqsnow.blockracing.managers.InventoryManager.*;
import static top.lqsnow.blockracing.managers.ScoreboardManager.*;
import static top.lqsnow.blockracing.utils.ColorUtil.t;
import static top.lqsnow.blockracing.utils.ConsoleCommandHandler.sendAll;

public class InventoryEventListener implements IListener {

    private int redRollCount;
    private int blueRollCount;
    public static ArrayList<String> redRollPlayers = new ArrayList<>();
    public static ArrayList<String> blueRollPlayers = new ArrayList<>();
    public static HashMap<String, Location> point1 = new HashMap<>();
    public static HashMap<String, Location> point2 = new HashMap<>();
    public static HashMap<String, Location> point3 = new HashMap<>();
    public static HashMap<String, Boolean> randomTP = new HashMap<>();
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
                        "中等难度方块：" + ChatColor.GREEN + "已启用", 20, "settings",
                        ChatColor.RED + "点击以禁用中等难度方块");
                enableNormalBlock = true;
                setBlockAmount(false);
                ScoreboardManager.update();
                return;
            } else if (clickedItem.getItemMeta().getDisplayName().equals("中等难度方块：" + ChatColor.GREEN + "已启用")) {
                setItem("RED_CONCRETE", 1,
                        "中等难度方块：" + ChatColor.RED + "已禁用", 20, "settings",
                        ChatColor.GREEN + "点击以启用中等难度方块");
                enableNormalBlock = false;
                setBlockAmount(false);
                ScoreboardManager.update();
                return;
            } else if (clickedItem.getItemMeta().getDisplayName().equals("困难难度方块：" + ChatColor.RED + "已禁用")) {
                setItem("GREEN_CONCRETE", 1,
                        "困难难度方块：" + ChatColor.GREEN + "已启用", 21, "settings",
                        ChatColor.RED + "点击以禁用困难难度方块");
                enableHardBlock = true;
                setBlockAmount(false);
                ScoreboardManager.update();
                return;
            } else if (clickedItem.getItemMeta().getDisplayName().equals("困难难度方块：" + ChatColor.GREEN + "已启用")) {
                setItem("RED_CONCRETE", 1,
                        "困难难度方块：" + ChatColor.RED + "已禁用", 21, "settings",
                        ChatColor.GREEN + "点击以启用困难难度方块");
                enableHardBlock = false;
                setBlockAmount(false);
                ScoreboardManager.update();
                return;
            } else if (clickedItem.getItemMeta().getDisplayName().equals("染色方块：" + ChatColor.RED + "已禁用")) {
                setItem("GREEN_CONCRETE", 1,
                        "染色方块：" + ChatColor.GREEN + "已启用", 22, "settings",
                        ChatColor.RED + "点击以禁用染色方块");
                enableDyedBlock = true;
                setBlockAmount(false);
                ScoreboardManager.update();
                return;
            } else if (clickedItem.getItemMeta().getDisplayName().equals("染色方块：" + ChatColor.GREEN + "已启用")) {
                setItem("RED_CONCRETE", 1,
                        "染色方块：" + ChatColor.RED + "已禁用", 22, "settings",
                        ChatColor.GREEN + "点击以启用染色方块");
                enableDyedBlock = false;
                setBlockAmount(false);
                ScoreboardManager.update();
                return;
            } else if (clickedItem.getItemMeta().getDisplayName().equals("末地方块：" + ChatColor.RED + "已禁用")) {
                setItem("GREEN_CONCRETE", 1,
                        "末地方块：" + ChatColor.GREEN + "已启用", 23, "settings",
                        ChatColor.RED + "点击以禁用末地方块");
                enableEndBlock = true;
                setBlockAmount(false);
                ScoreboardManager.update();
                return;
            } else if (clickedItem.getItemMeta().getDisplayName().equals("末地方块：" + ChatColor.GREEN + "已启用")) {
                setItem("RED_CONCRETE", 1,
                        "末地方块：" + ChatColor.RED + "已禁用", 23, "settings",
                        ChatColor.GREEN + "点击以启用末地方块");
                enableEndBlock = false;
                setBlockAmount(false);
                ScoreboardManager.update();
                return;
            }

            // 队伍选择
            if (clickedItem.getItemMeta().getDisplayName().equals(ChatColor.RED + "加入红队")) {
                if (redTeamPlayer.contains(player)) {
                    player.sendMessage(t("&c您已经加入红队了！"));
                    return;
                }
                red.addEntry(player.getName());
                sendAll("&c" + player.getName() + "加入了红队！");
                redTeamPlayer.add(player);
                redTeamPlayerString.add(player.getName());
                if (blueTeamPlayer.contains(player)) {
                    blueTeamPlayer.remove(player);
                    blueTeamPlayerString.remove(player.getName());
                    blue.removeEntry(player.getName());
                }
                return;
            } else if (clickedItem.getItemMeta().getDisplayName().equals(ChatColor.BLUE + "加入蓝队")) {
                if (blueTeamPlayer.contains(player)) {
                    player.sendMessage(t("&c您已经加入蓝队了！"));
                    return;
                }
                blue.addEntry(player.getName());
                sendAll("&9" + player.getName() + "加入了蓝队！");
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
                    sendAll("&b" + player.getName() + "已准备！");
                    if (prepareList.size() > 1 && prepareList.size() == Bukkit.getOnlinePlayers().size()) {
                        canStart = true;
                        sendAll("&b所有人都准备好了，可以开始游戏了！");
                    }
                    return;
                } else {
                    player.sendMessage(t("&c您已经准备过了！"));
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
                    if (blockAmount > maxBlockAmount) {
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
            if (clickedItem.getItemMeta().getDisplayName().equals(t("&e切换至：普通模式"))) {
                mode = 0;
                setItem("GREEN_CONCRETE", 1, t("&a当前模式：普通模式"), 29, "settings",
                        ChatColor.YELLOW + "普通模式：", ChatColor.YELLOW + t("两个队伍需要收集的方块及顺序完全&l随机&r&e，"), ChatColor.YELLOW + "收集方块会将一组该方块给到对方的队伍箱子里！");
                setItem("YELLOW_CONCRETE", 1, t("&e切换至：竞速模式"), 30, "settings",
                        ChatColor.YELLOW + "竞速模式：", ChatColor.YELLOW + t("两个队伍需要收集的方块及顺序将完全&l相同&r&e，"), ChatColor.YELLOW + t("且收集方块将&l不会&r&e给予一组方块到对方队伍！"));
                ScoreboardManager.update();
            } else if (clickedItem.getItemMeta().getDisplayName().equals(t("&e切换至：竞速模式"))) {
                mode = 1;
                setItem("YELLOW_CONCRETE", 1, t("&e切换至：普通模式"), 29, "settings",
                        ChatColor.YELLOW + "普通模式：", ChatColor.YELLOW + t("两个队伍需要收集的方块及顺序完全&l随机&r&e，"), ChatColor.YELLOW + "收集方块会将一组该方块给到对方的队伍箱子里！");
                setItem("GREEN_CONCRETE", 1, t("&a当前模式：竞速模式"), 30, "settings",
                        ChatColor.YELLOW + "竞速模式：", ChatColor.YELLOW + t("两个队伍需要收集的方块及顺序将完全&l相同&r&e，"), ChatColor.YELLOW + t("且收集方块将&l不会&r&e给予一组方块到对方队伍！"));
                ScoreboardManager.update();
            } else if (clickedItem.getItemMeta().getDisplayName().equals(t("&e当前未开启：极速模式"))) {
                speedMode = true;
                setItem("GREEN_CONCRETE", 1, t("&a当前已开启：极速模式"), 33, "settings",
                        ChatColor.YELLOW + "极速模式：", ChatColor.YELLOW + "开启极速模式后，开局时将会", ChatColor.YELLOW + "给予玩家部分道具和效果，", ChatColor.YELLOW + "且获得方块时加3分，以加快游戏进度。", ChatColor.AQUA + "物品：精准采集铁镐、熟牛排、损坏的鞘翅、经验修补附魔书", ChatColor.AQUA + "效果：急迫5");
                ScoreboardManager.update();
            } else if (clickedItem.getItemMeta().getDisplayName().equals(t("&a当前已开启：极速模式"))) {
                speedMode = false;
                setItem("YELLOW_CONCRETE", 1, t("&e当前未开启：极速模式"), 33, "settings",
                        ChatColor.YELLOW + "极速模式：", ChatColor.YELLOW + "开启极速模式后，开局时将会", ChatColor.YELLOW + "给予玩家部分道具和效果，", ChatColor.YELLOW + "且获得方块时加3分，以加快游戏进度。", ChatColor.AQUA + "物品：精准采集铁镐、熟牛排、损坏的鞘翅、经验修补附魔书", ChatColor.AQUA + "效果：急迫5");
                ScoreboardManager.update();
            }

            // menu 菜单界面
            if (clickedItem.getItemMeta().getDisplayName().equals(TEAM_CHEST)) {
                Bukkit.getPlayer(e.getWhoClicked().getName()).openInventory(chestSwitch);
                return;
            }
            if (clickedItem.getItemMeta().getDisplayName().equals(ROLL)) {
                if (redTeamPlayer.contains((Player) e.getWhoClicked()) && redRollCount < 3) {
                    if (!redRollPlayers.contains(e.getWhoClicked().getName())) {
                        redTeamPlayer.forEach(p -> {
                            p.sendMessage(ChatColor.AQUA + e.getWhoClicked().getName() + "申请使用ROLL！全队玩家全部申请即可ROLL掉当前方块！");
                        });
                        redRollPlayers.add(e.getWhoClicked().getName());
                    } else {
                        redTeamPlayer.forEach(p -> {
                            p.sendMessage(ChatColor.RED + e.getWhoClicked().getName() + "取消申请使用ROLL！");
                        });
                        redRollPlayers.remove(e.getWhoClicked().getName());
                    }
                    if (redRollPlayers.size() == redTeamPlayer.size()) {
                        for (int i = 0; i < 4; i++) {
                            if (redCurrentBlocks.size() != 0) {
                                redCurrentBlocks.remove(0);
                                redCurrentBlocks.add(blocks[r.nextInt(blocks.length)]);
                            } else break;
                        }

                        sendAll("&c红队Roll掉了所需方块！");
                        redRollCount++;
                        redTeamScore = 0;
                        redRollPlayers.clear();
                        ScoreboardManager.update();
                    }

                } else if (blueTeamPlayer.contains((Player) e.getWhoClicked()) && blueRollCount < 3) {
                    if (!blueRollPlayers.contains(e.getWhoClicked().getName())) {
                        blueTeamPlayer.forEach(p -> {
                            p.sendMessage(ChatColor.AQUA + e.getWhoClicked().getName() + "申请使用ROLL！全队玩家全部申请即可ROLL掉当前方块！");
                        });
                        blueRollPlayers.add(e.getWhoClicked().getName());
                    } else {
                        blueTeamPlayer.forEach(p -> {
                            p.sendMessage(ChatColor.RED + e.getWhoClicked().getName() + "取消申请使用ROLL！");
                        });
                        blueRollPlayers.remove(e.getWhoClicked().getName());
                    }
                    if (blueRollPlayers.size() == blueTeamPlayer.size()) {
                        for (int i = 0; i < 4; i++) {
                            if (blueCurrentBlocks.size() != 0) {
                                blueCurrentBlocks.remove(0);
                                blueCurrentBlocks.add(blocks[r.nextInt(blocks.length)]);
                            } else break;
                        }

                        sendAll("&c蓝队Roll掉了所需方块！");
                        blueRollCount++;
                        blueTeamScore = 0;
                        blueRollPlayers.clear();
                        ScoreboardManager.update();
                    }
                } else if (redTeamPlayer.contains(((Player) e.getWhoClicked())) || blueTeamPlayer.contains((Player) e.getWhoClicked())) {
                    e.getWhoClicked().sendMessage(ChatColor.DARK_RED + "您的队伍已经使用过3次Roll了！");
                }
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
                randomTP.putIfAbsent(player.getName(), false);
                if (randomTP.get(player.getName())) {
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
                sendAll("&a玩家" + p.getName() + "使用了随机传送！");
                if (randomTP.get(player.getName())) {
                    if (redTeamPlayer.contains(player)) redTeamScore -= 2;
                    else blueTeamScore -= 2;
                } else randomTP.put(player.getName(), true);
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
                    setItem("FILLED_MAP", 1, ACTIVATED_WAYPOINTS1, 0, "redWayPoints",
                            t("&a维度：") + e.getWhoClicked().getWorld().getName(),
                            t("&a坐标：") + e.getWhoClicked().getLocation().getBlockX() + ", " + e.getWhoClicked().getLocation().getBlockY() + ", " + e.getWhoClicked().getLocation().getBlockZ(),
                            t("&b左键点击传送，右键点击删除"));
                    point1.put("red", e.getWhoClicked().getLocation());
                } else if (blueTeamPlayer.contains((Player) e.getWhoClicked())) {
                    setItem("FILLED_MAP", 1, ACTIVATED_WAYPOINTS1, 0, "blueWayPoints",
                            t("&a维度：") + e.getWhoClicked().getWorld().getName(),
                            t("&a坐标：") + e.getWhoClicked().getLocation().getBlockX() + ", " + e.getWhoClicked().getLocation().getBlockY() + ", " + e.getWhoClicked().getLocation().getBlockZ(),
                            t("&b左键点击传送，右键点击删除"));
                    point1.put("blue", e.getWhoClicked().getLocation());
                }
            } else if (clickedItem.getItemMeta().getDisplayName().equals(WAYPOINTS2)) {
                if (redTeamPlayer.contains((Player) e.getWhoClicked())) {
                    setItem("FILLED_MAP", 1, ACTIVATED_WAYPOINTS2, 1, "redWayPoints",
                            t("&a维度：") + e.getWhoClicked().getWorld().getName(),
                            t("&a坐标：") + e.getWhoClicked().getLocation().getBlockX() + ", " + e.getWhoClicked().getLocation().getBlockY() + ", " + e.getWhoClicked().getLocation().getBlockZ(),
                            t("&b左键点击传送，右键点击删除"));
                    point2.put("red", e.getWhoClicked().getLocation());
                } else if (blueTeamPlayer.contains((Player) e.getWhoClicked())) {
                    setItem("FILLED_MAP", 1, ACTIVATED_WAYPOINTS2, 1, "blueWayPoints",
                            t("&a维度：") + e.getWhoClicked().getWorld().getName(),
                            t("&a坐标：") + e.getWhoClicked().getLocation().getBlockX() + ", " + e.getWhoClicked().getLocation().getBlockY() + ", " + e.getWhoClicked().getLocation().getBlockZ(),
                            t("&b左键点击传送，右键点击删除"));
                    point2.put("blue", e.getWhoClicked().getLocation());
                }
            } else if (clickedItem.getItemMeta().getDisplayName().equals(WAYPOINTS3)) {
                if (redTeamPlayer.contains((Player) e.getWhoClicked())) {
                    setItem("FILLED_MAP", 1, ACTIVATED_WAYPOINTS3, 2, "redWayPoints",
                            t("&a维度：") + e.getWhoClicked().getWorld().getName(),
                            t("&a坐标：") + e.getWhoClicked().getLocation().getBlockX() + ", " + e.getWhoClicked().getLocation().getBlockY() + ", " + e.getWhoClicked().getLocation().getBlockZ(),
                            t("&b左键点击传送，右键点击删除"));
                    point3.put("red", e.getWhoClicked().getLocation());
                } else if (blueTeamPlayer.contains((Player) e.getWhoClicked())) {
                    setItem("FILLED_MAP", 1, ACTIVATED_WAYPOINTS3, 2, "blueWayPoints",
                            t("&a维度：") + e.getWhoClicked().getWorld().getName(),
                            t("&a坐标：") + e.getWhoClicked().getLocation().getBlockX() + ", " + e.getWhoClicked().getLocation().getBlockY() + ", " + e.getWhoClicked().getLocation().getBlockZ(),
                            t("&b左键点击传送，右键点击删除"));
                    point3.put("blue", e.getWhoClicked().getLocation());
                }
            } else if ((clickedItem.getItemMeta().getDisplayName().equals(ACTIVATED_WAYPOINTS1)) & e.isLeftClick()) {
                if (redTeamPlayer.contains((Player) e.getWhoClicked())) {
                    (e.getWhoClicked()).teleport(point1.get("red"));
                } else if (blueTeamPlayer.contains((Player) e.getWhoClicked())) {
                    (e.getWhoClicked()).teleport(point1.get("blue"));
                }
            } else if ((clickedItem.getItemMeta().getDisplayName().equals(ACTIVATED_WAYPOINTS2)) & e.isLeftClick()) {
                if (redTeamPlayer.contains((Player) e.getWhoClicked())) {
                    (e.getWhoClicked()).teleport(point2.get("red"));
                } else if (blueTeamPlayer.contains((Player) e.getWhoClicked())) {
                    (e.getWhoClicked()).teleport(point2.get("blue"));
                }
            } else if ((clickedItem.getItemMeta().getDisplayName().equals(ACTIVATED_WAYPOINTS3)) & e.isLeftClick()) {
                if (redTeamPlayer.contains((Player) e.getWhoClicked())) {
                    (e.getWhoClicked()).teleport(point3.get("red"));
                } else if (blueTeamPlayer.contains((Player) e.getWhoClicked())) {
                    (e.getWhoClicked()).teleport(point3.get("blue"));
                }
            }else if ((clickedItem.getItemMeta().getDisplayName().equals(ACTIVATED_WAYPOINTS1)) && e.isRightClick()) {
                if (redTeamPlayer.contains((Player) e.getWhoClicked())) {
                    setItem("MAP", 1, WAYPOINTS1, 0, "redWayPoints",
                            ChatColor.AQUA + "左键点击创建记录点");
                    point1.remove("red");
                } else if (blueTeamPlayer.contains((Player) e.getWhoClicked())) {
                    setItem("MAP", 1, WAYPOINTS1, 0, "blueWayPoints",
                            ChatColor.AQUA + "左键点击创建记录点");
                    point1.remove("blue");
                }
            } else if ((clickedItem.getItemMeta().getDisplayName().equals(ACTIVATED_WAYPOINTS2)) && e.isRightClick()) {
                if (redTeamPlayer.contains((Player) e.getWhoClicked())) {
                    setItem("MAP", 1, WAYPOINTS2, 1, "redWayPoints",
                            ChatColor.AQUA + "左键点击创建记录点");
                    point2.remove("red");
                } else if (blueTeamPlayer.contains((Player) e.getWhoClicked())) {
                    setItem("MAP", 1, WAYPOINTS2, 1, "blueWayPoints",
                            ChatColor.AQUA + "左键点击创建记录点");
                    point2.remove("blue");
                }
            } else if ((clickedItem.getItemMeta().getDisplayName().equals(ACTIVATED_WAYPOINTS3)) && e.isRightClick()) {
                if (redTeamPlayer.contains((Player) e.getWhoClicked())) {
                    setItem("MAP", 1, WAYPOINTS3, 2, "redWayPoints",
                            ChatColor.AQUA + "左键点击创建记录点");
                    point3.remove("red");
                } else if (blueTeamPlayer.contains((Player) e.getWhoClicked())) {
                    setItem("MAP", 1, WAYPOINTS3, 2, "blueWayPoints",
                            ChatColor.AQUA + "左键点击创建记录点");
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
