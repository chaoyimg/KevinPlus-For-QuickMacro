package kevin.module.modules.movement


import kevin.event.EventTarget
import kevin.event.UpdateEvent
import kevin.module.BooleanValue
import kevin.module.ListValue
import kevin.module.Module
import kevin.module.ModuleCategory
import kevin.utils.BlockUtils
import net.minecraft.block.Block
import net.minecraft.block.BlockAir
import net.minecraft.block.BlockLiquid
import net.minecraft.block.BlockWeb
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import kotlin.math.roundToInt

class NoBlockSlow : Module("NoBlockSlow",description = "", category = ModuleCategory.MOVEMENT) {

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val thePlayer = mc.thePlayer
        if (thePlayer == null || thePlayer.isSneaking  || thePlayer.isSpectator) return

            val water2 = BlockUtils.searchBlocks(8)
            for (block in water2) {
                val blockpos = block.key
                val blocks = block.value
                if ((blocks is BlockLiquid || block is BlockWeb) && mc.thePlayer!!.getDistance(blockpos.x.toDouble(),
                        blockpos.y.toDouble(),
                        blockpos.z.toDouble()
                    ) >= 8.0){
                       mc.netHandler.addToSendQueue(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, blockpos, EnumFacing.DOWN))
                    mc.theWorld.setBlockToAir(blockpos)
                }
            }

    }

    override val tag: String
        get() = "Destory"
}