package top.lqsnow.blockracing.managers;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GameTargetRevealTest {
    @Test
    void returnsTheNewFourthVisibleTargetAfterCompletion() {
        List<String> remaining = new ArrayList<>(List.of("A", "B", "C", "D", "E", "F"));

        assertEquals("E", Game.removeAndGetNewVisibleTarget(remaining, "B"));
        assertEquals(List.of("A", "C", "D", "E", "F"), remaining);
    }

    @Test
    void returnsNothingWhenNoHiddenTargetRemains() {
        List<String> remaining = new ArrayList<>(List.of("A", "B", "C", "D"));

        assertNull(Game.removeAndGetNewVisibleTarget(remaining, "A"));
        assertEquals(List.of("B", "C", "D"), remaining);
    }
}
