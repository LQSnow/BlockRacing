package top.lqsnow.blockracing.pools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** Counts and representative IDs are derived from the same eligible tier. */
public record TierSummary(int count, List<String> examples) {
    public TierSummary { examples = List.copyOf(examples); }

    public static TierSummary from(Collection<PoolEntry> catalog, PoolEntry.Dimension dimension, int tier, boolean copperExtras) {
        if (tier < 1 || tier > 6) throw new IllegalArgumentException("Tier must be 1–6");
        List<String> ids = catalog.stream().filter(e -> e.dimension() == dimension && e.tier() == tier)
                .filter(e -> copperExtras || !e.copperExtra()).map(PoolEntry::id).distinct().sorted().toList();
        List<String> examples = new ArrayList<>();
        for (String preferred : preferred(dimension, tier)) {
            String id = "minecraft:" + preferred;
            if (ids.contains(id) && examples.size() < 3) examples.add(id);
        }
        for (String id : ids) {
            if (examples.size() == 3) break;
            if (!examples.contains(id)) examples.add(id);
        }
        return new TierSummary(ids.size(), examples);
    }

    private static List<String> preferred(PoolEntry.Dimension dimension, int tier) {
        return switch (dimension) {
            case OVERWORLD -> switch (tier) {
                case 1 -> List.of("dirt", "cobblestone", "oak_log");
                case 2 -> List.of("glass", "furnace", "barrel");
                case 3 -> List.of("cherry_log", "bamboo_block", "cactus");
                case 4 -> List.of("cake", "beehive", "anvil");
                case 5 -> List.of("bee_nest", "sponge", "turtle_egg");
                default -> List.of("heavy_core", "deepslate_emerald_ore", "creeper_head");
            };
            case NETHER -> switch (tier) {
                case 1 -> List.of("netherrack");
                case 2 -> List.of("blackstone", "soul_sand", "glowstone");
                case 3 -> List.of("shroomlight", "crimson_stem", "quartz_block");
                case 4 -> List.of("respawn_anchor", "gilded_blackstone", "nether_wart");
                case 5 -> List.of("ancient_debris", "nether_quartz_ore", "ochre_froglight");
                default -> List.of("netherite_block", "beacon", "piglin_head");
            };
            case END -> switch (tier) {
                case 1 -> List.of("end_stone");
                case 2 -> List.of("end_stone_bricks", "chorus_flower", "end_stone_brick_stairs");
                case 3 -> List.of("purpur_block", "end_rod", "purpur_pillar");
                case 4 -> List.of("shulker_box");
                case 5 -> List.of("white_shulker_box", "red_shulker_box", "black_shulker_box");
                default -> List.of("dragon_head");
            };
        };
    }
}
