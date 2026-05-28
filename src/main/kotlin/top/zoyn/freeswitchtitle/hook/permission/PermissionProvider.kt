package top.zoyn.freeswitchtitle.hook.permission

import org.bukkit.entity.Player

interface PermissionProvider {

    val name: String

    fun isAvailable(): Boolean

    fun addPermission(player: Player, permission: String): Boolean

    fun removePermission(player: Player, permission: String): Boolean
}
