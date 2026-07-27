package top.lqsnow.blockracing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.bukkit.configuration.file.YamlConfiguration;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YamlResourceTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void yamlResourcesDoNotContainDuplicateKeys() throws Exception {
        LoaderOptions options = new LoaderOptions();
        options.setAllowDuplicateKeys(false);
        Yaml yaml = new Yaml(new SafeConstructor(options));

        for (Path path : List.of(
                Path.of("src/main/resources/config.yml"),
                Path.of("src/main/resources/lang.yml"),
                Path.of("zh-cn/config.yml"),
                Path.of("zh-cn/lang.yml"),
                Path.of("en-us/config.yml"),
                Path.of("en-us/lang.yml")
        )) {
            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                yaml.load(reader);
            }
        }
    }

    @Test
    void bundledLanguagesContainTheSameMessageKeys() throws Exception {
        LoaderOptions options = new LoaderOptions();
        Yaml yaml = new Yaml(new SafeConstructor(options));
        Set<String> chinese = loadLeafKeys(yaml, Path.of("src/main/resources/lang.yml"));
        Set<String> english = loadLeafKeys(yaml, Path.of("en-us/lang.yml"));

        assertEquals(chinese, english);
        assertEquals(chinese, loadLeafKeys(yaml, Path.of("zh-cn/lang.yml")));
    }

    @Test
    void publishedConfigurationsContainTheSameKeys() throws Exception {
        LoaderOptions options = new LoaderOptions();
        Yaml yaml = new Yaml(new SafeConstructor(options));
        Set<String> defaults = loadLeafKeys(yaml, Path.of("src/main/resources/config.yml"));

        assertEquals(defaults, loadLeafKeys(yaml, Path.of("en-us/config.yml")));
        assertEquals(defaults, loadLeafKeys(yaml, Path.of("zh-cn/config.yml")));
    }

    @Test
    void configurationCommentsSurviveASettingsSave() throws Exception {
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.options().parseComments(true);
        configuration.load(Path.of("src/main/resources/config.yml").toFile());
        configuration.set("block-amount", 75);

        Path saved = temporaryDirectory.resolve("config.yml");
        configuration.save(saved.toFile());
        String text = Files.readString(saved, StandardCharsets.UTF_8);
        assertTrue(text.contains("# BlockRacing game settings"));
        assertTrue(text.contains("# Total number of target blocks in one game"));
    }

    private static Set<String> loadLeafKeys(Yaml yaml, Path path) throws Exception {
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            Set<String> result = new TreeSet<>();
            collectLeafKeys("", yaml.load(reader), result);
            return result;
        }
    }

    private static void collectLeafKeys(String prefix, Object value, Set<String> result) {
        if (value instanceof Map<?, ?> map) {
            map.forEach((key, child) -> collectLeafKeys(
                    prefix.isEmpty() ? key.toString() : prefix + "." + key,
                    child,
                    result
            ));
        } else {
            result.add(prefix);
        }
    }
}
