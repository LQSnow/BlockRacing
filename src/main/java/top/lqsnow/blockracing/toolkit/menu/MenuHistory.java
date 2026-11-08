package top.lqsnow.blockracing.toolkit.menu;

import java.util.*;

/** Stores destinations rather than inventories, so remembered menus are always recreated. */
public final class MenuHistory {
    public enum Kind { MAIN, CHEST_SELECT, CHEST, WAYPOINTS, TEAMMATES, LANGUAGE }
    public record Destination(Kind kind, String team, int index) {
        public Destination {
            Objects.requireNonNull(kind);
            Objects.requireNonNull(team);
            if (index < 0 || index > 52) throw new IllegalArgumentException("Invalid chest index");
        }
    }
    private final Set<UUID> enabled = new HashSet<>();
    private final Map<UUID, Destination> destinations = new HashMap<>();

    public boolean enabled(UUID player) { return enabled.contains(player); }
    public void enable(UUID player, boolean value) {
        if (value) enabled.add(player);
        else { enabled.remove(player); destinations.remove(player); }
    }
    public void record(UUID player, Destination destination) {
        if (enabled(player)) destinations.put(player, destination);
    }
    public Optional<Destination> last(UUID player) { return Optional.ofNullable(destinations.get(player)); }
    public void resetDestinations() { destinations.clear(); }
    public Set<UUID> enabledPlayers() { return Set.copyOf(enabled); }
}
