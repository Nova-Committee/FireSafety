package committee.nova.firesafety.api;

import com.mojang.datafixers.util.Function3;
import committee.nova.firesafety.FireSafety;
import committee.nova.firesafety.api.item.IFireFightingWaterContainer;
import committee.nova.firesafety.common.config.Configuration;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

import static committee.nova.firesafety.common.tools.TagKeyReference.BURNING;
import static committee.nova.firesafety.common.tools.TagKeyReference.IGNORED;
import static net.minecraft.sounds.SoundEvents.GENERIC_EXTINGUISH_FIRE;

public class FireSafetyApi {
    private static final HashMap<Short, ExtinguishableBlock> extinguishableBlockList = new HashMap<>();
    private static final HashMap<Short, ExtinguishableEntity> extinguishableEntityList = new HashMap<>();
    private static final HashMap<Short, FireFightingWaterContainerItem> firefightingWaterContainerList = new HashMap<>();

    public static void init() {
        addExtinguishable(Short.MAX_VALUE, new ExtinguishableBlock((w, b) -> b.is(Blocks.FIRE), (w, b) -> Blocks.AIR.defaultBlockState()));
        addExtinguishable((short) -32767, new ExtinguishableEntity((w, e) -> e.isOnFire() && !e.getType().is(IGNORED), (w, e) -> {
            e.clearFire();
            e.level.playSound(null, e, GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 1F, 1F);
        }));
        addExtinguishable((short) -32765, new ExtinguishableEntity((w, e) -> e.getType().is(BURNING), (w, e) -> e.hurt(DamageSource.FREEZE, Configuration.freezeDamage.get().floatValue())));
        addFireFightingWaterItem((short) 32767, new FireFightingWaterContainerItem((p, s) -> s.is(Items.WATER_BUCKET), (p, i) -> 1000, (p, a, s) -> Items.BUCKET.getDefaultInstance()));
        addFireFightingWaterItem((short) 32766, new FireFightingWaterContainerItem((p, i) -> i.getItem() instanceof IFireFightingWaterContainer,
                (p, i) -> ((IFireFightingWaterContainer) i.getItem()).getWaterAmount(i),
                (p, a, s) -> ((IFireFightingWaterContainer) s.getItem()).consume(p, a)));
    }

    public static short getTargetBlockStateIndex(Level level, BlockState state) {
        final short[] s = {Short.MIN_VALUE};
        extinguishableBlockList.forEach((p, e) -> {
            if (p > s[0] && e.blockCondition().test(level, state)) s[0] = p;
        });
        return s[0];
    }

    public static short getTargetEntityIndex(Level level, Entity entity) {
        final short[] s = {Short.MIN_VALUE};
        extinguishableEntityList.forEach((p, e) -> {
            if (p > s[0] && e.entityCondition().test(level, entity)) s[0] = p;
        });
        return s[0];
    }

    public static BiConsumer<Level, Entity> getTargetEntityAction(short index) {
        if (index == Short.MIN_VALUE) throw new NumberFormatException("Priority value should be greater than -32768");
        return extinguishableEntityList.get(index).entityAction();
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

    public static short getFireFightingContainerIndex(Player player, ItemStack stack) {
        final short[] s = {Short.MIN_VALUE};
        firefightingWaterContainerList.forEach((p, i) -> {
            if (p > s[0] && i.stackCondition().test(player, stack)) s[0] = p;
        });
        return s[0];
    }

    public static BiFunction<Level, BlockState, BlockState> getTargetBlockState(short index) {
        if (index == Short.MIN_VALUE) throw new NumberFormatException("Priority value should be greater than -32768");
        return extinguishableBlockList.get(index).targetBlock();
    }

    public static BiFunction<Player, ItemStack, Integer> getFireFightingContainerAmount(short index) {
        if (index == Short.MIN_VALUE) throw new NumberFormatException("Priority value should be greater than -32768");
        return firefightingWaterContainerList.get(index).amount();
    }

    public static Function3<Player, Integer, ItemStack, ItemStack> getFireFightingContainerUsedResult(short index) {
        if (index == Short.MIN_VALUE) throw new NumberFormatException("Priority value should be greater than -32768");
        return firefightingWaterContainerList.get(index).usedResult();
    }

    /**
     * @param stackCondition What kind of stack should be seen as a firefighting water container
     * @param amount         The water amount the stack can provide
     * @param usedResult     What is the corresponding water-consumed stack like
     **/
    @ParametersAreNonnullByDefault
    public record FireFightingWaterContainerItem(
            BiPredicate<Player, ItemStack> stackCondition,
            BiFunction<Player, ItemStack, Integer> amount,
            Function3<Player, Integer, ItemStack, ItemStack> usedResult) {
    }

    /**
     * @param blockCondition What kind of block state should be seen as extinguishable
     * @param targetBlock    What is the extinguished block state like
     **/
    @ParametersAreNonnullByDefault
    public record ExtinguishableBlock(
            BiPredicate<Level, BlockState> blockCondition,
            BiFunction<Level, BlockState, BlockState> targetBlock) {
    }

    /**
     * @param entityCondition What kind of entity should be seen as extinguishable
     * @param entityAction    What should the extinguisher do with such entity
     **/
    @ParametersAreNonnullByDefault
    public record ExtinguishableEntity(
            BiPredicate<Level, Entity> entityCondition,
            BiConsumer<Level, Entity> entityAction) {
    }
}
