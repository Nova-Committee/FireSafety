package committee.nova.firesafety.client.render.init;

import committee.nova.firesafety.FireSafety;
import committee.nova.firesafety.client.render.model.WaterBombModel;
import committee.nova.firesafety.client.render.renderer.base.FallingProjectileRenderer;
import committee.nova.firesafety.common.block.api.ISpecialRenderType;
import committee.nova.firesafety.common.block.init.BlockInit;
import committee.nova.firesafety.common.entity.init.EntityInit;
import committee.nova.firesafety.common.item.IArmPoseChangeable;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.RegistryObject;

import static committee.nova.firesafety.common.tools.reference.ItemReference.FIREFIGHTING_AIRSTRIKE_CONTROLLER;
import static committee.nova.firesafety.common.tools.reference.ItemReference.getRegisteredItem;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RenderInit {
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
    public static void onRenderPlayerPose(RenderPlayerEvent event) {
        if (!event.isCancelable() || event.isCanceled()) return;
        final var player = event.getPlayer();
        if (!(player.getMainHandItem().getItem() instanceof IArmPoseChangeable c)) return;
        event.getRenderer().getModel().rightArmPose = player.isUsingItem() ? c.getUsingPose() : player.isSprinting() ? c.getSprintingPose() : c.getIdlePose();
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
        overrideLaserTracker(event, getRegisteredItem(FIREFIGHTING_AIRSTRIKE_CONTROLLER));
    }

    public static void overrideLaserTracker(FMLClientSetupEvent event, Item laser) {
        event.enqueueWork(() -> ItemProperties.register(laser, new ResourceLocation("on"), (stack, world, entity, i) -> {
            if (entity == null) {
                return 0;
            } else {
                return stack == entity.getItemInHand(InteractionHand.MAIN_HAND) ? 1 : 0;
            }
        }));
    }
}
