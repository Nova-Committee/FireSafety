package committee.nova.firesafety.common.tools;

import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

public class PlayerHandler {
    public static void notifyServerPlayer(Player p, Component c) {
        if (!p.level.isClientSide) p.sendMessage(c, Util.NIL_UUID);
    }

    public static void displayClientMessage(Player p, Component c) {
        p.displayClientMessage(c, true);
    }

    public static void playSoundForThisPlayer(Player player, SoundEvent sound, float volume, float pitch) {
        player.playNotifySound(sound, SoundSource.PLAYERS, volume, pitch);
    }
}
