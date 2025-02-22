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
package kevin.utils

import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.Vec3
import kotlin.math.cos
import kotlin.math.sin

object RaycastUtils : MinecraftInstance() {

    @JvmStatic
    fun raycastEntity(range: Double, entityFilter: EntityFilter) = raycastEntity(range, RotationUtils.bestServerRotation().yaw, RotationUtils.bestServerRotation().pitch, entityFilter)


    fun raycastEntityYaw(
        range: Double,
        yaw: Float,
        pitch: Float,
        entityFilter: EntityFilter
    ): Entity? {
        return raycastEntity(
            range,
            RotationUtils.serverRotation.yaw,
            RotationUtils.serverRotation.pitch,
            entityFilter
        )
    }
    private fun raycastEntity(range: Double, yaw: Float, pitch: Float, entityFilter: EntityFilter): Entity? {
        val renderViewEntity = mc.renderViewEntity

        if (renderViewEntity != null && mc.theWorld != null) {
            var blockReachDistance = range
            val eyePosition = renderViewEntity.getPositionEyes(1f)

            val yawCos = cos(-yaw * 0.017453292f - Math.PI.toFloat())
            val yawSin = sin(-yaw * 0.017453292f - Math.PI.toFloat())
            val pitchCos = (-cos(-pitch * 0.017453292f.toDouble())).toFloat()
            val pitchSin = sin(-pitch * 0.017453292f.toDouble()).toFloat()

            val entityLook = Vec3((yawSin * pitchCos).toDouble(), pitchSin.toDouble(), (yawCos * pitchCos).toDouble())
            val vector = eyePosition.addVector(entityLook.xCoord * blockReachDistance, entityLook.yCoord * blockReachDistance, entityLook.zCoord * blockReachDistance)
            val entityList = mc.theWorld!!.getEntitiesInAABBexcluding(renderViewEntity, renderViewEntity.entityBoundingBox.addCoord(entityLook.xCoord * blockReachDistance, entityLook.yCoord * blockReachDistance, entityLook.zCoord * blockReachDistance).expand(1.0, 1.0, 1.0)) {
                it != null && ((it) !is EntityPlayer || !it.isSpectator) && it.canBeCollidedWith()
            }

            var pointedEntity: Entity? = null

            for (entity in entityList) {
                if (!entityFilter.canRaycast(entity))
                    continue

                val collisionBorderSize = entity.collisionBorderSize.toDouble()
                val axisAlignedBB = entity.entityBoundingBox.expand(collisionBorderSize, collisionBorderSize, collisionBorderSize)

                val movingObjectPosition = axisAlignedBB.calculateIntercept(eyePosition, vector)

                if (axisAlignedBB.isVecInside(eyePosition)) {
                    if (blockReachDistance >= 0.0) {
                        pointedEntity = entity
                        blockReachDistance = 0.0
                    }
                } else if (movingObjectPosition != null) {
                    val eyeDistance = eyePosition.distanceTo(movingObjectPosition.hitVec)

                    if (eyeDistance < blockReachDistance || blockReachDistance == 0.0) {
                        if (entity == renderViewEntity.ridingEntity) {
                            if (blockReachDistance == 0.0)
                                pointedEntity = entity
                        } else {
                            pointedEntity = entity
                            blockReachDistance = eyeDistance
                        }
                    }
                }
            }

            return pointedEntity
        }

        return null
    }

    interface EntityFilter {
        fun canRaycast(entity: Entity?): Boolean
    }
}