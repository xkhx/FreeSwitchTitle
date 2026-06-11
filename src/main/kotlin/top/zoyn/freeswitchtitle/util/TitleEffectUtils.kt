package top.zoyn.freeswitchtitle.util

import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.entity.Player
import taboolib.module.chat.colored
import taboolib.platform.util.bukkitPlugin
import top.zoyn.freeswitchtitle.data.TitleData
import top.zoyn.freeswitchtitle.hook.economy.CurrencyType
import java.util.Locale

object TitleEffectUtils {

    private val actionRegex = Regex("^\\s*\\[([^]]+)]\\s*(.*)$")
    private val delayRegex = Regex("^(\\d+)(ms|t|s|m)?$", RegexOption.IGNORE_CASE)

    fun runEquip(player: Player, title: TitleData) {
        runActions(player, title, title.equipActions)
    }

    fun runUnequip(player: Player, title: TitleData) {
        runActions(player, title, title.unequipActions)
    }

    fun runBuy(player: Player, title: TitleData) {
        runActions(player, title, title.buyActions)
    }

    fun runExpire(player: Player, title: TitleData) {
        runActions(player, title, title.expireActions)
    }

    fun runObtain(player: Player, title: TitleData) {
        runActions(player, title, title.obtainActions)
    }

    fun runRemove(player: Player, title: TitleData) {
        runActions(player, title, title.removeActions)
    }

    fun runReset(player: Player, title: TitleData) {
        runActions(player, title, title.resetActions)
    }

    private fun runActions(player: Player, title: TitleData, actions: List<String>) {
        runActionChain(player, title, actions, 0)
    }

    private fun runActionChain(player: Player, title: TitleData, actions: List<String>, startIndex: Int) {
        if (!player.isOnline) return
        var index = startIndex
        while (index < actions.size) {
            val raw = actions[index]
            index++
            if (raw.isBlank()) continue
            val match = actionRegex.matchEntire(raw)
            val type = match?.groupValues?.get(1)?.lowercase(Locale.ROOT) ?: "console"
            val content = match?.groupValues?.get(2) ?: raw
            if (content.isBlank()) continue
            if (type == "delay") {
                scheduleDelay(player, title, actions, index, parseDelayTicks(content))
                return
            }
            execute(player, type, replace(player, title, content))
        }
    }

    private fun scheduleDelay(player: Player, title: TitleData, actions: List<String>, nextIndex: Int, delayTicks: Long) {
        if (delayTicks <= 0L) {
            runActionChain(player, title, actions, nextIndex)
            return
        }
        bukkitPlugin.server.scheduler.runTaskLater(
            bukkitPlugin,
            Runnable { runActionChain(player, title, actions, nextIndex) },
            delayTicks
        )
    }

    private fun parseDelayTicks(content: String): Long {
        val raw = content.trim().lowercase(Locale.ROOT)
        val match = delayRegex.matchEntire(raw) ?: return 0L
        val amount = match.groupValues[1].toLongOrNull() ?: return 0L
        return when (match.groupValues[2].ifBlank { "t" }) {
            "ms" -> maxOf(1L, (amount + 49L) / 50L)
            "t" -> amount
            "s" -> amount * 20L
            "m" -> amount * 20L * 60L
            else -> 0L
        }
    }

    private fun execute(player: Player, type: String, content: String) {
        when (type) {
            "console" -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), normalizeCommand(content))
            "player" -> player.performCommand(normalizeCommand(content))
            "op" -> runAsOp(player, normalizeCommand(content))
            "message" -> player.sendMessage(content.colored())
            "broadcast" -> broadcast(content)
            "actionbar" -> sendActionBar(player, content)
            "title" -> sendTitle(player, content)
            "sound" -> playSound(player, content)
            else -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), normalizeCommand(content))
        }
    }

    private fun normalizeCommand(content: String): String {
        return content.trimStart().removePrefix("/")
    }

    private fun broadcast(content: String) {
        Bukkit.broadcastMessage(content.colored())
    }

    private fun sendActionBar(player: Player, content: String) {
        runCatching {
            val chatMessageType = Class.forName("net.md_5.bungee.api.ChatMessageType")
            val actionBar = chatMessageType.getField("ACTION_BAR").get(null)
            val baseComponent = Class.forName("net.md_5.bungee.api.chat.BaseComponent")
            val textComponent = Class.forName("net.md_5.bungee.api.chat.TextComponent")
                .getConstructor(String::class.java)
                .newInstance(content.colored())
            val components = java.lang.reflect.Array.newInstance(baseComponent, 1)
            java.lang.reflect.Array.set(components, 0, textComponent)
            val spigot = player.javaClass.getMethod("spigot").invoke(player)
            spigot.javaClass.getMethod("sendMessage", chatMessageType, components.javaClass)
                .invoke(spigot, actionBar, components)
        }.getOrElse {
            player.sendMessage(content.colored())
        }
    }

    private fun sendTitle(player: Player, content: String) {
        val parts = content.split(";", limit = 2)
        val title = parts.getOrNull(0).orEmpty().colored()
        val subtitle = parts.getOrNull(1).orEmpty().colored()
        player.sendTitle(title, subtitle, 10, 40, 10)
    }

    private fun playSound(player: Player, content: String) {
        val parts = content.split(";")
        val soundName = parts.getOrNull(0)?.trim()?.uppercase().orEmpty()
        if (soundName.isBlank()) return
        val volume = parts.getOrNull(1)?.trim()?.toFloatOrNull() ?: 1.0f
        val pitch = parts.getOrNull(2)?.trim()?.toFloatOrNull() ?: 1.0f
        runCatching {
            val sound = Sound.valueOf(soundName)
            player.playSound(player.location, sound, volume, pitch)
        }
    }

    private fun runAsOp(player: Player, content: String) {
        val wasOp = player.isOp
        try {
            if (!wasOp) {
                player.isOp = true
            }
            player.performCommand(content)
        } finally {
            if (!wasOp) {
                player.isOp = false
            }
        }
    }

    private fun replace(player: Player, title: TitleData, text: String): String {
        val uuid = player.uniqueId.toString()
        val price = formatPrice(title)
        val expire = TitleUtils.getTitleExpireText(player.uniqueId, title)
        return text
            .replace("{player}", player.name)
            .replace("{uuid}", uuid)
            .replace("{title}", title.title)
            .replace("{uid}", title.uid)
            .replace("{title_id}", title.uid)
            .replace("{duration}", title.durationText)
            .replace("{expire}", expire)
            .replace("{price}", price)
            .replace("%player%", player.name)
            .replace("%uuid%", uuid)
            .replace("%title%", title.title)
            .replace("%title_id%", title.uid)
            .replace("%duration%", title.durationText)
            .replace("%expire%", expire)
            .replace("%price%", price)
    }

    private fun formatPrice(title: TitleData): String {
        return when (title.shopCurrency) {
            CurrencyType.VAULT -> "金币 ${title.vaultPrice}"
            CurrencyType.PLAYER_POINTS -> "点券 ${title.pointsPrice}"
            CurrencyType.BOTH -> "金币 ${title.vaultPrice} + 点券 ${title.pointsPrice}"
            CurrencyType.FREE -> "免费"
        }
    }
}
