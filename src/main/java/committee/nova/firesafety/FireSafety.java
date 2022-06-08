package committee.nova.firesafety;

import committee.nova.firesafety.common.tools.reference.DataReference;
import committee.nova.firesafety.common.util.RegistryHandler;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;

import static committee.nova.firesafety.common.tools.reference.BlockReference.EXTINGUISHER;
import static committee.nova.firesafety.common.tools.reference.BlockReference.getRegisteredBlock;

@Mod(FireSafety.MODID)
public class FireSafety {
    public static final String MODID = "firesafety";
    public static final Logger LOGGER = LogManager.getLogger();
    public static final CreativeModeTab TAB_MAIN = new CreativeModeTab(MODID) {
        @Nonnull
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(getRegisteredBlock(EXTINGUISHER));
        }
    };

    public FireSafety() {
        DataReference.init();
        RegistryHandler.init();
        MinecraftForge.EVENT_BUS.register(this);
    }

    //todo:recipes
}
