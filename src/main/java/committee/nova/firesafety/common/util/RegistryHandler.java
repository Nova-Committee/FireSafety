package committee.nova.firesafety.common.util;

import committee.nova.firesafety.FireSafety;
import committee.nova.firesafety.common.block.entity.init.BlockEntityInit;
import committee.nova.firesafety.common.block.init.BlockInit;
import committee.nova.firesafety.common.entity.init.EntityInit;
import committee.nova.firesafety.common.item.init.ItemInit;
import committee.nova.firesafety.common.item.tab.TabInit;
import committee.nova.firesafety.common.tools.sound.init.SoundInit;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import static committee.nova.firesafety.common.config.Configuration.COMMON_CONFIG;
import static net.minecraftforge.fml.config.ModConfig.Type.COMMON;

public class RegistryHandler {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, FireSafety.MODID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, FireSafety.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, FireSafety.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, FireSafety.MODID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, FireSafety.MODID);

    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), FireSafety.MODID);

    public static void init() {
        ModLoadingContext.get().registerConfig(COMMON, COMMON_CONFIG);
        final var eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        SOUNDS.register(eventBus);
        BLOCKS.register(eventBus);
        BLOCK_ENTITIES.register(eventBus);
        ITEMS.register(eventBus);
        ENTITIES.register(eventBus);
        TABS.register(eventBus);
        SoundInit.init();
        BlockInit.init();
        BlockEntityInit.init();
        ItemInit.init();
        EntityInit.init();
        TabInit.init();
    }

    public static void debug(String registryType) {
        FireSafety.LOGGER.debug("Initializing FireSafety {}", registryType);
    }
}
