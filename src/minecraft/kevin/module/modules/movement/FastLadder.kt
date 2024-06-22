package kevin.module.modules.movement

import kevin.event.EventTarget
import kevin.event.MoveEvent
import kevin.module.*
import kevin.utils.BlockUtils
import net.minecraft.init.Blocks

import net.minecraft.util.BlockPos

class FastLadder : Module("FastLadder","Automatically setbacks you after falling a certain distance.", category = ModuleCategory.MOVEMENT) {

    @EventTarget
    fun onMove(event: MoveEvent) {
        val thePlayer = mc.thePlayer ?: return
          if ( thePlayer.isCollidedHorizontally &&
                    thePlayer.isOnLadder ) {
                val blockPos = BlockPos(mc.thePlayer!!.posX, mc.thePlayer!!.posY + 1, mc.thePlayer!!.posZ)
                val block = BlockUtils.getBlock(blockPos)!!

                if (block != Blocks.ladder) return

                if (mc.gameSettings.keyBindLeft.pressed || mc.gameSettings.keyBindRight.pressed) {
                    return
                }
                if (block == Blocks.ladder) {
                    event.y = 0.1699
                    thePlayer.motionY = 0.0
                }
            }
    }
}