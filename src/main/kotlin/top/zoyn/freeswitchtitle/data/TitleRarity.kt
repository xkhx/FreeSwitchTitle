package top.zoyn.freeswitchtitle.data

import java.util.Locale

enum class TitleRarity(
    val displayName: String,
    val color: String
) {
    COMMON("普通", "&f"),
    RARE("稀有", "&9"),
    EPIC("史诗", "&5"),
    LEGENDARY("传说", "&6"),
    LIMITED("限定", "&d");

    companion object {
        fun match(value: String?): TitleRarity {
            val raw = value?.trim()?.uppercase(Locale.getDefault()).orEmpty()
            return entries.firstOrNull { it.name == raw } ?: COMMON
        }
    }
}
