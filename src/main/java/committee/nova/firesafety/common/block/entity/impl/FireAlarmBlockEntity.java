package committee.nova.firesafety.common.block.entity.impl;

import committee.nova.firesafety.common.block.entity.base.RecordableDeviceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import javax.annotation.ParametersAreNonnullByDefault;

import static committee.nova.firesafety.api.FireSafetyApi.*;
import static committee.nova.firesafety.common.block.base.AbstractCeilingDeviceBlock.ONFIRE;
import static committee.nova.firesafety.common.block.impl.ExtinguisherBlock.WATERED;
import static committee.nova.firesafety.common.config.Configuration.*;
import static committee.nova.firesafety.common.tools.math.RayTraceUtil.vecToIntString;
import static committee.nova.firesafety.common.tools.misc.PlayerHandler.*;
import static committee.nova.firesafety.common.tools.reference.BlockReference.FIRE_ALARM;
import static committee.nova.firesafety.common.tools.reference.BlockReference.getRegisteredBlockEntityType;
import static committee.nova.firesafety.common.tools.sound.init.SoundInit.getSound;
import static net.minecraft.core.BlockPos.betweenClosed;
import static net.minecraft.sounds.SoundSource.BLOCKS;

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
        final var state = level.getBlockState(worldPosition);
        final int[] c = fireSourceCount();
        if (c[0] + c[1] <= 0) {
            level.setBlockAndUpdate(worldPosition, state.setValue(ONFIRE, false));
            fireStartedTick = 0;
            return false;
        }
        level.setBlockAndUpdate(worldPosition, state.setValue(ONFIRE, true));
        fireStartedTick++;
        if (level.getDayTime() % 100 != 25) return level.getDayTime() % 50 == 0;
        final var msg = Component.translatable("msg.firesafety.device.fire_detected",
                vecToIntString(worldPosition), c[0], c[1], (state.hasProperty(WATERED) && !state.getValue(WATERED)) ? Component.translatable("phrase.firesafety.insufficient_water").getString() : "");
        toListeningPlayers(level, player -> {
            if (notifyByChat.get()) notifyServerPlayer(player, msg);
            else displayClientMessage(player, msg);
        });
        toListeningPlayers(level, player -> playSoundForThisPlayer(player, getSound(0), .5F, 1F));
        level.playSound(null, worldPosition, getSound(0), BLOCKS, .8F, 1F);
        return false;
    }

    private int[] fireSourceCount() {
        if (level == null) return new int[]{0, 0};
        final var range = monitoringArea();
        int b = 0;
        final var blocks = betweenClosed(monitoringAreaPos()[0], monitoringAreaPos()[1]);
        for (final var p : blocks) {
            final var index = getTargetBlockIndex(level, p);
            if (index > Short.MIN_VALUE && getTargetBlock(index).detectable().test(level, p)) b++;
        }
        return new int[]{b, level.getEntitiesOfClass(Entity.class, range, e -> {
            final var index = getTargetEntityIndex(level, e);
            return index > Short.MIN_VALUE && getTargetEntity(index).detectable().test(level, e);
        }).size()};
    }

    public BlockPos[] monitoringAreaPos() {
        final int w = fireAlarmMonitoringWidth.get();
        final int h = fireAlarmMonitoringHeight.get();
        return new BlockPos[]{worldPosition.offset(w, 0, w), worldPosition.offset(-w, -h, -w)};
    }

    public AABB monitoringArea() {
        final var p = monitoringAreaPos();
        return new AABB(p[0], p[1]);
    }
}
