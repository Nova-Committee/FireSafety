package committee.nova.firesafety.common.sound.init;

import committee.nova.firesafety.FireSafety;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;

import static committee.nova.firesafety.common.util.RegistryHandler.SOUNDS;

public class SoundInit {
    public static final String[] soundNames = new String[]{
            "fire_alarm"
    };

    public static final HashMap<String, RegistryObject<SoundEvent>> soundList = new HashMap<>();

    public static void init() {
        for (final String id : soundNames) {
            soundList.put(id, SOUNDS.register(id, () -> new SoundEvent(new ResourceLocation(FireSafety.MODID, id))));
        }
    }

    public static SoundEvent getSound(int id) {
        if (id >= soundNames.length) return SoundEvents.PARROT_FLY;
        return soundList.get(soundNames[id]).get();
    }
}
