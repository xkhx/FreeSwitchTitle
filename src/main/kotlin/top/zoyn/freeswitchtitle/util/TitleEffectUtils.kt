package top.zoyn.freeswitchtitle.util

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import taboolib.module.chat.colored
import top.zoyn.freeswitchtitle.data.TitleData

object TitleEffectUtils {

    private val actionRegex = Regex("^\\s*\\[([^]]+)]\\s*(.*)$")

    fun runEquip(player: Player, title: TitleData) {
        runActions(player, title, title.equipActions)
    }

    fun runUnequip(player: Player, title: TitleData) {
        runActions(player, title, title.unequipActions)
    }

    fun runBuy(player: Player, title: TitleData) {
        runActions(player, title, title.buyActions)
    }

    private fun runActions(player: Player, title: TitleData, actions: List<String>) {
        actions.forEach { raw ->
            if (raw.isBlank()) return@forEach
            val match = actionRegex.matchEntire(raw)
            val type = match?.groupValues?.get(1)?.lowercase() ?: "console"
            val content = match?.groupValues?.get(2) ?: raw
            if (content.isBlank()) return@forEach
            execute(player, type, replace(player, title, content))
        }
    }

    private fun execute(player: Player, type: String, content: String) {
        when (type) {
            "console" -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), normalizeCommand(content))
            "player" -> player.performCommand(normalizeCommand(content))
            "op" -> runAsOp(player, normalizeCommand(content))
            "message" -> player.sendMessage(content.colored())
            else -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), normalizeCommand(content))
        }
    }

    private fun normalizeCommand(content: String): String {
        return content.trimStart().removePrefix("/")
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
        return text
            .replace("{player}", player.name)
            .replace("{uuid}", player.uniqueId.toString())
            .replace("{title}", title.title)
            .replace("{uid}", title.uid)
    }
}
