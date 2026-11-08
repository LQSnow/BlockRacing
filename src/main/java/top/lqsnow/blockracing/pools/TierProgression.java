package top.lqsnow.blockracing.pools;

import org.bukkit.configuration.ConfigurationSection;
import java.util.*;

/** Macro progress gates and tier weight modifiers; never inspects active-slot composition. */
public record TierProgression(boolean enabled, List<Stage> stages) {
    public record Stage(int startPercent, List<Integer> multipliers) {
        public Stage {
            multipliers = List.copyOf(multipliers);
            if (startPercent < 0 || startPercent > 99 || multipliers.size() != 6
                    || multipliers.stream().anyMatch(n -> n < 0 || n > 100)
                    || multipliers.stream().allMatch(n -> n == 0)) {
                throw new IllegalArgumentException("Invalid tier progression stage");
            }
        }
    }

    public TierProgression {
        stages = List.copyOf(stages);
        if (stages.isEmpty() || stages.getFirst().startPercent() != 0) {
            throw new IllegalArgumentException("Tier progression must start at 0%");
        }
        for (int i = 1; i < stages.size(); i++) {
            if (stages.get(i).startPercent() <= stages.get(i - 1).startPercent()) {
                throw new IllegalArgumentException("Tier progression stages must be strictly increasing");
            }
            for (int tier = 0; tier < 6; tier++) {
                if (stages.get(i - 1).multipliers().get(tier) > 0 && stages.get(i).multipliers().get(tier) == 0) {
                    throw new IllegalArgumentException("An unlocked tier must remain available");
                }
            }
        }
        if (stages.getLast().multipliers().stream().anyMatch(n -> n == 0)) {
            throw new IllegalArgumentException("Final progression stage must allow every selected tier");
        }
    }

    public static TierProgression standard() {
        return new TierProgression(true, List.of(
                new Stage(0, List.of(8, 3, 0, 0, 0, 0)),
                new Stage(15, List.of(4, 4, 1, 0, 0, 0)),
                new Stage(35, List.of(2, 3, 4, 1, 0, 0)),
                new Stage(60, List.of(1, 2, 4, 3, 1, 0)),
                new Stage(80, List.of(1, 1, 2, 4, 4, 2))));
    }

    public static TierProgression disabled() {
        return new TierProgression(false, List.of(new Stage(0, List.of(1, 1, 1, 1, 1, 1))));
    }

    public int multiplier(int tier, int completed, int total) {
        if (tier < 1 || tier > 6 || total < 1 || completed < 0 || completed > total) {
            throw new IllegalArgumentException("Invalid tier or completed progress");
        }
        if (!enabled) return 1;
        Stage current = stages.getFirst();
        for (Stage stage : stages) {
            if (completed < DifficultyPreset.unlockCount(total, stage.startPercent())) break;
            current = stage;
        }
        return current.multipliers().get(tier - 1);
    }

    public void save(ConfigurationSection section) {
        section.set("enabled", enabled);
        List<Map<String, Object>> values = new ArrayList<>();
        for (Stage stage : stages) values.add(Map.of("start-percent", stage.startPercent(), "tier-multipliers", stage.multipliers()));
        section.set("stages", values);
    }

    public static TierProgression load(ConfigurationSection section) {
        if (section == null || !section.isBoolean("enabled") || !(section.get("stages") instanceof List<?> values)) {
            throw new IllegalArgumentException("Missing/invalid tier-progression settings");
        }
        List<Stage> stages = new ArrayList<>();
        for (Object value : values) {
            if (!(value instanceof Map<?, ?> stage) || !(stage.get("start-percent") instanceof Integer start)
                    || !(stage.get("tier-multipliers") instanceof List<?> weights)
                    || weights.stream().anyMatch(n -> !(n instanceof Integer))) {
                throw new IllegalArgumentException("Invalid tier-progression stage data");
            }
            stages.add(new Stage(start, weights.stream().map(n -> (Integer) n).toList()));
        }
        return new TierProgression(section.getBoolean("enabled"), stages);
    }
}
