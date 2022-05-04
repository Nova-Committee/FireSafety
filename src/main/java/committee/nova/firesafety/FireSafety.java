package committee.nova.firesafety;

import committee.nova.firesafety.common.config.Configuration;
import committee.nova.firesafety.common.tools.DataReference;
import committee.nova.firesafety.common.util.RegistryHandler;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

import javax.annotation.Nonnull;

import static committee.nova.firesafety.common.block.reference.BlockReference.EXTINGUISHER;
import static committee.nova.firesafety.common.block.reference.BlockReference.getRegisteredBlock;

@Mod(FireSafety.MODID)
public class FireSafety {
    public static final String MODID = "firesafety";
    public static final CreativeModeTab TAB_MAIN = new CreativeModeTab(MODID) {
        @Nonnull
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(getRegisteredBlock(EXTINGUISHER));
        }
    };

    public FireSafety() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Configuration.COMMON_CONFIG);
        DataReference.init();
        RegistryHandler.init();
        MinecraftForge.EVENT_BUS.register(this);
    }
    //todo:recipes
}
