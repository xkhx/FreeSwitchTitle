package top.zoyn.freeswitchtitle.hook.economy

import org.bukkit.entity.Player
import taboolib.common.io.newFile
import taboolib.common.platform.function.getDataFolder
import top.zoyn.freeswitchtitle.FreeSwitchTitle
import top.zoyn.freeswitchtitle.data.TitleData
import top.zoyn.freeswitchtitle.util.ConfigUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PurchaseLogger {

    private val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT)

    fun log(player: Player, uid: String, title: TitleData?, result: PurchaseResult, source: PurchaseSource) {
        if (!ConfigUtils.shopLogPurchases) return
        runCatching {
            val file = File(getDataFolder(), "purchase-logs.yml")
            if (!file.exists()) {
                newFile(file)
            }
            file.appendText(buildEntry(player, uid, title, result, source), Charsets.UTF_8)
        }.getOrElse {
            FreeSwitchTitle.sendConsoleMessage("§c[FreeSwitchTitle] 购买日志写入失败: ${it.message}")
        }
    }

    private fun buildEntry(player: Player, uid: String, title: TitleData?, result: PurchaseResult, source: PurchaseSource): String {
        val currency = title?.shopCurrency?.name ?: "UNKNOWN"
        val titleName = title?.title ?: ""
        val vaultPrice = title?.vaultPrice ?: 0.0
        val pointsPrice = title?.pointsPrice ?: 0
        return buildString {
            appendLine("- time: '${escape(timeFormat.format(Date()))}'")
            appendLine("  player: '${escape(player.name)}'")
            appendLine("  uuid: '${escape(player.uniqueId.toString())}'")
            appendLine("  title: '${escape(title?.uid ?: uid)}'")
            appendLine("  title-name: '${escape(titleName)}'")
            appendLine("  result: '${escape(result.name)}'")
            appendLine("  currency: '${escape(currency)}'")
            appendLine("  vault-price: $vaultPrice")
            appendLine("  points-price: $pointsPrice")
            appendLine("  source: '${escape(source.name)}'")
        }
    }

    private fun escape(value: String): String {
        return value.replace("'", "''")
    }
}
