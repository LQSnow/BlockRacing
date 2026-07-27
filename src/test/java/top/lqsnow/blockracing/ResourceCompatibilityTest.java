package top.lqsnow.blockracing;

import org.bukkit.Material;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceCompatibilityTest {
    private static final List<String> BLOCK_LISTS = List.of(
            "EasyBlocks.txt",
            "MediumBlocks.txt",
            "HardBlocks.txt",
            "DyedBlocks.txt",
            "EndBlocks.txt"
    );

    @Test
    void everyConfiguredBlockExistsInPaper262() throws Exception {
        for (String resource : BLOCK_LISTS) {
            for (String name : readLines(resource)) {
                Material material = Material.getMaterial(name);
                assertNotNull(material, () -> resource + " contains unknown material " + name);
            }
        }
    }

    @Test
    void blockPoolsDoNotContainDuplicates() throws Exception {
        Set<String> seen = new HashSet<>();
        for (String resource : BLOCK_LISTS) {
            for (String name : readLines(resource)) {
                assertTrue(seen.add(name), () -> name + " occurs in more than one block pool");
            }
        }
    }

    @Test
    void everyConfiguredBlockHasBundledTranslations() throws Exception {
        JSONObject english = readJson("en_us.json");
        JSONObject chinese = readJson("zh_cn.json");
        for (String resource : BLOCK_LISTS) {
            for (String name : readLines(resource)) {
                Material material = Material.getMaterial(name);
                assertNotNull(material);
                String key = "block.minecraft." + name.toLowerCase();
                assertTrue(english.containsKey(key), () -> "en_us.json is missing " + key);
                assertTrue(chinese.containsKey(key), () -> "zh_cn.json is missing " + key);
            }
        }
    }

    @Test
    void includesMinecraft262Blocks() throws Exception {
        Set<String> configured = new HashSet<>();
        for (String resource : BLOCK_LISTS) {
            configured.addAll(readLines(resource));
        }
        assertTrue(configured.containsAll(List.of(
                "SULFUR",
                "CINNABAR",
                "SULFUR_SPIKE",
                "POTENT_SULFUR",
                "CHISELED_CINNABAR",
                "CHISELED_SULFUR"
        )));
    }

    private static List<String> readLines(String resource) throws Exception {
        try (InputStream input = ResourceCompatibilityTest.class.getClassLoader().getResourceAsStream(resource)) {
            assertNotNull(input, () -> "Missing test resource " + resource);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
                return reader.lines()
                        .map(String::trim)
                        .filter(line -> !line.isEmpty())
                        .toList();
            }
        }
    }

    private static JSONObject readJson(String resource) throws Exception {
        try (InputStream input = ResourceCompatibilityTest.class.getClassLoader().getResourceAsStream(resource)) {
            assertNotNull(input, () -> "Missing test resource " + resource);
            try (InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
                return (JSONObject) new JSONParser().parse(reader);
            }
        }
    }
}
