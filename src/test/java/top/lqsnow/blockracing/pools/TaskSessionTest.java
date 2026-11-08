package top.lqsnow.blockracing.pools;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Files;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class TaskSessionTest {
    @org.junit.jupiter.api.io.TempDir Path temporaryDirectory;
    private static BlockCatalog catalog() throws Exception {
        return BlockCatalog.load(Path.of("src/main/resources/block-pools"));
    }

    private static TaskSession session(DifficultyPreset.Kind kind, boolean copper, boolean racing, long seed) throws Exception {
        return new TaskSession(DifficultyPreset.defaults(kind), copper, 50, 3, racing, seed, catalog().entries());
    }

    @Test
    void fivePresetFilesMatchTheAgreedRangesThresholdsAndWeights() throws Exception {
        int[][] caps = {{2,0,0},{2,2,0},{4,3,0},{5,5,5},{6,6,6}};
        int[][] unlocks = {{0,100,100},{0,50,100},{0,25,100},{0,25,70},{0,20,65}};
        int[][] weights = {{60,40,0,0,0,0},{45,55,0,0,0,0},{15,35,35,15,0,0},{10,20,30,25,15,0},{5,15,20,25,20,15}};
        for (var kind : DifficultyPreset.Kind.standards()) {
            YamlConfiguration yaml = new YamlConfiguration();
            yaml.load(Path.of("src/main/resources/presets/" + kind.id() + ".yml").toFile());
            DifficultyPreset preset = DifficultyPreset.load(yaml);
            int i = kind.ordinal();
            assertEquals(Arrays.stream(caps[i]).boxed().toList(), preset.maxTiers());
            assertEquals(Arrays.stream(unlocks[i]).boxed().toList(), preset.unlockPercent());
            assertEquals(Arrays.stream(weights[i]).boxed().toList(), preset.tierWeights());
            assertEquals(DifficultyPreset.defaults(kind), preset);
        }
    }

    @Test
    void copperToggleFiltersOnlyTheExplicitlyTaggedExtrasWithinEachPreset() throws Exception {
        assertEquals(106, catalog().entries().stream().filter(PoolEntry::copperExtra).count());
        for (var kind : DifficultyPreset.Kind.standards()) {
            TaskSession off = session(kind, false, false, 1);
            TaskSession on = session(kind, true, false, 1);
            assertTrue(off.pool().stream().noneMatch(PoolEntry::copperExtra));
            assertEquals(on.pool().stream().filter(e -> !e.copperExtra()).toList(), off.pool());
            assertTrue(on.pool().stream().allMatch(on.preset()::allows));
        }
        TaskSession nightmare = session(DifficultyPreset.Kind.NIGHTMARE, false, false, 1);
        assertNotNull(nightmare.find("minecraft:copper_block"));
        assertNotNull(nightmare.find("minecraft:deepslate_copper_ore"));
        assertNotNull(nightmare.find("minecraft:copper_door"));
        assertNull(nightmare.find("minecraft:waxed_copper_block"));
        assertNull(nightmare.find("minecraft:copper_golem_statue"));
    }

    @Test
    void independentCopperListProtectsEarlierMetadataAndPresetReportMatchesRuntime() throws Exception {
        Path source = Path.of("src/main/resources/block-pools");
        try (var paths = Files.walk(source)) {
            for (Path file : paths.toList()) {
                Path target = temporaryDirectory.resolve(source.relativize(file));
                if (Files.isDirectory(file)) Files.createDirectories(target); else Files.copy(file, target);
            }
        }
        Path metadata = temporaryDirectory.resolve("material-metadata.tsv");
        List<String> lines = new ArrayList<>(Files.readAllLines(metadata));
        for (int i = 1; i < lines.size(); i++) {
            String[] fields = lines.get(i).split("\t", -1);
            fields[6] = Arrays.stream(fields[6].split(";")).filter(s -> !s.startsWith("copper_")).collect(java.util.stream.Collectors.joining(";"));
            lines.set(i, String.join("\t", fields));
        }
        Files.write(metadata, lines);
        assertEquals(106, BlockCatalog.load(temporaryDirectory).entries().stream().filter(PoolEntry::copperExtra).count());
        List<String> summary = Files.readAllLines(Path.of("docs/block-library/preset-summary.csv"));
        for (String row : summary.subList(1, summary.size())) {
            String[] fields = row.replace("\"", "").split(",");
            var kind = DifficultyPreset.Kind.parse(fields[0]);
            assertEquals(Integer.parseInt(fields[2]), session(kind, false, false, 1).pool().size());
            assertEquals(Integer.parseInt(fields[3]), session(kind, true, false, 1).pool().size());
        }
    }

    @Test
    void everyActualRefillAndRollHonorsCompletedProgressAcrossAllFivePresets() throws Exception {
        for (var kind : DifficultyPreset.Kind.standards()) for (long seed = 0; seed < 8; seed++) {
            TaskSession match = session(kind, true, false, seed);
            Set<String> completed = new HashSet<>();
            Random random = new Random(seed);
            for (int count = 0; count < 50; count++) {
                assertEquals(count, match.completed("red"));
                List<String> active = match.active("red");
                assertEquals(Math.min(4, 50 - count), active.size());
                assertEquals(active.size(), new HashSet<>(active).size());
                for (String id : active) {
                    assertFalse(completed.contains(id));
                    assertTrue(match.preset().unlocked(match.find(id), count, 50), () -> kind + " premature " + id);
                }
                if (count == 10 || count == 25 || count == 35) {
                    List<String> old = match.seen("red");
                    if (match.roll("red")) {
                        assertTrue(Collections.disjoint(old, match.active("red")));
                        for (String id : match.active("red")) assertTrue(match.preset().unlocked(match.find(id), count, 50));
                    }
                }
                String target = match.active("red").get(random.nextInt(match.active("red").size()));
                assertTrue(completed.add(target));
                assertTrue(match.complete("red", target));
                assertFalse(match.complete("red", target));
            }
            assertEquals(50, match.completed("red"));
            assertEquals(50, match.generated("red"));
            assertTrue(match.active("red").isEmpty());
        }
    }

    @Test
    void easyNetherWaitsUntilTwentyFiveAndHardEndUntilThirtyFiveCompletions() throws Exception {
        assertEquals(25, DifficultyPreset.unlockCount(50, 50));
        assertEquals(26, DifficultyPreset.unlockCount(51, 50));
        assertEquals(13, DifficultyPreset.unlockCount(50, 25));
        assertEquals(35, DifficultyPreset.unlockCount(50, 70));
        for (var kind : List.of(DifficultyPreset.Kind.EASY, DifficultyPreset.Kind.HARD)) {
            TaskSession match = session(kind, false, false, 51);
            PoolEntry.Dimension locked = kind == DifficultyPreset.Kind.EASY ? PoolEntry.Dimension.NETHER : PoolEntry.Dimension.END;
            int gate = kind == DifficultyPreset.Kind.EASY ? 25 : 35;
            for (int c = 0; c < gate; c++) {
                for (String id : match.active("red")) assertNotEquals(locked, match.find(id).dimension());
                if (c == gate - 1) break;
                match.complete("red", match.active("red").getFirst());
            }
        }
    }

    @Test
    void racingUsesTheSameSequenceDespiteDifferentTeamTimingAndCompletionOrder() throws Exception {
        TaskSession match = session(DifficultyPreset.Kind.HARD, true, true, 1234);
        assertEquals(match.active("red"), match.active("blue"));
        for (int c = 0; c < 50; c++) {
            List<String> active = match.active("red");
            assertTrue(match.complete("red", active.getLast()));
        }
        for (int c = 0; c < 50; c++) assertTrue(match.complete("blue", match.active("blue").getFirst()));
        assertEquals(match.seen("red"), match.seen("blue"));
        assertEquals(50, match.completed("red"));
        assertEquals(50, match.completed("blue"));
    }

    @Test
    void racingRollBranchesAndRestoresWithoutDuplicatesOrChangingTheOpponent() throws Exception {
        TaskSession original = session(DifficultyPreset.Kind.NIGHTMARE, true, true, 7);
        List<String> opponent = original.active("blue");
        assertTrue(original.roll("red"));
        assertEquals(opponent, original.active("blue"));
        for (int c = 0; c < 19; c++) original.complete("red", original.active("red").getLast());
        YamlConfiguration yaml = new YamlConfiguration();
        original.save(yaml.createSection("tasks"));
        YamlConfiguration reloaded = new YamlConfiguration();
        reloaded.loadFromString(yaml.saveToString());
        TaskSession restored = TaskSession.load(reloaded.getConfigurationSection("tasks"));
        assertEquals(original.preset(), restored.preset());
        assertTrue(restored.copperExtras());
        assertEquals(original.pool(), restored.pool());
        for (String team : List.of("red", "blue")) {
            assertEquals(original.active(team), restored.active(team));
            assertEquals(original.seen(team), restored.seen(team));
            while (!original.active(team).isEmpty()) {
                String id = original.active(team).getFirst();
                assertTrue(original.complete(team, id));
                assertTrue(restored.complete(team, id));
                assertEquals(original.active(team), restored.active(team));
            }
            assertEquals(50, restored.completed(team));
            assertEquals(original.seen(team), restored.seen(team));
        }
    }

    @Test
    void exhaustedRollIsAtomicAndDoesNotConsumeALimitedUseOrBreakFutureCapacity() throws Exception {
        List<PoolEntry> exact = catalog().entries().stream().filter(e -> e.dimension() == PoolEntry.Dimension.OVERWORLD && e.tier() <= 2).limit(50).toList();
        TaskSession match = new TaskSession(DifficultyPreset.defaults(DifficultyPreset.Kind.CASUAL), false, 50, 3, false, 1, exact);
        List<String> active = match.active("red"), seen = match.seen("red");
        assertFalse(match.roll("red"));
        assertEquals(active, match.active("red"));
        assertEquals(seen, match.seen("red"));
        assertEquals(0, match.rolls("red"));
        assertEquals(3, match.rollsRemaining("red"));
        while (!match.active("red").isEmpty()) assertTrue(match.complete("red", match.active("red").getFirst()));
        assertEquals(50, match.completed("red"));
    }

    @Test
    void emptyPoolsAndInsufficientPreUnlockCapacityFailBeforeStarting() throws Exception {
        var preset = DifficultyPreset.defaults(DifficultyPreset.Kind.EASY);
        assertThrows(IllegalArgumentException.class, () -> new TaskSession(preset, false, 50, 3, false, 1, List.of()));
        List<PoolEntry> onlyNether = catalog().entries().stream().filter(e -> e.dimension() == PoolEntry.Dimension.NETHER).toList();
        assertThrows(IllegalArgumentException.class, () -> new TaskSession(preset, false, 50, 3, false, 1, onlyNether));
        List<PoolEntry> shortOverworld = new ArrayList<>(catalog().entries().stream()
                .filter(e -> e.dimension() == PoolEntry.Dimension.OVERWORLD && e.tier() <= 2).limit(27).toList());
        shortOverworld.addAll(onlyNether);
        assertThrows(IllegalArgumentException.class, () -> new TaskSession(preset, false, 50, 3, false, 1, shortOverworld));
    }

    @Test
    void changingStandardRangesAndCorruptingSavedCountersAreRejected() throws Exception {
        var preset = DifficultyPreset.defaults(DifficultyPreset.Kind.NORMAL);
        assertThrows(IllegalArgumentException.class, () -> new DifficultyPreset(preset.kind(), List.of(6,6,6),
                preset.unlockPercent(), preset.tierWeights(), preset.dimensionWeights()));
        TaskSession match = session(DifficultyPreset.Kind.NORMAL, false, false, 19);
        YamlConfiguration yaml = new YamlConfiguration();
        match.save(yaml.createSection("tasks"));
        yaml.set("tasks.red.generated", 50);
        assertThrows(IllegalArgumentException.class, () -> TaskSession.load(yaml.getConfigurationSection("tasks")));
    }

    @Test
    void oddAndSmallTargetBudgetsCompleteExactlyAndRollLimitsStayFixed() throws Exception {
        for (int count : List.of(1, 3, 4, 5, 10, 51, 100)) {
            TaskSession match = new TaskSession(DifficultyPreset.defaults(DifficultyPreset.Kind.HARD), true,
                    count, 1, true, 543, catalog().entries());
            assertTrue(match.roll("red"));
            assertEquals(0, match.rollsRemaining("red"));
            assertFalse(match.roll("red"));
            for (String team : List.of("red", "blue")) {
                while (!match.active(team).isEmpty()) match.complete(team, match.active(team).getLast());
                assertEquals(count, match.completed(team));
                assertEquals(count, match.generated(team));
            }
        }
    }

    @Test
    void truncatedSequenceAndEarlyLockedActiveTargetsCannotBeRestored() throws Exception {
        TaskSession match = session(DifficultyPreset.Kind.HARD, true, true, 42);
        YamlConfiguration yaml = new YamlConfiguration();
        match.save(yaml.createSection("tasks"));
        yaml.set("tasks.sequences.red", List.of());
        YamlConfiguration truncated = yaml;
        assertThrows(IllegalArgumentException.class, () -> TaskSession.load(truncated.getConfigurationSection("tasks")));
        yaml = new YamlConfiguration();
        match.save(yaml.createSection("tasks"));
        List<String> active = new ArrayList<>(match.active("red"));
        String locked = match.pool().stream().filter(e -> e.dimension() == PoolEntry.Dimension.END).findFirst().orElseThrow().id();
        active.set(0, locked);
        yaml.set("tasks.red.active", active);
        List<String> seen = new ArrayList<>(match.seen("red"));
        seen.add(locked);
        yaml.set("tasks.red.seen", seen);
        YamlConfiguration corrupt = yaml;
        assertThrows(IllegalArgumentException.class, () -> TaskSession.load(corrupt.getConfigurationSection("tasks")));
    }

    @Test
    void missingSeedOrFractionalConfigurationCannotSilentlyChangeSavedRules() throws Exception {
        TaskSession match = session(DifficultyPreset.Kind.NORMAL, false, false, 77);
        YamlConfiguration yaml = new YamlConfiguration();
        match.save(yaml.createSection("tasks"));
        yaml.set("tasks.seed", null);
        assertThrows(IllegalArgumentException.class, () -> TaskSession.load(yaml.getConfigurationSection("tasks")));
        YamlConfiguration preset = new YamlConfiguration();
        DifficultyPreset.defaults(DifficultyPreset.Kind.NORMAL).save(preset);
        preset.set("unlock-percent", List.of(0, 25.5, 100));
        assertThrows(IllegalArgumentException.class, () -> DifficultyPreset.load(preset));
    }
}
