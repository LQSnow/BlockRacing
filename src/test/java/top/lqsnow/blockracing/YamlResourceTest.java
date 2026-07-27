package top.lqsnow.blockracing;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

class YamlResourceTest {
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
}
