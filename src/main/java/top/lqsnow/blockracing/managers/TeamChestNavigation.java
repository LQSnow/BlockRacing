package top.lqsnow.blockracing.managers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import top.lqsnow.blockracing.Main;
import top.lqsnow.blockracing.toolkit.item.ItemBuilder;
import top.lqsnow.blockracing.toolkit.menu.ReservedInventorySlot;

import java.util.IdentityHashMap;
import java.util.Map;

/** Reserves one navigation slot; legacy contents in that slot remain in the save until space frees. */
public final class TeamChestNavigation implements Listener {
    public static final int BACK_SLOT = 53;
    private static final NamespacedKey KEY = new NamespacedKey("blockracing", "team-chest-back");
    private static final Map<Inventory, ReservedInventorySlot<ItemStack>> slots = new IdentityHashMap<>();

    private static boolean teamChest(Inventory inventory) {
        return Game.redTeamChest.contains(inventory) || Game.blueTeamChest.contains(inventory);
    }

    private static boolean navigation(ItemStack item) {
        return item != null && item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(KEY, PersistentDataType.BYTE);
    }

    public static void prepare(Inventory inventory) {
        ItemStack back = ItemBuilder.of(Material.ARROW).name("§e返回上一级 / Back").build();
        back.editMeta(meta -> meta.getPersistentDataContainer().set(KEY, PersistentDataType.BYTE, (byte) 1));
        inventory.setContents(slots.computeIfAbsent(inventory, ignored -> new ReservedInventorySlot<>(BACK_SLOT))
                .install(inventory.getContents(), back, TeamChestNavigation::navigation, TeamChestNavigation::empty));
    }

    private static boolean empty(ItemStack item) { return item == null || item.getType().isAir(); }

    private static void flushPending(Inventory inventory) {
        var state = slots.get(inventory);
        if (state == null || state.pending() == null) return;
        ItemStack[] contents = inventory.getContents();
        ItemStack[] updated = state.flush(contents, TeamChestNavigation::empty);
        if (updated != contents) inventory.setContents(updated);
    }

    public static ItemStack[] savedContents(Inventory inventory) {
        var state = slots.get(inventory);
        return state == null ? inventory.getContents() : state.savedContents(inventory.getContents());
    }

    public static void restore(Inventory inventory, ItemStack[] contents) {
        slots.remove(inventory);
        inventory.setContents(contents);
        prepare(inventory);
    }

    public static boolean contains(Inventory inventory, Material material) {
        ItemStack held = slots.containsKey(inventory) ? slots.get(inventory).pending() : null;
        return inventory.contains(material) || held != null && held.getType() == material;
    }

    @EventHandler
    public void click(InventoryClickEvent event) {
        Inventory inventory = event.getView().getTopInventory();
        if (!teamChest(inventory)) return;
        if (event.getRawSlot() == BACK_SLOT) {
            event.setCancelled(true);
            if (event.getWhoClicked() instanceof Player player) Bukkit.getScheduler().runTask(Main.getInstance(), () -> Gui.openTeamChestSelection(player));
        } else if (event.getAction() == InventoryAction.COLLECT_TO_CURSOR) {
            // Collect-to-cursor scans other slots as well and must never pick up the navigation item.
            if (event.getCursor() != null && event.getCursor().isSimilar(inventory.getItem(BACK_SLOT))) event.setCancelled(true);
        }
        Bukkit.getScheduler().runTask(Main.getInstance(), () -> flushPending(inventory));
    }

    @EventHandler
    public void drag(InventoryDragEvent event) {
        Inventory inventory = event.getView().getTopInventory();
        if (!teamChest(inventory)) return;
        if (event.getRawSlots().contains(BACK_SLOT)) event.setCancelled(true);
        Bukkit.getScheduler().runTask(Main.getInstance(), () -> flushPending(inventory));
    }
}
