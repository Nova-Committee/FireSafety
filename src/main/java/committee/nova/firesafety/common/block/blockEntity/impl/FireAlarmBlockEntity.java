package committee.nova.firesafety.common.block.blockEntity.impl;

import committee.nova.firesafety.common.block.blockEntity.base.RecordableDeviceBlockEntity;
import committee.nova.firesafety.common.util.PlayerHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.stream.Stream;

import static committee.nova.firesafety.common.block.base.AbstractCeilingDeviceBlock.ONFIRE;
import static committee.nova.firesafety.common.block.reference.BlockReference.FIRE_ALARM;
import static committee.nova.firesafety.common.block.reference.BlockReference.getRegisteredBlockEntityType;

@ParametersAreNonnullByDefault
public class FireAlarmBlockEntity extends RecordableDeviceBlockEntity {
    public FireAlarmBlockEntity(BlockPos pos, BlockState state) {
        this(getRegisteredBlockEntityType(FIRE_ALARM), pos, state);
    }

    public FireAlarmBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public boolean tickServer(BlockState state) {
        if (level == null) return false;
        final long time = level.getDayTime();
        if (!isOnFireBelow()) {
            level.setBlockAndUpdate(worldPosition, state.setValue(ONFIRE, false));
            return false;
        }
        level.setBlockAndUpdate(worldPosition, state.setValue(ONFIRE, true));
        if (time % 100 != 0) return true;
        toListeningPlayers(level, player -> PlayerHandler.displayClientMessage(player, new TranslatableComponent("msg.firesafety.on_fire")));
        toListeningPlayers(level, player -> PlayerHandler.playSoundForThisPlayer(player, SoundEvents.CROSSBOW_SHOOT, 1F, 1F));
        return true;
    }

    private boolean isOnFireBelow() {
        if (level == null) return false;
        final Stream<BlockState> states = level.getBlockStatesIfLoaded(new AABB(worldPosition.offset(5, 0, 5), worldPosition.offset(-5, -10, -5))).filter(b -> b.is(Blocks.FIRE));
        return states.findAny().isPresent();
    }


}
