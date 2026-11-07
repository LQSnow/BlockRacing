package top.lqsnow.blockracing.toolkit.material;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class Materials {
    private static final Map<String, Material> BY_KEY = createKeyIndex();

    private Materials() {
    }

    public static Material require(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Material ID cannot be blank");
        }
        String id = name.trim();
        Material material = id.contains(":")
                ? BY_KEY.get(id)
                : Material.getMaterial(id.toUpperCase(Locale.ROOT));
        if (material == null || material.isLegacy()) {
            throw new IllegalArgumentException("Unknown material: " + name);
        }
        return material;
    }

    private static Map<String, Material> createKeyIndex() {
        Map<String, Material> index = new HashMap<>();
        for (Material material : Material.values()) {
            if (!material.isLegacy()) {
                index.put(material.getKey().toString(), material);
            }
        }
        return Map.copyOf(index);
    }

    public static ItemStack stack(String name, int amount) {
        return new ItemStack(require(name), amount);
    }

    public static ItemStack stack(Material material, int amount) {
        return new ItemStack(material, amount);
    }
}
