package committee.nova.firesafety.common.event;

import committee.nova.firesafety.common.item.api.IAdvancementTriggerable;
import committee.nova.firesafety.common.item.api.ITagResettable;
import committee.nova.firesafety.common.tools.PlayerHandler;
import committee.nova.firesafety.common.tools.advancement.AdvancementUtil;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class InternalEventHandler {
    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent e) {
        if (e.isCanceled()) return;
        final var player = e.getPlayer();
        final var inv = player.getInventory().items;
        for (final var stack : inv) {
            if (!(stack.getItem() instanceof ITagResettable i)) continue;
            i.resetTagOnDimensionChange(stack);
        }
    }

    @SubscribeEvent
    public static void onAdvancementAcquired(AdvancementEvent e) {
        if (e.isCanceled()) return;
        final var adv = e.getAdvancement().getId().getPath();
        if (!adv.equals("be_careful_with_candles") && !adv.equals("to_nip_it_in_the_spark")) return;
        PlayerHandler.notifyServerPlayer(e.getPlayer(), new TranslatableComponent("tips.firesafety.listen"));
    }

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickItem e) {
        if (e.getSide().isClient() || e.isCanceled()) return;
        final var item = e.getItemStack().getItem();
        if (!(item instanceof IAdvancementTriggerable i)) return;
        AdvancementUtil.tryAwardAdvancement((ServerPlayer) e.getPlayer(), i.getAdvancement());
    }
}
