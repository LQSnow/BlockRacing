package top.lqsnow.blockracing.managers;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LanguageManagerTest {
    @Test
    void automaticPreferenceFollowsClientLanguage() {
        assertTrue(LanguageManager.usesChinese(LanguageManager.Preference.AUTO, Locale.SIMPLIFIED_CHINESE));
        assertFalse(LanguageManager.usesChinese(LanguageManager.Preference.AUTO, Locale.US));
    }

    @Test
    void explicitPreferenceOverridesClientLanguage() {
        assertTrue(LanguageManager.usesChinese(LanguageManager.Preference.ZH_CN, Locale.US));
        assertFalse(LanguageManager.usesChinese(LanguageManager.Preference.EN_US, Locale.SIMPLIFIED_CHINESE));
    }
}
