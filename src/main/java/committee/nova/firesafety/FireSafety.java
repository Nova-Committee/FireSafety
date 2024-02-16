package committee.nova.firesafety;

import committee.nova.firesafety.common.tools.reference.DataReference;
import committee.nova.firesafety.common.util.RegistryHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(FireSafety.MODID)
public class FireSafety {
    public static final String MODID = "firesafety";
    public static final Logger LOGGER = LogManager.getLogger();

    public FireSafety() {
        DataReference.init();
        RegistryHandler.init();
        MinecraftForge.EVENT_BUS.register(this);
    }
}
