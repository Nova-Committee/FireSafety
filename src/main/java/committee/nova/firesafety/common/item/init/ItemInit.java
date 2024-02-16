package committee.nova.firesafety.common.item.init;

import committee.nova.firesafety.common.item.impl.FireDangerSnifferItem;
import committee.nova.firesafety.common.item.impl.FireFightingAirStrikeControllerItem;
import committee.nova.firesafety.common.item.impl.HandheldExtinguisherItem;
import committee.nova.firesafety.common.item.tab.TabInit;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;

import static committee.nova.firesafety.common.tools.reference.ItemReference.*;
import static committee.nova.firesafety.common.util.RegistryHandler.ITEMS;
import static committee.nova.firesafety.common.util.RegistryHandler.debug;

public class ItemInit {
    public static final HashMap<String, RegistryObject<Item>> items = new HashMap<>();

    public static void init() {
        debug("items");
        items.put(WATER_BOMB, ITEMS.register(WATER_BOMB, () -> new Item(new Item.Properties())));
        items.put(FIREFIGHTING_AIRSTRIKE_CONTROLLER, ITEMS.register(FIREFIGHTING_AIRSTRIKE_CONTROLLER, FireFightingAirStrikeControllerItem::new));
        items.put(FIRE_DANGER_SNIFFER, ITEMS.register(FIRE_DANGER_SNIFFER, FireDangerSnifferItem::new));
        items.put(HANDHELD_EXTINGUISHER, ITEMS.register(HANDHELD_EXTINGUISHER, HandheldExtinguisherItem::new));
        TabInit.itemsIn.addAll(items.values());
    }
}
