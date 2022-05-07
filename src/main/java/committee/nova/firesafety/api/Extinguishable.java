package committee.nova.firesafety.api;

import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Predicate;

public record Extinguishable(Predicate<BlockState> blockCondition, BlockState targetBlock) {
}
