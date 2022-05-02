package committee.nova.firesafety.client.render;

import committee.nova.firesafety.common.block.api.ISpecialRenderType;
import committee.nova.firesafety.common.block.init.BlockInit;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RenderTypeInit {
    @SubscribeEvent
    public static void onRenderTypeSetup(FMLClientSetupEvent e) {
        e.enqueueWork(() -> {
            for (final RegistryObject<Block> block : BlockInit.blockList.values()) {
                final Block b = block.get();
                if (b instanceof ISpecialRenderType t) ItemBlockRenderTypes.setRenderLayer(b, t.getRenderType());
            }
        });
    }
}
