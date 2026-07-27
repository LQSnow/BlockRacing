package top.lqsnow.blockracing.toolkit.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import top.lqsnow.blockracing.utils.ColorUtil;

import java.util.Collection;
import java.util.List;

public final class Texts {
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    private Texts() {
    }

    public static Component component(String text) {
        return LEGACY.deserialize(ColorUtil.t(text == null ? "" : text));
    }

    public static List<Component> components(Collection<String> lines) {
        return lines.stream().map(Texts::component).toList();
    }
}
