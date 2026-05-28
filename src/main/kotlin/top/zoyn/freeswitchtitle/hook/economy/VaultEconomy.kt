package top.zoyn.freeswitchtitle.hook.economy

import org.bukkit.entity.Player
import taboolib.platform.compat.VaultService

object VaultEconomy {

    fun isAvailable(): Boolean = VaultService.economy != null

    fun getBalance(player: Player): Double {
        return VaultService.economy?.getBalance(player) ?: 0.0
    }

    fun has(player: Player, amount: Double): Boolean {
        if (amount <= 0.0) return true
        return VaultService.economy?.has(player, amount) ?: false
    }

    fun withdraw(player: Player, amount: Double): Boolean {
        if (amount <= 0.0) return true
        return VaultService.economy?.withdrawPlayer(player, amount)?.transactionSuccess() ?: false
    }

    fun deposit(player: Player, amount: Double): Boolean {
        if (amount <= 0.0) return true
        return VaultService.economy?.depositPlayer(player, amount)?.transactionSuccess() ?: false
    }
}
