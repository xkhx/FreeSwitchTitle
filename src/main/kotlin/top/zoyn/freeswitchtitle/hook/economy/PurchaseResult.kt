package top.zoyn.freeswitchtitle.hook.economy

enum class PurchaseResult {
    SUCCESS,
    TITLE_NOT_FOUND,
    SHOP_DISABLED,
    TITLE_NOT_IN_SHOP,
    ALREADY_OWNED,
    NO_PERMISSION,
    VAULT_NOT_AVAILABLE,
    PLAYER_POINTS_NOT_AVAILABLE,
    NOT_ENOUGH_MONEY,
    NOT_ENOUGH_POINTS,
    WITHDRAW_FAILED
}
