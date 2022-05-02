package committee.nova.firesafety.common.tools;

import net.minecraft.world.phys.Vec2;

import java.util.ArrayList;

public class DataReference {
    public static final ArrayList<Vec2> water = new ArrayList<>();

    public static void init() {
        for (float x = -5; x <= 5; x++) {
            for (float z = -5; z <= 5; z++) {
                water.add(new Vec2(x / 100F, z / 100F));
            }
        }
    }
}
