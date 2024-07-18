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
package kevin.module.modules.movement.speeds.other

import kevin.event.UpdateEvent
import kevin.main.KevinClient
import kevin.module.FloatValue
import kevin.module.ListValue
import kevin.module.modules.movement.speeds.SpeedMode
import kevin.module.modules.player.Blink
import kevin.utils.MovementUtils
import kevin.utils.RotationUtils
import kevin.utils.rotation
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import kotlin.math.cos
import kotlin.math.sin

object Collision : SpeedMode("Collision") {
    private val mode by ListValue("Mode", arrayOf("EntityBHop","EntityBoost","Mix"),"EntityBoost")
    private val sizeValue by FloatValue("CustomRange", 1.5f, 0f, 2f)
    private val speedValue by FloatValue("Speed", 0.8f, 0f, 1f)

    override fun onUpdate(event: UpdateEvent) {
        if (!MovementUtils.isMoving || KevinClient.moduleManager.getModule(Blink::class.java)!!.state)
            return

        val playerBox = mc.thePlayer.entityBoundingBox.expand(sizeValue.toDouble(), sizeValue.toDouble(), sizeValue.toDouble())

        val collisions = mc.theWorld.loadedEntityList.count {
            it != mc.thePlayer && it is EntityLivingBase &&
                    it !is EntityArmorStand && playerBox.intersectsWith(it.entityBoundingBox)
        }
        if (collisions == 0) return
        val rotation = RotationUtils.serverRotation ?: mc.thePlayer.rotation

        val yaw = MovementUtils.getRawDirection(rotation.yaw)
        val boost = speedValue.toDouble() * collisions * 0.1
        when(mode) {
            "EntityBoost" -> {
                mc.thePlayer.addVelocity(-sin(yaw) * boost, 0.0, cos(yaw) * boost)
            }

            "EntityBHop" -> {
                mc.thePlayer.jumpMovementFactor = boost.toFloat()
            }

            "Mix" -> {
                if (mc.thePlayer.onGround || mc.thePlayer.serverSprintState) {
                    mc.thePlayer.addVelocity(-sin(yaw) * boost, 0.0, cos(yaw) * boost)
                } else {
                    mc.thePlayer.jumpMovementFactor = boost.toFloat()
                }
            }
        }
    }
}