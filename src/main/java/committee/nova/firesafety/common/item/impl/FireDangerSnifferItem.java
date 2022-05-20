package committee.nova.firesafety.common.item.impl;

import committee.nova.firesafety.common.item.IArmPoseChangeable;
import committee.nova.firesafety.common.item.base.FireSafetyItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Wearable;

public class FireDangerSnifferItem extends FireSafetyItem implements Wearable, IArmPoseChangeable {
    public FireDangerSnifferItem() {
        super(new Properties().stacksTo(1).fireResistant());
    }

    @Override
    public EquipmentSlot getEquipmentSlot(ItemStack stack) {
        return EquipmentSlot.HEAD;
    }

    //todo
}
