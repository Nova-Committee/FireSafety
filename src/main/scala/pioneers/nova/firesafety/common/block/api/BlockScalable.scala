package pioneers.nova.firesafety.common.block.api

import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.util.BlockRenderLayer

abstract class BlockScalable(material: Material) extends Block(material) {
  override def getBlockLayer: BlockRenderLayer = this match {
    case renderType: TSpecialRenderType => renderType.getRenderType
    case _ => BlockRenderLayer.SOLID
  }


}
