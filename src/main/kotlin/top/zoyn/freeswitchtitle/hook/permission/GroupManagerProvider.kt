package top.zoyn.freeswitchtitle.hook.permission

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import taboolib.platform.BukkitPlugin
import top.zoyn.freeswitchtitle.util.ConfigUtils

object GroupManagerPermissionProvider : PermissionProvider {

    override val name: String = "GroupManager"

    override fun isAvailable(): Boolean {
        return BukkitPlugin.getInstance().server.pluginManager.isPluginEnabled("GroupManager")
    }

    override fun addPermission(player: Player, permission: String): Boolean {
        return dispatch(ConfigUtils.groupManagerGiveCommand, player, permission)
    }

    override fun removePermission(player: Player, permission: String): Boolean {
        return dispatch(ConfigUtils.groupManagerTakeCommand, player, permission)
    }

    private fun dispatch(template: String, player: Player, permission: String): Boolean {
        val command = template
            .replace("{player}", player.name)
            .replace("{uuid}", player.uniqueId.toString())
            .replace("{permission}", permission)
        return Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command)
    }
}
