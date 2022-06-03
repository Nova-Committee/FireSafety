package committee.nova.firesafety.common.event;

import committee.nova.firesafety.FireSafety;
import committee.nova.firesafety.api.FireSafetyApi;
import committee.nova.firesafety.api.event.FireExtinguishedEvent;
import committee.nova.firesafety.api.event.FireSafetyExtensionEvent;
import committee.nova.firesafety.api.item.IFireFightingWaterContainer;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

import static committee.nova.firesafety.common.config.Configuration.freezeDamage;
import static committee.nova.firesafety.common.tools.advancement.AdvancementUtil.tryAwardAdvancement;
import static committee.nova.firesafety.common.tools.reference.TagKeyReference.*;
import static net.minecraft.sounds.SoundEvents.FIRE_EXTINGUISH;
import static net.minecraft.sounds.SoundEvents.GENERIC_EXTINGUISH_FIRE;
import static net.minecraft.sounds.SoundSource.BLOCKS;
import static net.minecraft.world.damagesource.DamageSource.FREEZE;
import static net.minecraft.world.entity.EntityType.BLAZE;
import static net.minecraft.world.item.Items.BUCKET;
import static net.minecraft.world.item.Items.WATER_BUCKET;
import static net.minecraft.world.level.block.Blocks.AIR;
import static net.minecraft.world.level.block.Blocks.LAVA;
import static net.minecraft.world.level.material.Material.REPLACEABLE_PLANT;

@Mod.EventBusSubscriber
public class EventHandler {
    public static final String MODNAME = "FireSafety";
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onExtension(FireSafetyExtensionEvent event) {
        event.addExtinguishable(MODNAME, Short.MAX_VALUE, new FireSafetyApi.ExtinguishableBlock(
                (w, p) -> w.getBlockState(p).is(BlockTags.FIRE),
                (w, p) -> AIR.defaultBlockState(),
                (w, p) -> {
                    final Random r = w.random;
                    w.playSound(null, p, FIRE_EXTINGUISH, BLOCKS, .7F, 1.6F + (r.nextFloat() - r.nextFloat()) * 0.4F);
                }));
        event.addExtinguishable(MODNAME, (short) -32767, new FireSafetyApi.ExtinguishableEntity(
                (w, e) -> e.isOnFire() && !e.getType().is(IGNORED),
                (w, e) -> {
                    e.clearFire();
                    w.playSound(null, e, GENERIC_EXTINGUISH_FIRE, BLOCKS, 1F, 1F);
                },
                (w, e) -> !e.getType().is(UNDETECTABLE)));
        event.addExtinguishable(MODNAME, (short) -32000, new FireSafetyApi.ExtinguishableEntity((w, e) -> e.getType().is(BURNING), (w, e) -> e.hurt(FREEZE, freezeDamage.get().floatValue())));
        event.addFireFightingWaterItem(MODNAME, (short) 32767, new FireSafetyApi.FireFightingWaterContainerItem(
                (p, s) -> s.is(WATER_BUCKET),
                (p, i) -> 1000, (p, a, s) -> BUCKET.getDefaultInstance(),
                (p, a, s) -> {
                }
        ));
        event.addFireFightingWaterItem(MODNAME, (short) 32766, new FireSafetyApi.FireFightingWaterContainerItem(
                (p, i) -> i.getItem() instanceof IFireFightingWaterContainer,
                (p, i) -> ((IFireFightingWaterContainer) i.getItem()).getWaterAmount(i),
                (p, a, s) -> ((IFireFightingWaterContainer) s.getItem()).consume(p, a, s),
                (p, a, s) -> ((IFireFightingWaterContainer) s.getItem()).influence(p, a, s)
        ));
        event.addFireDanger(MODNAME, (short) 32767, new FireSafetyApi.FireDangerBlock(
                (l, p) -> {
                    final var s = l.getBlockState(p);
                    return s.is(BlockTags.FIRE) || s.is(LAVA);
                },
                (l, p) -> 4,
                (l, p) -> new TranslatableComponent("tips.firesafety.danger.fire_n_lava")
        ));
        event.addFireDanger(MODNAME, (short) -32000, new FireSafetyApi.FireDangerBlock(
                (l, p) -> {
                    final var s = l.getBlockState(p);
                    return s.getMaterial().isFlammable() && !s.getMaterial().equals(REPLACEABLE_PLANT);
                },
                (l, p) -> -1,
                (l, p) -> new TranslatableComponent("tips.firesafety.danger.flammable_material")
        ));
        event.addFireDanger(MODNAME, (short) 32767, new FireSafetyApi.FireDangerEntity(
                (l, e) -> e.getType().equals(BLAZE),
                (l, e) -> 4,
                (l, e) -> new TranslatableComponent("tips.firesafety.danger.blaze")
        ));
    }

    @SubscribeEvent
    public static void onFireExtinguished(FireExtinguishedEvent event) {
        final var extinguisher = event.getExtinguisher();
        if (!(extinguisher instanceof ServerPlayer p)) return;
        final var handheld = event.getExtinguisherType() == FireExtinguishedEvent.ExtinguisherType.HANDHELD;
        tryAwardAdvancement(p, new ResourceLocation(FireSafety.MODID, handheld ? "a_hero_in_harm_s_way" : "to_nip_it_in_the_spark"));
    }
}
