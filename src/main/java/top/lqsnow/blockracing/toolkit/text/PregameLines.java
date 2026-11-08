package top.lqsnow.blockracing.toolkit.text;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class PregameLines {
    private static final Set<String> OLD_LABELS = Set.of("方块库", "难度", "难度/方块库", "blockpools", "blocklibrary", "difficulty");
    private PregameLines() {}

    /** Old language files split the label and its value across two rows. */
    public static List<String> mergeDifficulty(List<String> templates) {
        List<String> result = new ArrayList<>();
        for (String line : templates) {
            String plain = strip(line);
            if (plain.equals("%blocks%") && !result.isEmpty() && OLD_LABELS.contains(strip(result.getLast()))) {
                result.set(result.size() - 1, result.getLast() + " §f" + line);
            } else result.add(line);
        }
        return List.copyOf(result);
    }

    private static String strip(String value) {
        return value.replaceAll("(?i)[§&][0-9A-FK-OR]", "").replaceAll("[\\s：:]", "").toLowerCase(Locale.ROOT);
    }
}
