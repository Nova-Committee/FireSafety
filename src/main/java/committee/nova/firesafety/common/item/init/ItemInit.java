package committee.nova.firesafety.common.item.init;

import committee.nova.firesafety.common.item.base.FireSafetyItem;
import committee.nova.firesafety.common.item.impl.FireFightingAirStrikeControllerItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;

import static committee.nova.firesafety.common.tools.reference.ItemReference.FIREFIGHTING_AIRSTRIKE_CONTROLLER;
import static committee.nova.firesafety.common.tools.reference.ItemReference.WATER_BOMB;
import static committee.nova.firesafety.common.util.RegistryHandler.ITEMS;
import static committee.nova.firesafety.common.util.RegistryHandler.debug;

public class ItemInit {
    public static final HashMap<String, RegistryObject<Item>> items = new HashMap<>();

    public static void init() {
        debug("items");
        items.put(WATER_BOMB, ITEMS.register(WATER_BOMB, FireSafetyItem::new));
        items.put(FIREFIGHTING_AIRSTRIKE_CONTROLLER, ITEMS.register(FIREFIGHTING_AIRSTRIKE_CONTROLLER, FireFightingAirStrikeControllerItem::new));
    }
}
