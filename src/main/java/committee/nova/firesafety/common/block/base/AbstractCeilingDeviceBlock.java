package committee.nova.firesafety.common.block.base;

import committee.nova.firesafety.common.block.api.ISpecialRenderType;
import committee.nova.firesafety.common.block.entity.base.RecordableDeviceBlockEntity;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;

import static committee.nova.firesafety.common.tools.misc.PlayerHandler.notifyServerPlayer;
import static net.minecraft.sounds.SoundEvents.NOTE_BLOCK_IRON_XYLOPHONE;
import static net.minecraft.sounds.SoundSource.BLOCKS;
import static net.minecraft.world.level.block.Blocks.AIR;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@SuppressWarnings("deprecation")
public abstract class AbstractCeilingDeviceBlock extends Block implements ISpecialRenderType {
    public static final BooleanProperty ONFIRE = BooleanProperty.create("onfire");

    public AbstractCeilingDeviceBlock() {
        super(Properties.of().strength(2F, 1000F).sound(SoundType.METAL).destroyTime(1F).lightLevel(s -> s.getValue(ONFIRE) ? 12 : 6).noOcclusion());
        registerDefaultState(getStateDefinition().any().setValue(ONFIRE, false));
    }

    public static void tryHandleListener(Player player, Level world, BlockPos pos) {
        final var e = world.getBlockEntity(pos);
        if (!(e instanceof final RecordableDeviceBlockEntity r)) return;
        if (player.isShiftKeyDown()) {
            if (player.hasPermissions(2)) notifyServerPlayer(player, Component.literal(r.toString()));
            return;
        }
        final boolean b = r.handleListener(player);
        player.playNotifySound(NOTE_BLOCK_IRON_XYLOPHONE.get(), BLOCKS, .5F, b ? 1F : .5F);
        notifyServerPlayer(player, Component.translatable("msg.firesafety.device.listening." + b));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext ctx) {
        return Block.box(5, 15, 5, 11, 16, 11);
    }

    @Override
    public BlockState updateShape(BlockState state1, Direction direction, BlockState state2, LevelAccessor world, BlockPos pos1, BlockPos pos2) {
        if (state1.canSurvive(world, pos1)) return super.updateShape(state1, direction, state2, world, pos1, pos2);
        return AIR.defaultBlockState();
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        return Block.canSupportCenter(world, pos.above(), Direction.DOWN);
    }

    @Override
    public BlockState getStateForPlacement(@Nonnull BlockPlaceContext ctx) {
        return defaultBlockState().setValue(ONFIRE, false);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        setOwner(level, pos, placer);
    }

    @Override
    public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> definition) {
        definition.add(ONFIRE);
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!world.isClientSide) {
            tryHandleListener(player, world, pos);
            setOwner(world, pos, player);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder ctx) {
        return Collections.singletonList(new ItemStack(this));
    }

    public static void setOwner(Level level, BlockPos pos, @Nullable Entity owner) {
        final var blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof final RecordableDeviceBlockEntity r) r.setOwner(owner);
    }
}
