package committee.nova.firesafety.api;

import committee.nova.firesafety.FireSafety;
import committee.nova.firesafety.api.block.ExtinguishableBlock;
import committee.nova.firesafety.api.entity.ExtinguishableEntity;
import committee.nova.firesafety.common.config.Configuration;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.function.Consumer;

import static committee.nova.firesafety.common.tools.TagKeyReference.BURNING;
import static committee.nova.firesafety.common.tools.TagKeyReference.IGNORED;
import static net.minecraft.sounds.SoundEvents.GENERIC_EXTINGUISH_FIRE;

public class ExtinguishableUtil {
    private static final HashMap<Short, ExtinguishableBlock> extinguishableBlockList = new HashMap<>();
    private static final HashMap<Short, ExtinguishableEntity> extinguishableEntityList = new HashMap<>();

    public static void init() {
        addExtinguishable(Short.MAX_VALUE, new ExtinguishableBlock(b -> b.is(Blocks.FIRE), Blocks.AIR.defaultBlockState()));
        addExtinguishable((short) -32767, new ExtinguishableEntity(e -> e.isOnFire() && !e.getType().is(IGNORED), e -> {
            e.clearFire();
            e.level.playSound(null, e, GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 1F, 1F);
        }));
        addExtinguishable((short) -32765, new ExtinguishableEntity(e -> e.getType().is(BURNING), e -> e.hurt(DamageSource.FREEZE, Configuration.freezeDamage.get().floatValue())));
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
}
