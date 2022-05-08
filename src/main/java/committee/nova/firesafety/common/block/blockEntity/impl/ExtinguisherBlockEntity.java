package committee.nova.firesafety.common.block.blockEntity.impl;

import committee.nova.firesafety.api.FireSafetyApi;
import committee.nova.firesafety.common.tools.PlayerHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Random;

import static committee.nova.firesafety.common.block.impl.ExtinguisherBlock.WATERED;
import static committee.nova.firesafety.common.block.reference.BlockReference.EXTINGUISHER;
import static committee.nova.firesafety.common.block.reference.BlockReference.getRegisteredBlockEntityType;
import static committee.nova.firesafety.common.config.Configuration.*;
import static net.minecraft.sounds.SoundEvents.BUCKET_FILL;
import static net.minecraft.sounds.SoundEvents.GENERIC_EXTINGUISH_FIRE;

@ParametersAreNonnullByDefault
public class ExtinguisherBlockEntity extends FireAlarmBlockEntity {
    protected final FluidTank tank = new FluidTank(8 * FluidAttributes.BUCKET_VOLUME, f -> f.getFluid().isSame(Fluids.WATER));
    private final LazyOptional<IFluidHandler> holder = LazyOptional.of(() -> tank);

    public ExtinguisherBlockEntity(BlockPos pos, BlockState state) {
        super(getRegisteredBlockEntityType(EXTINGUISHER), pos, state);
    }

    public boolean tickServer() {
        final boolean needExtinguish = super.tickServer();
        if (level == null) return false;
        final BlockState state = level.getBlockState(worldPosition);
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
        toListeningPlayers(level, player -> PlayerHandler.playSoundForThisPlayer(player, BUCKET_FILL, 1F, 1F));
        toListeningPlayers(level, player -> PlayerHandler.notifyServerPlayer(player, new TranslatableComponent("msg.firesafety.device.insufficient_water", formatBlockPos())));
    }

    private void extinguish(int amount) {
        assert level != null;
        final Iterable<BlockPos> posList = BlockPos.betweenClosed(monitoringAreaPos()[0], monitoringAreaPos()[1]);
        final Random r = level.random;
        final int a = (int) (amount * 100F / waterConsumption.get()) + 1;
        for (final BlockPos p : posList) {
            if (r.nextInt(a) < 100 - blockExtinguishingPossibility.get() * 100) continue;
            final short i = FireSafetyApi.getTargetBlockStateIndex(level, level.getBlockState(p));
            if (i == Short.MIN_VALUE) continue;
            level.setBlockAndUpdate(p, FireSafetyApi.getTargetBlockState(i));
            level.playSound(null, p, GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 1F, 1F);
        }
        final List<Entity> entityList = level.getEntitiesOfClass(Entity.class, monitoringArea(), l -> FireSafetyApi.getTargetEntityIndex(level, l) > Short.MIN_VALUE);
        for (final Entity e : entityList) {
            if (r.nextInt(a) < 100 - entityExtinguishingPossibility.get() * 100) continue;
            final short i = FireSafetyApi.getTargetEntityIndex(level, e);
            if (i == Short.MIN_VALUE) continue;
            FireSafetyApi.getTargetEntityAction(i).accept(level, e);
        }
    }

    @Override
    @Nonnull
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
        return (capability != CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || facing != Direction.UP) ? super.getCapability(capability, facing) : holder.cast();
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
