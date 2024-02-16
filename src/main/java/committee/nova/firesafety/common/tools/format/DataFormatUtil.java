package committee.nova.firesafety.common.tools.format;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;

public class DataFormatUtil {
    public static long vec3iToLong(Vec3i v) {
        return BlockPos.asLong(v.getX(), v.getY(), v.getZ());
    }

    public static long vec3ToLong(Vec3 v) {
        return BlockPos.asLong((int) v.x, (int) v.y, (int) v.z);
    }
}
