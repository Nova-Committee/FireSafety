package committee.nova.firesafety.common.block.impl;

import committee.nova.firesafety.common.block.base.AbstractCeilingDeviceBlock;
import committee.nova.firesafety.common.block.entity.impl.ExtinguisherBlockEntity;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static committee.nova.firesafety.common.tools.misc.FluidUtil.tryFill;
import static committee.nova.firesafety.common.tools.misc.PlayerHandler.notifyServerPlayer;
import static committee.nova.firesafety.common.tools.reference.DataReference.water;
import static net.minecraft.core.particles.ParticleTypes.CAMPFIRE_COSY_SMOKE;
import static net.minecraft.sounds.SoundEvents.BUCKET_EMPTY;
import static net.minecraft.sounds.SoundSource.BLOCKS;
import static net.minecraft.world.InteractionResult.SUCCESS;

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
        if (world.isClientSide) return SUCCESS;
        final var stack = player.getItemInHand(hand);
        final var e = world.getBlockEntity(pos);
        if (!(e instanceof final ExtinguisherBlockEntity g)) return SUCCESS;
        final int needFill = g.getMaxWaterStorage() - g.getWaterStorage();
        if (needFill <= 0) return SUCCESS;
        final var filled = tryFill(player, hand, needFill, stack);
        if (filled.isEmpty()) {
            reportWaterAmount(player, world, pos);
            return super.use(state, world, pos, player, hand, hit);
        }
        g.getTank().fill(filled, IFluidHandler.FluidAction.EXECUTE);
        reportWaterAmount(player, world, pos);
        world.playSound(null, pos, BUCKET_EMPTY, BLOCKS, 1F, 1F);
        return SUCCESS;
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
            if (t instanceof final ExtinguisherBlockEntity extinguisher) extinguisher.tickServer();
        };
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource r) {
        if (!state.getValue(WATERED) || !state.getValue(ONFIRE)) return;
        extinguishParticle(world, pos);
    }

    private void extinguishParticle(Level world, BlockPos pos) {
        final Random r = ThreadLocalRandom.current();
        for (final var t : water) {
            world.addAlwaysVisibleParticle(CAMPFIRE_COSY_SMOKE, pos.getX() + .5, pos.getY() + .36, pos.getZ() + .5, t.x + r.nextFloat(.01F), -.2F, t.y + r.nextFloat(.01F));
        }
    }

    public int getSignal(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {
        return (state.getValue(ONFIRE) ? 15 : 9) + (state.getValue(WATERED) ? -6 : 0);
    }

    private void reportWaterAmount(Player player, Level world, BlockPos pos) {
        final var b = world.getBlockEntity(pos);
        if (!(b instanceof final ExtinguisherBlockEntity e)) return;
        notifyServerPlayer(player, Component.translatable("msg.firesafety.device.current_water_amount", e.getWaterStorage()));
    }
}
