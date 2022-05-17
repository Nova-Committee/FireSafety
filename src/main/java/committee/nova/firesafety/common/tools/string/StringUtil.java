package committee.nova.firesafety.common.tools.string;

import java.text.MessageFormat;

public class StringUtil {
    public static String formattedNumber(float raw, int amount) {
        return String.format(raw > 10.0F ? "%.0f" : MessageFormat.format("%.{0}f", amount), raw);
    }
}
