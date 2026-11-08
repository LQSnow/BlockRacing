package top.lqsnow.blockracing.pools;

import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import java.util.List;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;

class TierSummaryTest {
    @Test
    void everyTierShowsItsActualCountAndOnlyEligibleExamples() throws Exception {
        List<PoolEntry> catalog = BlockCatalog.load(Path.of("src/main/resources/block-pools")).entries();
        for (PoolEntry.Dimension dimension : PoolEntry.Dimension.values()) for (int tier = 1; tier <= 6; tier++) {
            int selectedTier = tier;
            for (boolean copper : List.of(false, true)) {
                List<String> expected = catalog.stream().filter(e -> e.dimension() == dimension && e.tier() == selectedTier)
                        .filter(e -> copper || !e.copperExtra()).map(PoolEntry::id).toList();
                TierSummary summary = TierSummary.from(catalog, dimension, tier, copper);
                assertEquals(expected.size(), summary.count());
                assertEquals(Math.min(3, expected.size()), summary.examples().size());
                assertTrue(expected.containsAll(summary.examples()));
                assertEquals(summary.examples().size(), new HashSet<>(summary.examples()).size());
            }
        }
        assertEquals(List.of("minecraft:dirt", "minecraft:cobblestone", "minecraft:oak_log"),
                TierSummary.from(catalog, PoolEntry.Dimension.OVERWORLD, 1, false).examples());
        assertEquals(List.of("minecraft:dragon_head"), TierSummary.from(catalog, PoolEntry.Dimension.END, 6, false).examples());
        assertTrue(TierSummary.from(catalog, PoolEntry.Dimension.OVERWORLD, 6, true).count()
                > TierSummary.from(catalog, PoolEntry.Dimension.OVERWORLD, 6, false).count());
    }

    @Test
    void emptyTiersDoNotInventExampleBlocks() {
        TierSummary summary = TierSummary.from(List.of(), PoolEntry.Dimension.END, 1, false);
        assertEquals(0, summary.count());
        assertTrue(summary.examples().isEmpty());
    }
}
