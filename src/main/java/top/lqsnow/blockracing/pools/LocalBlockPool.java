package top.lqsnow.blockracing.pools;

import org.bukkit.configuration.ConfigurationSection;
import top.lqsnow.blockracing.toolkit.material.Materials;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Local selections may be reclassified, but must be reviewed survival block items. */
public final class LocalBlockPool {
    private LocalBlockPool() {}

    public record Configuration(List<PoolEntry> entries, List<Integer> unlockPercent, TierProgression tierProgression) {
        public Configuration {
            entries = List.copyOf(entries);
            unlockPercent = List.copyOf(unlockPercent);
            if (tierProgression == null || unlockPercent.size() != 3 || unlockPercent.stream().anyMatch(n -> n < 0 || n > 100)) {
                throw new IllegalArgumentException("Invalid local unlock percentages");
            }
        }

        public DifficultyPreset preset() {
            DifficultyPreset defaults = DifficultyPreset.defaults(DifficultyPreset.Kind.CUSTOM);
            return new DifficultyPreset(defaults.kind(), defaults.maxTiers(), unlockPercent, defaults.tierWeights(),
                    defaults.dimensionWeights(), defaults.pacing(), List.of(63, 63, 63), tierProgression);
        }
    }

    public static List<PoolEntry> load(ConfigurationSection yaml, BlockCatalog catalog) {
        return read(yaml, catalog).entries();
    }

    public static Configuration read(ConfigurationSection yaml, BlockCatalog catalog) {
        if (yaml == null) throw new IllegalArgumentException("Missing custom/blocks.yml");
        Set<String> seen = new HashSet<>();
        List<PoolEntry> result = new ArrayList<>();
        List<Integer> thresholds = new ArrayList<>(List.of(0, 0, 0));
        Set<String> dimensions = Set.of("overworld", "nether", "end");
        for (String key : yaml.getKeys(false)) if (!dimensions.contains(key) && !key.equals("tier-progression")) {
            throw new IllegalArgumentException("Unknown custom dimension: " + key);
        }
        for (PoolEntry.Dimension dimension : PoolEntry.Dimension.values()) {
            String name = dimension.name().toLowerCase(Locale.ROOT);
            if (!yaml.isSet(name)) continue;
            ConfigurationSection group = yaml.getConfigurationSection(name);
            if (group == null) throw new IllegalArgumentException("Expected dimension section: " + name);
            if (group.isSet("unlock-percent")) {
                if (!(group.get("unlock-percent") instanceof Integer value) || value < 0 || value > 100) {
                    throw new IllegalArgumentException("Expected integer 0–100: " + name + ".unlock-percent");
                }
                thresholds.set(dimension.ordinal(), group.getInt("unlock-percent"));
            }
            for (String key : group.getKeys(false)) if (!key.equals("unlock-percent") && !key.matches("tier-[1-6]")) {
                throw new IllegalArgumentException("Unknown tier in " + name + ": " + key);
            }
            for (int tier = 1; tier <= 6; tier++) {
                String key = "tier-" + tier;
                if (!group.isSet(key)) continue;
                if (!(group.get(key) instanceof List<?> ids)) throw new IllegalArgumentException("Expected list: " + name + "." + key);
                for (Object value : ids) {
                    if (!(value instanceof String id)) throw new IllegalArgumentException("Expected block ID: " + name + "." + key);
                    String canonical = Materials.require(id).getKey().toString();
                    PoolEntry reviewed = catalog.find(canonical);
                    if (reviewed == null) throw new IllegalArgumentException("Not a reviewed survival block: " + id);
                    if (!seen.add(canonical)) throw new IllegalArgumentException("Duplicate custom block: " + canonical);
                    result.add(new PoolEntry(canonical, dimension, tier, reviewed.copperExtra(), reviewed.acquisition()));
                }
            }
        }
        if (result.isEmpty()) throw new IllegalArgumentException("No blocks in custom/blocks.yml");
        return new Configuration(result, thresholds, yaml.isSet("tier-progression")
                ? TierProgression.load(yaml.getConfigurationSection("tier-progression")) : TierProgression.disabled());
    }
}
