package committee.nova.firesafety.common.tools.reference;

import committee.nova.firesafety.common.block.entity.init.BlockEntityInit;
import committee.nova.firesafety.common.block.init.BlockInit;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class BlockReference {
    public static final String EXTINGUISHER = "extinguisher";
    public static final String FIRE_ALARM = "fire_alarm";

    public static Block getRegisteredBlock(String id) {
        return BlockInit.blockList.get(id).get();
    }

    public static BlockEntityType<?> getRegisteredBlockEntityType(String id) {
        return BlockEntityInit.blockEntityList.get(id).get();
    }
}
