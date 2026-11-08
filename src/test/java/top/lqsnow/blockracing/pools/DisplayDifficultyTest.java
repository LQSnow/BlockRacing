package top.lqsnow.blockracing.pools;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DisplayDifficultyTest {
    @Test void allTiersUseTheSameDisplayAndScoringGroups() {
        int[] points = {1, 1, 2, 3, 3, 4};
        for (int tier = 1; tier <= 6; tier++) {
            DisplayDifficulty difficulty = DisplayDifficulty.forTier(tier);
            assertEquals(points[tier - 1], difficulty.points(false));
            assertEquals(points[tier - 1] + 2, difficulty.points(true));
        }
        assertEquals(DisplayDifficulty.EASY, DisplayDifficulty.forTier(2));
        assertEquals(DisplayDifficulty.HARD, DisplayDifficulty.forTier(5));
        assertEquals(DisplayDifficulty.NIGHTMARE, DisplayDifficulty.forTier(6));
        assertThrows(IllegalArgumentException.class, () -> DisplayDifficulty.forTier(0));
        assertThrows(IllegalArgumentException.class, () -> DisplayDifficulty.forTier(7));
    }
}
