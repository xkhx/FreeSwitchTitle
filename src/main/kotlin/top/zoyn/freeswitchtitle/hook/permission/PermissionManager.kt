package top.zoyn.freeswitchtitle.hook.permission

import org.bukkit.entity.Player
import top.zoyn.freeswitchtitle.data.TitleData
import top.zoyn.freeswitchtitle.util.ConfigUtils

object PermissionManager {

    private fun provider(): PermissionProvider? {
        return when (ConfigUtils.permissionMode) {
            PermissionMode.NONE -> null
            PermissionMode.LUCKPERMS -> LuckPermsPermissionProvider.takeIf { it.isAvailable() }
            PermissionMode.GROUP_MANAGER -> GroupManagerPermissionProvider.takeIf { it.isAvailable() }
        }
    }

    fun grant(player: Player, title: TitleData): Boolean {
        if (title.permissions.isEmpty()) return true
        val provider = provider() ?: return ConfigUtils.permissionMode == PermissionMode.NONE
        var success = true
        title.permissions.forEach { permission ->
            if (player.hasPermission(permission) && ConfigUtils.permissionSafeCheck) {
                return@forEach
            }
            if (provider.addPermission(player, permission)) {
                PermissionGrantStore.markGranted(player.uniqueId, title.uid, permission)
            } else {
                success = false
            }
        }
        player.recalculatePermissions()
        return success
    }

    fun revoke(player: Player, title: TitleData, retainedPermissions: Set<String> = emptySet()): Boolean {
        if (title.permissions.isEmpty()) return true
        val provider = provider() ?: return ConfigUtils.permissionMode == PermissionMode.NONE
        var success = true
        val permissions = if (ConfigUtils.permissionSafeCheck) {
            PermissionGrantStore.getGrantedForTitle(player.uniqueId, title.uid)
        } else {
            title.permissions.toSet()
        }.filter { it !in retainedPermissions }
        permissions.forEach { permission ->
            if (provider.removePermission(player, permission)) {
                PermissionGrantStore.unmarkGranted(player.uniqueId, title.uid, permission)
            } else {
                success = false
            }
        }
        player.recalculatePermissions()
        return success
    }
}
