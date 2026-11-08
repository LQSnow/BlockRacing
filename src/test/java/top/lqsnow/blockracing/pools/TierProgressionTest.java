package top.lqsnow.blockracing.pools;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class TierProgressionTest {
    private static BlockCatalog catalog() throws Exception {
        return BlockCatalog.load(Path.of("src/main/resources/block-pools"));
    }
    private static int maxTier(int completed, int total) {
        return completed < DifficultyPreset.unlockCount(total, 15) ? 2
                : completed < DifficultyPreset.unlockCount(total, 35) ? 3
                : completed < DifficultyPreset.unlockCount(total, 60) ? 4
                : completed < DifficultyPreset.unlockCount(total, 80) ? 5 : 6;
    }

    @Test void gatesUseActualCompletedCountsAndCeilBoundaries() {
        TierProgression rules = TierProgression.standard();
        for (int total : List.of(50, 51)) {
            for (int tier = 3; tier <= 6; tier++) {
                int threshold = DifficultyPreset.unlockCount(total, List.of(15,35,60,80).get(tier - 3));
                assertEquals(0, rules.multiplier(tier, threshold - 1, total));
                assertTrue(rules.multiplier(tier, threshold, total) > 0);
            }
        }
        assertEquals(8, DifficultyPreset.unlockCount(50, 15));
        assertEquals(18, DifficultyPreset.unlockCount(50, 35));
        assertEquals(30, DifficultyPreset.unlockCount(50, 60));
        assertEquals(40, DifficultyPreset.unlockCount(50, 80));
    }

    @Test void allFivePresetsHaveOnlySimpleOpeningTargetsAcrossFiveHundredSeeds() throws Exception {
        List<PoolEntry> pool = catalog().entries();
        for (var kind : DifficultyPreset.Kind.standards()) for (int seed = 0; seed < 100; seed++) {
            TaskSession match = new TaskSession(DifficultyPreset.defaults(kind), seed % 2 == 0, 50, 3, seed % 3 == 0, seed, pool);
            for (String team : List.of("red", "blue")) {
                assertEquals(4, match.active(team).size());
                for (String id : match.active(team)) {
                    assertTrue(match.find(id).tier() <= 2, () -> kind + " opening target " + id);
                    assertEquals(PoolEntry.Dimension.OVERWORLD, match.find(id).dimension());
                }
            }
        }
    }

    @Test void refillsRollsAndRestoresKeepTierAndDimensionGatesForBothTeams() throws Exception {
        List<PoolEntry> pool = catalog().entries();
        for (var kind : DifficultyPreset.Kind.standards()) for (int seed = 0; seed < 12; seed++) {
            TaskSession match = new TaskSession(DifficultyPreset.defaults(kind), true, 50, 3, seed % 2 == 0, seed, pool);
            for (String team : List.of("red", "blue")) {
                for (int completed = 0; completed < 50; completed++) {
                    if (completed == 0 || completed == 17 || completed == 39) match.roll(team);
                    for (String id : match.active(team)) {
                        PoolEntry entry = match.find(id);
                        assertTrue(entry.tier() <= maxTier(completed, 50), "Premature tier " + entry);
                        assertTrue(match.preset().unlocked(entry, completed, 50));
                    }
                    if (completed == 12 || completed == 29 || completed == 41) {
                        YamlConfiguration yaml = new YamlConfiguration();
                        match.save(yaml.createSection("tasks"));
                        TaskSession restored = TaskSession.load(yaml.getConfigurationSection("tasks"), true);
                        assertEquals(match.preset(), restored.preset());
                        assertEquals(match.active(team), restored.active(team));
                        match = restored;
                    }
                    assertTrue(match.complete(team, match.active(team).getLast()));
                }
                assertEquals(50, match.completed(team));
                assertEquals(50, match.generated(team));
            }
        }
    }

    @Test void missingRulesMigrateForNewGamesButNeverChangeAnOldMatch() throws Exception {
        DifficultyPreset fresh = DifficultyPreset.defaults(DifficultyPreset.Kind.NIGHTMARE);
        YamlConfiguration preset = new YamlConfiguration();
        fresh.save(preset);
        preset.set("tier-progression", null);
        assertTrue(DifficultyPreset.load(preset).tierProgression().enabled());
        assertFalse(DifficultyPreset.loadSnapshot(preset, true).tierProgression().enabled());
        DifficultyPreset old = DifficultyPreset.loadSnapshot(preset, true);
        TaskSession previous = new TaskSession(old, true, 50, 3, true, 77, catalog().entries());
        YamlConfiguration yaml = new YamlConfiguration();
        previous.save(yaml.createSection("tasks"));
        yaml.set("tasks.generation-rules-version", null);
        yaml.set("tasks.preset.tier-progression", null);
        TaskSession restored = TaskSession.load(yaml.getConfigurationSection("tasks"), true);
        assertFalse(restored.preset().tierProgression().enabled());
        while (!previous.active("red").isEmpty()) {
            assertEquals(previous.active("red"), restored.active("red"));
            String target = previous.active("red").getFirst();
            previous.complete("red", target);
            restored.complete("red", target);
        }
        fresh.save(preset);
        TaskSession match = new TaskSession(fresh, true, 50, 3, false, 5, catalog().entries());
        YamlConfiguration newSave = new YamlConfiguration();
        match.save(newSave.createSection("tasks"));
        newSave.set("tasks.preset.tier-progression", null);
        assertThrows(IllegalArgumentException.class, () -> TaskSession.load(newSave.getConfigurationSection("tasks"), true));
    }

    @Test void impossibleCustomOpeningIsRejectedRatherThanBorrowingEasyTiers() throws Exception {
        CustomSettings onlyHigh = new CustomSettings(CustomSettings.Source.TIERS,
                List.of(true,false,false), List.of(32,0,0), List.of(0,0,0));
        List<PoolEntry> pool = catalog().entries();
        assertThrows(IllegalArgumentException.class, () -> new TaskSession(onlyHigh.preset(), true, 10, 3, false, 1, pool));
    }

    @Test void malformedStagesAndRelockingTiersAreRejected() {
        var defaults = TierProgression.standard();
        YamlConfiguration yaml = new YamlConfiguration();
        defaults.save(yaml);
        assertEquals(defaults, TierProgression.load(yaml));
        assertThrows(IllegalArgumentException.class, () -> new TierProgression(true, List.of(
                new TierProgression.Stage(15, List.of(1,1,1,1,1,1)))));
        assertThrows(IllegalArgumentException.class, () -> new TierProgression(true, List.of(
                new TierProgression.Stage(0, List.of(1,1,1,0,0,0)),
                new TierProgression.Stage(20, List.of(1,1,0,1,1,1)),
                new TierProgression.Stage(40, List.of(1,1,1,1,1,1)))));
    }
}
