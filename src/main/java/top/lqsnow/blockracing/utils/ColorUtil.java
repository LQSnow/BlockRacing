package top.lqsnow.blockracing.utils;

import java.util.regex.Pattern;

public class ColorUtil {
    private static final Pattern LEGACY_COLOR = Pattern.compile("(?i)&([0-9A-FK-ORX])");

    public static String t(String str) {
        return str == null ? null : LEGACY_COLOR.matcher(str).replaceAll("\u00A7$1");
    }
}
