package committee.nova.firesafety.common.block.blockEntity.impl;

import committee.nova.firesafety.common.config.Configuration;
import committee.nova.firesafety.common.tools.PlayerHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
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
import static committee.nova.firesafety.common.tools.TagKeyReference.BURNING;
import static committee.nova.firesafety.common.tools.TagKeyReference.IGNORED;
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
        if (!needExtinguish) {
            return true;
        }
        if (fireStartedTick <= 25) return true;
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
        final int consumption = Configuration.waterConsumption.get();
        extinguish(tank.drain(consumption, IFluidHandler.FluidAction.EXECUTE).getAmount());
        if (remain > consumption) return;
        toListeningPlayers(level, player -> PlayerHandler.playSoundForThisPlayer(player, BUCKET_FILL, 1F, 1F));
        toListeningPlayers(level, player -> PlayerHandler.notifyServerPlayer(player, new TranslatableComponent("msg.firesafety.device.insufficient_water", formatBlockPos())));
    }

    private void extinguish(int amount) {
        assert level != null;
        final Iterable<BlockPos> posList = BlockPos.betweenClosed(monitoringAreaPos()[0], monitoringAreaPos()[1]);
        final Random r = level.random;
        final int a = (int) (amount * 100F / Configuration.waterConsumption.get()) + 1;
        for (final BlockPos p : posList) {
            if (level.getBlockState(p).is(Blocks.FIRE) && r.nextInt(a) > 100 - Configuration.blockExtinguishingPossibility.get() * 100) {
                level.setBlockAndUpdate(p, Blocks.AIR.defaultBlockState());
                level.playSound(null, p, GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 1F, 1F);
            }
        }
        final List<LivingEntity> entityList = level.getEntitiesOfClass(LivingEntity.class, monitoringArea(), l -> (l.isOnFire() || l.getType().is(BURNING)) && !l.getType().is(IGNORED));
        final float freezeDamage = Configuration.freezeDamage.get().floatValue();
        for (final LivingEntity e : entityList) {
            if (r.nextInt(a) <= 100 - Configuration.entityExtinguishingPossibility.get() * 100) continue;
            e.clearFire();
            if (e.getType().is(BURNING)) e.hurt(DamageSource.FREEZE, freezeDamage);
            level.playSound(null, e, GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 1F, 1F);
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
