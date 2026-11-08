package top.lqsnow.blockracing.toolkit.menu;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class MenuHistoryTest {
    @Test void memoryIsOptInAndIsolatedBetweenPlayers() {
        MenuHistory history = new MenuHistory();
        UUID first = UUID.randomUUID(), second = UUID.randomUUID();
        var chest = new MenuHistory.Destination(MenuHistory.Kind.CHEST, "red", 1);
        history.record(first, chest);
        assertTrue(history.last(first).isEmpty());
        history.enable(first, true);
        history.record(first, chest);
        assertEquals(chest, history.last(first).orElseThrow());
        assertFalse(history.enabled(second));
        assertTrue(history.last(second).isEmpty());
        history.record(first, new MenuHistory.Destination(MenuHistory.Kind.MAIN, "", 0));
        assertEquals(MenuHistory.Kind.MAIN, history.last(first).orElseThrow().kind());
        history.enable(first, false);
        assertTrue(history.last(first).isEmpty());
    }

    @Test void newMatchesForgetDestinationsWhileKeepingPlayerPreferences() {
        MenuHistory history = new MenuHistory();
        UUID player = UUID.randomUUID();
        history.enable(player, true);
        history.record(player, new MenuHistory.Destination(MenuHistory.Kind.LANGUAGE, "", 0));
        history.resetDestinations();
        assertTrue(history.enabled(player));
        assertTrue(history.last(player).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> new MenuHistory.Destination(MenuHistory.Kind.CHEST, "blue", 53));
    }
}
