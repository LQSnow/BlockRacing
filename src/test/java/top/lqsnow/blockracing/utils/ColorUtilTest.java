package top.lqsnow.blockracing.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ColorUtilTest {
    @Test
    void translatesOnlyValidLegacyColorCodes() {
        assertEquals("\u00A7cWarning & details", ColorUtil.t("&cWarning & details"));
        assertEquals("Unknown &z code", ColorUtil.t("Unknown &z code"));
        assertNull(ColorUtil.t(null));
    }
}
