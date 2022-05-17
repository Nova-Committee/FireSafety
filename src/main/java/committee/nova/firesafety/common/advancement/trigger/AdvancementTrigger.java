package committee.nova.firesafety.common.advancement.trigger;

import committee.nova.firesafety.common.tools.PlayerHandler;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class AdvancementTrigger {
    @SubscribeEvent
    public static void onAdvancementAcquired(AdvancementEvent event) {
        final var adv = event.getAdvancement().getId().getPath();
        if (!adv.equals("be_careful_with_candles") && !adv.equals("to_nip_it_in_the_spark")) return;
        PlayerHandler.notifyServerPlayer(event.getPlayer(), new TranslatableComponent("tips.firesafety.listen"));
    }
}
