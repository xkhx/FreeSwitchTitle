package top.zoyn.freeswitchtitle.listener

import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.util.replaceWithOrder
import taboolib.expansion.releaseDataContainer
import taboolib.expansion.setupDataContainer
import taboolib.module.chat.colored
import taboolib.platform.compat.replacePlaceholder
import top.zoyn.freeswitchtitle.util.ConfigUtils
import top.zoyn.freeswitchtitle.util.TitleBuffManager
import top.zoyn.freeswitchtitle.util.TitleDisplayManager
import top.zoyn.freeswitchtitle.util.TitleParticleManager
import top.zoyn.freeswitchtitle.util.TitlePreviewManager
import top.zoyn.freeswitchtitle.util.TitleUtils
import top.zoyn.freeswitchtitle.util.getCurrentTitle

object PlayerListener {

    @SubscribeEvent(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun chat(event: AsyncPlayerChatEvent) {
        if (!ConfigUtils.enableChat) return
        val player = event.player
        event.format = ConfigUtils.format
            .replace("{DISPLAY_NAME}", player.displayName)
            .replace("{MESSAGE}", event.message)
            .replacePlaceholder(player)
            .colored()
    }

    @SubscribeEvent
    fun join(event: PlayerJoinEvent) {
        val player = event.player
        player.setupDataContainer()
        TitleUtils.migratePlayerData(player.uniqueId)
        TitleUtils.cleanupExpiredTitles(player.uniqueId)
        TitleUtils.checkCollectionRewards(player.uniqueId)
        player.getCurrentTitle()?.let { title ->
            TitleBuffManager.apply(player, title)
            TitleParticleManager.start(player, title)
            TitleDisplayManager.start(player, title)
            if (title.joinMessage.isNotEmpty()) {
                event.joinMessage = title.joinMessage.replaceWithOrder(player.name)
            }
        }
    }

    @SubscribeEvent
    fun quit(event: PlayerQuitEvent) {
        TitlePreviewManager.stop(event.player)
        TitleDisplayManager.stop(event.player)
        TitleBuffManager.clear(event.player)
        TitleParticleManager.stop(event.player)
        event.player.releaseDataContainer()
    }
}
