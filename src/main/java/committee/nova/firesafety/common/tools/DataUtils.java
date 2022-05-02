package committee.nova.firesafety.common.tools;

import net.minecraft.core.BlockPos;

import java.text.MessageFormat;

public class DataUtils {
    public static String formatBlockPos(BlockPos pos) {
        return MessageFormat.format("[{0}, {1}, {2}]", pos.getX(), pos.getY(), pos.getZ());
    }
}
