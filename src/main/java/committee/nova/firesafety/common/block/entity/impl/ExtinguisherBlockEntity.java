package committee.nova.firesafety.common.block.entity.impl;

import committee.nova.firesafety.api.event.FireExtinguishedEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import static committee.nova.firesafety.api.FireSafetyApi.*;
import static committee.nova.firesafety.common.block.impl.ExtinguisherBlock.WATERED;
import static committee.nova.firesafety.common.config.Configuration.*;
import static committee.nova.firesafety.common.tools.math.RayTraceUtil.vecToIntString;
import static committee.nova.firesafety.common.tools.misc.PlayerHandler.notifyServerPlayer;
import static committee.nova.firesafety.common.tools.misc.PlayerHandler.playSoundForThisPlayer;
import static committee.nova.firesafety.common.tools.reference.BlockReference.EXTINGUISHER;
import static committee.nova.firesafety.common.tools.reference.BlockReference.getRegisteredBlockEntityType;
import static net.minecraft.core.BlockPos.betweenClosed;
import static net.minecraft.core.Direction.UP;
import static net.minecraft.sounds.SoundEvents.BUCKET_FILL;
import static net.minecraftforge.fluids.FluidType.BUCKET_VOLUME;

@ParametersAreNonnullByDefault
public class ExtinguisherBlockEntity extends FireAlarmBlockEntity {
    protected final FluidTank tank = new FluidTank(8 * BUCKET_VOLUME, f -> f.getFluid().isSame(Fluids.WATER));
    private final LazyOptional<IFluidHandler> holder = LazyOptional.of(() -> tank);

    public ExtinguisherBlockEntity(BlockPos pos, BlockState state) {
        super(getRegisteredBlockEntityType(EXTINGUISHER), pos, state);
    }

    public boolean tickServer() {
        final var needExtinguish = super.tickServer();
        if (level == null) return false;
        final var state = level.getBlockState(worldPosition);
        if (tank.isEmpty()) {
            level.setBlockAndUpdate(worldPosition, state.setValue(WATERED, false));
            return true;
        }
        level.setBlockAndUpdate(worldPosition, state.setValue(WATERED, true));
        if (!needExtinguish) return true;
        if (fireStartedTick <= extinguishDelay.get()) return true;
        tryExtinguish();
        return true;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        tank.readFromNBT(tag);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tank.writeToNBT(tag);
    }

    private void tryExtinguish() {
        assert level != null;
        final int remain = tank.getFluidAmount();
        final int consumption = waterConsumption.get();
        extinguish(tank.drain(consumption, IFluidHandler.FluidAction.EXECUTE).getAmount());
        if (remain > consumption) return;
        toListeningPlayers(level, player -> playSoundForThisPlayer(player, BUCKET_FILL, 1F, 1F));
        toListeningPlayers(level, player -> notifyServerPlayer(player, Component.translatable("msg.firesafety.device.insufficient_water", vecToIntString(worldPosition))));
    }

    private void extinguish(int amount) {
        assert level != null;
        final var posList = betweenClosed(monitoringAreaPos()[0], monitoringAreaPos()[1]);
        final var r = level.random;
        final int a = (int) (amount * 100F / waterConsumption.get()) + 1;
        for (final var p : posList) {
            if (r.nextInt(a) < 100 - blockExtinguishingPossibility.get() * 100) continue;
            final short i = getTargetBlockIndex(level, p);
            if (i == Short.MIN_VALUE) continue;
            final var b = getTargetBlock(i);
            level.setBlockAndUpdate(p, b.targetBlock().apply(level, p));
            b.extinguishedInfluence().accept(level, p);
            final var event = new FireExtinguishedEvent(FireExtinguishedEvent.ExtinguisherType.DEVICE, level, worldPosition, p, i);
            MinecraftForge.EVENT_BUS.post(event);
        }
        final var entityList = level.getEntitiesOfClass(Entity.class, monitoringArea(), l -> getTargetEntityIndex(level, l) > Short.MIN_VALUE);
        for (final var e : entityList) {
            if (r.nextInt(a) < 100 - entityExtinguishingPossibility.get() * 100) continue;
            final short i = getTargetEntityIndex(level, e);
            if (i == Short.MIN_VALUE) continue;
            final var t = getTargetEntity(i);
            t.entityAction().accept(level, e);
            final var event = new FireExtinguishedEvent(FireExtinguishedEvent.ExtinguisherType.DEVICE, level, worldPosition, e, i);
            MinecraftForge.EVENT_BUS.post(event);
        }
    }

    @Override
    @Nonnull
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
        return (capability != ForgeCapabilities.FLUID_HANDLER || facing != UP) ? super.getCapability(capability, facing) : holder.cast();
    }

    public FluidTank getTank() {
        return tank;
    }

    public int getWaterStorage() {
        return tank.getFluidAmount();
    }

    public int getMaxWaterStorage() {
        return tank.getCapacity();
    }
}
