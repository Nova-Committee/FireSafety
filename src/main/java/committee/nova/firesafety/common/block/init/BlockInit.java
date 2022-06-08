package committee.nova.firesafety.common.block.init;

import committee.nova.firesafety.FireSafety;
import committee.nova.firesafety.common.block.impl.ExtinguisherBlock;
import committee.nova.firesafety.common.block.impl.FireAlarmBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;

import java.util.TreeMap;

import static committee.nova.firesafety.common.tools.reference.BlockReference.EXTINGUISHER;
import static committee.nova.firesafety.common.tools.reference.BlockReference.FIRE_ALARM;
import static committee.nova.firesafety.common.util.RegistryHandler.*;

public class BlockInit {
    public static final TreeMap<String, RegistryObject<Block>> blockList = new TreeMap<>();

    public static void init() {
        debug("blocks");
        blockList.put(FIRE_ALARM, BLOCKS.register(FIRE_ALARM, FireAlarmBlock::new));
        blockList.put(EXTINGUISHER, BLOCKS.register(EXTINGUISHER, ExtinguisherBlock::new));
        debug("blockItems");
        blockList.forEach(BlockInit::registerBlockItem);
    }

    public static void registerBlockItem(String id, RegistryObject<Block> block) {
        ITEMS.register(id, () -> new BlockItem(block.get(), new Item.Properties().tab(FireSafety.TAB_MAIN)));
    }
}
