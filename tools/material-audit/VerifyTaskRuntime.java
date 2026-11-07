import org.bukkit.configuration.file.YamlConfiguration;
import top.lqsnow.blockracing.managers.Block;
import top.lqsnow.blockracing.managers.Game;
import top.lqsnow.blockracing.pools.*;

import java.nio.file.*;
import java.util.*;

/** Developer smoke check against the real Paper registries. Does not start a server or generate worlds. */
public class VerifyTaskRuntime {
    private static void require(boolean value, String reason) {
        if (!value) throw new IllegalStateException(reason);
    }

    public static void main(String[] args) throws Exception {
        Path root = Path.of(args[0]).toAbsolutePath();
        Class.forName("net.minecraft.SharedConstants").getMethod("tryDetectVersion").invoke(null);
        Class.forName("net.minecraft.server.Bootstrap").getMethod("bootStrap").invoke(null);
        BlockCatalog catalog = BlockCatalog.load(root.resolve("src/main/resources/block-pools"));
        BlockCatalog.validateLive(catalog.entries());
        int matches = 0;
        for (var kind : DifficultyPreset.Kind.values()) {
            YamlConfiguration yaml = new YamlConfiguration();
            yaml.load(root.resolve("src/main/resources/presets/" + kind.id() + ".yml").toFile());
            for (boolean copper : List.of(false, true)) for (boolean racing : List.of(false, true)) {
                TaskSession session = new TaskSession(DifficultyPreset.load(yaml), copper, 50, 3, racing, 78, catalog.entries());
                for (String team : List.of("red", "blue")) {
                    for (int completed = 0; completed < 50; completed++) {
                        if (completed == 7 || completed == 26) session.roll(team);
                        for (String id : session.active(team)) {
                            require(session.preset().unlocked(session.find(id), completed, 50), "Premature target " + id);
                        }
                        require(session.complete(team, session.active(team).getLast()), "Could not complete target");
                        if (completed == 24) {
                            YamlConfiguration saved = new YamlConfiguration();
                            session.save(saved.createSection("tasks"));
                            YamlConfiguration restored = new YamlConfiguration();
                            restored.loadFromString(saved.saveToString());
                            session = TaskSession.load(restored.getConfigurationSection("tasks"));
                        }
                    }
                    require(session.completed(team) == 50 && session.active(team).isEmpty(), "Wrong target budget");
                }
                matches++;
            }
        }

        // Restore the old enum-name format and keep its original full pending queue.
        YamlConfiguration legacy = new YamlConfiguration();
        legacy.set("format-version", 1);
        List<String> original = List.of("DIRT", "STONE", "OAK_LOG", "COBBLESTONE", "SAND");
        legacy.set("blocks.red-all", original);
        legacy.set("blocks.blue-all", original);
        legacy.set("blocks.red-remaining", original.subList(1, 5));
        legacy.set("blocks.blue-remaining", original);
        legacy.set("legacy-pool", original);
        Game.redTeamCurrentBlockAmount = 1;
        Game.blueTeamCurrentBlockAmount = 0;
        Game.redTeamTotalBlockAmount = Game.blueTeamTotalBlockAmount = 5;
        Block.restoreTasks(legacy);
        require(Block.session() == null, "Legacy save unexpectedly adopted new rules");
        require(Block.redTeamRemainingBlocks.equals(List.of("minecraft:stone", "minecraft:oak_log", "minecraft:cobblestone", "minecraft:sand")), "Legacy targets changed");
        Block.saveTasks(legacy);
        legacy.set("format-version", 2);
        require(legacy.getBoolean("tasks-legacy"), "Missing legacy format marker");
        Block.restoreTasks(legacy);
        require(Block.blueTeamRemainingBlocks.size() == 5, "Lost legacy pending targets");
        legacy.set("format-version", 3);
        Block.restoreTasks(legacy);
        require(Block.blueTeamRemainingBlocks.size() == 5, "Lost legacy queue after v3 upgrade");

        // A real pre-pacing v2 fixture must continue its original sequence, not adopt new defaults.
        YamlConfiguration oldRules = new YamlConfiguration();
        oldRules.load(root.resolve("src/test/resources/legacy-task-session-v2.yml").toFile());
        Block.restoreTasks(oldRules);
        require(!Block.session().preset().pacing().enabled(), "Old match adopted pacing");
        for (String team : List.of("red", "blue")) {
            for (String frame : oldRules.getStringList("expected." + team)) {
                require(frame.equals(String.join(",", Block.session().active(team))), "Old v2 future sequence changed");
                if (!Block.session().active(team).isEmpty()) require(Block.complete(team, Block.session().active(team).getFirst()), "Old v2 completion failed");
            }
        }

        // Restore a new match through the production adapter, not just the pure task engine.
        TaskSession match = new TaskSession(DifficultyPreset.defaults(DifficultyPreset.Kind.HARD), false, 50, 3, true, 77, catalog.entries());
        for (int i = 0; i < 17; i++) match.complete("red", match.active("red").getFirst());
        require(match.roll("red"), "Expected legal roll");
        YamlConfiguration snapshot = new YamlConfiguration();
        snapshot.set("format-version", 3);
        match.save(snapshot.createSection("tasks"));
        Block.restoreTasks(snapshot);
        require(Game.redTeamCurrentBlockAmount == 17 && Game.redTeamTotalBlockAmount == 50 && Game.redTeamRollCount == 1, "Adapter restored wrong counters");
        require(Block.redTeamRemainingBlocks.equals(match.active("red")), "Adapter changed active slots");
        require(Block.session().preset().pacing().enabled(), "New match lost pacing rules");
        require(Block.complete("red", Block.redTeamRemainingBlocks.getFirst()), "Adapter completion failed");
        require(Block.session().completed("red") == 18 && Block.redTeamRemainingBlocks.size() == 4, "Adapter refill failed");
        String result = "{\"paper\":\"26.3.build.244-stable\",\"simulated_matches\":" + matches
                + ",\"snapshot_version\":3,\"legacy_v1_v2_v3_queue_restore\":true,\"legacy_pacing_sequence_preserved\":true,\"production_adapter_restore\":true,\"status\":\"passed\"}\n";
        Files.writeString(root.resolve(".runtime/material-audit/task-runtime-verification.json"), result);
    }
}
