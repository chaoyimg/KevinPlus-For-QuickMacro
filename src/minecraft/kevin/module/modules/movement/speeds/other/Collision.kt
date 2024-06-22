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
import kevin.module.FloatValue
import kevin.module.modules.movement.speeds.SpeedMode
import kevin.module.modules.movement.speeds.other.Collision.rangeValue
import kevin.utils.EntityUtils
import kevin.utils.MinecraftInstance.mc
import kevin.utils.MovementUtils
import kevin.utils.connection.getDistanceToEntityBox2
import net.minecraft.entity.Entity
import kotlin.math.cos
import kotlin.math.sin

object Collision : SpeedMode("Collision") {
    private val rangeValue = FloatValue("CustomRange", 1.5f, 0f, 2f)
    private val speedValue = FloatValue("Speed", 0.8f, 0f, 1f)
    override fun onUpdate(event: UpdateEvent) {
        if (mc.thePlayer == null) return

        if (mc.thePlayer.moveForward == 0.0f && mc.thePlayer.moveStrafing == 0.0f) return

        var ticks = 0

        if (getNearestEntityInRange() != null) {
            ticks++
        }

        val yaw = Math.toRadians(mc.thePlayer.rotationYaw.toDouble())
        val boost = 0.1 * speedValue.get() * ticks
        mc.thePlayer.addVelocity(-sin(yaw) * boost, 0.0, cos(yaw) * boost)

    }

    private fun getAllEntities(): List<Entity> {
        return mc.theWorld.loadedEntityList.filter {
            EntityUtils.isSelected(it, false)
        }.toList()
    }

    private fun getNearestEntityInRange(): Entity? {
        val entitiesInRange = getAllEntities().filter {
            val distance = mc.thePlayer.getDistanceToEntityBox2(it)
            (distance <= rangeValue.get())
        }
        return entitiesInRange.minBy { mc.thePlayer.getDistanceToEntityBox2(it) }
    }
}