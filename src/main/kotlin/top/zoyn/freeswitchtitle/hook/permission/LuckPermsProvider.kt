package top.zoyn.freeswitchtitle.hook.permission

import net.luckperms.api.LuckPermsProvider as LuckPermsApiProvider
import net.luckperms.api.node.Node
import org.bukkit.entity.Player
import taboolib.platform.BukkitPlugin

object LuckPermsPermissionProvider : PermissionProvider {

    override val name: String = "LuckPerms"

    override fun isAvailable(): Boolean {
        return BukkitPlugin.getInstance().server.pluginManager.isPluginEnabled("LuckPerms")
    }

    override fun addPermission(player: Player, permission: String): Boolean {
        return runCatching {
            val user = LuckPermsApiProvider.get().userManager.loadUser(player.uniqueId).join()
            user.data().add(Node.builder(permission).value(true).build())
            LuckPermsApiProvider.get().userManager.saveUser(user).join()
            true
        }.getOrDefault(false)
    }

    override fun removePermission(player: Player, permission: String): Boolean {
        return runCatching {
            val user = LuckPermsApiProvider.get().userManager.loadUser(player.uniqueId).join()
            user.data().remove(Node.builder(permission).value(true).build())
            LuckPermsApiProvider.get().userManager.saveUser(user).join()
            true
        }.getOrDefault(false)
    }
}
