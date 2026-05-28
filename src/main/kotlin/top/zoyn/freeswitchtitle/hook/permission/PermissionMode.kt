package top.zoyn.freeswitchtitle.hook.permission

enum class PermissionMode {
    LUCKPERMS,
    GROUP_MANAGER,
    NONE;

    companion object {
        fun match(value: String): PermissionMode {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: NONE
        }
    }
}
