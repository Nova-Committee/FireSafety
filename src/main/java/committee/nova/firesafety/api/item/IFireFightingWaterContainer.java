package committee.nova.firesafety.api.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface IFireFightingWaterContainer {
    /**
     * @param stack The water container stack
     * @return Returns the current water amount of the itemstack
     **/
    int getWaterAmount(ItemStack stack);

    /**
     * @param player The player using the itemstack
     * @param amount The amount of water consumed
     * @param stack  The old stack
     * @return Returns an ItemStack being consumed of water
     **/
    ItemStack consume(Player player, int amount, ItemStack stack);

    /**
     * @param player The player using the itemstack
     * @param amount The amount of water consumed
     * @param stack  The old stack
     *               Execute other actions when consumed
     */
    void influence(Player player, int amount, ItemStack stack);
}
