package committee.nova.firesafety.common.tools.misc;

import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;

import static committee.nova.firesafety.api.FireSafetyApi.getFireFightingContainer;
import static committee.nova.firesafety.api.FireSafetyApi.getFireFightingContainerIndex;
import static committee.nova.firesafety.common.tools.misc.PlayerHandler.getActionByMode;
import static committee.nova.firesafety.common.tools.reference.TagKeyReference.FIREFIGHTING;
import static net.minecraft.world.level.material.Fluids.WATER;
import static net.minecraftforge.fluids.capability.CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY;

public class FluidUtil {
    public static boolean areFluidsIn(@Nonnull FluidStack stack, @Nonnull FluidStack other, TagKey<Fluid> tag) {
        return stack.getFluid().is(tag) && other.getFluid().is(tag);
    }

    public static FluidStack tryFill(Player player, InteractionHand hand, int needed, ItemStack off) {
        final var toFill = new FluidStack[1];
        toFill[0] = FluidStack.EMPTY;
        final short index = getFireFightingContainerIndex(player, off);
        if (index > Short.MIN_VALUE) {
            final var i = getFireFightingContainer(index);
            final int shouldFill = Math.min(i.amount().apply(player, off), needed);
            toFill[0] = new FluidStack(WATER, shouldFill);
            if (!player.isCreative()) player.setItemInHand(hand, i.usedResult().apply(player, shouldFill, off));
            i.usedInfluence().accept(player, shouldFill, off);
        } else {
            off.getCapability(FLUID_HANDLER_ITEM_CAPABILITY).ifPresent(f -> {
                if (f.getFluidInTank(0).getFluid().is(FIREFIGHTING))
                    toFill[0] = new FluidStack(WATER, f.drain(needed, getActionByMode(player)).getAmount());
            });
        }
        return toFill[0];
    }
}
