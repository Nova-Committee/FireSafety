package committee.nova.firesafety.common.event;

import committee.nova.firesafety.api.FireSafetyApi;
import committee.nova.firesafety.api.event.FireSafetyExtensionEvent;
import committee.nova.firesafety.api.item.IFireFightingWaterContainer;
import committee.nova.firesafety.common.config.Configuration;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

import static committee.nova.firesafety.common.tools.reference.TagKeyReference.BURNING;
import static committee.nova.firesafety.common.tools.reference.TagKeyReference.IGNORED;
import static net.minecraft.sounds.SoundEvents.FIRE_EXTINGUISH;
import static net.minecraft.sounds.SoundEvents.GENERIC_EXTINGUISH_FIRE;
import static net.minecraft.world.entity.EntityType.BLAZE;

@Mod.EventBusSubscriber
public class EventHandler {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onExtension(FireSafetyExtensionEvent event) {
        event.addExtinguishable(Short.MAX_VALUE, new FireSafetyApi.ExtinguishableBlock(
                (w, p) -> w.getBlockState(p).is(Blocks.FIRE),
                (w, p) -> Blocks.AIR.defaultBlockState(),
                (w, p) -> {
                    final Random r = w.random;
                    w.playSound(null, p, FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.7F, 1.6F + (r.nextFloat() - r.nextFloat()) * 0.4F);
                }));
        event.addExtinguishable((short) -32767, new FireSafetyApi.ExtinguishableEntity(
                (w, e) -> e.isOnFire() && !e.getType().is(IGNORED),
                (w, e) -> {
                    e.clearFire();
                    e.level.playSound(null, e, GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 1F, 1F);
                }));
        event.addExtinguishable((short) -32765, new FireSafetyApi.ExtinguishableEntity((w, e) -> e.getType().is(BURNING), (w, e) -> e.hurt(DamageSource.FREEZE, Configuration.freezeDamage.get().floatValue())));
        event.addFireFightingWaterItem((short) 32767, new FireSafetyApi.FireFightingWaterContainerItem(
                (p, s) -> s.is(Items.WATER_BUCKET),
                (p, i) -> 1000, (p, a, s) -> Items.BUCKET.getDefaultInstance(),
                (p, a, s) -> {
                }
        ));
        event.addFireFightingWaterItem((short) 32766, new FireSafetyApi.FireFightingWaterContainerItem(
                (p, i) -> i.getItem() instanceof IFireFightingWaterContainer,
                (p, i) -> ((IFireFightingWaterContainer) i.getItem()).getWaterAmount(i),
                (p, a, s) -> ((IFireFightingWaterContainer) s.getItem()).consume(p, a, s),
                (p, a, s) -> {
                }
        ));
        event.addFireDanger((short) 32767, new FireSafetyApi.FireDangerBlock(
                (l, p) -> {
                    final var s = l.getBlockState(p);
                    return s.is(Blocks.FIRE) || s.is(Blocks.LAVA);
                },
                (l, p) -> 4,
                (l, p) -> new TranslatableComponent("tips.firesafety.danger.fire_n_lava")
        ));
        event.addFireDanger((short) 32766, new FireSafetyApi.FireDangerBlock(
                (l, p) -> {
                    final var s = l.getBlockState(p);
                    return s.getMaterial().isFlammable() && !s.getMaterial().equals(Material.REPLACEABLE_PLANT);
                },
                (l, p) -> -1,
                (l, p) -> new TranslatableComponent("tips.firesafety.danger.flammable_material")
        ));
        event.addFireDanger((short) 32767, new FireSafetyApi.FireDangerEntity(
                (l, e) -> e.getType().equals(BLAZE),
                (l, e) -> 4,
                (l, e) -> new TranslatableComponent("tips.firesafety.danger.blaze")
        ));
    }
}
