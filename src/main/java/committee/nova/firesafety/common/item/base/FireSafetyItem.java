package committee.nova.firesafety.common.item.base;

import committee.nova.firesafety.FireSafety;
import net.minecraft.world.item.Item;

public class FireSafetyItem extends Item {
    public FireSafetyItem(Properties p) {
        super(p.tab(FireSafety.TAB_MAIN));
    }

    public FireSafetyItem() {
        this(new Properties());
    }
}
