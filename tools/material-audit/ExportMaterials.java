import org.bukkit.Material;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Runs against the real Paper server registries, without starting a server/world. */
public class ExportMaterials {
    public static void main(String[] args) throws Exception {
        if (args.length != 1 && args.length != 3) {
            throw new IllegalArgumentException("Usage: ExportMaterials <output.csv> [<metadata.tsv> <pool-directory>]");
        }
        // Bootstrap and the feature check are developer-only NMS code; catalogue enumeration uses Bukkit.
        Class.forName("net.minecraft.SharedConstants").getMethod("tryDetectVersion").invoke(null);
        Class.forName("net.minecraft.server.Bootstrap").getMethod("bootStrap").invoke(null);
        var rows = new ArrayList<String>();
        for (Material material : Material.values()) {
            if (material.isLegacy()) continue;
            rows.add(String.join(",", material.getKey().toString(), material.name(),
                    Boolean.toString(material.isBlock()), Boolean.toString(material.isItem()),
                    Boolean.toString(material.isAir()),
                    Boolean.toString(material.isItem() && material.asItemType().hasBlockType()),
                    material.translationKey(), Boolean.toString(enabledByDefault(material))));
        }
        rows.sort(Comparator.naturalOrder());
        rows.addFirst("id,material,is_block,is_item,is_air,has_block_item,translation_key,enabled_by_default");
        Path output = Path.of(args[0]).toAbsolutePath();
        Files.createDirectories(output.getParent());
        Files.write(output, rows, StandardCharsets.UTF_8);
        if (args.length == 3) validateLibrary(Path.of(args[1]), Path.of(args[2]));
    }

    // Feature flags are a server implementation detail unavailable without a World in Bukkit.
    // This developer-only check rejects experimental items; the enumerated IDs/flags above use Bukkit.
    private static boolean enabledByDefault(Material material) {
        var id = net.minecraft.resources.Identifier.parse(material.getKey().toString());
        if (material.isItem()) {
            return net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(id).requiredFeatures()
                    .isSubsetOf(net.minecraft.world.flag.FeatureFlags.DEFAULT_FLAGS);
        }
        return net.minecraft.core.registries.BuiltInRegistries.BLOCK.getValue(id).requiredFeatures()
                .isSubsetOf(net.minecraft.world.flag.FeatureFlags.DEFAULT_FLAGS);
    }

    private static void validateLibrary(Path metadata, Path pools) throws Exception {
        Map<String, Material> candidates = new HashMap<>();
        for (Material material : Material.values()) {
            if (!material.isLegacy() && material.isBlock() && material.isItem() && !material.isAir()) {
                candidates.put(material.getKey().toString(), material);
            }
        }
        Map<String, String[]> reviews = new HashMap<>();
        var lines = Files.readAllLines(metadata, StandardCharsets.UTF_8);
        for (String line : lines.subList(1, lines.size())) {
            String[] row = line.split("\t", -1);
            if (row.length != 8 || reviews.put(row[0], row) != null) {
                throw new IllegalStateException("Malformed/duplicate metadata: " + line);
            }
            Material material = candidates.get(row[0]);
            if (material == null) throw new IllegalStateException("Removed/invalid candidate: " + row[0]);
            if (row[1].equals("included") && (!row[2].equals("true")
                    || !material.asItemType().hasBlockType() || !enabledByDefault(material))) {
                throw new IllegalStateException("Illegal normal target: " + row[0]);
            }
        }
        Set<String> unreviewed = new HashSet<>(candidates.keySet());
        unreviewed.removeAll(reviews.keySet());
        if (!unreviewed.isEmpty()) throw new IllegalStateException("Unreviewed new API candidates: " + unreviewed);
        Set<String> seen = new HashSet<>();
        for (String dimension : new String[]{"overworld", "nether", "end"}) {
            for (int tier = 1; tier <= 6; tier++) {
                for (String id : Files.readAllLines(pools.resolve(dimension + "/tier-" + tier + ".txt"))) {
                    String[] row = reviews.get(id);
                    if (!seen.add(id) || row == null || !row[1].equals("included")
                            || !row[3].equals(dimension.toUpperCase(java.util.Locale.ROOT))
                            || !row[4].equals(Integer.toString(tier))) {
                        throw new IllegalStateException("Duplicate/misclassified target: " + id);
                    }
                }
            }
        }
        for (var entry : reviews.entrySet()) {
            if (entry.getValue()[1].equals("included") && !seen.contains(entry.getKey())) {
                throw new IllegalStateException("Target missing from pool: " + entry.getKey());
            }
        }
    }
}
