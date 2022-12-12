package top.lqsnow.blockracing.utils;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.ParametersAreNullableByDefault;
import java.util.*;

/**
 * Build an ItemStack.
 * Paper changed to use method displayName(Component).
 * See <a href="https://github.com/PaperMC/Paper/pull/4842">Pull #4842</a>.
 * For compatibility, we won't change until Spigot and Bukkit change this.
 *
 * @see net.kyori.adventure.text.Component
 */
@SuppressWarnings("deprecation")
public class ItemBuilder {
    @Nonnull
    private final ItemStack stack;
    @Nonnull
    private ItemMeta meta;

    @ParametersAreNonnullByDefault
    public ItemBuilder(ItemStack originStack) {
        stack = originStack;
        meta = Objects.requireNonNullElse(originStack.getItemMeta(), Bukkit.getItemFactory().getItemMeta(stack.getType()));
    }

    @ParametersAreNonnullByDefault
    public ItemBuilder(ItemStack originStack, ItemMeta originMeta) {
        stack = originStack;
        meta = originMeta;
    }

    /**
     * Return the ItemStack.
     *
     * @return The modified ItemStack.
     */
    @Nonnull
    public ItemStack toItemStack() {
        stack.setItemMeta(meta);
        return stack;
    }

    @Nonnull
    @ParametersAreNullableByDefault

    public ItemBuilder setDisplayName(String name) {
        if (name == null) {
            return this;
        }
        meta.setDisplayName(name);
        return this;
    }

    @Nonnull
    @ParametersAreNullableByDefault
    public ItemBuilder setDisplayName(BaseComponent... name) {
        if (name == null) {
            return this;
        }
        meta.setDisplayNameComponent(name);
        return this;
    }

    @Nonnull
    @ParametersAreNullableByDefault
    public ItemBuilder setLore(String... lore) {
        if (lore == null) {
            return this;
        }
        meta.setLore(Arrays.asList(lore));
        return this;
    }

    @Nonnull
    @ParametersAreNullableByDefault
    public ItemBuilder setLore(BaseComponent[]... lore) {
        if (lore == null) {
            return this;
        }
        meta.setLoreComponents(Arrays.asList(lore));
        return this;
    }

    /**
     * Insert something after the lore.
     *
     * @param lore What to prepend.
     * @return The builder.
     */
    @Nonnull
    @ParametersAreNullableByDefault
    public ItemBuilder appendLore(String... lore) {
        return insertLoreAt(-1, lore);
    }

    /**
     * Insert something after the lore.
     *
     * @param lore What to prepend.
     * @return The builder.
     */
    @Nonnull
    @ParametersAreNullableByDefault
    public ItemBuilder appendLore(BaseComponent[]... lore) {
        return insertLoreAt(-1, lore);
    }

    /**
     * Insert something to the very start of the lore.
     *
     * @param lore What to prepend.
     * @return The builder.
     */
    @Nonnull
    @ParametersAreNullableByDefault
    public ItemBuilder prependLore(String... lore) {
        return insertLoreAt(0, lore);
    }

    /**
     * Insert something to the very start of the lore.
     *
     * @param lore What to prepend.
     * @return The builder.
     */
    @Nonnull
    @ParametersAreNullableByDefault
    public ItemBuilder prependLore(BaseComponent[]... lore) {
        return insertLoreAt(0, lore);
    }

    /**
     * Insert a series of things into the Lore at a specified position.
     *
     * @param position Where to insert, negative to count from back.
     * @param lore     What to insert.
     * @return The builder.
     */
    @Nonnull
    @ParametersAreNullableByDefault
    public ItemBuilder insertLoreAt(int position, String... lore) {

        if (lore == null) {
            return this;
        }
        List<String> rawLore = meta.getLore();

        // If null then create
        if (rawLore == null || rawLore.size() == 0) {
            meta.setLore(Arrays.asList(lore));
            return this;
        }

        // Reverse position
        if (position < 0) {
            position = rawLore.size() + position;
        }

        // If writeable
        if (rawLore instanceof ArrayList || rawLore instanceof Vector || rawLore instanceof LinkedList) {
            rawLore.addAll(position, Arrays.asList(lore));
            meta.setLore(rawLore);
            return this;
        }

        // Change to Arraylist then add
        List<String> properLore = Arrays.asList(rawLore.toArray(new String[0]));
        properLore.addAll(position, Arrays.asList(lore));
        meta.setLore(properLore);
        return this;
    }

    /**
     * Insert a series of things into the Lore at a specified position.
     *
     * @param position Where to insert, negative to count from back.
     * @param lore     What to insert.
     * @return The builder.
     */
    @Nonnull
    @ParametersAreNullableByDefault
    public ItemBuilder insertLoreAt(int position, BaseComponent[]... lore) {
        if (lore == null) {
            return this;
        }
        List<BaseComponent[]> rawLore = meta.getLoreComponents();
        if (rawLore == null || rawLore.size() == 0) {
            meta.setLoreComponents(Arrays.asList(lore));
            return this;
        }
        if (position < 0) {
            position = rawLore.size() + position;
        }
        if (rawLore instanceof ArrayList || rawLore instanceof Vector || rawLore instanceof LinkedList) {
            rawLore.addAll(position, Arrays.asList(lore));
            meta.setLoreComponents(rawLore);
            return this;
        }
        List<BaseComponent[]> properLore = Arrays.asList(rawLore.toArray(new BaseComponent[0][0]));
        properLore.addAll(position, Arrays.asList(lore));
        meta.setLoreComponents(properLore);
        return this;
    }

    @Nonnull
    @ParametersAreNullableByDefault
    public ItemBuilder setLoreByLines(String lore) {
        if (lore == null) {
            return this;
        }
        return setLore(lore.split("\\r?\\n"));
    }

    @Nonnull
    public ItemBuilder dropChanges() {
        meta = stack.getItemMeta();
        return this;
    }

    @Nonnull
    public ItemBuilder setAmount(int amount) {
        stack.setAmount(amount);
        return this;
    }

    @Override
    @Nonnull
    public ItemBuilder clone() {
        return new ItemBuilder(stack.clone(), meta.clone());
    }

    /**
     * To check if the two ItemBuilders will produce same results.
     *
     * @param builder The object to compare.
     * @return If the stacks are same.
     */
    @Override
    @ParametersAreNonnullByDefault
    public boolean equals(Object builder) {
        if (!(builder instanceof ItemBuilder)) {
            return false;
        }
        if (this == builder) {
            return true;
        }
        ItemStack spare = ((ItemBuilder) builder).clone().toItemStack();
        ItemStack self = clone().toItemStack();
        return spare.equals(self);
    }

    @Override
    public int hashCode() {
        return clone().toItemStack().hashCode();
    }

    @Nonnull
    @ParametersAreNullableByDefault
    public ItemBuilder addEnchants(int level, Enchantment... enchantment) {
        if (enchantment == null) {
            return this;
        }
        for (Enchantment e : enchantment) {
            if (e != null) {
                meta.addEnchant(e, level, true);
            }
        }
        return this;
    }

    @Nonnull
    @ParametersAreNullableByDefault
    public ItemBuilder removeEnchants(Enchantment... enchantment) {
        if (enchantment == null) {
            return this;
        }
        for (Enchantment e : enchantment) {
            if (e != null) {
                meta.removeEnchant(e);
            }
        }
        return this;
    }

    @Nonnull
    public ItemBuilder setUnbreakable(boolean unbreakable) {
        meta.setUnbreakable(unbreakable);
        return this;
    }

    /**
     * Set an attribute modifier, null to remove.
     *
     * @param attribute The attribute.
     * @param am        The modifier.
     * @return The builder.
     */
    @Nonnull
    @ParametersAreNullableByDefault
    public ItemBuilder setAttributeModifier(Attribute attribute, AttributeModifier am) {
        if (attribute == null) {
            return this;
        }
        if (am == null) {
            meta.removeAttributeModifier(attribute);
            return this;
        }
        meta.addAttributeModifier(attribute, am);
        return this;
    }

}