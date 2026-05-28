package top.zoyn.freeswitchtitle.hook.economy

import org.bukkit.entity.Player
import taboolib.platform.util.sendLang
import top.zoyn.freeswitchtitle.api.FreeSwitchTitleAPI
import top.zoyn.freeswitchtitle.data.TitleData
import top.zoyn.freeswitchtitle.util.ConfigUtils
import top.zoyn.freeswitchtitle.util.TitleEffectUtils
import top.zoyn.freeswitchtitle.util.TitleUtils

object EconomyManager {

    fun purchase(player: Player, uid: String): PurchaseResult {
        if (!ConfigUtils.shopEnable) return PurchaseResult.SHOP_DISABLED
        val title = FreeSwitchTitleAPI.getTitle(uid) ?: return PurchaseResult.TITLE_NOT_FOUND
        if (!title.shopEnable) return PurchaseResult.TITLE_NOT_IN_SHOP
        if (TitleUtils.hasTitle(player.uniqueId, uid)) return PurchaseResult.ALREADY_OWNED
        if (title.shopPermission.isNotBlank() && !player.hasPermission(title.shopPermission)) return PurchaseResult.NO_PERMISSION

        val check = checkBalance(player, title)
        if (check != PurchaseResult.SUCCESS) return check

        val withdraw = withdraw(player, title)
        if (withdraw != PurchaseResult.SUCCESS) return withdraw

        if (!TitleUtils.addTitle(player.uniqueId, uid)) {
            rollback(player, title)
            return PurchaseResult.WITHDRAW_FAILED
        }
        TitleEffectUtils.runBuy(player, title)
        return PurchaseResult.SUCCESS
    }

    private fun checkBalance(player: Player, title: TitleData): PurchaseResult {
        return when (ConfigUtils.shopCurrency) {
            CurrencyType.VAULT -> checkVault(player, title)
            CurrencyType.PLAYER_POINTS -> checkPoints(player, title)
            CurrencyType.BOTH -> {
                val vault = checkVault(player, title)
                if (vault != PurchaseResult.SUCCESS) vault else checkPoints(player, title)
            }
        }
    }

    private fun checkVault(player: Player, title: TitleData): PurchaseResult {
        if (title.vaultPrice <= 0.0) return PurchaseResult.SUCCESS
        if (!VaultEconomy.isAvailable()) return PurchaseResult.VAULT_NOT_AVAILABLE
        if (!VaultEconomy.has(player, title.vaultPrice)) return PurchaseResult.NOT_ENOUGH_MONEY
        return PurchaseResult.SUCCESS
    }

    private fun checkPoints(player: Player, title: TitleData): PurchaseResult {
        if (title.pointsPrice <= 0) return PurchaseResult.SUCCESS
        if (!PlayerPointsEconomy.isAvailable()) return PurchaseResult.PLAYER_POINTS_NOT_AVAILABLE
        if (!PlayerPointsEconomy.has(player, title.pointsPrice)) return PurchaseResult.NOT_ENOUGH_POINTS
        return PurchaseResult.SUCCESS
    }

    private fun withdraw(player: Player, title: TitleData): PurchaseResult {
        return when (ConfigUtils.shopCurrency) {
            CurrencyType.VAULT -> withdrawVault(player, title)
            CurrencyType.PLAYER_POINTS -> withdrawPoints(player, title)
            CurrencyType.BOTH -> {
                val vault = withdrawVault(player, title)
                if (vault != PurchaseResult.SUCCESS) return vault
                val points = withdrawPoints(player, title)
                if (points != PurchaseResult.SUCCESS) {
                    VaultEconomy.deposit(player, title.vaultPrice)
                    return points
                }
                PurchaseResult.SUCCESS
            }
        }
    }

    private fun withdrawVault(player: Player, title: TitleData): PurchaseResult {
        if (title.vaultPrice <= 0.0) return PurchaseResult.SUCCESS
        return if (VaultEconomy.withdraw(player, title.vaultPrice)) PurchaseResult.SUCCESS else PurchaseResult.WITHDRAW_FAILED
    }

    private fun withdrawPoints(player: Player, title: TitleData): PurchaseResult {
        if (title.pointsPrice <= 0) return PurchaseResult.SUCCESS
        return if (PlayerPointsEconomy.withdraw(player, title.pointsPrice)) PurchaseResult.SUCCESS else PurchaseResult.WITHDRAW_FAILED
    }

    private fun rollback(player: Player, title: TitleData) {
        when (ConfigUtils.shopCurrency) {
            CurrencyType.VAULT -> VaultEconomy.deposit(player, title.vaultPrice)
            CurrencyType.PLAYER_POINTS -> PlayerPointsEconomy.deposit(player, title.pointsPrice)
            CurrencyType.BOTH -> {
                VaultEconomy.deposit(player, title.vaultPrice)
                PlayerPointsEconomy.deposit(player, title.pointsPrice)
            }
        }
    }

    fun sendResult(player: Player, result: PurchaseResult, title: TitleData? = null) {
        when (result) {
            PurchaseResult.SUCCESS -> player.sendLang("purchase-success", title?.title ?: "")
            PurchaseResult.TITLE_NOT_FOUND -> player.sendLang("purchase-title-not-found")
            PurchaseResult.SHOP_DISABLED -> player.sendLang("purchase-shop-disabled")
            PurchaseResult.TITLE_NOT_IN_SHOP -> player.sendLang("purchase-not-in-shop")
            PurchaseResult.ALREADY_OWNED -> player.sendLang("purchase-already-owned")
            PurchaseResult.NO_PERMISSION -> player.sendLang("purchase-no-permission")
            PurchaseResult.VAULT_NOT_AVAILABLE -> player.sendLang("purchase-vault-not-available")
            PurchaseResult.PLAYER_POINTS_NOT_AVAILABLE -> player.sendLang("purchase-player-points-not-available")
            PurchaseResult.NOT_ENOUGH_MONEY -> player.sendLang("purchase-not-enough-money")
            PurchaseResult.NOT_ENOUGH_POINTS -> player.sendLang("purchase-not-enough-points")
            PurchaseResult.WITHDRAW_FAILED -> player.sendLang("purchase-withdraw-failed")
        }
    }
}
