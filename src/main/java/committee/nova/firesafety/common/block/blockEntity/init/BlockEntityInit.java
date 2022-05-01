package committee.nova.firesafety.common.block.blockEntity.init;

import com.mojang.datafixers.DSL;
import committee.nova.firesafety.common.block.blockEntity.impl.ExtinguisherBlockEntity;
import committee.nova.firesafety.common.block.blockEntity.impl.FireAlarmBlockEntity;
import committee.nova.firesafety.common.block.reference.BlockReference;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;

import static committee.nova.firesafety.common.block.reference.BlockReference.EXTINGUISHER;
import static committee.nova.firesafety.common.block.reference.BlockReference.FIRE_ALARM;
import static committee.nova.firesafety.common.util.RegistryHandler.BLOCK_ENTITIES;

public class BlockEntityInit {
    public static final HashMap<String, RegistryObject<BlockEntityType<?>>> blockEntityList = new HashMap<>();

    public static void init() {
        blockEntityList.put(FIRE_ALARM, BLOCK_ENTITIES.register(FIRE_ALARM, () -> BlockEntityType.Builder.of(FireAlarmBlockEntity::new, BlockReference.getRegisteredBlock(FIRE_ALARM)).build(DSL.remainderType())));
        blockEntityList.put(EXTINGUISHER, BLOCK_ENTITIES.register(EXTINGUISHER, () -> BlockEntityType.Builder.of(ExtinguisherBlockEntity::new, BlockReference.getRegisteredBlock(EXTINGUISHER)).build(DSL.remainderType())));
    }
}
