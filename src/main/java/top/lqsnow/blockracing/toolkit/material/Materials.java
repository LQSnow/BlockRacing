package top.lqsnow.blockracing.toolkit.material;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class Materials {
    private Materials() {
    }

    public static Material require(String name) {
        Material material = Material.getMaterial(name);
        if (material == null) {
            throw new IllegalArgumentException("Unknown material: " + name);
        }
        return material;
    }

    public static ItemStack stack(String name, int amount) {
        return new ItemStack(require(name), amount);
    }

    public static ItemStack stack(Material material, int amount) {
        return new ItemStack(material, amount);
    }
}
