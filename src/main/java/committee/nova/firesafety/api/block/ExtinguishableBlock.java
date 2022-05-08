package committee.nova.firesafety.api.block;

import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

public record ExtinguishableBlock(@Nonnull Predicate<BlockState> blockCondition, @Nonnull BlockState targetBlock) {
}
