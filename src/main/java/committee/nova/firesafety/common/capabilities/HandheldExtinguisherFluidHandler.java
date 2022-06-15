package committee.nova.firesafety.common.capabilities;

import committee.nova.firesafety.common.tools.misc.FluidUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;

import javax.annotation.ParametersAreNonnullByDefault;

import static committee.nova.firesafety.common.tools.reference.TagKeyReference.FIREFIGHTING;
import static net.minecraft.world.level.material.Fluids.WATER;

@ParametersAreNonnullByDefault
public class HandheldExtinguisherFluidHandler extends FluidHandlerItemStack {
    public HandheldExtinguisherFluidHandler(ItemStack container, int capacity) {
        super(container, capacity);
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return stack.getFluid().is(FIREFIGHTING);
    }

    @Override
    public int fill(FluidStack resource, FluidAction doFill) {
        if (container.getCount() != 1 || resource.isEmpty() || !canFillFluidType(resource)) return 0;
        final var contained = getFluidInTank(0);
        if (contained.isEmpty()) {
            final int fillAmount = Math.min(capacity, resource.getAmount());
            if (doFill.execute()) setFluid(new FluidStack(WATER, fillAmount));
            return fillAmount;
        }
        if (!FluidUtil.areFluidsIn(contained, resource, FIREFIGHTING)) return 0;
        final int fillAmount = Math.min(capacity - contained.getAmount(), resource.getAmount());
        if (doFill.execute() && fillAmount > 0) {
            contained.grow(fillAmount);
            setFluid(contained);
        }
        return fillAmount;
    }
}
