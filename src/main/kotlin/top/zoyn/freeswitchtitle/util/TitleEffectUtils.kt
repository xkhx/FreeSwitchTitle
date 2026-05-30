package top.zoyn.freeswitchtitle.util

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import top.zoyn.freeswitchtitle.data.TitleData

object TitleEffectUtils {

    fun runEquip(player: Player, title: TitleData) {
        runCommands(player, title, title.equipCommands)
    }

    fun runUnequip(player: Player, title: TitleData) {
        runCommands(player, title, title.unequipCommands)
    }

    fun runBuy(player: Player, title: TitleData) {
        runCommands(player, title, title.buyCommands)
    }

    private fun runCommands(player: Player, title: TitleData, commands: List<String>) {
        commands.forEach { raw ->
            if (raw.isBlank()) return@forEach
            val command = raw
                .replace("{player}", player.name)
                .replace("{uuid}", player.uniqueId.toString())
                .replace("{title}", title.title)
                .replace("{uid}", title.uid)
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command)
        }
    }
}
