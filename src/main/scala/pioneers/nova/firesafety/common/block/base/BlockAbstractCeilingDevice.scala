package pioneers.nova.firesafety.common.block.base

import net.minecraft.block.material.Material
import net.minecraft.block.properties.PropertyBool
import net.minecraft.block.state.{BlockStateContainer, IBlockState}
import net.minecraft.block.{Block, SoundType}
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.SoundEvents
import net.minecraft.util.math.{AxisAlignedBB, BlockPos}
import net.minecraft.util.text.TextComponentTranslation
import net.minecraft.util.{EnumFacing, EnumHand}
import net.minecraft.world.{IBlockAccess, World}
import pioneers.nova.firesafety.common.block.api.{BlockScalable, TSpecialRenderType}
import pioneers.nova.firesafety.common.block.base.BlockAbstractCeilingDevice.onfire
import pioneers.nova.firesafety.common.block.tile.TileEntityRecordableDevice
import pioneers.nova.firesafety.common.utils.misc.PlayerHandler.notifyServerPlayer

object BlockAbstractCeilingDevice {
  val onfire: PropertyBool = PropertyBool.create("onfire")
}

class BlockAbstractCeilingDevice extends BlockScalable(Material.IRON) with TSpecialRenderType {
  setHardness(2F)
  setResistance(1000F)
  setSoundType(SoundType.METAL)
  setDefaultState(blockState.getBaseState.withProperty(onfire, false))

  override def getLightValue(state: IBlockState, world: IBlockAccess, pos: BlockPos): Int = if (state.getValue(onfire)) 12 else 6

  override def isOpaqueCube(state: IBlockState): Boolean = false

  override def createBlockState(): BlockStateContainer = new BlockStateContainer(this, onfire)

  override def getStateForPlacement(world: World, pos: BlockPos, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float, meta: Int, placer: EntityLivingBase, hand: EnumHand)
  : IBlockState = getDefaultState.withProperty(onfire, false)

  override def getStateFromMeta(meta: Int): IBlockState = getDefaultState.withProperty(onfire, meta == 1)

  override def getMetaFromState(state: IBlockState): Int = if (state.getValue(onfire)) 1 else 0

  override def getBoundingBox(state: IBlockState, source: IBlockAccess, pos: BlockPos): AxisAlignedBB = new AxisAlignedBB(5, 15, 5, 11, 16, 11)

  override def canPlaceBlockAt(worldIn: World, pos: BlockPos): Boolean = {
    val posUpon = pos.up()
    val blockUpon = worldIn.getBlockState(posUpon)
    blockUpon.isSideSolid(worldIn, posUpon, EnumFacing.DOWN)
  }

  override def neighborChanged(state: IBlockState, worldIn: World, pos: BlockPos, blockIn: Block, fromPos: BlockPos): Unit = {
    if (canPlaceBlockAt(worldIn, pos)) return
    dropBlockAsItem(worldIn, pos, state, 0)
    worldIn.setBlockToAir(pos)
  }

  override def onBlockActivated(worldIn: World, pos: BlockPos, state: IBlockState, playerIn: EntityPlayer, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): Boolean = {
    if (!worldIn.isRemote) tryHandleListener(playerIn, pos)
    true
  }

  def tryHandleListener(player: EntityPlayer, pos: BlockPos): Unit = {
    val world = player.world
    if (world == null) return
    val tile = world.getTileEntity(pos)
    if (!tile.isInstanceOf[TileEntityRecordableDevice]) return
    val recordable = tile.asInstanceOf[TileEntityRecordableDevice]
    val b = recordable.handleListener(player)
    player.playSound(SoundEvents.BLOCK_NOTE_XYLOPHONE, .5F, if (b) 1F else .5F)
    notifyServerPlayer(player, new TextComponentTranslation("msg.firesafety.device.listening." + (if (b) "true" else "false")))
  }
}
