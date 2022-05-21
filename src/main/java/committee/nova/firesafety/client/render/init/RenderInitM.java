package committee.nova.firesafety.client.render.init;

import committee.nova.firesafety.FireSafety;
import committee.nova.firesafety.client.render.model.WaterBombModel;
import committee.nova.firesafety.client.render.renderer.base.FallingProjectileRenderer;
import committee.nova.firesafety.common.block.api.ISpecialRenderType;
import committee.nova.firesafety.common.block.init.BlockInit;
import committee.nova.firesafety.common.entity.init.EntityInit;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.RegistryObject;

import static committee.nova.firesafety.common.tools.reference.ItemReference.*;
import static committee.nova.firesafety.common.tools.reference.NBTReference.FDS_PROGRESS;
import static committee.nova.firesafety.common.tools.reference.NBTReference.USING;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RenderInitM {
    @SubscribeEvent
    public static void setupRenderType(FMLClientSetupEvent e) {
        e.enqueueWork(() -> {
            for (final RegistryObject<Block> block : BlockInit.blockList.values()) {
                final Block b = block.get();
                if (b instanceof ISpecialRenderType t) ItemBlockRenderTypes.setRenderLayer(b, t.getRenderType());
            }
        });
    }


    @SubscribeEvent
    public static void registerRenderer(FMLClientSetupEvent event) {
        EntityRenderers.register(EntityInit.waterBomb.get(), ctx -> new FallingProjectileRenderer<>(ctx, new ResourceLocation(FireSafety.MODID, "textures/entity/bombs/water_bomb/water_bomb.png")));
    }

    @SubscribeEvent
    public static void registerLayerDefinition(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(WaterBombModel.LAYER_LOCATION, WaterBombModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void overrideRegistry(FMLClientSetupEvent event) {
        event.enqueueWork(laserTracker(getRegisteredItem(FIREFIGHTING_AIRSTRIKE_CONTROLLER)));
        event.enqueueWork(fds(getRegisteredItem(FIRE_DANGER_SNIFFER)));
    }

    public static Runnable laserTracker(Item laser) {
        return () -> ItemProperties.register(laser, new ResourceLocation("on"),
                (stack, world, entity, i) -> stack.getOrCreateTag().getBoolean(USING) ? 1 : 0);
    }

    public static Runnable fds(Item fds) {
        return () -> ItemProperties.register(fds, new ResourceLocation("on"),
                (stack, world, entity, i) -> stack.getOrCreateTag().getInt(FDS_PROGRESS) > 0 ? 1 : 0);
    }
}
