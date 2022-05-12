package committee.nova.firesafety.common.event;

import committee.nova.firesafety.api.FireSafetyApi;
import committee.nova.firesafety.api.event.FireSafetyExtensionEvent;
import committee.nova.firesafety.api.item.IFireFightingWaterContainer;
import committee.nova.firesafety.common.config.Configuration;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static committee.nova.firesafety.common.tools.TagKeyReference.BURNING;
import static committee.nova.firesafety.common.tools.TagKeyReference.IGNORED;
import static net.minecraft.sounds.SoundEvents.GENERIC_EXTINGUISH_FIRE;

@Mod.EventBusSubscriber
public class EventHandler {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onExtension(FireSafetyExtensionEvent event) {
        event.addExtinguishable(Short.MAX_VALUE, new FireSafetyApi.ExtinguishableBlock((w, p) -> w.getBlockState(p).is(Blocks.FIRE), (w, p) -> Blocks.AIR.defaultBlockState()));
        event.addExtinguishable((short) -32767, new FireSafetyApi.ExtinguishableEntity((w, e) -> e.isOnFire() && !e.getType().is(IGNORED), (w, e) -> {
            e.clearFire();
            e.level.playSound(null, e, GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 1F, 1F);
        }));
        event.addExtinguishable((short) -32765, new FireSafetyApi.ExtinguishableEntity((w, e) -> e.getType().is(BURNING), (w, e) -> e.hurt(DamageSource.FREEZE, Configuration.freezeDamage.get().floatValue())));
        event.addFireFightingWaterItem((short) 32767, new FireSafetyApi.FireFightingWaterContainerItem((p, s) -> s.is(Items.WATER_BUCKET), (p, i) -> 1000, (p, a, s) -> Items.BUCKET.getDefaultInstance()));
        event.addFireFightingWaterItem((short) 32766, new FireSafetyApi.FireFightingWaterContainerItem((p, i) -> i.getItem() instanceof IFireFightingWaterContainer,
                (p, i) -> ((IFireFightingWaterContainer) i.getItem()).getWaterAmount(i),
                (p, a, s) -> ((IFireFightingWaterContainer) s.getItem()).consume(p, a, s)));
    }
}
