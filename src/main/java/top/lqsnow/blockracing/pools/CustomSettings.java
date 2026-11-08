package top.lqsnow.blockracing.pools;

import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

/** Editable preparation settings; TaskSession takes an immutable snapshot at match start. */
public record CustomSettings(Source source, List<Boolean> dimensions, List<Integer> tierMasks,
                             List<Integer> unlockPercent) {
    public enum Source { TIERS, LOCAL }

    public CustomSettings {
        dimensions = List.copyOf(dimensions);
        tierMasks = List.copyOf(tierMasks);
        unlockPercent = List.copyOf(unlockPercent);
        if (source == null || dimensions.size() != 3 || tierMasks.size() != 3 || unlockPercent.size() != 3
                || tierMasks.stream().anyMatch(n -> n < 0 || n > 63)
                || unlockPercent.stream().anyMatch(n -> n < 0 || n > 100)) {
            throw new IllegalArgumentException("Invalid custom dimensions/tiers/thresholds");
        }
    }

    public static CustomSettings defaults() {
        return new CustomSettings(Source.TIERS, List.of(true, true, false), List.of(15, 7, 63), List.of(0, 25, 70));
    }

    public boolean enabled(PoolEntry.Dimension dimension) { return dimensions.get(dimension.ordinal()); }
    public boolean tierEnabled(PoolEntry.Dimension dimension, int tier) {
        if (tier < 1 || tier > 6) throw new IllegalArgumentException("Tier must be 1–6");
        return (tierMasks.get(dimension.ordinal()) & (1 << (tier - 1))) != 0;
    }
    public CustomSettings withSource(Source next) { return new CustomSettings(next, dimensions, tierMasks, unlockPercent); }
    public CustomSettings toggleDimension(PoolEntry.Dimension dimension) {
        List<Boolean> changed = new ArrayList<>(dimensions);
        changed.set(dimension.ordinal(), !enabled(dimension));
        return new CustomSettings(source, changed, tierMasks, unlockPercent);
    }
    public CustomSettings toggleTier(PoolEntry.Dimension dimension, int tier) {
        tierEnabled(dimension, tier);
        List<Integer> changed = new ArrayList<>(tierMasks);
        changed.set(dimension.ordinal(), changed.get(dimension.ordinal()) ^ (1 << (tier - 1)));
        return new CustomSettings(source, dimensions, changed, unlockPercent);
    }
    public CustomSettings withThreshold(PoolEntry.Dimension dimension, int percentage) {
        List<Integer> changed = new ArrayList<>(unlockPercent);
        changed.set(dimension.ordinal(), Math.clamp(percentage, 0, 100));
        return new CustomSettings(source, dimensions, tierMasks, changed);
    }

    public DifficultyPreset preset() {
        if (source == Source.LOCAL) throw new IllegalStateException("Local match rules must be read from custom/blocks.yml");
        DifficultyPreset defaults = DifficultyPreset.defaults(DifficultyPreset.Kind.CUSTOM);
        List<Integer> activeMasks = new ArrayList<>();
        for (PoolEntry.Dimension dimension : PoolEntry.Dimension.values()) {
            activeMasks.add(enabled(dimension) ? tierMasks.get(dimension.ordinal()) : 0);
        }
        return new DifficultyPreset(defaults.kind(), defaults.maxTiers(), unlockPercent, defaults.tierWeights(),
                defaults.dimensionWeights(), defaults.pacing(), activeMasks);
    }

    public void save(ConfigurationSection section) {
        section.set("source", source.name().toLowerCase(java.util.Locale.ROOT));
        for (PoolEntry.Dimension dimension : PoolEntry.Dimension.values()) {
            String path = dimension.name().toLowerCase(java.util.Locale.ROOT);
            section.set(path + ".enabled", enabled(dimension));
            List<Integer> tiers = new ArrayList<>();
            for (int tier = 1; tier <= 6; tier++) if (tierEnabled(dimension, tier)) tiers.add(tier);
            section.set(path + ".tiers", tiers);
            section.set(path + ".unlock-percent", unlockPercent.get(dimension.ordinal()));
        }
    }

    public static CustomSettings load(ConfigurationSection section) {
        List<Boolean> dimensions = new ArrayList<>();
        List<Integer> masks = new ArrayList<>(), thresholds = new ArrayList<>();
        if (section == null) throw new IllegalArgumentException("Missing custom settings");
        for (PoolEntry.Dimension dimension : PoolEntry.Dimension.values()) {
            String path = dimension.name().toLowerCase(java.util.Locale.ROOT);
            if (!section.isBoolean(path + ".enabled") || !(section.get(path + ".unlock-percent") instanceof Integer)
                    || !(section.get(path + ".tiers") instanceof List<?> tiers)) {
                throw new IllegalArgumentException("Invalid custom settings for " + path);
            }
            int mask = 0;
            for (Object value : tiers) {
                if (!(value instanceof Integer tier) || tier < 1 || tier > 6 || (mask & (1 << (tier - 1))) != 0) {
                    throw new IllegalArgumentException("Invalid/duplicate tier in " + path);
                }
                mask |= 1 << (tier - 1);
            }
            dimensions.add(section.getBoolean(path + ".enabled"));
            masks.add(mask);
            thresholds.add(section.getInt(path + ".unlock-percent"));
        }
        return new CustomSettings(Source.valueOf(section.getString("source", "").toUpperCase(java.util.Locale.ROOT)),
                dimensions, masks, thresholds);
    }
}
