package committee.nova.firesafety.common.block.api;

import net.minecraft.client.renderer.RenderType;

public interface ISpecialRenderType {
    default RenderType getRenderType() {
        return RenderType.translucent();
    }
}
