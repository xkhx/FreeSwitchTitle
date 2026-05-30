package top.zoyn.freeswitchtitle.util

import java.util.Locale
import kotlin.math.max

object TitleDurationUtils {

    private val durationRegex = Regex("^(\\d+)(ms|s|m|h|d)?$")

    fun parse(text: String?): Long {
        val raw = text?.trim()?.lowercase(Locale.getDefault()).orEmpty()
        if (raw.isBlank() || raw == "0" || raw == "permanent" || raw == "forever" || raw == "永久") {
            return 0L
        }
        val match = durationRegex.matchEntire(raw) ?: return 0L
        val amount = match.groupValues[1].toLongOrNull() ?: return 0L
        return when (match.groupValues[2].ifBlank { "s" }) {
            "ms" -> amount
            "s" -> amount * 1000L
            "m" -> amount * 60_000L
            "h" -> amount * 3_600_000L
            "d" -> amount * 86_400_000L
            else -> 0L
        }
    }

    fun format(durationMillis: Long): String {
        if (durationMillis <= 0L) return "永久"
        var seconds = max(1L, durationMillis / 1000L)
        val days = seconds / 86_400L
        seconds %= 86_400L
        val hours = seconds / 3_600L
        seconds %= 3_600L
        val minutes = seconds / 60L
        seconds %= 60L

        val parts = mutableListOf<String>()
        if (days > 0) parts += "${days}天"
        if (hours > 0) parts += "${hours}小时"
        if (minutes > 0) parts += "${minutes}分钟"
        if (seconds > 0 && parts.isEmpty()) parts += "${seconds}秒"
        return parts.take(2).joinToString("")
    }

    fun formatRemaining(expiresAt: Long?): String {
        if (expiresAt == null || expiresAt <= 0L) return "永久"
        val remaining = expiresAt - System.currentTimeMillis()
        return if (remaining <= 0L) "已过期" else format(remaining)
    }
}
