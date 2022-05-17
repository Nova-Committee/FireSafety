package committee.nova.firesafety.common.util;

import committee.nova.firesafety.FireSafety;
import committee.nova.firesafety.common.block.blockEntity.init.BlockEntityInit;
import committee.nova.firesafety.common.block.init.BlockInit;
import committee.nova.firesafety.common.config.Configuration;
import committee.nova.firesafety.common.entity.init.EntityInit;
import committee.nova.firesafety.common.item.init.ItemInit;
import committee.nova.firesafety.common.sound.init.SoundInit;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class RegistryHandler {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, FireSafety.MODID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, FireSafety.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, FireSafety.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, FireSafety.MODID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, FireSafety.MODID);

    public static void init() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Configuration.COMMON_CONFIG);
        final var eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        SOUNDS.register(eventBus);
        BLOCKS.register(eventBus);
        BLOCK_ENTITIES.register(eventBus);
        ITEMS.register(eventBus);
        ENTITIES.register(eventBus);
        SoundInit.init();
        BlockInit.init();
        BlockEntityInit.init();
        ItemInit.init();
        EntityInit.init();
    }
}
