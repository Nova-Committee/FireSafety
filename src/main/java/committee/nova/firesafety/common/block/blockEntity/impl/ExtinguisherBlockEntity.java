package committee.nova.firesafety.common.block.blockEntity.impl;

import committee.nova.firesafety.common.tools.PlayerHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.text.MessageFormat;
import java.util.List;
import java.util.Random;

import static committee.nova.firesafety.common.block.impl.ExtinguisherBlock.WATERED;
import static committee.nova.firesafety.common.block.reference.BlockReference.EXTINGUISHER;
import static committee.nova.firesafety.common.block.reference.BlockReference.getRegisteredBlockEntityType;
import static committee.nova.firesafety.common.tools.DataUtils.formatBlockPos;

@ParametersAreNonnullByDefault
public class ExtinguisherBlockEntity extends FireAlarmBlockEntity {
    protected final FluidTank tank = new FluidTank(FluidAttributes.BUCKET_VOLUME, f -> f.getFluid().is(FluidTags.WATER));
    private final LazyOptional<IFluidHandler> holder = LazyOptional.of(() -> tank);

    public ExtinguisherBlockEntity(BlockPos pos, BlockState state) {
        super(getRegisteredBlockEntityType(EXTINGUISHER), pos, state);
    }

    public boolean tickServer() {
        final boolean needExtinguish = super.tickServer();
        assert level != null;
        final BlockState state = level.getBlockState(worldPosition);
        if (tank.isEmpty()) {
            level.setBlockAndUpdate(worldPosition, state.setValue(WATERED, false));
            return true;
        }
        level.setBlockAndUpdate(worldPosition, state.setValue(WATERED, true));
        if (!needExtinguish) {
            return true;
        }
        if (fireStartedTick >= 40) tryExtinguish();
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
        if (level.getDayTime() % 50 != 0) return;
        if (tank.getFluidAmount() > 50) {
            tank.drain(50, IFluidHandler.FluidAction.EXECUTE);
            extinguish();
            return;
        }
        tank.drain(50, IFluidHandler.FluidAction.EXECUTE);
        toListeningPlayers(level, player -> PlayerHandler.playSoundForThisPlayer(player, SoundEvents.BUCKET_FILL, 1F, 1F));
        toListeningPlayers(level, player -> PlayerHandler.notifyServerPlayer(player, new TextComponent(MessageFormat.format(new TranslatableComponent("msg.firesafety.device.insufficient_water").getString(), formatBlockPos(worldPosition)))));
    }

    private void extinguish() {
        assert level != null;
        final Iterable<BlockPos> posList = BlockPos.betweenClosed(worldPosition.offset(5, 0, 5), worldPosition.offset(-5, -10, -5));
        final Random r = level.random;
        for (final BlockPos p : posList) {
            if (level.getBlockState(p).is(Blocks.FIRE) && r.nextInt(51) > 15) {
                level.setBlockAndUpdate(p, Blocks.AIR.defaultBlockState());
                level.playSound(null, p, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 1F, 1F);
            }
        }
        final List<LivingEntity> entityList = level.getEntitiesOfClass(LivingEntity.class, new AABB(worldPosition.offset(5, 0, 5), worldPosition.offset(-5, -10, -5)), l -> (l.isOnFire() || l.getType().is(BURNING)) && !l.getType().is(IGNORED));
        for (final LivingEntity e : entityList) {
            e.clearFire();
            if (e.getType().is(BURNING)) e.hurt(DamageSource.FREEZE, 5);
            level.playSound(null, e, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 1F, 1F);
        }

    }

    @Override
    @Nonnull
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
        if (capability != CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || facing != Direction.UP)
            return super.getCapability(capability, facing);
        return holder.cast();
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
