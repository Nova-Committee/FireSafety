package committee.nova.firesafety.common.item.api;

import net.minecraft.world.item.ItemStack;

public interface ITagResettable {
    void resetTagOnDimensionChange(ItemStack stack);
}
