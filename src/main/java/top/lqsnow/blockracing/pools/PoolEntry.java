package top.lqsnow.blockracing.pools;

import org.bukkit.Material;
import top.lqsnow.blockracing.toolkit.material.Materials;

public record PoolEntry(String id, Dimension dimension, int tier, boolean copperExtra, Acquisition acquisition) {
    public enum Dimension { OVERWORLD, NETHER, END }
    public enum Acquisition { GATHER, CRAFT, EXPLORE }

    /** Untagged entries are retained only when decoding older match snapshots. */
    public PoolEntry(String id, Dimension dimension, int tier, boolean copperExtra) {
        this(id, dimension, tier, copperExtra, null);
    }

    public PoolEntry {
        if (dimension == null || tier < 1 || tier > 6 || !Materials.require(id).getKey().toString().equals(id)) {
            throw new IllegalArgumentException("Invalid pool entry: " + id);
        }
    }

    public Material material() { return Materials.require(id); }

    public String encode() {
        return id + "|" + dimension + "|" + tier + "|" + copperExtra + (acquisition == null ? "" : "|" + acquisition);
    }

    public static PoolEntry decode(String encoded) {
        String[] parts = encoded.split("\\|", -1);
        if ((parts.length != 4 && parts.length != 5) || !parts[3].matches("true|false")) {
            throw new IllegalArgumentException("Invalid saved pool entry: " + encoded);
        }
        return new PoolEntry(parts[0], Dimension.valueOf(parts[1]), Integer.parseInt(parts[2]), Boolean.parseBoolean(parts[3]),
                parts.length == 5 ? Acquisition.valueOf(parts[4]) : null);
    }
}
