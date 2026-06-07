package top.zoyn.freeswitchtitle.hook.economy

import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import taboolib.platform.util.bukkitPlugin
import java.util.UUID

object PlayerPointsEconomy {

    private val api: Any?
        get() = runCatching {
            val plugin = bukkitPlugin.server.pluginManager.getPlugin("PlayerPoints") ?: return@runCatching null
            plugin.javaClass.getMethod("getAPI").invoke(plugin)
        }.getOrNull()

    fun isAvailable(): Boolean = api != null

    fun getBalance(player: Player): Int {
        return getBalance(player as OfflinePlayer)
    }

    fun getBalance(player: OfflinePlayer): Int {
        return invokeInt("look", player.uniqueId) ?: 0
    }

    fun has(player: Player, amount: Int): Boolean {
        if (amount <= 0) return true
        return getBalance(player) >= amount
    }

    fun withdraw(player: Player, amount: Int): Boolean {
        if (amount <= 0) return true
        return invokeBoolean("take", player.uniqueId, amount)
    }

    fun deposit(player: Player, amount: Int): Boolean {
        if (amount <= 0) return true
        return invokeBoolean("give", player.uniqueId, amount)
    }

    private fun invokeInt(method: String, uuid: UUID): Int? {
        val api = api ?: return null
        return runCatching {
            api.javaClass.getMethod(method, UUID::class.java).invoke(api, uuid) as? Int
        }.getOrNull()
    }

    private fun invokeBoolean(method: String, uuid: UUID, amount: Int): Boolean {
        val api = api ?: return false
        return runCatching {
            api.javaClass.getMethod(method, UUID::class.java, Int::class.javaPrimitiveType).invoke(api, uuid, amount) as? Boolean ?: false
        }.getOrDefault(false)
    }
}
