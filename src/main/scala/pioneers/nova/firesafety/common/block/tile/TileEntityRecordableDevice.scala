package pioneers.nova.firesafety.common.block.tile

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.tileentity.TileEntity

import java.util.UUID
import scala.collection.mutable

class TileEntityRecordableDevice extends TileEntity {
  val listeners = new mutable.HashMap[UUID, Boolean]()

  def handleListener(player: EntityPlayer): Boolean = {
    val uuid = player.getUniqueID
    if (!listeners.contains(uuid) || !listeners(uuid)) {
      listeners.put(uuid, true)
      return true
    }
    listeners.remove(uuid)
    false
  }
}
