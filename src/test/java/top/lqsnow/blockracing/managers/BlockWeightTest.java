package top.lqsnow.blockracing.managers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BlockWeightTest {

    @Test
    void easyWeightDecreasesAcrossGame() {
        assertEquals(100, Block.calculateEasyBlocksWeight(0));
        assertEquals(60, Block.calculateEasyBlocksWeight(0.5f));
        assertEquals(20, Block.calculateEasyBlocksWeight(1));
    }

    @Test
    void mediumWeightRisesThenPlateaus() {
        assertEquals(20, Block.calculateMediumBlocksWeight(0));
        assertEquals(60, Block.calculateMediumBlocksWeight(0.4f));
        assertEquals(60, Block.calculateMediumBlocksWeight(1));
    }

    @Test
    void hardWeightRisesAcrossBothSegments() {
        assertEquals(1, Block.calculateHardBlocksWeight(0));
        assertEquals(20, Block.calculateHardBlocksWeight(0.5f));
        assertEquals(60, Block.calculateHardBlocksWeight(1));
    }

    @Test
    void dyedWeightIsConstant() {
        assertEquals(10, Block.calculateDyedBlocksWeight(0));
        assertEquals(10, Block.calculateDyedBlocksWeight(1));
    }
}
