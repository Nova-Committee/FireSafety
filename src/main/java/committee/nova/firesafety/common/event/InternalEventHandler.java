package committee.nova.firesafety.common.event;

import committee.nova.firesafety.common.item.api.IAdvancementTriggerable;
import committee.nova.firesafety.common.tools.PlayerHandler;
import committee.nova.firesafety.common.tools.advancement.AdvancementUtil;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static committee.nova.firesafety.common.tools.reference.ItemReference.FIRE_DANGER_SNIFFER;
import static committee.nova.firesafety.common.tools.reference.ItemReference.getRegisteredItem;
import static committee.nova.firesafety.common.tools.reference.NBTReference.FDS_CENTER;
import static committee.nova.firesafety.common.tools.reference.NBTReference.FDS_PROGRESS;

@Mod.EventBusSubscriber
public class InternalEventHandler {
    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent e) {
        final var player = e.getPlayer();
        final var inv = player.getInventory().items;
        for (final var stack : inv) {
            if (!stack.is(getRegisteredItem(FIRE_DANGER_SNIFFER))) continue;
            final var tag = stack.getOrCreateTag();
            tag.putInt(FDS_PROGRESS, 0);
            tag.remove(FDS_CENTER);
        }
    }

    @SubscribeEvent
    public static void onAdvancementAcquired(AdvancementEvent event) {
        final var adv = event.getAdvancement().getId().getPath();
        if (!adv.equals("be_careful_with_candles") && !adv.equals("to_nip_it_in_the_spark")) return;
        PlayerHandler.notifyServerPlayer(event.getPlayer(), new TranslatableComponent("tips.firesafety.listen"));
    }

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickItem e) {
        if (e.getSide().isClient()) return;
        final var item = e.getItemStack().getItem();
        if (!(item instanceof IAdvancementTriggerable i)) return;
        AdvancementUtil.tryAwardAdvancement((ServerPlayer) e.getPlayer(), i.getAdvancement());
    }
}
