package committee.nova.firesafety.api;

import committee.nova.firesafety.FireSafety;
import committee.nova.firesafety.api.block.ExtinguishableBlock;
import committee.nova.firesafety.api.entity.ExtinguishableEntity;
import committee.nova.firesafety.api.item.FireFightingWaterContainerItem;
import committee.nova.firesafety.common.config.Configuration;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;

import static committee.nova.firesafety.common.tools.TagKeyReference.BURNING;
import static committee.nova.firesafety.common.tools.TagKeyReference.IGNORED;
import static net.minecraft.sounds.SoundEvents.GENERIC_EXTINGUISH_FIRE;

public class FireSafetyApi {
    private static final HashMap<Short, ExtinguishableBlock> extinguishableBlockList = new HashMap<>();
    private static final HashMap<Short, ExtinguishableEntity> extinguishableEntityList = new HashMap<>();
    private static final HashMap<Short, FireFightingWaterContainerItem> firefightingWaterContainerList = new HashMap<>();

    public static void init() {
        addExtinguishable(Short.MAX_VALUE, new ExtinguishableBlock(b -> b.is(Blocks.FIRE), Blocks.AIR.defaultBlockState()));
        addExtinguishable((short) -32767, new ExtinguishableEntity(e -> e.isOnFire() && !e.getType().is(IGNORED), e -> {
            e.clearFire();
            e.level.playSound(null, e, GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 1F, 1F);
        }));
        addExtinguishable((short) -32765, new ExtinguishableEntity(e -> e.getType().is(BURNING), e -> e.hurt(DamageSource.FREEZE, Configuration.freezeDamage.get().floatValue())));
        addFireFightingWaterItem((short) 32767, new FireFightingWaterContainerItem(s -> s.is(Items.WATER_BUCKET), a -> 1000, r -> Items.BUCKET.getDefaultInstance()));
    }

    public static void addExtinguishable(short priority, ExtinguishableBlock extinguishable) {
        if (extinguishableBlockList.containsKey(priority)) {
            FireSafety.LOGGER.warn("Duplicate priority value {}, new extinguishable block won't be added!", priority);
            return;
        }
        extinguishableBlockList.put(priority, extinguishable);
        FireSafety.LOGGER.info("Adding new extinguishable block with priority {}!", priority);
    }

    public static void addExtinguishable(short priority, ExtinguishableEntity extinguishable) {
        if (extinguishableEntityList.containsKey(priority)) {
            FireSafety.LOGGER.warn("Duplicate priority value {}, new extinguishable entity won't be added!", priority);
            return;
        }
        extinguishableEntityList.put(priority, extinguishable);
        FireSafety.LOGGER.info("Adding new extinguishable entity with priority {}!", priority);
    }

    public static void addFireFightingWaterItem(short priority, FireFightingWaterContainerItem container) {
        if (firefightingWaterContainerList.containsKey(priority)) {
            FireSafety.LOGGER.warn("Duplicate priority value {}, new firefighting container won't be added!", priority);
            return;
        }
        firefightingWaterContainerList.put(priority, container);
        FireSafety.LOGGER.info("Adding new firefighting container with priority {}!", priority);
    }

    public static short getTargetBlockStateIndex(BlockState state) {
        final short[] s = {Short.MIN_VALUE};
        extinguishableBlockList.forEach((p, e) -> {
            if (p > s[0] && e.blockCondition().test(state)) s[0] = p;
        });
        return s[0];
    }

    public static BlockState getTargetBlockState(short index) {
        if (index == Short.MIN_VALUE) throw new NumberFormatException("Priority value should be greater than -32768");
        return extinguishableBlockList.get(index).targetBlock();
    }

    public static short getTargetEntityIndex(Entity entity) {
        final short[] s = {Short.MIN_VALUE};
        extinguishableEntityList.forEach((p, e) -> {
            if (p > s[0] && e.entityCondition().test(entity)) s[0] = p;
        });
        return s[0];
    }

    public static Consumer<Entity> getTargetEntityAction(short index) {
        if (index == Short.MIN_VALUE) throw new NumberFormatException("Priority value should be greater than -32768");
        return extinguishableEntityList.get(index).entityAction();
    }

    public static short getFireFightingContainerIndex(ItemStack stack) {
        final short[] s = {Short.MIN_VALUE};
        firefightingWaterContainerList.forEach((p, i) -> {
            if (p > s[0] && i.stackCondition().test(stack)) s[0] = p;
        });
        return s[0];
    }

    public static Function<ItemStack, Integer> getFireFightingContainerAmount(short index) {
        if (index == Short.MIN_VALUE) throw new NumberFormatException("Priority value should be greater than -32768");
        return firefightingWaterContainerList.get(index).amount();
    }

    public static Function<Integer, ItemStack> getFireFightingContainerUsedResult(short index) {
        if (index == Short.MIN_VALUE) throw new NumberFormatException("Priority value should be greater than -32768");
        return firefightingWaterContainerList.get(index).usedResult();
    }
}
