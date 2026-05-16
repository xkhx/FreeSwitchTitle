package top.zoyn.freeswitchtitle.hook

import org.bukkit.OfflinePlayer
import taboolib.platform.compat.PlaceholderExpansion
import top.zoyn.freeswitchtitle.util.ConfigUtils
import top.zoyn.freeswitchtitle.util.getTitleText

object PapiHook : PlaceholderExpansion {

    override val identifier: String = "fst"

    override fun onPlaceholderRequest(player: OfflinePlayer?, args: String): String {
        if (player == null) return ""
        return when (args.lowercase()) {
            "title" -> player.getTitleText()
            "title_show" -> player.getTitleText().let { title -> if (title == ConfigUtils.prefix) "无" else title }
            else -> ""
        }
    }
}
