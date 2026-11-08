package top.lqsnow.blockracing.pools;

import org.bukkit.configuration.ConfigurationSection;
import java.util.List;
import java.util.Locale;

public record DifficultyPreset(Kind kind, List<Integer> maxTiers, List<Integer> unlockPercent,
                               List<Integer> tierWeights, List<Integer> dimensionWeights, PacingProfile pacing,
                               List<Integer> tierMasks, TierProgression tierProgression) {
    public DifficultyPreset(Kind kind, List<Integer> maxTiers, List<Integer> unlockPercent,
                            List<Integer> tierWeights, List<Integer> dimensionWeights, PacingProfile pacing,
                            List<Integer> tierMasks) {
        this(kind, maxTiers, unlockPercent, tierWeights, dimensionWeights, pacing, tierMasks, TierProgression.standard());
    }
    public DifficultyPreset(Kind kind, List<Integer> maxTiers, List<Integer> unlockPercent,
                            List<Integer> tierWeights, List<Integer> dimensionWeights, PacingProfile pacing) {
        this(kind, maxTiers, unlockPercent, tierWeights, dimensionWeights, pacing, masks(maxTiers));
    }
    public DifficultyPreset(Kind kind, List<Integer> maxTiers, List<Integer> unlockPercent,
                            List<Integer> tierWeights, List<Integer> dimensionWeights) {
        this(kind, maxTiers, unlockPercent, tierWeights, dimensionWeights, PacingProfile.standard());
    }
    public enum Kind { CASUAL, EASY, NORMAL, HARD, NIGHTMARE, CUSTOM;
        public static List<Kind> standards() { return List.of(CASUAL, EASY, NORMAL, HARD, NIGHTMARE); }
        public String id() { return name().toLowerCase(Locale.ROOT); }
        public static Kind parse(String id) { return valueOf(id.toUpperCase(Locale.ROOT)); }
    }

    public DifficultyPreset {
        maxTiers = List.copyOf(maxTiers);
        unlockPercent = List.copyOf(unlockPercent);
        tierWeights = List.copyOf(tierWeights);
        dimensionWeights = List.copyOf(dimensionWeights);
        tierMasks = List.copyOf(tierMasks);
        if (kind == null || pacing == null || tierProgression == null || !maxTiers.equals(caps(kind)) || unlockPercent.size() != 3
                || tierWeights.size() != 6 || dimensionWeights.size() != 3 || tierMasks.size() != 3
                || (kind != Kind.CUSTOM && !tierMasks.equals(masks(maxTiers)))
                || tierMasks.stream().anyMatch(n -> n < 0 || n > 63)
                || (kind != Kind.CUSTOM && unlockPercent.getFirst() != 0)
                || unlockPercent.stream().anyMatch(n -> n < 0 || n > 100)
                || tierWeights.stream().anyMatch(n -> n < 0 || n > 1_000_000)
                || dimensionWeights.stream().anyMatch(n -> n < 0 || n > 1_000_000)) {
            throw new IllegalArgumentException("Invalid preset ranges/weights/thresholds: " + kind);
        }
    }

    private static List<Integer> caps(Kind kind) {
        return switch (kind) {
            case CASUAL -> List.of(2, 0, 0);
            case EASY -> List.of(2, 2, 0);
            case NORMAL -> List.of(4, 3, 0);
            case HARD -> List.of(5, 5, 5);
            case NIGHTMARE -> List.of(6, 6, 6);
            case CUSTOM -> List.of(6, 6, 6);
        };
    }

    private static List<Integer> masks(List<Integer> caps) {
        return caps.stream().map(cap -> (1 << cap) - 1).toList();
    }

    public static DifficultyPreset defaults(Kind kind) {
        List<Integer> unlock = switch (kind) {
            case CASUAL -> List.of(0, 100, 100);
            case EASY -> List.of(0, 50, 100);
            case NORMAL -> List.of(0, 25, 100);
            case HARD -> List.of(0, 25, 70);
            case NIGHTMARE -> List.of(0, 20, 65);
            case CUSTOM -> List.of(0, 25, 70);
        };
        List<Integer> weights = switch (kind) {
            case CASUAL -> List.of(60, 40, 0, 0, 0, 0);
            case EASY -> List.of(45, 55, 0, 0, 0, 0);
            case NORMAL -> List.of(15, 35, 35, 15, 0, 0);
            case HARD -> List.of(10, 20, 30, 25, 15, 0);
            case NIGHTMARE -> List.of(5, 15, 20, 25, 20, 15);
            case CUSTOM -> List.of(15, 35, 35, 15, 10, 5);
        };
        return new DifficultyPreset(kind, caps(kind), unlock, weights, List.of(70, 30, 25), PacingProfile.standard(),
                kind == Kind.CUSTOM ? List.of(15, 7, 0) : masks(caps(kind)));
    }

    public boolean allows(PoolEntry entry) {
        return entry.tier() <= maxTiers.get(entry.dimension().ordinal())
                && (tierMasks.get(entry.dimension().ordinal()) & (1 << (entry.tier() - 1))) != 0
                && tierWeights.get(entry.tier() - 1) > 0 && dimensionWeights.get(entry.dimension().ordinal()) > 0;
    }

    public boolean unlocked(PoolEntry entry, int completed, int total) {
        return allows(entry) && completed >= unlockCount(total, unlockPercent.get(entry.dimension().ordinal()))
                && tierProgression.multiplier(entry.tier(), completed, total) > 0;
    }

    public int tierWeight(int tier, int completed, int total) {
        return tierWeights.get(tier - 1) * tierProgression.multiplier(tier, completed, total);
    }

    public static int unlockCount(int total, int percentage) {
        return (int) (((long) total * percentage + 99) / 100);
    }

    public void save(ConfigurationSection section) {
        section.set("id", kind.id());
        section.set("max-tiers", maxTiers);
        section.set("unlock-percent", unlockPercent);
        section.set("tier-weights", tierWeights);
        section.set("dimension-weights", dimensionWeights);
        section.set("tier-masks", tierMasks);
        pacing.save(section.createSection("pacing"));
        tierProgression.save(section.createSection("tier-progression"));
    }

    public static DifficultyPreset load(ConfigurationSection section) {
        return load(section, false, false);
    }

    public static DifficultyPreset loadSnapshot(ConfigurationSection section, boolean requirePacing) {
        return load(section, true, requirePacing);
    }

    private static DifficultyPreset load(ConfigurationSection section, boolean snapshot, boolean requirePacing) {
        if (section == null) throw new IllegalArgumentException("Missing difficulty preset");
        PacingProfile pacing;
        if (section.isSet("pacing")) {
            pacing = PacingProfile.load(section.getConfigurationSection("pacing"));
        } else {
            if (requirePacing) throw new IllegalArgumentException("Missing saved pacing rules");
            pacing = snapshot ? PacingProfile.legacy() : PacingProfile.standard();
        }
        Kind kind = Kind.parse(section.getString("id", ""));
        List<Integer> maxTiers = integers(section, "max-tiers");
        if (kind == Kind.CUSTOM && !section.isSet("tier-masks")) throw new IllegalArgumentException("Missing custom tier selection");
        return new DifficultyPreset(kind, maxTiers, integers(section, "unlock-percent"), integers(section, "tier-weights"),
                integers(section, "dimension-weights"), pacing,
                section.isSet("tier-masks") ? integers(section, "tier-masks") : masks(maxTiers),
                section.isSet("tier-progression") ? TierProgression.load(section.getConfigurationSection("tier-progression"))
                        : snapshot ? TierProgression.disabled() : TierProgression.standard());
    }

    private static List<Integer> integers(ConfigurationSection section, String key) {
        Object value = section.get(key);
        if (!(value instanceof List<?> list) || list.stream().anyMatch(n -> !(n instanceof Integer))) {
            throw new IllegalArgumentException("Expected integer list: " + key);
        }
        return list.stream().map(n -> (Integer) n).toList();
    }
}
