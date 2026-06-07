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
        val now = System.currentTimeMillis()
        val availableAt = cooldowns[player.uniqueId] ?: 0L
        if (availableAt > now) {
            player.sendLang("preview-cooling", TitleDurationUtils.format(availableAt - now))
            return false
        }
        cooldowns[player.uniqueId] = now + ConfigUtils.previewCooldownMillis
        val durationMillis = ConfigUtils.previewDurationMillis
        TitleParticleManager.startPreview(player, title, (durationMillis / 50L).coerceAtLeast(1L)) {
            if (player.isOnline) {
                player.sendLang("preview-end")
            }
        }
        player.sendLang("preview-start", title.title, TitleDurationUtils.format(durationMillis))
        return true
    }

    fun stop(player: Player) {
        TitleParticleManager.stopPreview(player)
    }

    fun stopAll() {
        TitleParticleManager.stopAll()
        cooldowns.clear()
    }
}
