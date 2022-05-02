package committee.nova.firesafety.common.block.init;

import committee.nova.firesafety.FireSafety;
import committee.nova.firesafety.common.block.impl.ExtinguisherBlock;
import committee.nova.firesafety.common.block.impl.FireAlarmBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;

import static committee.nova.firesafety.common.block.reference.BlockReference.EXTINGUISHER;
import static committee.nova.firesafety.common.block.reference.BlockReference.FIRE_ALARM;
import static committee.nova.firesafety.common.util.RegistryHandler.BLOCKS;
import static committee.nova.firesafety.common.util.RegistryHandler.ITEMS;

public class BlockInit {
    public static final HashMap<String, RegistryObject<Block>> blockList = new HashMap<>();

    public static void init() {
        blockList.put(FIRE_ALARM, BLOCKS.register(FIRE_ALARM, FireAlarmBlock::new));
        blockList.put(EXTINGUISHER, BLOCKS.register(EXTINGUISHER, ExtinguisherBlock::new));
        blockList.forEach(BlockInit::registerBlockItem);
    }

    public static void registerBlockItem(String id, RegistryObject<Block> block) {
        ITEMS.register(id, () -> new BlockItem(block.get(), new Item.Properties().tab(FireSafety.TAB_MAIN)));
    }
}
