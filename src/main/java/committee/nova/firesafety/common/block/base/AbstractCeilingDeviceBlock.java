package committee.nova.firesafety.common.block.base;

import committee.nova.firesafety.common.block.blockEntity.base.RecordableDeviceBlockEntity;
import committee.nova.firesafety.common.util.PlayerHandler;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class AbstractCeilingDeviceBlock extends Block {
    public static final BooleanProperty ONFIRE = BooleanProperty.create("onfire");

    //0 -> no fire, no water; 1 -> no fire, water sufficient, 2 -> fires, no water, 3 -> fires, water sufficient
    public AbstractCeilingDeviceBlock() {
        super(Properties.of(Material.METAL).strength(2F, 1000F).sound(SoundType.METAL).destroyTime(1F).lightLevel(s -> s.getValue(ONFIRE) ? 5 : 0).noOcclusion());
        registerDefaultState(getStateDefinition().any().setValue(ONFIRE, false));
    }

    public static void tryHandleListener(Player player, Level world, BlockPos pos) {
        final BlockEntity e = world.getBlockEntity(pos);
        if (!(e instanceof final RecordableDeviceBlockEntity r)) return;
        if (player.isCrouching()) {
            if (player.hasPermissions(2)) PlayerHandler.notifyServerPlayer(player, new TextComponent(r.toString()));
            return;
        }
        final boolean b = r.handleListener(player);
        PlayerHandler.notifyServerPlayer(player, new TranslatableComponent("msg.firesafety.device.listening." + b));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext ctx) {
        return Block.box(5, 13, 5, 11, 16, 11);
    }

    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        return Block.canSupportCenter(world, pos.above(), Direction.DOWN);
    }

    @Override
    public BlockState getStateForPlacement(@Nonnull BlockPlaceContext ctx) {
        return defaultBlockState().setValue(ONFIRE, false);
    }

    @Override
    public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> definition) {
        definition.add(ONFIRE);
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!world.isClientSide) tryHandleListener(player, world, pos);
        return InteractionResult.SUCCESS;
    }
}
