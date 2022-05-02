package committee.nova.firesafety.common.block.blockEntity.impl;

import committee.nova.firesafety.common.block.blockEntity.base.RecordableDeviceBlockEntity;
import committee.nova.firesafety.common.sound.init.SoundInit;
import committee.nova.firesafety.common.tools.PlayerHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.stream.Stream;

import static committee.nova.firesafety.common.block.base.AbstractCeilingDeviceBlock.ONFIRE;
import static committee.nova.firesafety.common.block.impl.ExtinguisherBlock.WATERED;
import static committee.nova.firesafety.common.block.reference.BlockReference.FIRE_ALARM;
import static committee.nova.firesafety.common.block.reference.BlockReference.getRegisteredBlockEntityType;
import static committee.nova.firesafety.common.tools.TagKeyReference.BURNING;
import static committee.nova.firesafety.common.tools.TagKeyReference.IGNORED;

@ParametersAreNonnullByDefault
public class FireAlarmBlockEntity extends RecordableDeviceBlockEntity {

    protected int fireStartedTick = 0;

    public FireAlarmBlockEntity(BlockPos pos, BlockState state) {
        this(getRegisteredBlockEntityType(FIRE_ALARM), pos, state);
    }

    public FireAlarmBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public boolean tickServer() {
        if (level == null) return false;
        final BlockState state = level.getBlockState(worldPosition);
        final long time = level.getDayTime();
        if (!isOnFireBelow()) {
            level.setBlockAndUpdate(worldPosition, state.setValue(ONFIRE, false));
            fireStartedTick = 0;
            return false;
        }
        level.setBlockAndUpdate(worldPosition, state.setValue(ONFIRE, true));
        fireStartedTick++;
        if (time % 100 != 0) return true;
        toListeningPlayers(level, player -> PlayerHandler.displayClientMessage(player, new TranslatableComponent("msg.firesafety.device.fire_detected",
                formatBlockPos(), (state.hasProperty(WATERED) && !state.getValue(WATERED)) ? new TranslatableComponent("phrase.firesafety.insufficient_water").getString() : "")));
        toListeningPlayers(level, player -> PlayerHandler.playSoundForThisPlayer(player, SoundInit.getSound(0), 1F, 1F));
        return true;
    }

    private boolean isOnFireBelow() {
        if (level == null) return false;
        final AABB range = new AABB(worldPosition.offset(3, 0, 3), worldPosition.offset(-3, -10, -3));
        final Stream<BlockState> states = level.getBlockStatesIfLoaded(range).filter(b -> b.is(Blocks.FIRE));
        if (states.findAny().isPresent()) return true;
        final List<LivingEntity> entityList = level.getEntitiesOfClass(LivingEntity.class, range, l -> (l.isOnFire() || l.getType().is(BURNING)) && !l.getType().is(IGNORED));
        return !entityList.isEmpty();
    }
}
