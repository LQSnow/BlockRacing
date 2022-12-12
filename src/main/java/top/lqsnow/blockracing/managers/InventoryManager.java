package top.lqsnow.blockracing.managers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import top.lqsnow.blockracing.utils.ItemBuilder;

import java.util.Objects;

public class InventoryManager {
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
    public static final String FIND = ChatColor.AQUA + "定位";
    public static final String WAYPOINTS = ChatColor.LIGHT_PURPLE + "记录点";
    
    public static Player holder = Bukkit.getPlayer("InventoryHolder");
    public static Inventory settings = Bukkit.createInventory(holder, 9, TITLE_SETTINGS);
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

    public static void init() {
        // settings
        setItem("RED_CONCRETE", 1,
                "中等难度方块：" + ChatColor.RED + "已禁用",
                ChatColor.GREEN + "点击以启用中等难度方块", 1, "settings");
        setItem("RED_CONCRETE", 1,
                "困难难度方块：" + ChatColor.RED + "已禁用",
                ChatColor.GREEN + "点击以启用困难难度方块", 3, "settings");
        setItem("RED_CONCRETE", 1,
                "染色方块：" + ChatColor.RED + "已禁用",
                ChatColor.GREEN + "点击以启用染色方块", 5, "settings");
        setItem("RED_CONCRETE", 1,
                "末地方块：" + ChatColor.RED + "已禁用",
                ChatColor.GREEN + "点击以启用末地方块", 7, "settings");

        // menu
        setItem("CHEST", 1,
                TEAM_CHEST, ChatColor.AQUA + "打开队伍箱子选择界面",
                0, "menu");
        setItem("TOTEM_OF_UNDYING", 1,
                ROLL, ChatColor.AQUA + "花费全部积分更换当前需求方块（每局每队仅限一次）",
                2, "menu");
        setItem("COMPASS", 1,
                FIND, ChatColor.AQUA + "花费10积分获得一次定位命令使用权限，命令为/find",
                4, "menu");
        setItem("MAP", 1,
                WAYPOINTS, ChatColor.AQUA + "保存或传送至记录点",
                6, "menu");

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

    public static void setItem(String material, int amount, String displayName, String lore, int slot, String inv){
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
