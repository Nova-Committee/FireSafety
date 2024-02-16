package committee.nova.firesafety.client.render.init;

import committee.nova.firesafety.common.item.api.IArmPoseChangeable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class RenderInitF {
    @SubscribeEvent
    public static void onRenderPlayerPose(RenderPlayerEvent event) {
        if (!event.isCancelable() || event.isCanceled()) return;
        final var player = event.getEntity();
        if (!(player.getMainHandItem().getItem() instanceof final IArmPoseChangeable c)) return;
        event.getRenderer().getModel().rightArmPose = player.isUsingItem() ? c.getUsingPose() : player.isSprinting() ? c.getSprintingPose() : c.getIdlePose();
    }
}
