package committee.nova.firesafety.api;

import committee.nova.firesafety.FireSafety;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.HashMap;

public class ExtinguishableUtil {
    private static final HashMap<Short, Extinguishable> extinguishableList = new HashMap<>();

    public static void init() {
        addExtinguishable(Short.MAX_VALUE, new Extinguishable(b -> b.is(Blocks.FIRE), Blocks.AIR.defaultBlockState()));
    }

    public static void addExtinguishable(short priority, Extinguishable extinguishable) {
        if (!extinguishableList.containsKey(priority)) {
            extinguishableList.put(priority, extinguishable);
            return;
        }
        FireSafety.LOGGER.warn("Duplicate priority value {}, new extinguishable won't be added!", priority);
    }

    public static short getTargetIndex(BlockState state) {
        final short[] s = {Short.MIN_VALUE};
        extinguishableList.forEach((p, e) -> {
            if (p > s[0] && e.blockCondition().test(state)) s[0] = p;
        });
        return s[0];
    }

    @Nullable
    public static BlockState getTarget(short index) {
        return index == Short.MIN_VALUE ? null : extinguishableList.get(index).targetBlock();
    }
}
