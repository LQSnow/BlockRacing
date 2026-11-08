package top.lqsnow.blockracing.managers;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import top.lqsnow.blockracing.Main;
import top.lqsnow.blockracing.pools.*;
import top.lqsnow.blockracing.toolkit.material.Materials;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Block {
    // Retained for legacy saves and debug commands. New remaining lists contain only active targets.
    public static List<String> easyBlocks = List.of(), mediumBlocks = List.of(), hardBlocks = List.of(),
            dyedBlocks = List.of(), endBlocks = List.of(), blocks = List.of();
    public static List<String> allBlocks = new ArrayList<>();
    public static int maxBlockAmount;
    public static List<String> redTeamBlocks = new ArrayList<>(), blueTeamBlocks = new ArrayList<>();
    public static List<String> redTeamRemainingBlocks = new ArrayList<>(), blueTeamRemainingBlocks = new ArrayList<>();
    private static BlockCatalog catalog;
    private static final Map<DifficultyPreset.Kind, DifficultyPreset> presets = new EnumMap<>(DifficultyPreset.Kind.class);
    private static TaskSession session;
    private static CustomSettings customSettings;
    private static List<String> legacyPool = List.of();

    public Block() { reloadBlock(); }

    private static void saveIfAbsent(String resource) {
        if (!Files.exists(Main.getInstance().getDataFolder().toPath().resolve(resource))) {
            Main.getInstance().saveResource(resource, false);
        }
    }

    public static void reloadBlock() {
        try {
            saveIfAbsent("block-pools/material-metadata.tsv");
            saveIfAbsent("block-pools/catalogue.yml");
            saveIfAbsent("block-pools/copper-basic.txt");
            saveIfAbsent("block-pools/copper-extras.txt");
            saveIfAbsent("block-pools/acquisition-tags.tsv");
            for (PoolEntry.Dimension dimension : PoolEntry.Dimension.values()) {
                for (int tier = 1; tier <= 6; tier++) {
                    saveIfAbsent("block-pools/" + dimension.name().toLowerCase(Locale.ROOT) + "/tier-" + tier + ".txt");
                }
            }
            for (DifficultyPreset.Kind kind : DifficultyPreset.Kind.standards()) {
                String resource = "presets/" + kind.id() + ".yml";
                saveIfAbsent(resource);
                YamlConfiguration yaml = new YamlConfiguration();
                yaml.options().parseComments(true);
                yaml.load(Main.getInstance().getDataFolder().toPath().resolve(resource).toFile());
                DifficultyPreset preset = DifficultyPreset.load(yaml);
                if (preset.kind() != kind) throw new IllegalArgumentException("Preset ID does not match " + resource);
                boolean migrated = false;
                if (!yaml.isSet("pacing")) {
                    preset.pacing().save(yaml.createSection("pacing"));
                    migrated = true;
                }
                if (!yaml.isSet("tier-progression")) {
                    preset.tierProgression().save(yaml.createSection("tier-progression"));
                    migrated = true;
                }
                if (migrated) yaml.save(Main.getInstance().getDataFolder().toPath().resolve(resource).toFile());
                presets.put(kind, preset);
            }
            catalog = BlockCatalog.load(Main.getInstance().getDataFolder().toPath().resolve("block-pools"));
            BlockCatalog.validateLive(catalog.entries());
            saveIfAbsent("custom/settings.yml");
            saveIfAbsent("custom/blocks.yml");
            YamlConfiguration customYaml = new YamlConfiguration();
            customYaml.load(Main.getInstance().getDataFolder().toPath().resolve("custom/settings.yml").toFile());
            customSettings = CustomSettings.load(customYaml);
            easyBlocks = readLegacy("EasyBlocks.txt");
            mediumBlocks = readLegacy("MediumBlocks.txt");
            hardBlocks = readLegacy("HardBlocks.txt");
            dyedBlocks = readLegacy("DyedBlocks.txt");
            endBlocks = readLegacy("EndBlocks.txt");
            addUpBlocks();
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to load reviewed block pools/presets; inspect the named file and Material ID", ex);
        }
    }

    private static List<String> readLegacy(String name) throws IOException {
        Path path = Main.getInstance().getDataFolder().toPath().resolve(name);
        return Files.readAllLines(path, StandardCharsets.UTF_8).stream().map(String::strip)
                .filter(line -> !line.isEmpty() && !line.startsWith("#")).toList();
    }

    public static DifficultyPreset preset(DifficultyPreset.Kind kind) {
        return kind == DifficultyPreset.Kind.CUSTOM ? customSelection(customSettings, Setting.isCopperExtras()).preset() : presets.get(kind);
    }
    public static CustomSettings customSettings() { return customSettings; }

    public static void saveCustomSettings(CustomSettings next) {
        try {
            YamlConfiguration yaml = new YamlConfiguration();
            yaml.options().parseComments(true);
            Path path = Main.getInstance().getDataFolder().toPath().resolve("custom/settings.yml");
            yaml.load(path.toFile());
            next.save(yaml);
            yaml.save(path.toFile());
            customSettings = next;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Unable to save custom/settings.yml: " + ex.getMessage(), ex);
        }
    }

    private record Selection(DifficultyPreset preset, List<PoolEntry> pool, boolean copperExtras) {}

    private static Selection customSelection(CustomSettings settings, boolean copperExtras) {
        if (settings.source() == CustomSettings.Source.TIERS) return new Selection(settings.preset(), catalog.entries(), copperExtras);
        try {
            YamlConfiguration yaml = new YamlConfiguration();
            yaml.load(Main.getInstance().getDataFolder().toPath().resolve("custom/blocks.yml").toFile());
            LocalBlockPool.Configuration local = LocalBlockPool.read(yaml, catalog);
            // Local files explicitly choose their blocks and thresholds, independently of GUI settings.
            return new Selection(local.preset(), local.entries(), true);
        } catch (Exception ex) {
            throw new IllegalArgumentException("custom/blocks.yml: " + ex.getMessage(), ex);
        }
    }

    public static void activateCustom(CustomSettings.Source source) {
        CustomSettings next = customSettings.withSource(source);
        createSession(customSelection(next, Setting.isCopperExtras()), 0L); // Validate before changing the active selection.
        saveCustomSettings(next);
        Setting.setDifficulty(DifficultyPreset.Kind.CUSTOM);
        addUpBlocks();
    }
    public static DifficultyPreset effectivePreset() { return session == null ? preset(Setting.getDifficulty()) : session.preset(); }
    public static TaskSession session() { return session; }
    public static int targetTotal() { return session == null ? Setting.getBlockAmount() : session.total(); }
    public static int maxRolls() { return session == null ? Setting.getMaxRollCount() : session.maxRolls(); }
    public static int rollsRemaining(String team) {
        if (!team.equals("red") && !team.equals("blue")) throw new IllegalArgumentException("Unknown team: " + team);
        return session == null ? Math.max(0, maxRolls() - (team.equals("red") ? Game.redTeamRollCount : Game.blueTeamRollCount))
                : session.rollsRemaining(team);
    }

    public static PoolEntry find(String id) {
        String key = Materials.require(id).getKey().toString();
        PoolEntry saved = session == null ? null : session.find(key);
        return saved != null ? saved : catalog.find(key);
    }

    public static DisplayDifficulty displayDifficulty(String id) {
        PoolEntry entry = find(id);
        return entry == null ? DisplayDifficulty.EASY : DisplayDifficulty.forTier(entry.tier());
    }

    public static List<PoolEntry> candidates(DifficultyPreset.Kind kind, boolean copperExtras) {
        Selection selection = selection(kind, copperExtras);
        return selection.pool().stream().filter(selection.preset()::allows)
                .filter(e -> selection.copperExtras() || !e.copperExtra()).toList();
    }

    public static TierSummary tierSummary(PoolEntry.Dimension dimension, int tier) {
        return TierSummary.from(catalog.entries(), dimension, tier, Setting.isCopperExtras());
    }

    public static void addUpBlocks() {
        try {
            blocks = candidates(Setting.getDifficulty(), Setting.isCopperExtras()).stream().map(PoolEntry::id).toList();
        } catch (IllegalArgumentException ex) {
            // Invalid edits remain visible in preparation; start/apply provides the detailed error.
            blocks = List.of();
        }
        allBlocks = new ArrayList<>(blocks);
        maxBlockAmount = blocks.size();
    }

    public static boolean refreshAvailableBlocksAndClampAmount() {
        // Capacity problems are reported before starting instead of silently reducing the requested game length.
        addUpBlocks();
        return false;
    }

    public static int clampBlockAmount(int requestedAmount, int availableBlocks) {
        if (availableBlocks < 0) throw new IllegalArgumentException("Available block amount cannot be negative");
        return Math.min(Math.max(10, requestedAmount), availableBlocks);
    }

    public static void validateSettings() {
        createSession(selection(Setting.getDifficulty(), Setting.isCopperExtras()), 0L);
    }

    private static Selection selection(DifficultyPreset.Kind kind, boolean copperExtras) {
        return kind == DifficultyPreset.Kind.CUSTOM ? customSelection(customSettings, copperExtras)
                : new Selection(presets.get(kind), catalog.entries(), copperExtras);
    }

    private static TaskSession createSession(Selection selection, long seed) {
        return new TaskSession(selection.preset(), selection.copperExtras(), Setting.getBlockAmount(), Setting.getMaxRollCount(),
                Setting.getCurrentGameMode() == Setting.GameMode.RACING, seed, selection.pool());
    }

    public static boolean checkBlock() {
        try {
            validateSettings();
            return true;
        } catch (IllegalArgumentException | IllegalStateException ex) {
            Bukkit.getLogger().severe("[BlockRacing] Invalid block-library settings: " + ex.getMessage());
            return false;
        }
    }

    public static void setupBlocks() {
        TaskSession next = createSession(selection(Setting.getDifficulty(), Setting.isCopperExtras()), new Random().nextLong());
        session = next;
        legacyPool = List.of();
        Game.redTeamCurrentBlockAmount = Game.blueTeamCurrentBlockAmount = 0;
        Game.redTeamRollCount = Game.blueTeamRollCount = 0;
        syncLists();
    }

    private static void syncLists() {
        if (session == null) return;
        redTeamBlocks = new ArrayList<>(session.seen("red"));
        blueTeamBlocks = new ArrayList<>(session.seen("blue"));
        redTeamRemainingBlocks = new ArrayList<>(session.active("red"));
        blueTeamRemainingBlocks = new ArrayList<>(session.active("blue"));
    }

    public static boolean complete(String team, String id) {
        if (session != null) {
            boolean success = session.complete(team, id);
            if (success) syncLists();
            return success;
        }
        List<String> remaining = team.equals("red") ? redTeamRemainingBlocks : blueTeamRemainingBlocks;
        if (!remaining.subList(0, Math.min(4, remaining.size())).contains(id)) return false;
        return remaining.remove(id);
    }

    public static boolean roll(String team) {
        if (session != null) {
            boolean success = session.roll(team);
            if (success) syncLists();
            return success;
        }
        List<String> remaining = team.equals("red") ? redTeamRemainingBlocks : blueTeamRemainingBlocks;
        List<String> seen = team.equals("red") ? redTeamBlocks : blueTeamBlocks;
        List<String> available = new ArrayList<>(legacyPool);
        available.removeAll(seen);
        available.removeAll(remaining);
        int amount = Math.min(4, remaining.size());
        if (available.size() < amount || amount == 0) return false;
        Collections.shuffle(available);
        List<String> replacements = new ArrayList<>(available.subList(0, amount));
        for (int i = 0; i < amount; i++) remaining.set(i, replacements.get(i));
        seen.addAll(replacements);
        return true;
    }

    public static void saveTasks(YamlConfiguration state) {
        state.set("tasks-legacy", session == null);
        if (session != null) session.save(state.createSection("tasks"));
        else state.set("legacy-pool", legacyPool);
    }

    public static void restoreTasks(YamlConfiguration state) {
        if (state.getInt("format-version", 1) >= 2 && !state.getBoolean("tasks-legacy")) {
            TaskSession restored = TaskSession.load(state.getConfigurationSection("tasks"), state.getInt("format-version") >= 3);
            BlockCatalog.validateLive(restored.pool());
            session = restored;
            syncLists();
            Game.redTeamCurrentBlockAmount = session.completed("red");
            Game.blueTeamCurrentBlockAmount = session.completed("blue");
            Game.redTeamTotalBlockAmount = Game.blueTeamTotalBlockAmount = session.total();
            Game.redTeamRollCount = session.rolls("red");
            Game.blueTeamRollCount = session.rolls("blue");
            return;
        }
        session = null;
        redTeamBlocks = canonical(state.getStringList("blocks.red-all"));
        blueTeamBlocks = canonical(state.getStringList("blocks.blue-all"));
        redTeamRemainingBlocks = canonical(state.getStringList("blocks.red-remaining"));
        blueTeamRemainingBlocks = canonical(state.getStringList("blocks.blue-remaining"));
        for (String team : List.of("red", "blue")) {
            List<String> remaining = team.equals("red") ? redTeamRemainingBlocks : blueTeamRemainingBlocks;
            List<String> history = team.equals("red") ? redTeamBlocks : blueTeamBlocks;
            int completed = team.equals("red") ? Game.redTeamCurrentBlockAmount : Game.blueTeamCurrentBlockAmount;
            int total = team.equals("red") ? Game.redTeamTotalBlockAmount : Game.blueTeamTotalBlockAmount;
            if (completed < 0 || total < 1 || completed + remaining.size() != total
                    || new HashSet<>(remaining).size() != remaining.size()) {
                throw new IllegalArgumentException("Invalid/duplicate legacy targets for " + team + "; save preserved for manual repair");
            }
            for (String id : remaining) if (!history.contains(id)) history.add(id);
        }
        if (state.contains("legacy-pool")) legacyPool = canonical(state.getStringList("legacy-pool"));
        else {
            List<String> old = new ArrayList<>(easyBlocks);
            if (Setting.isEnableMediumBlock()) old.addAll(mediumBlocks);
            if (Setting.isEnableHardBlock()) old.addAll(hardBlocks);
            if (Setting.isEnableDyedBlock()) old.addAll(dyedBlocks);
            if (Setting.isEnableEndBlock()) old.addAll(endBlocks);
            legacyPool = canonical(old).stream().distinct().toList();
        }
        java.util.logging.Logger.getLogger("BlockRacing").warning("[BlockRacing] Legacy match recovered with its original pending targets; new presets apply next match.");
    }

    private static List<String> canonical(List<String> ids) {
        List<String> result = new ArrayList<>();
        for (String id : ids) {
            var material = Materials.require(id);
            if (!material.isItem() || material.isAir()) throw new IllegalArgumentException("Invalid saved Material: " + id);
            result.add(material.getKey().toString());
        }
        return result;
    }

    // Compatibility helpers for existing extensions. Fresh matches use preset tier weights exclusively.
    @Deprecated public static int calculateEasyBlocksWeight(float p) { return (int) (100 - 80 * p); }
    @Deprecated public static int calculateMediumBlocksWeight(float p) { return p <= .4 ? (int) (20 + 40 * p / .4) : 60; }
    @Deprecated public static int calculateHardBlocksWeight(float p) { return p <= .5 ? (int) (1 + 19 * p / .5) : (int) (20 + 40 * (p - .5) / .5); }
    @Deprecated public static int calculateDyedBlocksWeight(float p) { return 10; }
}
