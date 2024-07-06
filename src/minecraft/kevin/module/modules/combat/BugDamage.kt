package kevin.module.modules.combat

import kevin.event.*
import kevin.module.Module
import kevin.module.ModuleCategory
import kevin.utils.ItemUtils
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.enchantment.Enchantment
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemSword
import net.minecraft.network.play.client.C0EPacketClickWindow

class BugDamage : Module("BugDamage", "do Minecraft bug", category = ModuleCategory.COMBAT) {

    private var switching = false
    private var firstSwordIndex = -1
    private var secondSwordIndex = -1
    private var needSearch = false
    @EventTarget
    fun onAttack(event: AttackEvent) {
            if (switching) return
            if (mc.thePlayer.heldItem == null) return
        //如果打的是第一刀，那么剑一定是第二好的，然后切换来切换去，造成叠伤的效果
            if (mc.thePlayer.heldItem.item is ItemSword && (mc.currentScreen) !is GuiContainer) {
                if (firstSwordIndex != -1 && secondSwordIndex != -1 && firstSwordIndex != null && secondSwordIndex != null && !ItemUtils.isStackEmpty(
                        mc.thePlayer?.inventoryContainer?.getSlot(firstSwordIndex)?.stack
                    ) && !ItemUtils.isStackEmpty(mc.thePlayer?.inventoryContainer?.getSlot(secondSwordIndex)?.stack)
                ) {
                    if ((event.targetEntity as EntityLivingBase).hurtTime == 0) {
                            switching = true
                            if (mc.thePlayer!!.inventory.currentItem == 0) {
                                if (secondSwordIndex != 36) {
                                    switchslot(0, secondSwordIndex)
                                    secondSwordIndex = 0
                                }
                            } else {
                                if ((mc.thePlayer.inventory.currentItem + 36) != secondSwordIndex) {
                                    switchslot(secondSwordIndex, mc.thePlayer!!.inventory.currentItem + 36)
                                    secondSwordIndex = mc.thePlayer!!.inventory.currentItem + 36
                                }
                            }
                            switching = false
                    } else {
                        switching = true
                        if (mc.thePlayer!!.inventory.currentItem == 0) {
                            if (firstSwordIndex == 36) {
                                switchslot(firstSwordIndex,36)
                                secondSwordIndex = 36
                            }
                            if (secondSwordIndex == 36) {
                                switchslot(36,firstSwordIndex)
                                firstSwordIndex = 36
                            }
                        } else {
                            if ((mc.thePlayer.inventory.currentItem + 36) == firstSwordIndex) {
                                switchslot(secondSwordIndex, firstSwordIndex)
                                val temp = secondSwordIndex
                                secondSwordIndex = firstSwordIndex
                                firstSwordIndex = temp
                            }
                            if ((mc.thePlayer.inventory.currentItem + 36) == secondSwordIndex) {
                                switchslot(firstSwordIndex, secondSwordIndex)
                                val temp = firstSwordIndex
                                firstSwordIndex = secondSwordIndex
                                secondSwordIndex = temp
                            }
                        }
                        switching = false
                    }
                }
            }
    }
    //如果打开物品栏或者点击物品栏或者其他七七八八的，那么就搜索一次最好的剑和第二号的剑
    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (!switching && (mc.currentScreen is GuiContainer || needSearch)) {
            findNeedSword()
        }
    }
    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is C0EPacketClickWindow) needSearch = true
    }
    @EventTarget
    fun onClick(event: ClickWindowEvent) {
        needSearch = true
    }
    @EventTarget
    fun onWorld(event: WorldEvent) {
        needSearch = true
    }
    private fun items(start : Int , end : Int): Map<Int, ItemStack> {
        val items = mutableMapOf<Int, ItemStack>()

        for (i in end - 1 downTo start) {
            val itemStack = mc.thePlayer?.inventoryContainer?.getSlot(i)?.stack ?: continue

            if (ItemUtils.isStackEmpty(itemStack))
                continue

            items[i] = itemStack
        }

        return items
    }

    private fun switchslot(startslot: Int, endslot: Int) {
        mc.playerController.windowClick(0, startslot, 0, 2, mc.thePlayer)
        mc.playerController.windowClick(0, endslot, 0, 2, mc.thePlayer)
        mc.playerController.windowClick(0, startslot, 0, 2, mc.thePlayer)
        mc.playerController.updateController()
    }

    private fun findNeedSword() {
        val items = items(9, 45)
            .filter {
                it.value.item is ItemSword
            }.toMutableMap()
        firstSwordIndex = items.maxByOrNull {
            it.value.attributeModifiers["generic.attackDamage"].first().amount + 1.25 * ItemUtils.getEnchantment(
                it.value,
                Enchantment.sharpness
            )
        }!!.key
        secondSwordIndex = items.filter {    it.value.attributeModifiers["generic.attackDamage"].first().amount + 1.25 * ItemUtils.getEnchantment(
            it.value,
            Enchantment.sharpness
        )!=    mc.thePlayer?.inventoryContainer?.getSlot(firstSwordIndex)!!.stack.attributeModifiers["generic.attackDamage"].first().amount + 1.25 * ItemUtils.getEnchantment(
            mc.thePlayer?.inventoryContainer?.getSlot(firstSwordIndex)?.stack,
            Enchantment.sharpness
        ) }.maxByOrNull {
            it.value.attributeModifiers["generic.attackDamage"].first().amount + 1.25 * ItemUtils.getEnchantment(
                it.value,
                Enchantment.sharpness
            )
        }!!.key
    }
    override val tag: String
        get() = "ClickWindow"
}
