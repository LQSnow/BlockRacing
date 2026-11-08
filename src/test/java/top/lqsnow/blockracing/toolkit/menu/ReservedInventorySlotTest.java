package top.lqsnow.blockracing.toolkit.menu;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;
import java.util.Objects;

class ReservedInventorySlotTest {
    @Test void fullLegacyChestSurvivesRepeatedSaveRestoreAndLaterFreesItsNavigationSlot() {
        String[] legacy = new String[54];
        for (int i = 0; i < legacy.length; i++) legacy[i] = "stack-" + i;
        var state = new ReservedInventorySlot<String>(53);
        String[] visible = state.install(legacy, "BACK", "BACK"::equals, Objects::isNull);
        assertEquals("BACK", visible[53]);
        assertEquals("stack-53", state.pending());
        assertArrayEquals(legacy, state.savedContents(visible));
        visible = state.install(visible, "BACK", "BACK"::equals, Objects::isNull);
        var restored = new ReservedInventorySlot<String>(53);
        visible = restored.install(state.savedContents(visible), "BACK", "BACK"::equals, Objects::isNull);
        assertArrayEquals(legacy, restored.savedContents(visible));
        visible[7] = null;
        visible = restored.flush(visible, Objects::isNull);
        assertEquals("stack-53", visible[7]);
        assertNull(restored.pending());
        assertNull(restored.savedContents(visible)[53]);
        assertEquals(53, Arrays.stream(restored.savedContents(visible)).filter(Objects::nonNull).count());
        assertEquals("BACK", visible[53]);
    }

    @Test void anAvailableSlotReceivesTheLegacyItemImmediatelyWithoutSerializingTheControl() {
        var state = new ReservedInventorySlot<String>(53);
        String[] original = new String[54];
        original[53] = "valuable";
        String[] contents = state.install(original, "BACK", "BACK"::equals, Objects::isNull);
        assertEquals("valuable", contents[0]);
        assertNull(state.pending());
        assertNull(state.savedContents(contents)[53]);
        assertEquals("valuable", original[53], "Input save data must not be mutated");
    }
}
