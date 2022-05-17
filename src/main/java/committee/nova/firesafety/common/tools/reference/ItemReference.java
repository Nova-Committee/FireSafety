package committee.nova.firesafety.common.tools.reference;

import net.minecraft.world.item.Item;

import static committee.nova.firesafety.common.item.init.ItemInit.items;

public class ItemReference {
    public static final String WATER_BOMB = "water_bomb";
    public static final String FIREFIGHTING_AIRSTRIKE_CONTROLLER = "ffasc";

    public static Item getRegisteredItem(String id) {
        return items.get(id).get();
    }
}
