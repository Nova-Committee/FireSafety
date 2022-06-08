package committee.nova.firesafety.common.tools.sound.init;

import committee.nova.firesafety.FireSafety;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;

import static committee.nova.firesafety.common.util.RegistryHandler.SOUNDS;
import static committee.nova.firesafety.common.util.RegistryHandler.debug;
import static net.minecraft.sounds.SoundEvents.PARROT_FLY;

public class SoundInit {
    public static final String[] soundNames = new String[]{
            "fire_alarm", "confirm", "awaiting_orders", "standby", "fds_scan"
    };

    public static final HashMap<String, RegistryObject<SoundEvent>> soundList = new HashMap<>();

    public static void init() {
        debug("sounds");
        for (final String id : soundNames) {
            soundList.put(id, SOUNDS.register(id, () -> new SoundEvent(new ResourceLocation(FireSafety.MODID, id))));
        }
    }

    public static SoundEvent getSound(int id) {
        if (id >= soundNames.length) return PARROT_FLY;
        return soundList.get(soundNames[id]).get();
    }
}
