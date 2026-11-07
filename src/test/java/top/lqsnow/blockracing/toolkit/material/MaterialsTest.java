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
        assertThrows(IllegalArgumentException.class, () -> Materials.require("other:oak_log"));
        assertThrows(IllegalArgumentException.class, () -> Materials.require("minecraft:not_a_real_block"));
        assertThrows(IllegalArgumentException.class, () -> Materials.require("LEGACY_STONE"));
        assertThrows(IllegalArgumentException.class, () -> Materials.require(""));
        assertThrows(IllegalArgumentException.class, () -> Materials.require(null));
    }

    @Test
    void resolvesCanonicalKeysAndLegacyTextFormatWithoutAliases() {
        assertEquals(Material.OAK_LOG, Materials.require("minecraft:oak_log"));
        assertEquals(Material.OAK_LOG, Materials.require("OAK_LOG"));
        assertEquals(Material.SULFUR, Materials.require("minecraft:sulfur"));
        assertEquals(Material.SULFUR, Materials.require("SULFUR"));
    }
}
