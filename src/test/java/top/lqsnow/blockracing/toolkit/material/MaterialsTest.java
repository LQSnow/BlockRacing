package top.lqsnow.blockracing.toolkit.material;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MaterialsTest {
    @Test
    void resolvesMinecraft262MaterialsWithoutCompatibilityMappings() {
        assertEquals(Material.SULFUR, Materials.require("SULFUR"));
        assertEquals(Material.CINNABAR, Materials.require("CINNABAR"));
        assertEquals(Material.SULFUR_SPIKE, Materials.require("SULFUR_SPIKE"));
        assertEquals(Material.POTENT_SULFUR, Materials.require("POTENT_SULFUR"));
    }

    @Test
    void rejectsUnknownMaterials() {
        assertThrows(IllegalArgumentException.class, () -> Materials.require("NOT_A_REAL_BLOCK"));
    }
}
