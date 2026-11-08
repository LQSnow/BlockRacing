package top.lqsnow.blockracing.toolkit.text;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PregameLinesTest {
    @Test void mergesOldChineseAndEnglishDifficultyRowsWithoutEditingLanguageFiles() {
        assertEquals(List.of("模式", "§7方块库 §f%blocks%", " "),
                PregameLines.mergeDifficulty(List.of("模式", "§7方块库", "%blocks%", " ")));
        assertEquals(List.of("&7Block Pools §f&f%blocks%"),
                PregameLines.mergeDifficulty(List.of("&7Block Pools", "&f%blocks%")));
    }

    @Test void preservesAlreadyMergedRowsAndUnrelatedCustomLayouts() {
        List<String> merged = List.of("§7方块库 §f%blocks%", " ");
        assertEquals(merged, PregameLines.mergeDifficulty(merged));
        List<String> unrelated = List.of("目标 %block_amount%", "%blocks%");
        assertEquals(unrelated, PregameLines.mergeDifficulty(unrelated));
    }
}
