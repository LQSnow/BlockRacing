package top.lqsnow.blockracing.pools;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class PacingTest {
    private static final Path ROOT = Path.of("src/main/resources/block-pools");
    @TempDir Path temporaryDirectory;

    private static BlockCatalog catalog() throws Exception { return BlockCatalog.load(ROOT); }

    private static List<PoolEntry> controlledPool() throws Exception {
        List<PoolEntry> source = catalog().entries().subList(0, 300);
        List<PoolEntry> entries = new ArrayList<>();
        for (int i = 0; i < source.size(); i++) {
            entries.add(new PoolEntry(source.get(i).id(), PoolEntry.Dimension.OVERWORLD, 1, false,
                    PoolEntry.Acquisition.values()[i / 100]));
        }
        return entries;
    }

    @Test
    void everyIncludedMaterialHasExactlyOneReviewedTagWithAnAcquisitionReason() throws Exception {
        Set<String> seen = new HashSet<>();
        BlockCatalog catalog = catalog();
        List<String> lines = Files.readAllLines(ROOT.resolve("acquisition-tags.tsv"));
        for (String line : lines.subList(1, lines.size())) {
            String[] row = line.split("\t", -1);
            assertEquals(3, row.length);
            assertTrue(seen.add(row[0]));
            assertFalse(row[2].isBlank());
            assertEquals(PoolEntry.Acquisition.valueOf(row[1]), catalog.find(row[0]).acquisition());
        }
        assertEquals(catalog.entries().stream().map(PoolEntry::id).collect(java.util.stream.Collectors.toSet()), seen);
        assertEquals(1094, seen.size());
        assertEquals(PoolEntry.Acquisition.CRAFT, catalog.find("minecraft:oak_planks").acquisition());
        assertEquals(PoolEntry.Acquisition.EXPLORE, catalog.find("minecraft:cherry_planks").acquisition());
        assertEquals(PoolEntry.Acquisition.GATHER, catalog.find("minecraft:copper_ore").acquisition());
        assertEquals(PoolEntry.Acquisition.CRAFT, catalog.find("minecraft:netherite_block").acquisition());
        assertEquals(PoolEntry.Acquisition.EXPLORE, catalog.find("minecraft:waxed_oxidized_copper").acquisition());
    }

    @Test
    void phasesUseActualCompletedProgressWithCeilBoundaries() {
        PacingProfile profile = PacingProfile.standard();
        assertEquals(List.of(45, 40, 15), profile.weightsAt(0, 50));
        assertEquals(profile.early(), profile.weightsAt(9, 50));
        assertEquals(List.of(25, 30, 45), profile.weightsAt(10, 50));
        assertEquals(profile.middle(), profile.weightsAt(39, 50));
        assertEquals(List.of(30, 40, 30), profile.weightsAt(40, 50));
        assertEquals(profile.early(), profile.weightsAt(10, 51));
        assertEquals(profile.middle(), profile.weightsAt(11, 51));
        assertEquals(profile.middle(), profile.weightsAt(40, 51));
        assertEquals(profile.late(), profile.weightsAt(41, 51));
    }

    @Test
    void weightedSamplingFollowsTheThreePhaseTrendsWithoutFixedQuotas() throws Exception {
        List<PoolEntry> pool = controlledPool();
        int[][] counts = new int[3][3];
        Random seeds = new Random(574);
        for (int trial = 0; trial < 300; trial++) {
            TaskSession match = new TaskSession(DifficultyPreset.defaults(DifficultyPreset.Kind.CASUAL), false,
                    50, 3, false, seeds.nextLong(), pool);
            for (String id : match.seen("red")) counts[0][match.find(id).acquisition().ordinal()]++;
            for (int completed = 1; completed <= 50; completed++) {
                int generated = match.generated("red");
                assertTrue(match.complete("red", match.active("red").getFirst()));
                if (generated < 50) {
                    int phase = completed < 10 ? 0 : completed < 40 ? 1 : 2;
                    String id = match.seen("red").getLast();
                    counts[phase][match.find(id).acquisition().ordinal()]++;
                }
            }
        }
        double[][] expected = {{.45,.40,.15},{.25,.30,.45},{.30,.40,.30}};
        for (int phase = 0; phase < 3; phase++) {
            int total = Arrays.stream(counts[phase]).sum();
            for (int category = 0; category < 3; category++) {
                assertEquals(expected[phase][category], (double) counts[phase][category] / total, .04,
                        "Phase " + phase + " category " + category);
            }
        }
    }

    @Test
    void differentVisibleSlotCompositionsDoNotChangeTheSequence() throws Exception {
        var preset = DifficultyPreset.defaults(DifficultyPreset.Kind.NORMAL);
        TaskSession first = new TaskSession(preset, false, 50, 3, true, 831, catalog().entries());
        TaskSession last = new TaskSession(preset, false, 50, 3, true, 831, catalog().entries());
        boolean differentSlotsObserved = false;
        for (int i = 0; i < 50; i++) {
            first.complete("red", first.active("red").getFirst());
            last.complete("red", last.active("red").getLast());
            differentSlotsObserved |= !first.active("red").equals(last.active("red"));
            assertEquals(first.seen("red"), last.seen("red"));
        }
        assertTrue(differentSlotsObserved);
    }

    @Test
    void absentCategoriesAreSkippedWithoutBorrowingUnallowedDimensionsOrTiers() throws Exception {
        List<PoolEntry> onlyCraft = controlledPool().stream().filter(e -> e.acquisition() == PoolEntry.Acquisition.CRAFT).toList();
        TaskSession match = new TaskSession(DifficultyPreset.defaults(DifficultyPreset.Kind.CASUAL), false, 50, 3, false, 66, onlyCraft);
        while (!match.active("red").isEmpty()) {
            for (String id : match.active("red")) {
                PoolEntry entry = match.find(id);
                assertEquals(PoolEntry.Acquisition.CRAFT, entry.acquisition());
                assertEquals(PoolEntry.Dimension.OVERWORLD, entry.dimension());
                assertEquals(1, entry.tier());
            }
            if (match.completed("red") == 20) assertTrue(match.roll("red"));
            match.complete("red", match.active("red").getFirst());
        }
        assertEquals(50, match.completed("red"));
    }

    @Test
    void newSnapshotsKeepTheirTagsAndPacingRulesAndRejectMissingRules() throws Exception {
        var normal = DifficultyPreset.defaults(DifficultyPreset.Kind.NORMAL);
        var tuned = new PacingProfile(true, 30, 70, List.of(50,35,15), List.of(20,30,50), List.of(35,35,30));
        var preset = new DifficultyPreset(normal.kind(), normal.maxTiers(), normal.unlockPercent(), normal.tierWeights(), normal.dimensionWeights(), tuned);
        TaskSession match = new TaskSession(preset, false, 50, 3, true, 73, catalog().entries());
        for (int i = 0; i < 14; i++) match.complete("red", match.active("red").getFirst());
        assertTrue(match.roll("red"));
        YamlConfiguration yaml = new YamlConfiguration();
        match.save(yaml.createSection("tasks"));
        YamlConfiguration decoded = new YamlConfiguration();
        decoded.loadFromString(yaml.saveToString());
        TaskSession restored = TaskSession.load(decoded.getConfigurationSection("tasks"), true);
        assertEquals(tuned, restored.preset().pacing());
        assertEquals(match.pool(), restored.pool());
        while (!match.active("red").isEmpty()) {
            String id = match.active("red").getFirst();
            assertTrue(match.complete("red", id));
            assertTrue(restored.complete("red", id));
            assertEquals(match.active("red"), restored.active("red"));
        }
        decoded.set("tasks.preset.pacing", null);
        assertThrows(IllegalArgumentException.class, () -> TaskSession.load(decoded.getConfigurationSection("tasks"), true));
    }

    @Test
    void capturedPrePacingMatchContinuesItsExactOriginalSequenceIncludingARollBranch() throws Exception {
        // Fixture and continuation frames were exported from the real pre-pacing plugin JAR.
        YamlConfiguration fixture = new YamlConfiguration();
        try (var stream = Objects.requireNonNull(getClass().getResourceAsStream("/legacy-task-session-v2.yml"));
             var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) { fixture.load(reader); }
        TaskSession restored = TaskSession.load(fixture.getConfigurationSection("tasks"));
        assertFalse(restored.preset().pacing().enabled());
        assertTrue(restored.pool().stream().allMatch(e -> e.acquisition() == null));
        assertEquals(1, restored.rolls("red"));
        for (String team : List.of("red", "blue")) {
            for (String frame : fixture.getStringList("expected." + team)) {
                assertEquals(frame, String.join(",", restored.active(team)));
                if (!restored.active(team).isEmpty()) assertTrue(restored.complete(team, restored.active(team).getFirst()));
            }
            assertEquals(50, restored.completed(team));
        }
        YamlConfiguration upgraded = new YamlConfiguration();
        restored.save(upgraded.createSection("tasks"));
        TaskSession roundTrip = TaskSession.load(upgraded.getConfigurationSection("tasks"), true);
        assertFalse(roundTrip.preset().pacing().enabled());
        assertEquals(50, roundTrip.completed("red"));
        assertEquals(50, roundTrip.completed("blue"));
    }

    @Test
    void savedConfigurationStaysFrozenWhileTheLiveMatchContinues() throws Exception {
        TaskSession match = new TaskSession(DifficultyPreset.defaults(DifficultyPreset.Kind.NORMAL), false, 50, 3, true, 121, catalog().entries());
        List<String> initial = match.active("red");
        YamlConfiguration saved = new YamlConfiguration();
        match.save(saved.createSection("tasks"));
        for (int i = 0; i < 12; i++) match.complete("red", match.active("red").getFirst());
        assertTrue(match.roll("red"));
        TaskSession restored = TaskSession.load(saved.getConfigurationSection("tasks"), true);
        assertEquals(initial, restored.active("red"));
        assertEquals(initial, restored.seen("red"));
        assertEquals(0, restored.completed("red"));
        assertEquals(0, restored.rolls("red"));
        assertEquals(4, restored.generated("red"));
    }

    @Test
    void badTagCoverageAndInvalidPacingSettingsFailExplicitly() throws Exception {
        try (var paths = Files.walk(ROOT)) {
            for (Path file : paths.toList()) {
                Path target = temporaryDirectory.resolve(ROOT.relativize(file));
                if (Files.isDirectory(file)) Files.createDirectories(target); else Files.copy(file, target);
            }
        }
        Path tagFile = temporaryDirectory.resolve("acquisition-tags.tsv");
        List<String> lines = new ArrayList<>(Files.readAllLines(tagFile));
        lines.remove(1);
        Files.write(tagFile, lines);
        assertThrows(java.io.IOException.class, () -> BlockCatalog.load(temporaryDirectory));
        assertThrows(IllegalArgumentException.class, () -> new PacingProfile(true, 20, 80, List.of(0,50,50), List.of(20,30,50), List.of(30,40,30)));
        assertThrows(IllegalArgumentException.class, () -> new PacingProfile(true, 80, 20, List.of(45,40,15), List.of(20,30,50), List.of(30,40,30)));
        YamlConfiguration oldPreset = new YamlConfiguration();
        DifficultyPreset.defaults(DifficultyPreset.Kind.NORMAL).save(oldPreset);
        oldPreset.set("pacing", null);
        assertTrue(DifficultyPreset.load(oldPreset).pacing().enabled());
        assertFalse(DifficultyPreset.loadSnapshot(oldPreset, false).pacing().enabled());
    }
}
