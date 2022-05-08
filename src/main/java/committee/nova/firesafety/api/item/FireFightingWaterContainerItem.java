package committee.nova.firesafety.api.item;

import net.minecraft.world.item.ItemStack;

import java.util.function.Function;
import java.util.function.Predicate;

public record FireFightingWaterContainerItem(Predicate<ItemStack> stackCondition, Function<ItemStack, Integer> amount,
                                             Function<Integer, ItemStack> usedResult) {
}
