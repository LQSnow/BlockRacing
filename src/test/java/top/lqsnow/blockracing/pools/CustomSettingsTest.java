package top.lqsnow.blockracing.pools;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static top.lqsnow.blockracing.pools.PoolEntry.Dimension.*;

class CustomSettingsTest {
    private static BlockCatalog catalog() throws Exception {
        return BlockCatalog.load(Path.of("src/main/resources/block-pools"));
    }

    @Test
    void arbitraryTiersAndDimensionTogglesSurviveYamlRoundTrip() throws Exception {
        CustomSettings settings = new CustomSettings(CustomSettings.Source.TIERS,
                List.of(true, false, true), List.of(22, 63, 5), List.of(0, 15, 60));
        YamlConfiguration yaml = new YamlConfiguration();
        settings.save(yaml);
        YamlConfiguration reloaded = new YamlConfiguration();
        reloaded.loadFromString(yaml.saveToString());
        assertEquals(settings, CustomSettings.load(reloaded));
        assertTrue(settings.tierEnabled(OVERWORLD, 2));
        assertTrue(settings.tierEnabled(OVERWORLD, 3));
        assertTrue(settings.tierEnabled(OVERWORLD, 5));
        assertFalse(settings.tierEnabled(OVERWORLD, 1));
        assertFalse(settings.enabled(NETHER));
        assertTrue(settings.toggleDimension(NETHER).enabled(NETHER));
        assertFalse(settings.toggleTier(OVERWORLD, 3).tierEnabled(OVERWORLD, 3));
        assertEquals(100, settings.withThreshold(END, 105).unlockPercent().get(2));
        assertEquals(0, settings.withThreshold(END, -5).unlockPercent().get(2));
    }

    @Test
    void customMatchUsesOnlyIndependentSelectionsAndRestoresItsOwnRules() throws Exception {
        CustomSettings settings = new CustomSettings(CustomSettings.Source.TIERS,
                List.of(true, false, true), List.of(22, 63, 5), List.of(0, 25, 70));
        TaskSession match = new TaskSession(settings.preset(), true, 50, 3, true, 7, catalog().entries());
        assertTrue(match.pool().stream().allMatch(e -> settings.preset().allows(e)));
        assertTrue(match.pool().stream().noneMatch(e -> e.dimension() == NETHER));
        assertTrue(match.pool().stream().filter(e -> e.dimension() == OVERWORLD)
                .allMatch(e -> List.of(2, 3, 5).contains(e.tier())));
        for (int i = 0; i < 20; i++) match.complete("red", match.active("red").getFirst());
        assertTrue(match.roll("red"));
        YamlConfiguration yaml = new YamlConfiguration();
        match.save(yaml.createSection("tasks"));
        TaskSession restored = TaskSession.load(yaml.getConfigurationSection("tasks"), true);
        assertEquals(settings.preset(), restored.preset());
        assertEquals(match.pool(), restored.pool());
        for (String team : List.of("red", "blue")) {
            while (!match.active(team).isEmpty()) {
                assertEquals(match.active(team), restored.active(team));
                String id = match.active(team).getFirst();
                assertTrue(settings.preset().unlocked(match.find(id), match.completed(team), 50));
                match.complete(team, id);
                restored.complete(team, id);
            }
            assertEquals(50, restored.completed(team));
        }
    }

    @Test
    void emptySelectionsAndOnlyLockedDimensionsAreRejectedBeforeStarting() throws Exception {
        List<PoolEntry> pool = catalog().entries();
        CustomSettings empty = new CustomSettings(CustomSettings.Source.TIERS,
                List.of(false, false, false), List.of(63, 63, 63), List.of(0, 25, 70));
        assertThrows(IllegalArgumentException.class, () -> new TaskSession(empty.preset(), true, 10, 3, false, 1, pool));
        CustomSettings nether = empty.toggleDimension(NETHER);
        assertThrows(IllegalArgumentException.class, () -> new TaskSession(nether.preset(), true, 10, 3, false, 1, pool));
        assertDoesNotThrow(() -> new TaskSession(nether.withThreshold(NETHER, 0).preset(), true, 10, 3, false, 1, pool));
        CustomSettings noTiers = new CustomSettings(CustomSettings.Source.TIERS,
                List.of(true, true, true), List.of(0, 0, 0), List.of(0, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new TaskSession(noTiers.preset(), true, 10, 3, false, 1, pool));
    }

    @Test
    void localPoolUsesExactlyItsIdsAndClassificationWhilePreservingCopperAndTags() throws Exception {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("end.tier-6", List.of("DIRT", "minecraft:waxed_copper_block"));
        LocalBlockPool.Configuration local = LocalBlockPool.read(yaml, catalog());
        List<PoolEntry> entries = local.entries();
        assertEquals(List.of("minecraft:dirt", "minecraft:waxed_copper_block"), entries.stream().map(PoolEntry::id).toList());
        assertTrue(entries.stream().allMatch(e -> e.dimension() == END && e.tier() == 6 && e.acquisition() != null));
        assertTrue(entries.get(1).copperExtra());
        // Local mode always includes explicitly listed copper variants.
        TaskSession match = new TaskSession(local.preset(), true, 2, 3, true, 1, entries);
        assertEquals(java.util.Set.of("minecraft:dirt", "minecraft:waxed_copper_block"), new java.util.HashSet<>(match.active("red")));
        YamlConfiguration saved = new YamlConfiguration();
        match.save(saved.createSection("tasks"));
        yaml.set("end.tier-6", List.of("minecraft:stone"));
        TaskSession restored = TaskSession.load(saved.getConfigurationSection("tasks"), true);
        assertEquals(match.pool(), restored.pool(), "Editing the local file must not change an ongoing match");
    }

    @Test
    void malformedOrUnsafeLocalBlocksAreRejectedWithNoPartialPool() throws Exception {
        BlockCatalog catalog = catalog();
        for (List<?> ids : List.of(List.of("minecraft:bedrock"), List.of("minecraft:diamond"),
                List.of("minecraft:missing_block"), List.of("minecraft:dirt", "DIRT"), List.of(42))) {
            YamlConfiguration yaml = new YamlConfiguration();
            yaml.set("overworld.tier-1", ids);
            assertThrows(IllegalArgumentException.class, () -> LocalBlockPool.load(yaml, catalog));
        }
        YamlConfiguration duplicate = new YamlConfiguration();
        duplicate.set("overworld.tier-1", List.of("minecraft:dirt"));
        duplicate.set("nether.tier-2", List.of("minecraft:dirt"));
        assertThrows(IllegalArgumentException.class, () -> LocalBlockPool.load(duplicate, catalog));
        YamlConfiguration typo = new YamlConfiguration();
        typo.set("overwrold.tier-1", List.of("minecraft:dirt"));
        assertThrows(IllegalArgumentException.class, () -> LocalBlockPool.load(typo, catalog));
        assertThrows(IllegalArgumentException.class, () -> LocalBlockPool.load(new YamlConfiguration(), catalog));
    }

    @Test
    void bundledTemplatesMatchDefaultsAndTheLocalExampleCanStartAFiftyTargetMatch() throws Exception {
        YamlConfiguration settings = new YamlConfiguration();
        settings.load(Path.of("src/main/resources/custom/settings.yml").toFile());
        assertEquals(CustomSettings.defaults(), CustomSettings.load(settings));
        YamlConfiguration blocks = new YamlConfiguration();
        blocks.load(Path.of("src/main/resources/custom/blocks.yml").toFile());
        LocalBlockPool.Configuration local = LocalBlockPool.read(blocks, catalog());
        List<PoolEntry> pool = local.entries();
        assertTrue(pool.size() >= 50);
        assertDoesNotThrow(() -> new TaskSession(local.preset(), true, 50, 3, false, 1, pool));
    }

    @Test
    void localThresholdsAreIndependentFromGuiAndPersistInTheMatchSnapshot() throws Exception {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.load(Path.of("src/main/resources/custom/blocks.yml").toFile());
        yaml.set("nether.unlock-percent", 40);
        yaml.set("nether.tier-1", List.of("minecraft:netherrack"));
        yaml.set("nether.tier-2", List.of("minecraft:blackstone", "minecraft:glowstone"));
        yaml.set("end.unlock-percent", 85);
        LocalBlockPool.Configuration local = LocalBlockPool.read(yaml, catalog());
        assertEquals(List.of(0, 40, 85), local.preset().unlockPercent());
        CustomSettings gui = new CustomSettings(CustomSettings.Source.LOCAL,
                List.of(false, false, false), List.of(0, 0, 0), List.of(100, 100, 100));
        assertThrows(IllegalStateException.class, gui::preset, "Local rules must never fall back to GUI thresholds");
        TaskSession match = new TaskSession(local.preset(), true, 10, 3, false, 45, local.entries());
        for (int completed = 0; completed < 4; completed++) {
            assertTrue(match.active("red").stream().allMatch(id -> match.find(id).dimension() == OVERWORLD));
            match.complete("red", match.active("red").getFirst());
        }
        assertTrue(match.roll("red"));
        YamlConfiguration saved = new YamlConfiguration();
        match.save(saved.createSection("tasks"));
        yaml.set("nether.unlock-percent", 0);
        TaskSession restored = TaskSession.load(saved.getConfigurationSection("tasks"), true);
        assertEquals(List.of(0, 40, 85), restored.preset().unlockPercent());
        assertTrue(restored.copperExtras());
        while (!restored.active("red").isEmpty()) restored.complete("red", restored.active("red").getFirst());
        assertEquals(10, restored.completed("red"));
    }

    @Test
    void localThresholdsValidateTypesAndCannotDeadlockTheInitialTargets() throws Exception {
        BlockCatalog catalog = catalog();
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("nether.tier-1", List.of("minecraft:netherrack"));
        for (Object invalid : List.of(-1, 101, 25.5, "25", false)) {
            yaml.set("nether.unlock-percent", invalid);
            assertThrows(IllegalArgumentException.class, () -> LocalBlockPool.read(yaml, catalog));
        }
        yaml.set("nether.unlock-percent", 25);
        var locked = LocalBlockPool.read(yaml, catalog);
        assertThrows(IllegalArgumentException.class, () -> new TaskSession(locked.preset(), true, 1, 3, false, 1, locked.entries()));
        yaml.set("nether.unlock-percent", null);
        var legacyFile = LocalBlockPool.read(yaml, catalog);
        assertEquals(List.of(0, 0, 0), legacyFile.unlockPercent(), "Missing local thresholds default to 0, not GUI settings");
        assertDoesNotThrow(() -> new TaskSession(legacyFile.preset(), true, 1, 3, false, 1, legacyFile.entries()));
    }

    @Test
    void invalidCustomSelectionsAndTamperedSnapshotsAreNotSilentlyDefaulted() throws Exception {
        YamlConfiguration yaml = new YamlConfiguration();
        CustomSettings.defaults().save(yaml);
        yaml.set("overworld.tiers", List.of(2, 2));
        assertThrows(IllegalArgumentException.class, () -> CustomSettings.load(yaml));
        TaskSession match = new TaskSession(CustomSettings.defaults().preset(), false, 50, 3, false, 1, catalog().entries());
        YamlConfiguration saved = new YamlConfiguration();
        match.save(saved.createSection("tasks"));
        saved.set("tasks.preset.tier-masks", null);
        assertThrows(IllegalArgumentException.class, () -> TaskSession.load(saved.getConfigurationSection("tasks")));
    }
}
