package pioneers.nova.firesafety.common.utils.misc

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.text.ITextComponent

object PlayerHandler {
  def notifyServerPlayer(player: EntityPlayer, msg: ITextComponent): Unit = if (!player.world.isRemote) player.sendMessage(msg)

}
