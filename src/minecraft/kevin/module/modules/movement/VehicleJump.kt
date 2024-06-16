/*
 * This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package kevin.module.modules.movement

import kevin.event.EventTarget
import kevin.event.Render3DEvent
import kevin.event.UpdateEvent
import kevin.module.*
import kevin.utils.*
import kevin.utils.BlockUtils.getBlock
import net.minecraft.block.BlockAir
import net.minecraft.block.BlockFence
import net.minecraft.block.BlockSnow
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.MovingObjectPosition
import net.minecraft.util.Vec3
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

class VehicleJump : Module("VehicleJump","Use vehicle long/high jump.",category = ModuleCategory.MOVEMENT) {
    private val mode = ListValue("Mode", arrayOf("Motion","SetPos"),"Motion")
    private val motionLong = FloatValue("MotionLong",5F,0.1F,20F)
    private val motionHigh = FloatValue("MotionHigh",.9F,0.1F,20F)
    private val delay = IntegerValue("Delay",200,100,500)
    private val renderPos = ListValue("RenderPos", arrayOf("None","LiquidTP","OutLine"),"OutLine")
    private val colorMode = ListValue("ColorMode", arrayOf("Custom","Rainbow"),"Custom")
    private val colorR = IntegerValue("R",255,0,255)
    private val colorG = IntegerValue("G",255,0,255)
    private val colorB = IntegerValue("B",255,0,255)
    private val lineWidthValue = FloatValue("LineWidth",2F,1F,4F)
    private var jumpState = 1
    private var timer = MSTimer()
    private var lastRide = false
    private var objectPosition: MovingObjectPosition? = null
    private var fixedY = 0.0
    private var endPos: BlockPos? = null
    private var st: IBlockState? = null
    private val ti = MSTimer()
    private var endX = 0.0
    private var endZ = 0.0
    private var endendx = 0.0
    private var endendz = 0.0
    override fun onEnable() {
        jumpState=1
        lastRide=false
        endX = 0.0
        endZ = 0.0
        endendx = 0.0
        endendz = 0.0
        endPos = null
    }
    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (mc.currentScreen == null && Mouse.isButtonDown(2) && ti.hasTimePassed(300) && mode equal "SetPos"){
            ti.reset()
            endPos = objectPosition?.blockPos
            st = BlockUtils.getState(endPos?:return)
            if(st?.block?.material == Material.air){
                endPos = null
                return
            }
            ChatUtils.messageWithStart("§7[§8§lBoatJump§7] §3Position was set to §8" + endPos!!.x + "§3, §8" + ((if (getBlock(objectPosition!!.blockPos!!)!!.getCollisionBoundingBox(mc.theWorld!!, objectPosition!!.blockPos!!, getBlock(objectPosition!!.blockPos!!)!!.defaultState!!) == null) endPos!!.y + 1.0 else getBlock(objectPosition!!.blockPos!!)!!.getCollisionBoundingBox(mc.theWorld!!, objectPosition!!.blockPos!!, getBlock(objectPosition!!.blockPos!!)!!.defaultState!!)!!.maxY) + fixedY) + "§3, §8" + endPos!!.z)
        }
        if (endPos != null){
            endX = endPos!!.x + 0.5
            endZ = endPos!!.z + 0.5
        }

        if (mc.thePlayer!!.isRiding && jumpState == 1 && (endPos != null || !(mode equal "SetPos"))) {
            if (mc.thePlayer != null){
                endendx = endX - mc.thePlayer!!.posX
                endendz = endZ - mc.thePlayer!!.posZ
            }

            if (!lastRide) {
                timer.reset()
            }
            if (timer.hasTimePassed(delay.get().toLong())) {
                jumpState = 2
                mc.gameSettings.keyBindSneak.pressed = true
            }
        } else if (jumpState == 2 && !mc.thePlayer!!.isRiding) {
            mc.gameSettings.keyBindSneak.pressed = false
            val radiansYaw = mc.thePlayer!!.rotationYaw * Math.PI / 180
            if (mode equal "Motion") {
                mc.thePlayer!!.motionX = motionLong.get() * -sin(radiansYaw)
                mc.thePlayer!!.motionZ = motionLong.get() * cos(radiansYaw)
                mc.thePlayer!!.motionY = motionHigh.get().toDouble()
            }else {
                mc.thePlayer!!.motionX = endendx*0.098
                mc.thePlayer!!.motionZ = endendz*0.098
                mc.thePlayer!!.motionY = motionHigh.get().toDouble()
            }
            jumpState = 1
            timer.reset()
        }
        lastRide=mc.thePlayer!!.isRiding
    }
    @EventTarget
    fun onRender3D(event: Render3DEvent){
        if (!(mode equal "SetPos")) return
        val theplayer = mc.thePlayer ?: return
        val entityLookVec = theplayer.lookVec ?: return
        val lookVec = Vec3(entityLookVec.xCoord * 300, entityLookVec.yCoord * 300, entityLookVec.zCoord * 300)
        val posVec = Vec3(theplayer.posX, theplayer.posY + 1.62, theplayer.posZ)
        objectPosition = mc.theWorld?.rayTraceBlocks(posVec, posVec.add(lookVec), false, false, false)
        if (objectPosition == null || objectPosition!!.blockPos == null) return
        val belowBlockPos = BlockPos(objectPosition!!.blockPos!!.x, objectPosition!!.blockPos!!.y - 1, objectPosition!!.blockPos!!.z)
        fixedY = if (getBlock(objectPosition!!.blockPos!!) is BlockFence) if (mc.theWorld!!.getCollidingBoundingBoxes(theplayer, theplayer.entityBoundingBox.offset(objectPosition!!.blockPos!!.x + 0.5 - theplayer.posX, objectPosition!!.blockPos!!.y + 1.5 - theplayer.posY, objectPosition!!.blockPos!!.z + 0.5 - theplayer.posZ)).isEmpty()) 0.5 else 0.0 else if (getBlock(belowBlockPos) is BlockFence) if (!mc.theWorld!!.getCollidingBoundingBoxes(theplayer, theplayer.entityBoundingBox.offset(objectPosition!!.blockPos!!.x + 0.5 - theplayer.posX, objectPosition!!.blockPos!!.y + 0.5 - theplayer.posY, objectPosition!!.blockPos!!.z + 0.5 - theplayer.posZ)).isEmpty() || getBlock(objectPosition!!.blockPos!!)!!.getCollisionBoundingBox(mc.theWorld!!, objectPosition!!.blockPos!!, getBlock(objectPosition!!.blockPos!!)!!.defaultState!!) == null) 0.0 else 0.5 - 1 else if (getBlock(objectPosition!!.blockPos!!) is BlockSnow) 1 - 0.125 else 0.0
        val x = objectPosition!!.blockPos!!.x
        val y: Double = (if (getBlock(objectPosition!!.blockPos!!)!!.getCollisionBoundingBox(mc.theWorld!!, objectPosition!!.blockPos!!, getBlock(objectPosition!!.blockPos!!)!!.defaultState!!) == null) objectPosition!!.blockPos!!.y + 1.0 else getBlock(objectPosition!!.blockPos!!)!!.getCollisionBoundingBox(mc.theWorld!!, objectPosition!!.blockPos!!, getBlock(objectPosition!!.blockPos!!)!!.defaultState!!)!!.maxY) - 1.0 + fixedY
        val z = objectPosition!!.blockPos!!.z
        if (getBlock(objectPosition!!.blockPos!!) !is BlockAir) {
            when(renderPos.get()){
                "LiquidTP" -> {
                    val renderManager = mc.renderManager
                    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
                    GL11.glEnable(GL11.GL_BLEND)
                    GL11.glLineWidth(2f)
                    GL11.glDisable(GL11.GL_TEXTURE_2D)
                    GL11.glDisable(GL11.GL_DEPTH_TEST)
                    GL11.glDepthMask(false)
                    RenderUtils.glColor(if (!mc.theWorld!!.getCollidingBoundingBoxes(theplayer, theplayer.entityBoundingBox.offset(x + 0.5 - theplayer.posX, y + 1.0 - theplayer.posY, z + 0.5 - theplayer.posZ)).isEmpty()) Color(255, 0, 0, 90) else Color(0, 255, 0, 90))
                    RenderUtils.drawFilledBox(
                        AxisAlignedBB(
                            x - renderManager.renderPosX,
                            y + 1 - renderManager.renderPosY,
                            z - renderManager.renderPosZ,
                            x - renderManager.renderPosX + 1.0,
                            y + 1.2 - renderManager.renderPosY,
                            z - renderManager.renderPosZ + 1.0
                        )
                    )
                    GL11.glEnable(GL11.GL_TEXTURE_2D)
                    GL11.glEnable(GL11.GL_DEPTH_TEST)
                    GL11.glDepthMask(true)
                    GL11.glDisable(GL11.GL_BLEND)
                    RenderUtils.renderNameTag(
                        theplayer.getDistance(x + 0.5, y + 1.0, z + 0.5).roundToInt().toString() + "m",
                        x + 0.5,
                        y + 1.7,
                        z + 0.5
                    )
                    GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
                }
                "OutLine" -> {
                    val color = if (colorMode.get() == "Custom") Color(colorR.get(),colorG.get(),colorB.get()) else ColorUtils.rainbow()
                    val blockPosition = objectPosition!!.blockPos!!
                    val block = mc.theWorld!!.getBlockState(blockPosition).block

                    GlStateManager.enableBlend()
                    GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)
                    RenderUtils.glColor(color)
                    GL11.glLineWidth(lineWidthValue.get())
                    GlStateManager.disableTexture2D()
                    GL11.glDepthMask(false)
                    block.setBlockBoundsBasedOnState(mc.theWorld!!, blockPosition)
                    val thePlayer = mc.thePlayer ?: return
                    val partialTicks = event.partialTicks

                    val px = thePlayer.lastTickPosX + (thePlayer.posX - thePlayer.lastTickPosX) * partialTicks
                    val py = thePlayer.lastTickPosY + (thePlayer.posY - thePlayer.lastTickPosY) * partialTicks
                    val pz = thePlayer.lastTickPosZ + (thePlayer.posZ - thePlayer.lastTickPosZ) * partialTicks

                    val axisAlignedBB = block.getSelectedBoundingBox(mc.theWorld!!, blockPosition)
                        .expand(0.0020000000949949026, 0.0020000000949949026, 0.0020000000949949026)
                        .offset(-px, -py, -pz)

                    RenderUtils.drawSelectionBoundingBox(axisAlignedBB)
                    GL11.glDepthMask(true)
                    GlStateManager.enableTexture2D()
                    GlStateManager.disableBlend()
                    GlStateManager.resetColor()
                }
            }
        }
    }
}