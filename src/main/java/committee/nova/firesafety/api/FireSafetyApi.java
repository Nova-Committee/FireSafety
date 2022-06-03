package committee.nova.firesafety.api;

import com.mojang.datafixers.util.Function3;
import committee.nova.firesafety.api.event.FireSafetyExtensionEvent;
import committee.nova.firesafety.api.fp.Consumer3;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

@Mod.EventBusSubscriber
public class FireSafetyApi {
    private static final HashMap<Short, FireFightingWaterContainerItem> firefightingWaterContainerList = new HashMap<>();
    private static final HashMap<Short, ExtinguishableBlock> extinguishableBlockList = new HashMap<>();
    private static final HashMap<Short, ExtinguishableEntity> extinguishableEntityList = new HashMap<>();
    private static final HashMap<Short, FireDangerBlock> fireDangerBlockList = new HashMap<>();
    private static final HashMap<Short, FireDangerEntity> fireDangerEntityList = new HashMap<>();

    @SubscribeEvent
    public static void onStarted(ServerStartedEvent v) {
        final var event = new FireSafetyExtensionEvent();
        MinecraftForge.EVENT_BUS.post(event);
        firefightingWaterContainerList.putAll(event.getFirefightingWaterContainerList());
        extinguishableBlockList.putAll(event.getExtinguishableBlockList());
        extinguishableEntityList.putAll(event.getExtinguishableEntityList());
        fireDangerBlockList.putAll(event.getFireDangerBlockList());
        fireDangerEntityList.putAll(event.getFireDangerEntityList());
    }

    /**
     * @param stackCondition What kind of stack should be seen as a firefighting water container
     * @param amount         The water amount the stack can provide
     * @param usedResult     What is the corresponding water-consumed stack like
     * @param usedInfluence  What else will happen when the container stack is consumed
     **/
    @ParametersAreNonnullByDefault
    public record FireFightingWaterContainerItem(
            BiPredicate<Player, ItemStack> stackCondition,
            BiFunction<Player, ItemStack, Integer> amount,
            Function3<Player, Integer, ItemStack, ItemStack> usedResult,
            Consumer3<Player, Integer, ItemStack> usedInfluence) {
    }

    /**
     * @param blockCondition        What kind of block state should be seen as extinguishable
     * @param targetBlock           What is the extinguished block state like
     * @param extinguishedInfluence What else will happen when extinguished
     * @param detectable            Will the block state be detectable? If returns false it won't be detected,
     *                              but will be extinguished when the extinguisher does work.
     *                              Default is true.
     **/
    @ParametersAreNonnullByDefault
    public record ExtinguishableBlock(
            BiPredicate<Level, BlockPos> blockCondition,
            BiFunction<Level, BlockPos, BlockState> targetBlock,
            BiConsumer<Level, BlockPos> extinguishedInfluence,
            BiPredicate<Level, BlockPos> detectable) {
        public ExtinguishableBlock(
                BiPredicate<Level, BlockPos> blockCondition,
                BiFunction<Level, BlockPos, BlockState> targetBlock,
                BiConsumer<Level, BlockPos> extinguishedInfluence) {
            this(blockCondition, targetBlock, extinguishedInfluence, (l, p) -> true);
        }
    }

    /**
     * @param entityCondition What kind of entity should be seen as extinguishable
     * @param entityAction    What should the extinguisher do with such entity
     * @param detectable      Will the entity be detectable? If returns false it won't be detected,
     *                        but will be extinguished when the extinguisher does work.
     *                        Default is true.
     **/
    @ParametersAreNonnullByDefault
    public record ExtinguishableEntity(
            BiPredicate<Level, Entity> entityCondition,
            BiConsumer<Level, Entity> entityAction,
            BiPredicate<Level, Entity> detectable) {
        public ExtinguishableEntity(
                BiPredicate<Level, Entity> entityCondition,
                BiConsumer<Level, Entity> entityAction) {
            this(entityCondition, entityAction, (l, e) -> true);
        }
    }

    /**
     * @param blockCondition What kind of block state should be seen as a fire danger
     * @param dangerousness  The dangerousness of the block, -1 -> flammable, 0 -> very low, 1 -> low, 2 -> normal, 3 -> high, 4 -> very high
     * @param tips           The tips about the fire danger block, normally the reason why it's a fire danger
     */
    public record FireDangerBlock(
            BiPredicate<Level, BlockPos> blockCondition,
            BiFunction<Level, BlockPos, Integer> dangerousness,
            BiFunction<Level, BlockPos, MutableComponent> tips) {
    }

    /**
     * @param entityCondition What kind of entity should be seen as a fire danger
     * @param dangerousness   The dangerousness of the entity, <=0 -> very low, 1 -> low, 2 -> normal, 3 -> high, 4 -> very high
     * @param tips            The tips about the fire danger entity, normally the reason why it's a fire danger
     */
    public record FireDangerEntity(
            BiPredicate<Level, Entity> entityCondition,
            BiFunction<Level, Entity, Integer> dangerousness,
            BiFunction<Level, Entity, MutableComponent> tips) {
    }

    public static short getFireFightingContainerIndex(Player player, ItemStack stack) {
        final short[] s = {Short.MIN_VALUE};
        firefightingWaterContainerList.forEach((p, i) -> {
            if (p > s[0] && i.stackCondition().test(player, stack)) s[0] = p;
        });
        return s[0];
    }

    public static short getTargetBlockIndex(Level level, BlockPos pos) {
        final short[] s = {Short.MIN_VALUE};
        extinguishableBlockList.forEach((p, e) -> {
            if (p > s[0] && e.blockCondition().test(level, pos)) s[0] = p;
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

    public static short getFireDangerBlockIndex(Level level, BlockPos pos) {
        final short[] s = {Short.MIN_VALUE};
        fireDangerBlockList.forEach((p, e) -> {
            if (p > s[0] && e.blockCondition().test(level, pos)) s[0] = p;
        });
        return s[0];
    }

    public static short getFireDangerEntityIndex(Level level, Entity entity) {
        final short[] s = {Short.MIN_VALUE};
        fireDangerEntityList.forEach((p, e) -> {
            if (p > s[0] && e.entityCondition().test(level, entity)) s[0] = p;
        });
        return s[0];
    }

    public static FireFightingWaterContainerItem getFireFightingContainer(short index) {
        if (index == Short.MIN_VALUE) throw new NumberFormatException("Priority value should be greater than -32768");
        return firefightingWaterContainerList.get(index);
    }

    public static ExtinguishableBlock getTargetBlock(short index) {
        if (index == Short.MIN_VALUE) throw new NumberFormatException("Priority value should be greater than -32768");
        return extinguishableBlockList.get(index);
    }

    public static ExtinguishableEntity getTargetEntity(short index) {
        if (index == Short.MIN_VALUE) throw new NumberFormatException("Priority value should be greater than -32768");
        return extinguishableEntityList.get(index);
    }

    public static FireDangerBlock getFireDangerBlock(short index) {
        if (index == Short.MIN_VALUE) throw new NumberFormatException("Priority value should be greater than -32768");
        return fireDangerBlockList.get(index);
    }

    public static FireDangerEntity getFireDangerEntity(short index) {
        if (index == Short.MIN_VALUE) throw new NumberFormatException("Priority value should be greater than -32768");
        return fireDangerEntityList.get(index);
    }
}
