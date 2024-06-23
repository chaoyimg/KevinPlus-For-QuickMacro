package kevin.module.modules.movement


import kevin.event.EventTarget
import kevin.event.MotionEvent
import kevin.module.IntegerValue
import kevin.module.Module
import kevin.module.ModuleCategory
import kevin.utils.BlockUtils
import net.minecraft.block.Block
import net.minecraft.block.BlockLiquid
import net.minecraft.block.BlockWeb
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing

class NoBlockSlow : Module("NoBlockSlow",description = "", category = ModuleCategory.MOVEMENT) {
    private val tickPacket = IntegerValue("tickDestory", 3, 1, 10)
    @EventTarget
    fun onMotion(event: MotionEvent) {
        val thePlayer = mc.thePlayer
        if (thePlayer == null || thePlayer.isSneaking  || thePlayer.isSpectator) return
            var needcancel = BlockUtils.searchBlocks(8).toMutableMap()
        repeat(tickPacket.get()) {
            val block = needcancel.entries.firstNotNullOfOrNull { entry: Map.Entry<BlockPos, Block> ->
                val block = entry.value
                val blockpos = entry.key
                if (mc.thePlayer!!.getDistance(
                        blockpos.x.toDouble(),
                        blockpos.y.toDouble(),
                        blockpos.z.toDouble()
                    ) >= 7.0 && (block is BlockLiquid || block is BlockWeb)
                ) {
                    blockpos
                } else null
            }
            if (block != null) {
                mc.netHandler.addToSendQueue(
                    C07PacketPlayerDigging(
                        C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK,
                        block,
                        EnumFacing.DOWN
                    )
                )
                mc.theWorld.setBlockToAir(block)
                needcancel.remove(block)
            }
        }
    }

    override val tag: String
        get() = "Destory"
}