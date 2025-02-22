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

import kevin.event.EventTarget
import kevin.event.Listenable
import kevin.event.WorldEvent
import net.minecraft.network.Packet
import net.minecraft.network.play.server.S12PacketEntityVelocity
import kotlin.math.roundToInt

object PacketUtils : MinecraftInstance(), Listenable {
    val packetList = arrayListOf<Packet<*>>()

    fun sendPacketNoEvent(packet: Packet<*>){
        mc.netHandler.networkManager.sendPacketNoEvent(packet)
    }

    fun sendPacket(packet: Packet<*>) {
        mc.netHandler.addToSendQueue(packet)
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {}

    override fun handleEvents() = true

    var S12PacketEntityVelocity.realMotionY
        get() = motionY / 8000.0
        set(value) {
            motionX = (value * 8000.0).roundToInt()
        }
}