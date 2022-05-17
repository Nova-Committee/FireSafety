package committee.nova.firesafety.common.block.impl;

import committee.nova.firesafety.api.FireSafetyApi;
import committee.nova.firesafety.common.block.base.AbstractCeilingDeviceBlock;
import committee.nova.firesafety.common.block.blockEntity.impl.ExtinguisherBlockEntity;
import committee.nova.firesafety.common.tools.PlayerHandler;
import committee.nova.firesafety.common.tools.reference.DataReference;
import committee.nova.firesafety.common.tools.reference.TagKeyReference;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Random;

import static net.minecraft.sounds.SoundEvents.BUCKET_EMPTY;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@SuppressWarnings("deprecation")
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
        final var stack = player.getItemInHand(hand);
        if (world.isClientSide) return InteractionResult.SUCCESS;
        final var e = world.getBlockEntity(pos);
        if (!(e instanceof final ExtinguisherBlockEntity g)) return InteractionResult.SUCCESS;
        final int needFill = g.getMaxWaterStorage() - g.getWaterStorage();
        if (needFill <= 0) return InteractionResult.SUCCESS;
        final var toFill = new FluidStack[1];
        toFill[0] = FluidStack.EMPTY;
        final short index = FireSafetyApi.getFireFightingContainerIndex(player, stack);
        if (index > Short.MIN_VALUE) {
            final var i = FireSafetyApi.getFireFightingContainer(index);
            final int shouldFill = Math.min(i.amount().apply(player, stack), needFill);
            toFill[0] = new FluidStack(Fluids.WATER, shouldFill);
            if (!player.isCreative()) player.setItemInHand(hand, i.usedResult().apply(player, shouldFill, stack));
            i.usedInfluence().accept(player, shouldFill, stack);
        } else {
            stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).ifPresent(f -> {
                if (f.getFluidInTank(0).getFluid().is(TagKeyReference.FIREFIGHTING))
                    toFill[0] = new FluidStack(Fluids.WATER, f.drain(needFill, player.isCreative() ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE).getAmount());
            });
        }
        if (toFill[0].isEmpty()) {
            reportWaterAmount(player, world, pos);
            return super.use(state, world, pos, player, hand, hit);
        }
        g.getTank().fill(toFill[0], IFluidHandler.FluidAction.EXECUTE);
        reportWaterAmount(player, world, pos);
        world.playSound(null, pos, BUCKET_EMPTY, SoundSource.BLOCKS, 1F, 1F);
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
        for (final var t : DataReference.water) {
            world.addAlwaysVisibleParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, pos.getX() + .5, pos.getY() + .36, pos.getZ() + .5, t.x + r.nextFloat(.01F), -.2F, t.y + r.nextFloat(.01F));
        }
    }

    public int getSignal(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {
        return (state.getValue(ONFIRE) ? 15 : 9) + (state.getValue(WATERED) ? -6 : 0);
    }

    private void reportWaterAmount(Player player, Level world, BlockPos pos) {
        final var b = world.getBlockEntity(pos);
        if (!(b instanceof ExtinguisherBlockEntity e)) return;
        PlayerHandler.notifyServerPlayer(player, new TranslatableComponent("msg.firesafety.device.current_water_amount", e.getWaterStorage()));
    }
}
