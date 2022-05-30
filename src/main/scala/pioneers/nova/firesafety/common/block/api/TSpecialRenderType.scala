package pioneers.nova.firesafety.common.block.api

import net.minecraft.util.BlockRenderLayer

trait TSpecialRenderType {
  def getRenderType: BlockRenderLayer = BlockRenderLayer.TRANSLUCENT
}
