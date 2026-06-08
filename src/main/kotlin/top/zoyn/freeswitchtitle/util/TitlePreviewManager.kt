package top.zoyn.freeswitchtitle.util

import org.bukkit.entity.Player
import taboolib.platform.util.sendLang
import top.zoyn.freeswitchtitle.data.TitleData
import java.util.UUID

object TitlePreviewManager {

    private val cooldowns = mutableMapOf<UUID, Long>()

    fun preview(player: Player, title: TitleData): Boolean {
        if (!ConfigUtils.previewEnable) {
            player.sendLang("preview-disabled")
            return false
        }
        if (!title.particleEffect.enabled) {
            player.sendLang("preview-no-effect", title.title)
            return false
        }
        if (!checkCooldown(player)) return false
        cooldowns[player.uniqueId] = System.currentTimeMillis() + ConfigUtils.previewCooldownMillis
        val durationMillis = ConfigUtils.previewDurationMillis
        val started = TitleParticleManager.startPreview(player, title, (durationMillis / 50L).coerceAtLeast(1L)) {
            if (player.isOnline) {
                player.sendLang("preview-end")
            }
        }
        if (!started) {
            cooldowns.remove(player.uniqueId)
            player.sendLang("preview-no-effect", title.title)
            return false
        }
        player.sendLang("preview-start", title.title, TitleDurationUtils.format(durationMillis))
        return true
    }

    fun previewDisplay(player: Player, title: TitleData): Boolean {
        if (!ConfigUtils.previewEnable) {
            player.sendLang("preview-disabled")
            return false
        }
        if (!title.displayEffect.enabled || title.displayEffect.text.isBlank()) {
            player.sendLang("preview-display-no-effect", title.title)
            return false
        }
        if (!checkCooldown(player)) return false
        cooldowns[player.uniqueId] = System.currentTimeMillis() + ConfigUtils.previewCooldownMillis
        val durationMillis = ConfigUtils.previewDurationMillis
        val started = TitleDisplayManager.startPreview(player, title, (durationMillis / 50L).coerceAtLeast(1L)) {
            if (player.isOnline) {
                player.sendLang("preview-display-end")
            }
        }
        if (!started) {
            cooldowns.remove(player.uniqueId)
            player.sendLang("preview-display-no-effect", title.title)
            return false
        }
        player.sendLang("preview-display-start", title.title, TitleDurationUtils.format(durationMillis))
        player.sendLang("preview-display-resource-pack-tip")
        return true
    }

    private fun checkCooldown(player: Player): Boolean {
        val now = System.currentTimeMillis()
        val availableAt = cooldowns[player.uniqueId] ?: 0L
        if (availableAt > now) {
            player.sendLang("preview-cooling", TitleDurationUtils.format(availableAt - now))
            return false
        }
        return true
    }

    fun stop(player: Player) {
        TitleParticleManager.stopPreview(player)
        TitleDisplayManager.stopPreview(player)
    }

    fun stopAll() {
        TitleParticleManager.stopAllPreview()
        TitleDisplayManager.stopAllPreview()
        cooldowns.clear()
    }
}
