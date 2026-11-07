package top.lqsnow.blockracing.pools;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class BlockCatalog {
    private final Map<String, PoolEntry> entries;

    public BlockCatalog(Collection<PoolEntry> entries) {
        Map<String, PoolEntry> indexed = new TreeMap<>();
        for (PoolEntry entry : entries) {
            if (indexed.put(entry.id(), entry) != null) throw new IllegalArgumentException("Duplicate target: " + entry.id());
        }
        this.entries = Collections.unmodifiableMap(indexed);
    }

    public List<PoolEntry> entries() { return List.copyOf(entries.values()); }
    public PoolEntry find(String id) { return entries.get(id); }

    public static BlockCatalog load(Path root) throws IOException {
        Map<String, PoolEntry.Acquisition> acquisition = new HashMap<>();
        List<String> tagLines = Files.readAllLines(root.resolve("acquisition-tags.tsv"), StandardCharsets.UTF_8);
        if (tagLines.isEmpty() || !tagLines.getFirst().equals("id\tcategory\treason")) throw new IOException("Invalid acquisition tag header");
        for (String line : tagLines.subList(1, tagLines.size())) {
            String[] row = line.split("\t", -1);
            if (row.length != 3 || row[2].isBlank() || acquisition.containsKey(row[0])) {
                throw new IOException("Malformed/duplicate acquisition tag: " + line);
            }
            try {
                acquisition.put(row[0], PoolEntry.Acquisition.valueOf(row[1]));
            } catch (IllegalArgumentException ex) {
                throw new IOException("Invalid acquisition category for " + row[0] + ": " + row[1], ex);
            }
        }
        Set<String> copperExtras = new HashSet<>(Files.readAllLines(root.resolve("copper-extras.txt"), StandardCharsets.UTF_8)
                .stream().map(String::strip).filter(s -> !s.isEmpty() && !s.startsWith("#")).toList());
        Map<String, PoolEntry> reviewed = new HashMap<>();
        List<String> metadata = Files.readAllLines(root.resolve("material-metadata.tsv"), StandardCharsets.UTF_8);
        Set<String> metadataIds = new HashSet<>();
        for (String line : metadata.subList(1, metadata.size())) {
            String[] row = line.split("\t", -1);
            if (row.length != 8 || !metadataIds.add(row[0])) throw new IOException("Malformed/duplicate metadata: " + line);
            if (!row[1].equals("included")) continue;
            if (!row[2].equals("true")) throw new IOException("Not survival obtainable: " + row[0]);
            reviewed.put(row[0], new PoolEntry(row[0], PoolEntry.Dimension.valueOf(row[3]), Integer.parseInt(row[4]),
                    copperExtras.contains(row[0]) || Arrays.asList(row[6].split(";")).contains("copper_extra"), acquisition.get(row[0])));
        }
        if (!reviewed.keySet().containsAll(copperExtras)) throw new IOException("Unreviewed ID in copper-extras.txt");
        List<PoolEntry> targets = new ArrayList<>();
        for (PoolEntry.Dimension dimension : PoolEntry.Dimension.values()) {
            for (int tier = 1; tier <= 6; tier++) {
                Path file = root.resolve(dimension.name().toLowerCase(Locale.ROOT) + "/tier-" + tier + ".txt");
                for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                    String id = line.strip();
                    if (id.isEmpty() || id.startsWith("#")) continue;
                    PoolEntry entry = reviewed.get(id);
                    if (entry == null || entry.dimension() != dimension || entry.tier() != tier) {
                        throw new IOException("Unreviewed/misclassified target in " + file + ": " + id);
                    }
                    if (entry.acquisition() == null) throw new IOException("Missing acquisition-tags.tsv entry: " + id);
                    targets.add(entry);
                }
            }
        }
        return new BlockCatalog(targets);
    }

    public static void validateLive(Collection<PoolEntry> entries) {
        for (PoolEntry entry : entries) {
            var material = entry.material();
            if (!material.isBlock() || !material.isItem() || material.isAir() || material.isLegacy()
                    || !material.asItemType().hasBlockType()) {
                throw new IllegalArgumentException("Not a valid block item: " + entry.id());
            }
        }
    }
}
