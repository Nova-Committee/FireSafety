package committee.nova.firesafety.common.tools.string;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;

import java.text.MessageFormat;

public class StringUtil {
    public static String formattedNumber(float raw, int amount) {
        return String.format(raw > 10.0F ? "%.0f" : MessageFormat.format("%.{0}f", amount), raw);
    }

    public static MutableComponent wrapIn(String key, Component component) {
        return new TranslatableComponent(key, component);
    }

    public static MutableComponent wrapInArrows(Component component) {
        return wrapIn("format.firesafety.arrows", component);
    }
}
