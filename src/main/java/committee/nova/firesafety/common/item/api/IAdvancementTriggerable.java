package committee.nova.firesafety.common.item.api;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;

@MethodsReturnNonnullByDefault
public interface IAdvancementTriggerable {
    ResourceLocation getAdvancement();
}
