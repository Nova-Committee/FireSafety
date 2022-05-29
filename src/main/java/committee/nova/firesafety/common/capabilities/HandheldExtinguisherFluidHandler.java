package committee.nova.firesafety.common.capabilities;

import committee.nova.firesafety.common.tools.misc.FluidUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import org.jetbrains.annotations.NotNull;

import static committee.nova.firesafety.common.tools.reference.TagKeyReference.FIREFIGHTING;

public class HandheldExtinguisherFluidHandler extends FluidHandlerItemStack {
    public HandheldExtinguisherFluidHandler(@NotNull ItemStack container, int capacity) {
        super(container, capacity);
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return stack.getFluid().is(FIREFIGHTING);
    }

    @Override
    public int fill(FluidStack resource, FluidAction doFill) {
        if (container.getCount() != 1 || resource.isEmpty() || !canFillFluidType(resource)) return 0;
        final var contained = getFluidInTank(0);
        if (contained.isEmpty()) {
            final int fillAmount = Math.min(capacity, resource.getAmount());
            if (doFill.execute()) {
                final var filled = resource.copy();
                filled.setAmount(fillAmount);
                setFluid(filled);
            }
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
