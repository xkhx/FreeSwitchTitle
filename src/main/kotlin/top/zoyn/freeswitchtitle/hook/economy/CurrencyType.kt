package top.zoyn.freeswitchtitle.hook.economy

enum class CurrencyType {
    VAULT,
    PLAYER_POINTS,
    BOTH;

    companion object {
        fun match(value: String): CurrencyType {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: VAULT
        }
    }
}
