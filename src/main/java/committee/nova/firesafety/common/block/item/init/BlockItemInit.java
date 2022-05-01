package committee.nova.firesafety.common.block.item.init;

import committee.nova.firesafety.FireSafety;
import committee.nova.firesafety.common.block.init.BlockInit;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;

import static committee.nova.firesafety.common.util.RegistryHandler.ITEMS;

public class BlockItemInit {
    public static void init() {
        BlockInit.blockList.forEach(BlockItemInit::registerBlockItem);
    }

    public static void registerBlockItem(String id, RegistryObject<Block> block) {
        ITEMS.register(id, () -> new BlockItem(block.get(), new Item.Properties().tab(FireSafety.TAB_MAIN)));
    }
}
