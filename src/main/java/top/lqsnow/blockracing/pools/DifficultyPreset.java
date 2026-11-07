package top.lqsnow.blockracing.pools;

import org.bukkit.configuration.ConfigurationSection;
import java.util.List;
import java.util.Locale;

public record DifficultyPreset(Kind kind, List<Integer> maxTiers, List<Integer> unlockPercent,
                               List<Integer> tierWeights, List<Integer> dimensionWeights, PacingProfile pacing) {
    public DifficultyPreset(Kind kind, List<Integer> maxTiers, List<Integer> unlockPercent,
                            List<Integer> tierWeights, List<Integer> dimensionWeights) {
        this(kind, maxTiers, unlockPercent, tierWeights, dimensionWeights, PacingProfile.standard());
    }
    public enum Kind { CASUAL, EASY, NORMAL, HARD, NIGHTMARE;
        public String id() { return name().toLowerCase(Locale.ROOT); }
        public static Kind parse(String id) { return valueOf(id.toUpperCase(Locale.ROOT)); }
    }

    public DifficultyPreset {
        maxTiers = List.copyOf(maxTiers);
        unlockPercent = List.copyOf(unlockPercent);
        tierWeights = List.copyOf(tierWeights);
        dimensionWeights = List.copyOf(dimensionWeights);
        if (kind == null || pacing == null || !maxTiers.equals(caps(kind)) || unlockPercent.size() != 3
                || tierWeights.size() != 6 || dimensionWeights.size() != 3 || unlockPercent.getFirst() != 0
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
        };
    }

    public static DifficultyPreset defaults(Kind kind) {
        List<Integer> unlock = switch (kind) {
            case CASUAL -> List.of(0, 100, 100);
            case EASY -> List.of(0, 50, 100);
            case NORMAL -> List.of(0, 25, 100);
            case HARD -> List.of(0, 25, 70);
            case NIGHTMARE -> List.of(0, 20, 65);
        };
        List<Integer> weights = switch (kind) {
            case CASUAL -> List.of(60, 40, 0, 0, 0, 0);
            case EASY -> List.of(45, 55, 0, 0, 0, 0);
            case NORMAL -> List.of(15, 35, 35, 15, 0, 0);
            case HARD -> List.of(10, 20, 30, 25, 15, 0);
            case NIGHTMARE -> List.of(5, 15, 20, 25, 20, 15);
        };
        return new DifficultyPreset(kind, caps(kind), unlock, weights, List.of(70, 30, 25));
    }

    public boolean allows(PoolEntry entry) {
        return entry.tier() <= maxTiers.get(entry.dimension().ordinal())
                && tierWeights.get(entry.tier() - 1) > 0 && dimensionWeights.get(entry.dimension().ordinal()) > 0;
    }

    public boolean unlocked(PoolEntry entry, int completed, int total) {
        return allows(entry) && completed >= unlockCount(total, unlockPercent.get(entry.dimension().ordinal()));
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
        pacing.save(section.createSection("pacing"));
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
        return new DifficultyPreset(Kind.parse(section.getString("id", "")), integers(section, "max-tiers"),
                integers(section, "unlock-percent"), integers(section, "tier-weights"), integers(section, "dimension-weights"), pacing);
    }

    private static List<Integer> integers(ConfigurationSection section, String key) {
        Object value = section.get(key);
        if (!(value instanceof List<?> list) || list.stream().anyMatch(n -> !(n instanceof Integer))) {
            throw new IllegalArgumentException("Expected integer list: " + key);
        }
        return list.stream().map(n -> (Integer) n).toList();
    }
}
