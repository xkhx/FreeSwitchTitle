package top.zoyn.freeswitchtitle.util

import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import top.zoyn.freeswitchtitle.FreeSwitchTitle
import top.zoyn.freeswitchtitle.data.TitleAttributeEffect
import top.zoyn.freeswitchtitle.data.TitleData
import top.zoyn.freeswitchtitle.data.TitlePotionEffect
import java.nio.charset.StandardCharsets
import java.util.Locale
import java.util.UUID

object TitleBuffManager {

    private const val POTION_DURATION_TICKS = 20 * 60 * 60
    private val appliedPotions = mutableMapOf<UUID, Map<PotionEffectType, PotionEffect?>>()
    private val appliedAttributes = mutableMapOf<UUID, Set<Attribute>>()

    fun apply(player: Player, title: TitleData) {
        clear(player)
        if (!title.buffEffect.enabled) return
        val potionSnapshots = title.buffEffect.potions.mapNotNull { applyPotion(player, title, it) }.toMap()
        val attributeTypes = title.buffEffect.attributes.mapNotNull { applyAttribute(player, title, it) }.toSet()
        if (potionSnapshots.isNotEmpty()) {
            appliedPotions[player.uniqueId] = potionSnapshots
        }
        if (attributeTypes.isNotEmpty()) {
            appliedAttributes[player.uniqueId] = attributeTypes
        }
    }

    fun clear(player: Player) {
        appliedPotions.remove(player.uniqueId)?.forEach { (type, previous) ->
            player.removePotionEffect(type)
            if (previous != null) {
                player.addPotionEffect(previous, true)
            }
        }
        appliedAttributes.remove(player.uniqueId)?.forEach { attribute ->
            player.getAttribute(attribute)?.modifiers
                ?.filter { it.uniqueId == modifierId(player.uniqueId, attribute.name) }
                ?.forEach { player.getAttribute(attribute)?.removeModifier(it) }
        }
    }

    fun clearAll() {
        val players = org.bukkit.Bukkit.getOnlinePlayers().toList()
        players.forEach { clear(it) }
        appliedPotions.clear()
        appliedAttributes.clear()
    }

    private fun applyPotion(player: Player, title: TitleData, effect: TitlePotionEffect): Pair<PotionEffectType, PotionEffect?>? {
        val type = parsePotion(effect.type) ?: run {
            FreeSwitchTitle.sendConsoleMessage("§e[FreeSwitchTitle] 称号 ${title.uid} 的药水效果无效: ${effect.type}")
            return null
        }
        val previous = player.getPotionEffect(type)
        player.addPotionEffect(PotionEffect(type, POTION_DURATION_TICKS, effect.amplifier, effect.ambient, effect.particles, effect.icon), true)
        return type to previous
    }

    private fun applyAttribute(player: Player, title: TitleData, effect: TitleAttributeEffect): Attribute? {
        val attribute = parseAttribute(effect.attribute) ?: run {
            FreeSwitchTitle.sendConsoleMessage("§e[FreeSwitchTitle] 称号 ${title.uid} 的属性效果无效: ${effect.attribute}")
            return null
        }
        val instance = player.getAttribute(attribute) ?: return null
        val id = modifierId(player.uniqueId, attribute.name)
        instance.modifiers.filter { it.uniqueId == id }.forEach { instance.removeModifier(it) }
        val modifier = AttributeModifier(id, "FreeSwitchTitle:${title.uid}:${attribute.name}", effect.amount, effect.operation.bukkitOperation)
        instance.addModifier(modifier)
        return attribute
    }

    private fun parsePotion(name: String): PotionEffectType? {
        return PotionEffectType.getByName(name.trim().uppercase(Locale.getDefault()))
    }

    private fun parseAttribute(name: String): Attribute? {
        return runCatching { Attribute.valueOf(name.trim().uppercase(Locale.getDefault())) }.getOrNull()
    }

    private fun modifierId(uuid: UUID, key: String): UUID {
        return UUID.nameUUIDFromBytes("FreeSwitchTitle:$uuid:$key".toByteArray(StandardCharsets.UTF_8))
    }
}
