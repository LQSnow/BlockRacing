package top.lqsnow.blockracing.pools;

import org.bukkit.configuration.ConfigurationSection;
import java.util.List;

/** Fixed progress phases only. No active-slot feedback or task quotas. */
public record PacingProfile(boolean enabled, int middleStartPercent, int lateStartPercent,
                            List<Integer> early, List<Integer> middle, List<Integer> late) {
    public PacingProfile {
        early = checked(early);
        middle = checked(middle);
        late = checked(late);
        if (middleStartPercent < 1 || middleStartPercent >= lateStartPercent || lateStartPercent > 99) {
            throw new IllegalArgumentException("Pacing phases must satisfy 0 < middle < late < 100");
        }
    }

    private static List<Integer> checked(List<Integer> weights) {
        if (weights.size() != 3 || weights.stream().anyMatch(n -> n == null || n < 1 || n > 1_000_000)) {
            throw new IllegalArgumentException("Pacing weights need three positive integers: GATHER, CRAFT, EXPLORE");
        }
        return List.copyOf(weights);
    }

    public static PacingProfile standard() {
        return new PacingProfile(true, 20, 80, List.of(45, 40, 15), List.of(25, 30, 45), List.of(30, 40, 30));
    }

    public static PacingProfile legacy() {
        PacingProfile profile = standard();
        return new PacingProfile(false, profile.middleStartPercent, profile.lateStartPercent,
                profile.early, profile.middle, profile.late);
    }

    public List<Integer> weightsAt(int completed, int total) {
        if (completed < 0 || total < 1 || completed > total) throw new IllegalArgumentException("Invalid completed progress");
        if (completed >= DifficultyPreset.unlockCount(total, lateStartPercent)) return late;
        if (completed >= DifficultyPreset.unlockCount(total, middleStartPercent)) return middle;
        return early;
    }

    public void save(ConfigurationSection section) {
        section.set("enabled", enabled);
        section.set("middle-start-percent", middleStartPercent);
        section.set("late-start-percent", lateStartPercent);
        section.set("category-weights.early", early);
        section.set("category-weights.middle", middle);
        section.set("category-weights.late", late);
    }

    public static PacingProfile load(ConfigurationSection section) {
        if (section == null || !section.isBoolean("enabled")
                || !(section.get("middle-start-percent") instanceof Integer)
                || !(section.get("late-start-percent") instanceof Integer)) {
            throw new IllegalArgumentException("Missing/invalid pacing settings");
        }
        return new PacingProfile(section.getBoolean("enabled"), section.getInt("middle-start-percent"),
                section.getInt("late-start-percent"), integers(section, "category-weights.early"),
                integers(section, "category-weights.middle"), integers(section, "category-weights.late"));
    }

    private static List<Integer> integers(ConfigurationSection section, String key) {
        Object value = section.get(key);
        if (!(value instanceof List<?> list) || list.stream().anyMatch(n -> !(n instanceof Integer))) {
            throw new IllegalArgumentException("Expected integer pacing weights: " + key);
        }
        return list.stream().map(n -> (Integer) n).toList();
    }
}
