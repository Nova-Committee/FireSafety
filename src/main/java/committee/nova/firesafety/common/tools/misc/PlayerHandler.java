package committee.nova.firesafety.common.tools.misc;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;

import static net.minecraft.Util.NIL_UUID;
import static net.minecraft.sounds.SoundSource.PLAYERS;

public class PlayerHandler {
    public static void notifyServerPlayer(Player p, Component c) {
        if (!p.level.isClientSide) p.sendMessage(c, NIL_UUID);
    }

    public static void displayClientMessage(Player p, Component c) {
        p.displayClientMessage(c, true);
    }

    public static void playSoundForThisPlayer(Player player, SoundEvent sound, float volume, float pitch) {
        player.playNotifySound(sound, PLAYERS, volume, pitch);
    }

    public static IFluidHandler.FluidAction getActionByMode(@Nonnull Player player) {
        return player.isCreative() ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE;
    }
}
