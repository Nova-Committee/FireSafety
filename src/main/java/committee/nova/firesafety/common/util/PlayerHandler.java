package committee.nova.firesafety.common.util;

import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

public class PlayerHandler {
    public static int notifyServerPlayer(Player p, Component c) {
        if (!p.level.isClientSide) p.sendMessage(c, Util.NIL_UUID);
        return 0;
    }

    public static int displayClientMessage(Player p, Component c) {
        p.displayClientMessage(c, true);
        return 0;
    }

    public static int playSoundForThisPlayer(Player player, SoundEvent sound, float volume, float pitch) {
        player.playNotifySound(sound, SoundSource.PLAYERS, volume, pitch);
        return 0;
    }
}
