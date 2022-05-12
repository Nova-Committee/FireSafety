package committee.nova.firesafety.api;

import com.mojang.datafixers.util.Function3;
import committee.nova.firesafety.api.event.FireSafetyExtensionEvent;
import net.minecraft.core.BlockPos;
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
    private static final HashMap<Short, ExtinguishableBlock> extinguishableBlockList = new HashMap<>();
    private static final HashMap<Short, ExtinguishableEntity> extinguishableEntityList = new HashMap<>();
    private static final HashMap<Short, FireFightingWaterContainerItem> firefightingWaterContainerList = new HashMap<>();

    @SubscribeEvent
    public static void onStarted(ServerStartedEvent v) {
        final FireSafetyExtensionEvent event = new FireSafetyExtensionEvent();
        MinecraftForge.EVENT_BUS.post(event);
        extinguishableBlockList.putAll(event.getExtinguishableBlockList());
        extinguishableEntityList.putAll(event.getExtinguishableEntityList());
        firefightingWaterContainerList.putAll(event.getFirefightingWaterContainerList());
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
            BiPredicate<Level, BlockPos> blockCondition,
            BiFunction<Level, BlockPos, BlockState> targetBlock) {
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

    public static BiFunction<Player, ItemStack, Integer> getFireFightingContainerAmount(short index) {
        if (index == Short.MIN_VALUE) throw new NumberFormatException("Priority value should be greater than -32768");
        return firefightingWaterContainerList.get(index).amount();
    }

    public static Function3<Player, Integer, ItemStack, ItemStack> getFireFightingContainerUsedResult(short index) {
        if (index == Short.MIN_VALUE) throw new NumberFormatException("Priority value should be greater than -32768");
        return firefightingWaterContainerList.get(index).usedResult();
    }

    public static BiFunction<Level, BlockPos, BlockState> getTargetBlockState(short index) {
        if (index == Short.MIN_VALUE) throw new NumberFormatException("Priority value should be greater than -32768");
        return extinguishableBlockList.get(index).targetBlock();
    }

    public static BiConsumer<Level, Entity> getTargetEntityAction(short index) {
        if (index == Short.MIN_VALUE) throw new NumberFormatException("Priority value should be greater than -32768");
        return extinguishableEntityList.get(index).entityAction();
    }

}
