package pioneers.nova.firesafety

import net.minecraftforge.fml.common.Mod.EventHandler
import net.minecraftforge.fml.common.event.{FMLInitializationEvent, FMLPostInitializationEvent, FMLPreInitializationEvent}
import net.minecraftforge.fml.common.{Mod, SidedProxy}
import pioneers.nova.firesafety.FireSafety.MODID
import pioneers.nova.firesafety.proxies.CommonProxy

@Mod(modid = MODID, useMetadata = true, modLanguage = "scala")
object FireSafety {
  final val MODID = "firesafety"

  @SidedProxy(
    serverSide = "pioneers.nova.firesafety.proxies.CommonProxy",
    clientSide = "pioneers.nova.firesafety.proxies.ClientProxy"
  )
  var proxy: CommonProxy = _

  @EventHandler
  def preInit(e: FMLPreInitializationEvent): Unit = proxy.preInit(e)

  @EventHandler
  def init(e: FMLInitializationEvent): Unit = proxy.init(e)

  @EventHandler
  def postInit(e: FMLPostInitializationEvent): Unit = proxy.postInit(e)
}
