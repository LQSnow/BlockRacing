package top.lqsnow.blockracing;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;
import top.lqsnow.blockracing.toolkit.material.Materials;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/** Data contract tests. Actual isBlock/isItem checks run in the real Paper registry exporter. */
class BlockPoolDataTest {
    private static final Path POOLS = Path.of("src/main/resources/block-pools");
    private static final Path SNAPSHOT = Path.of("src/test/resources/catalogue/materials-api-26.3.csv");

    private static List<String[]> rows(Path path, String delimiter) throws Exception {
        return Files.readAllLines(path, StandardCharsets.UTF_8).stream().skip(1)
                .map(line -> line.split(delimiter, -1)).toList();
    }

    private static Map<String, String[]> metadata() throws Exception {
        Map<String, String[]> result = new HashMap<>();
        for (String[] row : rows(POOLS.resolve("material-metadata.tsv"), "\t")) {
            assertEquals(8, row.length);
            assertNull(result.put(row[0], row), () -> "Duplicate review " + row[0]);
            assertTrue(row[0].matches("minecraft:[a-z0-9_]+"));
            assertFalse(row[5].isBlank(), () -> "Missing acquisition/exclusion reason " + row[0]);
            assertFalse(row[7].isBlank(), () -> "Missing evidence " + row[0]);
        }
        return result;
    }

    @Test
    void snapshotCoversTheExactCompiledApiMaterialSet() throws Exception {
        Set<String> actual = Arrays.stream(Material.values()).filter(m -> !m.isLegacy())
                .map(m -> m.getKey().toString()).collect(Collectors.toSet());
        List<String[]> snapshot = rows(SNAPSHOT, ",");
        Set<String> stored = snapshot.stream().map(row -> row[0]).collect(Collectors.toSet());
        assertEquals(snapshot.size(), stored.size());
        assertEquals(actual, stored, "Regenerate the real Bukkit snapshot after any API update");
        for (String[] row : snapshot) {
            assertEquals(row[1], Materials.require(row[0]).name());
        }
    }

    @Test
    void everyCandidateHasAnExplicitReviewAndExcludedItemsHaveNoClassification() throws Exception {
        Map<String, String[]> metadata = metadata();
        Set<String> candidates = new HashSet<>();
        for (String[] row : rows(SNAPSHOT, ",")) {
            if (row[2].equals("true") && row[3].equals("true") && row[4].equals("false")) {
                candidates.add(row[0]);
                String[] review = metadata.get(row[0]);
                assertNotNull(review, () -> "Unreviewed candidate " + row[0]);
                if (review[1].equals("included")) {
                    assertEquals("true", review[2], () -> "Not survival obtainable " + row[0]);
                    assertEquals("true", row[5], () -> "Not a block item " + row[0]);
                    assertEquals("true", row[7], () -> "Experimental item " + row[0]);
                    assertTrue(Set.of("OVERWORLD", "NETHER", "END").contains(review[3]));
                    assertTrue(review[4].matches("[1-6]"));
                } else {
                    assertEquals("excluded", review[1]);
                    assertEquals("", review[3]);
                    assertEquals("", review[4]);
                }
            }
        }
        assertEquals(candidates, metadata.keySet());
    }

    @Test
    void allEighteenPoolsExactlyMatchReviewsWithoutDuplicates() throws Exception {
        Map<String, String[]> metadata = metadata();
        Set<String> seen = new HashSet<>();
        for (String dimension : List.of("overworld", "nether", "end")) {
            for (int tier = 1; tier <= 6; tier++) {
                Path path = POOLS.resolve(dimension + "/tier-" + tier + ".txt");
                assertTrue(Files.exists(path));
                for (String id : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                    assertTrue(seen.add(id), () -> "Duplicate target " + id);
                    String[] review = metadata.get(id);
                    assertNotNull(review, () -> "Unreviewed target " + id);
                    assertEquals("included", review[1]);
                    assertEquals(dimension.toUpperCase(java.util.Locale.ROOT), review[3]);
                    assertEquals(Integer.toString(tier), review[4]);
                    assertEquals(id, Materials.require(id).getKey().toString());
                }
            }
        }
        Set<String> included = metadata.values().stream().filter(row -> row[1].equals("included"))
                .map(row -> row[0]).collect(Collectors.toSet());
        assertEquals(included, seen);
    }

    @Test
    void acquisitionExceptionsAndFairnessExclusionsStayExplicit() throws Exception {
        Map<String, String[]> reviews = metadata();
        assertClassification(reviews, "beehive", "OVERWORLD", 4);
        assertClassification(reviews, "bee_nest", "OVERWORLD", 5);
        assertClassification(reviews, "lodestone", "OVERWORLD", 3);
        assertClassification(reviews, "small_dripleaf", "OVERWORLD", 3);
        assertClassification(reviews, "waxed_oxidized_copper", "OVERWORLD", 4);
        assertClassification(reviews, "observer", "NETHER", 3);
        assertClassification(reviews, "white_shulker_box", "END", 5);
        assertClassification(reviews, "dragon_head", "END", 6);
        for (String id : List.of("bedrock", "barrier", "command_block", "budding_amethyst", "chorus_plant",
                "player_head", "suspicious_sand", "trial_spawner", "vault", "wheat", "dragon_egg")) {
            assertEquals("excluded", reviews.get("minecraft:" + id)[1]);
        }
        assertEquals("true", reviews.get("minecraft:dragon_egg")[2], "Unique does not mean unobtainable");
        assertTrue(reviews.get("minecraft:tall_grass")[6].contains("structure_loot_only"));
    }

    private static void assertClassification(Map<String, String[]> reviews, String id, String dimension, int tier) {
        String[] review = reviews.get("minecraft:" + id);
        assertNotNull(review);
        assertEquals("included", review[1]);
        assertEquals(dimension, review[3]);
        assertEquals(Integer.toString(tier), review[4]);
    }
}
