package committee.nova.firesafety.common.util;

import net.minecraft.world.phys.Vec2;

import java.util.ArrayList;

public class DataReference {
    public static final ArrayList<Vec2> water = new ArrayList<>();

    public static void init() {
        for (int x = -10; x <= 10; x++) {
            for (int z = -10; z <= 10; z++) {
                water.add(new Vec2(x / 10F, z / 10F));
            }
        }
    }
}
