package top.lqsnow.blockracing.toolkit.item;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import top.lqsnow.blockracing.toolkit.text.Texts;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ItemBuilder {
    private final ItemStack item;
    private String name;
    private Collection<String> lore;
    private final Map<Enchantment, Integer> enchantments = new LinkedHashMap<>();
    private ItemFlag[] flags = new ItemFlag[0];

    private ItemBuilder(ItemStack item) {
        this.item = item.clone();
    }

    public static ItemBuilder of(Material material) {
        return new ItemBuilder(new ItemStack(material));
    }

    public static ItemBuilder of(ItemStack item) {
        return new ItemBuilder(item);
    }

    public ItemBuilder amount(int amount) {
        item.setAmount(amount);
        return this;
    }

    public ItemBuilder name(String name) {
        this.name = name;
        return this;
    }

    public ItemBuilder lore(Collection<String> lore) {
        this.lore = lore;
        return this;
    }

    public ItemBuilder enchant(Enchantment enchantment, int level) {
        enchantments.put(enchantment, level);
        return this;
    }

    public ItemBuilder flags(ItemFlag... flags) {
        this.flags = flags.clone();
        return this;
    }

    public ItemStack build() {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (name != null) {
                meta.displayName(Texts.component(name));
            }
            if (lore != null) {
                meta.lore(Texts.components(lore));
            }
            enchantments.forEach((enchantment, level) -> meta.addEnchant(enchantment, level, true));
            meta.addItemFlags(flags);
            item.setItemMeta(meta);
        }
        return item.clone();
    }
}
