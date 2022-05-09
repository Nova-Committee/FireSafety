package committee.nova.firesafety.common.block.blockEntity.impl;

import committee.nova.firesafety.api.FireSafetyApi;
import committee.nova.firesafety.common.block.blockEntity.base.RecordableDeviceBlockEntity;
import committee.nova.firesafety.common.sound.init.SoundInit;
import committee.nova.firesafety.common.tools.PlayerHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import javax.annotation.ParametersAreNonnullByDefault;

import static committee.nova.firesafety.common.block.base.AbstractCeilingDeviceBlock.ONFIRE;
import static committee.nova.firesafety.common.block.impl.ExtinguisherBlock.WATERED;
import static committee.nova.firesafety.common.block.reference.BlockReference.FIRE_ALARM;
import static committee.nova.firesafety.common.block.reference.BlockReference.getRegisteredBlockEntityType;
import static committee.nova.firesafety.common.config.Configuration.*;
import static committee.nova.firesafety.common.tools.PlayerHandler.displayClientMessage;
import static committee.nova.firesafety.common.tools.PlayerHandler.notifyServerPlayer;

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
        final int[] c = fireSourceCount();
        if (c[0] + c[1] <= 0) {
            level.setBlockAndUpdate(worldPosition, state.setValue(ONFIRE, false));
            fireStartedTick = 0;
            return false;
        }
        level.setBlockAndUpdate(worldPosition, state.setValue(ONFIRE, true));
        fireStartedTick++;
        if (level.getDayTime() % 100 != 25) return level.getDayTime() % 50 == 0;
        final Component msg = new TranslatableComponent("msg.firesafety.device.fire_detected",
                formatBlockPos(), c[0], c[1], (state.hasProperty(WATERED) && !state.getValue(WATERED)) ? new TranslatableComponent("phrase.firesafety.insufficient_water").getString() : "");
        toListeningPlayers(level, player -> {
            if (notifyByChat.get()) notifyServerPlayer(player, msg);
            else displayClientMessage(player, msg);
        });
        toListeningPlayers(level, player -> PlayerHandler.playSoundForThisPlayer(player, SoundInit.getSound(0), .5F, 1F));
        level.playSound(null, worldPosition, SoundInit.getSound(0), SoundSource.BLOCKS, .8F, 1F);
        return false;
    }

    private int[] fireSourceCount() {
        if (level == null) return new int[]{0, 0};
        final AABB range = monitoringArea();
        int b = 0;
        for (final BlockPos p : BlockPos.betweenClosed(monitoringAreaPos()[0], monitoringAreaPos()[1]))
            if (FireSafetyApi.getTargetBlockIndex(level, p) > Short.MIN_VALUE) b++;
        return new int[]{b, level.getEntitiesOfClass(Entity.class, range, l -> FireSafetyApi.getTargetEntityIndex(level, l) > Short.MIN_VALUE).size()};
    }

    public BlockPos[] monitoringAreaPos() {
        final int w = fireAlarmMonitoringWidth.get();
        final int h = fireAlarmMonitoringHeight.get();
        return new BlockPos[]{worldPosition.offset(w, 0, w), worldPosition.offset(-w, -h, -w)};
    }

    public AABB monitoringArea() {
        final BlockPos[] p = monitoringAreaPos();
        return new AABB(p[0], p[1]);
    }
}
