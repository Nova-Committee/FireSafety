package committee.nova.firesafety.api.block;

import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Predicate;

public record ExtinguishableBlock(Predicate<BlockState> blockCondition, BlockState targetBlock) {
}
