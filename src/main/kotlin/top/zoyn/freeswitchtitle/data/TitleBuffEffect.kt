package top.zoyn.freeswitchtitle.data

import org.bukkit.attribute.AttributeModifier
import java.util.Locale

data class TitleBuffEffect(
    val potionPreset: String = "",
    val attributePreset: String = "",
    val potions: List<TitlePotionEffect> = emptyList(),
    val attributes: List<TitleAttributeEffect> = emptyList(),
) {
    val enabled: Boolean
        get() = potions.isNotEmpty() || attributes.isNotEmpty()
}

data class TitlePotionEffect(
    val type: String,
    val amplifier: Int = 0,
    val ambient: Boolean = true,
    val particles: Boolean = false,
    val icon: Boolean = true,
)

data class TitleAttributeEffect(
    val attribute: String,
    val amount: Double,
    val operation: TitleAttributeOperation = TitleAttributeOperation.ADD_NUMBER,
)

enum class TitleAttributeOperation(val bukkitOperation: AttributeModifier.Operation) {
    ADD_NUMBER(AttributeModifier.Operation.ADD_NUMBER),
    ADD_SCALAR(AttributeModifier.Operation.ADD_SCALAR),
    MULTIPLY_SCALAR_1(AttributeModifier.Operation.MULTIPLY_SCALAR_1);

    companion object {
        fun match(value: String?): TitleAttributeOperation {
            val raw = value?.trim()?.uppercase(Locale.ROOT).orEmpty()
            return entries.firstOrNull { it.name == raw } ?: ADD_NUMBER
        }
    }
}
