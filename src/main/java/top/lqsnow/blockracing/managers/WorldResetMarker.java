package top.lqsnow.blockracing.managers;

import org.bukkit.Bukkit;
import top.lqsnow.blockracing.Main;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

public final class WorldResetMarker {
    public static final String FILE_NAME = "reset-worlds.flag";
    private static final DateTimeFormatter BACKUP_TIME =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").withZone(ZoneId.systemDefault());

    private WorldResetMarker() {
    }

    public static boolean request() {
        File marker = new File(Main.getInstance().getDataFolder(), FILE_NAME);
        try {
            Files.writeString(marker.toPath(),
                    "Requested by an approved /restartgame vote at " + Instant.now() + System.lineSeparator(),
                    StandardCharsets.UTF_8);
            return true;
        } catch (IOException ex) {
            Main.getInstance().getLogger().log(Level.SEVERE,
                    "Unable to create the world-reset marker; worlds will not be reset.", ex);
            return false;
        }
    }

    /**
     * Runs from JavaPlugin#onLoad, before BlockRacing is enabled. Paper normally
     * has no worlds open at this point. If another loader changes that ordering,
     * the reset is refused and the marker is retained instead of touching a live
     * world.
     */
    public static void processPendingReset(Main plugin) {
        Path marker = plugin.getDataFolder().toPath().resolve(FILE_NAME);
        if (!Files.isRegularFile(marker)) {
            return;
        }
        if (!Bukkit.getWorlds().isEmpty()) {
            plugin.getLogger().severe(
                    "World reset marker found after worlds were already loaded. "
                            + "No files were changed; the marker was retained.");
            return;
        }

        try {
            Path dataFolder = plugin.getDataFolder().toPath().toAbsolutePath().normalize();
            Path pluginsFolder = dataFolder.getParent();
            Path serverRoot = pluginsFolder == null ? null : pluginsFolder.getParent();
            if (serverRoot == null) {
                throw new IOException("Unable to resolve the server root from " + dataFolder);
            }
            String levelName = readLevelName(serverRoot);
            Path configuredWorld = Path.of(levelName);
            if (configuredWorld.isAbsolute()) {
                throw new IOException("level-name must be relative for automatic world reset: " + levelName);
            }
            Path primaryWorld = serverRoot.resolve(configuredWorld).normalize();
            if (!primaryWorld.startsWith(serverRoot) || primaryWorld.equals(serverRoot)) {
                throw new IOException("Unsafe level-name path: " + levelName);
            }

            List<Path> worlds = List.of(
                    primaryWorld,
                    primaryWorld.resolveSibling(primaryWorld.getFileName() + "_nether"),
                    primaryWorld.resolveSibling(primaryWorld.getFileName() + "_the_end")
            );
            Path backup = uniqueBackupDirectory(serverRoot);
            Files.createDirectories(backup);
            List<Move> completed = new ArrayList<>();
            boolean playerStorageRecreated = false;
            try {
                for (int i = 0; i < worlds.size(); i++) {
                    Path world = worlds.get(i);
                    if (!Files.exists(world)) {
                        continue;
                    }
                    Path destination = backup.resolve(world.getFileName().toString());
                    if (i == 0) {
                        moveWorldContents(world, destination, completed);
                    } else {
                        move(world, destination);
                        completed.add(new Move(world, destination));
                    }
                }
                recreatePlayerStorage(primaryWorld);
                playerStorageRecreated = true;
                Files.delete(marker);
                plugin.getLogger().info("Backed up old worlds to " + backup
                        + "; Paper will generate a fresh world.");
            } catch (IOException moveFailure) {
                if (playerStorageRecreated) {
                    removeEmptyPlayerStorage(primaryWorld, plugin);
                }
                rollback(completed, plugin);
                throw moveFailure;
            }
        } catch (Exception ex) {
            plugin.getLogger().log(Level.SEVERE,
                    "Automatic world reset failed. No new world will be forced; "
                            + "the reset marker was retained for the next startup.", ex);
        }
    }

    private static String readLevelName(Path serverRoot) throws IOException {
        Properties properties = new Properties();
        Path serverProperties = serverRoot.resolve("server.properties");
        if (Files.isRegularFile(serverProperties)) {
            try (InputStream input = Files.newInputStream(serverProperties)) {
                properties.load(input);
            }
        }
        return properties.getProperty("level-name", "world").trim();
    }

    private static Path uniqueBackupDirectory(Path serverRoot) {
        Path backups = serverRoot.resolve("world-backups");
        String base = BACKUP_TIME.format(Instant.now());
        Path candidate = backups.resolve(base);
        int suffix = 1;
        while (Files.exists(candidate)) {
            candidate = backups.resolve(base + "-" + suffix++);
        }
        return candidate;
    }

    /**
     * Paper creates these directories before plugins are loaded and keeps their
     * paths for the lifetime of the server. Moving the old world therefore
     * requires recreating the empty directory structure immediately.
     */
    private static void recreatePlayerStorage(Path world) throws IOException {
        Path players = world.resolve("players");
        try {
            Files.createDirectories(players.resolve("data"));
            Files.createDirectories(players.resolve("stats"));
            Files.createDirectories(players.resolve("advancements"));
        } catch (IOException ex) {
            removeEmptyPlayerStorage(world, null);
            throw ex;
        }
    }

    private static void removeEmptyPlayerStorage(Path world, Main plugin) {
        Path players = world.resolve("players");
        List<Path> directories = List.of(
                players.resolve("advancements"),
                players.resolve("stats"),
                players.resolve("data"),
                players
        );
        for (Path directory : directories) {
            try {
                Files.deleteIfExists(directory);
            } catch (IOException ex) {
                if (plugin != null) {
                    plugin.getLogger().log(Level.WARNING,
                            "Could not clean temporary player storage " + directory, ex);
                }
            }
        }
    }

    /**
     * Paper acquires the primary world's session.lock before Bukkit plugins are
     * loaded. On Windows that open file prevents moving the world directory as a
     * whole, so keep only the lock in place and move every other entry.
     */
    private static void moveWorldContents(Path world, Path destination,
                                          List<Move> completed) throws IOException {
        Files.createDirectories(destination);
        try (var entries = Files.list(world)) {
            for (Path source : entries.toList()) {
                if (source.getFileName().toString().equals("session.lock")) {
                    continue;
                }
                Path target = destination.resolve(source.getFileName().toString());
                move(source, target);
                completed.add(new Move(source, target));
            }
        }
    }

    private static void rollback(List<Move> completed, Main plugin) {
        List<Move> reversed = new ArrayList<>(completed);
        Collections.reverse(reversed);
        for (Move move : reversed) {
            try {
                move(move.destination(), move.source());
            } catch (IOException rollbackFailure) {
                plugin.getLogger().log(Level.SEVERE,
                        "Could not roll back world directory " + move.source(), rollbackFailure);
            }
        }
    }

    private static void move(Path source, Path destination) throws IOException {
        try {
            Files.move(source, destination, StandardCopyOption.ATOMIC_MOVE);
        } catch (java.nio.file.AtomicMoveNotSupportedException ex) {
            Files.move(source, destination);
        }
    }

    private record Move(Path source, Path destination) {
    }
}
