package committee.nova.firesafety.common.item.tab;

import committee.nova.firesafety.FireSafety;
import committee.nova.firesafety.common.util.RegistryHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;

import static committee.nova.firesafety.common.tools.reference.BlockReference.EXTINGUISHER;
import static committee.nova.firesafety.common.tools.reference.BlockReference.getRegisteredBlock;
import static committee.nova.firesafety.common.tools.reference.ItemReference.HANDHELD_EXTINGUISHER;
import static committee.nova.firesafety.common.tools.reference.ItemReference.getRegisteredItem;

public class TabInit {
    public static List<RegistryObject<Item>> itemsIn = new ArrayList<>();

    public static void init() {
        RegistryHandler.TABS.register(FireSafety.MODID, () -> CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.firesafety"))
                .icon(() -> getRegisteredBlock(EXTINGUISHER).asItem().getDefaultInstance())
                .displayItems((p, o) -> {
                    itemsIn.stream().map(RegistryObject::get).forEach(o::accept);
                    final var filled = getRegisteredItem(HANDHELD_EXTINGUISHER).getDefaultInstance();
                    filled.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM)
                            .resolve()
                            .ifPresent(iFluidHandlerItem -> iFluidHandlerItem.fill(
                                    new FluidStack(Fluids.WATER, 10000),
                                    IFluidHandler.FluidAction.EXECUTE)
                            );
                    o.accept(filled);
                })
                .build());
    }
}
