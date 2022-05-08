package committee.nova.firesafety.api.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface IFireFightingWaterContainer {
    /**
     * Returns the current water amount of the itemstack
     **/
    int getWaterAmount(ItemStack stack);

    /**
     * Returns an ItemStack being consumed of water
     **/
    ItemStack consume(Player p, int amount);
}
