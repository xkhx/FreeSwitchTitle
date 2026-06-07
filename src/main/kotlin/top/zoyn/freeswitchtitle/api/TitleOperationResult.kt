package top.zoyn.freeswitchtitle.api

enum class TitleOperationResult {
    SUCCESS,
    TITLE_NOT_FOUND,
    ALREADY_OWNED,
    NOT_OWNED,
    NO_CURRENT_TITLE,
    PERMISSION_FAILED,
    EVENT_CANCELLED,
    PLAYER_OFFLINE,
    FAILED
}
