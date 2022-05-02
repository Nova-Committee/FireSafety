package committee.nova.firesafety.common.block.impl;

import committee.nova.firesafety.common.block.base.AbstractCeilingDeviceBlock;
import committee.nova.firesafety.common.block.blockEntity.impl.ExtinguisherBlockEntity;
import committee.nova.firesafety.common.tools.DataReference;
import committee.nova.firesafety.common.tools.TagKeyReference;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Random;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ExtinguisherBlock extends AbstractCeilingDeviceBlock implements EntityBlock {
    public static final BooleanProperty ONFIRE = BooleanProperty.create("onfire");
    public static final BooleanProperty WATERED = BooleanProperty.create("watered");

    public ExtinguisherBlock() {
        super();
        registerDefaultState(getStateDefinition().any().setValue(WATERED, true));
    }

    @Override
    public BlockState getStateForPlacement(@Nonnull BlockPlaceContext ctx) {
        return defaultBlockState().setValue(ONFIRE, false).setValue(WATERED, true);
    }

    @Override
    public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> definition) {
        definition.add(ONFIRE, WATERED);
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        final ItemStack stack = player.getItemInHand(hand);
        if (world.isClientSide) return InteractionResult.SUCCESS;
        if (!stack.is(Items.WATER_BUCKET) && !stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).isPresent())
            return super.use(state, world, pos, player, hand, hit);
        final BlockEntity e = world.getBlockEntity(pos);
        if (!(e instanceof final ExtinguisherBlockEntity g)) return InteractionResult.SUCCESS;
        final int needFill = g.getMaxWaterStorage() - g.getWaterStorage();
        if (needFill <= 0) return InteractionResult.SUCCESS;
        final FluidStack[] toFill = new FluidStack[1];
        toFill[0] = FluidStack.EMPTY;
        if (stack.is(Items.WATER_BUCKET)) {
            toFill[0] = new FluidStack(Fluids.WATER, Math.min(1000, needFill));
            player.setItemInHand(hand, Items.BUCKET.getDefaultInstance());
        } else {
            stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).ifPresent(f -> {
                if (f.getFluidInTank(0).getFluid().is(TagKeyReference.FIREFIGHTING))
                    toFill[0] = new FluidStack(Fluids.WATER, f.drain(needFill, IFluidHandler.FluidAction.EXECUTE).getAmount());
            });
        }
        g.getTank().fill(toFill[0], IFluidHandler.FluidAction.EXECUTE);
        world.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1F, 1F);
        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ExtinguisherBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        if (world.isClientSide()) return null;
        return (l, p, s, t) -> {
            if (t instanceof ExtinguisherBlockEntity extinguisher) extinguisher.tickServer();
        };
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, Random r) {
        if (!state.getValue(WATERED) || !state.getValue(ONFIRE)) return;
        extinguishParticle(world, pos, r);
    }

    private void extinguishParticle(Level world, BlockPos pos, Random r) {
        for (final Vec2 t : DataReference.water) {
            world.addAlwaysVisibleParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, pos.getX() + 0.5, pos.getY() + 0.7, pos.getZ() + 0.5, t.x + r.nextFloat(0.01F), -.2F, t.y + r.nextFloat(0.01F));
        }
    }

    public int getSignal(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {
        return (state.getValue(ONFIRE) ? 15 : 9) + (state.getValue(WATERED) ? -6 : 0);
    }

}
