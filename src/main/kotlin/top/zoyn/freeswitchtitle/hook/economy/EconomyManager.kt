package top.zoyn.freeswitchtitle.hook.economy

import org.bukkit.entity.Player
import taboolib.platform.util.sendLang
import top.zoyn.freeswitchtitle.api.FreeSwitchTitleAPI
import taboolib.platform.util.bukkitPlugin
import top.zoyn.freeswitchtitle.data.TitleData
import top.zoyn.freeswitchtitle.event.TitleBuyEvent
import top.zoyn.freeswitchtitle.util.ConfigUtils
import top.zoyn.freeswitchtitle.util.TitleEffectUtils
import top.zoyn.freeswitchtitle.util.TitleUtils

object EconomyManager {

    fun purchase(player: Player, uid: String, source: PurchaseSource = PurchaseSource.COMMAND): PurchaseResult {
        if (!ConfigUtils.shopEnable) return finish(player, uid, null, PurchaseResult.SHOP_DISABLED, source)
        val title = FreeSwitchTitleAPI.getTitle(uid) ?: return finish(player, uid, null, PurchaseResult.TITLE_NOT_FOUND, source)
        if (!title.shopEnable) return finish(player, uid, title, PurchaseResult.TITLE_NOT_IN_SHOP, source)
        if (!title.isShopAvailableNow()) return finish(player, uid, title, PurchaseResult.TITLE_NOT_AVAILABLE_TIME, source)
        if (TitleUtils.hasTitle(player.uniqueId, uid)) return finish(player, uid, title, PurchaseResult.ALREADY_OWNED, source)
        if (title.shopPermission.isNotBlank() && !player.hasPermission(title.shopPermission)) return finish(player, uid, title, PurchaseResult.NO_PERMISSION, source)
        if (title.requiredPermissions.any { !player.hasPermission(it) }) return finish(player, uid, title, PurchaseResult.REQUIREMENT_NOT_MET, source)

        val check = checkBalance(player, title)
        if (check != PurchaseResult.SUCCESS) return finish(player, uid, title, check, source)

        val withdraw = withdraw(player, title)
        if (withdraw != PurchaseResult.SUCCESS) return finish(player, uid, title, withdraw, source)

        if (!TitleUtils.addTitle(player.uniqueId, uid, source.name)) {
            rollback(player, title)
            return finish(player, uid, title, PurchaseResult.WITHDRAW_FAILED, source)
        }
        bukkitPlugin.server.pluginManager.callEvent(TitleBuyEvent(player, title, source))
        TitleEffectUtils.runBuy(player, title)
        return finish(player, uid, title, PurchaseResult.SUCCESS, source)
    }

    fun renew(player: Player, uid: String): PurchaseResult {
        val title = FreeSwitchTitleAPI.getTitle(uid) ?: return PurchaseResult.TITLE_NOT_FOUND
        if (!TitleUtils.hasTitle(player.uniqueId, uid)) return PurchaseResult.NOT_OWNED
        if (title.durationMillis <= 0L) return PurchaseResult.PERMANENT_TITLE

        val check = checkBalance(player, title)
        if (check != PurchaseResult.SUCCESS) return check

        val withdraw = withdraw(player, title)
        if (withdraw != PurchaseResult.SUCCESS) return withdraw

        if (!TitleUtils.renewTitle(player.uniqueId, uid)) {
            rollback(player, title)
            return PurchaseResult.WITHDRAW_FAILED
        }
        return PurchaseResult.SUCCESS
    }

    private fun finish(player: Player, uid: String, title: TitleData?, result: PurchaseResult, source: PurchaseSource): PurchaseResult {
        PurchaseLogger.log(player, uid, title, result, source)
        return result
    }

    private fun checkBalance(player: Player, title: TitleData): PurchaseResult {
        return when (title.shopCurrency) {
            CurrencyType.VAULT -> checkVault(player, title)
            CurrencyType.PLAYER_POINTS -> checkPoints(player, title)
            CurrencyType.BOTH -> {
                val vault = checkVault(player, title)
                if (vault != PurchaseResult.SUCCESS) vault else checkPoints(player, title)
            }
            CurrencyType.FREE -> PurchaseResult.SUCCESS
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
        return when (title.shopCurrency) {
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
            CurrencyType.FREE -> PurchaseResult.SUCCESS
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
        when (title.shopCurrency) {
            CurrencyType.VAULT -> VaultEconomy.deposit(player, title.vaultPrice)
            CurrencyType.PLAYER_POINTS -> PlayerPointsEconomy.deposit(player, title.pointsPrice)
            CurrencyType.BOTH -> {
                VaultEconomy.deposit(player, title.vaultPrice)
                PlayerPointsEconomy.deposit(player, title.pointsPrice)
            }
            CurrencyType.FREE -> Unit
        }
    }

    fun sendRenewResult(player: Player, result: PurchaseResult, title: TitleData? = null) {
        when (result) {
            PurchaseResult.SUCCESS -> {
                val expireText = title?.let { FreeSwitchTitleAPI.getTitleExpireText(player, it.uid) } ?: ""
                player.sendLang("renew-success", title?.title ?: "", expireText)
            }
            PurchaseResult.TITLE_NOT_FOUND -> player.sendLang("purchase-title-not-found")
            PurchaseResult.TITLE_NOT_AVAILABLE_TIME -> player.sendLang("purchase-not-available-time")
            PurchaseResult.NOT_OWNED -> player.sendLang("renew-not-owned")
            PurchaseResult.PERMANENT_TITLE -> player.sendLang("renew-permanent-title")
            PurchaseResult.VAULT_NOT_AVAILABLE -> player.sendLang("purchase-vault-not-available")
            PurchaseResult.PLAYER_POINTS_NOT_AVAILABLE -> player.sendLang("purchase-player-points-not-available")
            PurchaseResult.NOT_ENOUGH_MONEY -> player.sendLang("purchase-not-enough-money")
            PurchaseResult.NOT_ENOUGH_POINTS -> player.sendLang("purchase-not-enough-points")
            PurchaseResult.WITHDRAW_FAILED -> player.sendLang("renew-failed")
            else -> player.sendLang("renew-failed")
        }
    }

    fun sendResult(player: Player, result: PurchaseResult, title: TitleData? = null) {
        when (result) {
            PurchaseResult.SUCCESS -> player.sendLang("purchase-success", title?.title ?: "")
            PurchaseResult.TITLE_NOT_FOUND -> player.sendLang("purchase-title-not-found")
            PurchaseResult.SHOP_DISABLED -> player.sendLang("purchase-shop-disabled")
            PurchaseResult.TITLE_NOT_IN_SHOP -> player.sendLang("purchase-not-in-shop")
            PurchaseResult.TITLE_NOT_AVAILABLE_TIME -> player.sendLang("purchase-not-available-time")
            PurchaseResult.ALREADY_OWNED -> player.sendLang("purchase-already-owned")
            PurchaseResult.NOT_OWNED -> player.sendLang("renew-not-owned")
            PurchaseResult.PERMANENT_TITLE -> player.sendLang("renew-permanent-title")
            PurchaseResult.NO_PERMISSION -> player.sendLang("purchase-no-permission")
            PurchaseResult.REQUIREMENT_NOT_MET -> player.sendLang("purchase-requirement-not-met")
            PurchaseResult.VAULT_NOT_AVAILABLE -> player.sendLang("purchase-vault-not-available")
            PurchaseResult.PLAYER_POINTS_NOT_AVAILABLE -> player.sendLang("purchase-player-points-not-available")
            PurchaseResult.NOT_ENOUGH_MONEY -> player.sendLang("purchase-not-enough-money")
            PurchaseResult.NOT_ENOUGH_POINTS -> player.sendLang("purchase-not-enough-points")
            PurchaseResult.WITHDRAW_FAILED -> player.sendLang("purchase-withdraw-failed")
        }
    }
}
