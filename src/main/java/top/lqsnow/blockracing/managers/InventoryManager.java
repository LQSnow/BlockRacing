package top.lqsnow.blockracing.managers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import top.lqsnow.blockracing.utils.ItemBuilder;

import java.util.Objects;

import static top.lqsnow.blockracing.managers.GameManager.locateCost;

public class InventoryManager {
    // 常量
    public static final String TITLE_SETTINGS = "设置菜单";
    public static final String TITLE_MENU = "主菜单";
    public static final String TITLE_TEAM_CHEST_SWITCH = "队伍箱子选择";
    public static final String TITLE_WAYPOINTS = "记录点";
    public static final String TEAM_CHEST = ChatColor.GREEN + "队伍箱子";
    public static final String TEAM_CHEST1 = ChatColor.GREEN + "队伍箱子1";
    public static final String TEAM_CHEST2 = ChatColor.GREEN + "队伍箱子2";
    public static final String TEAM_CHEST3 = ChatColor.GREEN + "队伍箱子3";
    public static final String RED_TEAM_CHEST1 = ChatColor.RED + "红队队伍箱子1";
    public static final String RED_TEAM_CHEST2 = ChatColor.RED + "红队队伍箱子2";
    public static final String RED_TEAM_CHEST3 = ChatColor.RED + "红队队伍箱子3";
    public static final String BLUE_TEAM_CHEST1 = ChatColor.BLUE + "蓝队队伍箱子1";
    public static final String BLUE_TEAM_CHEST2 = ChatColor.BLUE + "蓝队队伍箱子2";
    public static final String BLUE_TEAM_CHEST3 = ChatColor.BLUE + "蓝队队伍箱子3";
    public static final String WAYPOINTS1 = ChatColor.LIGHT_PURPLE + "记录点1";
    public static final String WAYPOINTS2 = ChatColor.LIGHT_PURPLE + "记录点2";
    public static final String WAYPOINTS3 = ChatColor.LIGHT_PURPLE + "记录点3";
    public static final String ACTIVATED_WAYPOINTS1 = ChatColor.GOLD + "记录点1";
    public static final String ACTIVATED_WAYPOINTS2 = ChatColor.GOLD + "记录点2";
    public static final String ACTIVATED_WAYPOINTS3 = ChatColor.GOLD + "记录点3";
    public static final String ROLL = ChatColor.YELLOW + "Roll";
    public static final String LOCATE = ChatColor.AQUA + "定位";
    public static final String WAYPOINTS = ChatColor.LIGHT_PURPLE + "记录点";
    public static final String RANDOMTP = ChatColor.BLUE + "随机传送";

    // 创建物品栏
    public static Player holder = Bukkit.getPlayer("InventoryHolder");
    public static Inventory settings = Bukkit.createInventory(holder, 6 * 9, TITLE_SETTINGS);
    public static Inventory menu = Bukkit.createInventory(holder, 9, TITLE_MENU);
    public static Inventory chestSwitch = Bukkit.createInventory(holder, 9, TITLE_TEAM_CHEST_SWITCH);
    public static Inventory redTeamChest1 = Bukkit.createInventory(holder, 6 * 9, RED_TEAM_CHEST1);
    public static Inventory redTeamChest2 = Bukkit.createInventory(holder, 6 * 9, RED_TEAM_CHEST2);
    public static Inventory redTeamChest3 = Bukkit.createInventory(holder, 6 * 9, RED_TEAM_CHEST3);
    public static Inventory blueTeamChest1 = Bukkit.createInventory(holder, 6 * 9, BLUE_TEAM_CHEST1);
    public static Inventory blueTeamChest2 = Bukkit.createInventory(holder, 6 * 9, BLUE_TEAM_CHEST2);
    public static Inventory blueTeamChest3 = Bukkit.createInventory(holder, 6 * 9, BLUE_TEAM_CHEST3);
    public static Inventory redWayPoints = Bukkit.createInventory(holder, 9, TITLE_WAYPOINTS);
    public static Inventory blueWayPoints = Bukkit.createInventory(holder, 9, TITLE_WAYPOINTS);

    // 初始化
    public static void init() {
        // settings
        setItem("RED_WOOL", 1,
                ChatColor.RED + "加入红队",
                ChatColor.RED + "点击此处加入红队", 10, "settings");
        setItem("BLUE_WOOL", 1,
                ChatColor.BLUE + "加入蓝队",
                ChatColor.BLUE + "点击此处加入蓝队", 12, "settings");
        setItem("EMERALD", 1,
                ChatColor.YELLOW + "准备",
                ChatColor.BLUE + "点击此处准备开始游戏", 14, "settings");
        setItem("DIAMOND", 1,
                ChatColor.AQUA + "开始游戏",
                ChatColor.AQUA + "点击此处开始游戏", 16, "settings");
        setItem("NAME_TAG", 1,
                ChatColor.YELLOW + "目标方块数量",
                ChatColor.YELLOW + "点击此处修改目标方块数量", 33, "settings");
        setItem("RED_CONCRETE", 1,
                "中等难度方块：" + ChatColor.RED + "已禁用",
                ChatColor.GREEN + "点击以启用中等难度方块", 28, "settings");
        setItem("RED_CONCRETE", 1,
                "困难难度方块：" + ChatColor.RED + "已禁用",
                ChatColor.GREEN + "点击以启用困难难度方块", 29, "settings");
        setItem("RED_CONCRETE", 1,
                "染色方块：" + ChatColor.RED + "已禁用",
                ChatColor.GREEN + "点击以启用染色方块", 30, "settings");
        setItem("RED_CONCRETE", 1,
                "末地方块：" + ChatColor.RED + "已禁用",
                ChatColor.GREEN + "点击以启用末地方块", 31, "settings");
        ItemStack stack = new ItemStack(Objects.requireNonNull(Material.RED_CONCRETE));
        ItemBuilder builder = new ItemBuilder(stack);
        builder.setAmount(1);
        builder.setDisplayName("点击此处切换至" + ChatColor.YELLOW + "极限竞速模式");
        builder.setLore(ChatColor.GREEN + "普通模式：", ChatColor.GREEN + "两个队伍需要收集的方块及顺序完全随机，", ChatColor.GREEN + "收集方块会将一组该方块给到对方的队伍箱子里！", ChatColor.YELLOW + "极限竞速模式：", ChatColor.YELLOW + "两个队伍需要收集的方块及顺序完全相同，", ChatColor.YELLOW + "收集方块将不会给予一组方块到对方队伍！");
        builder.toItemStack();
        settings.setItem(34, stack);


        // menu
        setItem("CHEST", 1,
                TEAM_CHEST, ChatColor.AQUA + "打开队伍箱子选择界面",
                0, "menu");
        setItem("TOTEM_OF_UNDYING", 1,
                ROLL, ChatColor.AQUA + "花费全部积分将当前目标方块全部替换成普通方块（每局每队仅限一次）",
                2, "menu");
        setItem("MAP", 1,
                WAYPOINTS, ChatColor.AQUA + "保存或传送至记录点",
                6, "menu");
        setItem("ENDER_PEARL", 1,
                RANDOMTP, ChatColor.AQUA + "花费2积分随机传送",
                8, "menu");

        // chestSwitch
        setItem("CHEST", 1,
                TEAM_CHEST1, ChatColor.AQUA + "队伍箱子1",
                0, "chestSwitch");
        setItem("CHEST", 1,
                TEAM_CHEST2, ChatColor.AQUA + "队伍箱子2",
                1, "chestSwitch");
        setItem("CHEST", 1,
                TEAM_CHEST3, ChatColor.AQUA + "队伍箱子3",
                2, "chestSwitch");

        // WayPoints
        setItem("MAP", 1,
                WAYPOINTS1, ChatColor.AQUA + "左键点击创建记录点",
                0, "redWayPoints");
        setItem("MAP", 1,
                WAYPOINTS2, ChatColor.AQUA + "左键点击创建记录点",
                1, "redWayPoints");
        setItem("MAP", 1,
                WAYPOINTS3, ChatColor.AQUA + "左键点击创建记录点",
                2, "redWayPoints");
        setItem("MAP", 1,
                WAYPOINTS1, ChatColor.AQUA + "左键点击创建记录点",
                0, "blueWayPoints");
        setItem("MAP", 1,
                WAYPOINTS2, ChatColor.AQUA + "左键点击创建记录点",
                1, "blueWayPoints");
        setItem("MAP", 1,
                WAYPOINTS3, ChatColor.AQUA + "左键点击创建记录点",
                2, "blueWayPoints");
    }

    public static void setLocateItem() {
        ItemStack stack = new ItemStack(Objects.requireNonNull(Material.COMPASS));
        ItemBuilder builder = new ItemBuilder(stack);
        builder.setAmount(1);
        builder.setDisplayName(LOCATE);
        builder.setLore(ChatColor.AQUA + "花费" + locateCost + "积分获得一次locate命令使用权限", ChatColor.RED + "注意！如果距离过远可能会引发服务器卡顿！");
        builder.toItemStack();
        menu.setItem(4, stack);
    }

    // 添加物品
    public static void setItem(String material, int amount, String displayName, String lore, int slot, String inv) {
        ItemStack stack = new ItemStack(Objects.requireNonNull(Material.getMaterial(material)));
        ItemBuilder builder = new ItemBuilder(stack);
        builder.setAmount(amount);
        builder.setDisplayName(displayName);
        builder.setLore(lore);
        builder.toItemStack();
        switch (inv) {
            case "settings" -> {
                settings.setItem(slot, stack);
            }
            case "menu" -> {
                menu.setItem(slot, stack);
            }
            case "chestSwitch" -> {
                chestSwitch.setItem(slot, stack);
            }
            case "redWayPoints" -> {
                redWayPoints.setItem(slot, stack);
            }
            case "blueWayPoints" -> {
                blueWayPoints.setItem(slot, stack);
            }
        }
    }
}
