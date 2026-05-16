package top.zoyn.freeswitchtitle.command

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.PermissionDefault
import taboolib.common.platform.command.mainCommand
import taboolib.common.platform.command.player
import taboolib.common.platform.command.subCommand
import taboolib.expansion.createHelper
import taboolib.platform.util.sendLang
import top.zoyn.freeswitchtitle.FreeSwitchTitle
import top.zoyn.freeswitchtitle.api.FreeSwitchTitleAPI
import top.zoyn.freeswitchtitle.gui.type.GuiType
import top.zoyn.freeswitchtitle.util.ConfigUtils
import top.zoyn.freeswitchtitle.util.addTitle
import top.zoyn.freeswitchtitle.util.openTitleListMenu
import top.zoyn.freeswitchtitle.util.removeTitle
import top.zoyn.freeswitchtitle.util.resetCurrentTitle
import java.util.UUID

@CommandHeader(
    name = "freeswitchtitle",
    aliases = ["fst"],
    permission = "freeswitchtitle.command",
    permissionDefault = PermissionDefault.TRUE
)
object FreeSwitchTitleCommand {

    @CommandBody
    val main = mainCommand {
        createHelper()
    }

    @CommandBody(permission = "freeswitchtitle.command.open", permissionDefault = PermissionDefault.TRUE)
    val open = subCommand {
        execute<Player> { sender, _, _ ->
            sender.openTitleListMenu(GuiType.PLAYER_LIST)
        }
    }

    @CommandBody(permission = "freeswitchtitle.command.shop", permissionDefault = PermissionDefault.TRUE)
    val shop = subCommand {
        execute<Player> { sender, _, _ ->
            if (!ConfigUtils.enableShop) {
                sender.sendLang("command-shop-disabled")
                return@execute
            }
            sender.openTitleListMenu(GuiType.TITLE_SHOP)
        }
    }

    @CommandBody(permission = "freeswitchtitle.command.reset", permissionDefault = PermissionDefault.TRUE)
    val reset = subCommand {
        execute<Player> { sender, _, _ ->
            sender.resetCurrentTitle()
            sender.sendLang("reset-title-message")
        }
    }

    @CommandBody(permission = "freeswitchtitle.command.list", permissionDefault = PermissionDefault.OP)
    val list = subCommand {
        execute<CommandSender> { sender, _, _ ->
            if (sender !is Player) {
                sender.sendMessage(FreeSwitchTitleAPI.getTitleUidList().toString())
                return@execute
            }
            sender.openTitleListMenu(GuiType.TITLE_LIST)
        }
    }

    @CommandBody(permission = "freeswitchtitle.command.reload", permissionDefault = PermissionDefault.OP)
    val reload = subCommand {
        execute<CommandSender> { sender, _, _ ->
            FreeSwitchTitle.reload()
            sender.sendLang("command-reload-success")
        }
    }

    @CommandBody(permission = "freeswitchtitle.command.show", permissionDefault = PermissionDefault.OP)
    val show = subCommand {
        dynamic("uid") {
            suggestionUncheck<CommandSender> { _, _ ->
                FreeSwitchTitleAPI.getTitleUidList()
            }
            execute<CommandSender> { sender, context, _ ->
                val uid = context["uid"]
                val title = FreeSwitchTitleAPI.getTitle(uid) ?: run {
                    sender.sendLang("show-title-failed")
                    return@execute
                }
                sender.sendLang("show-title-success", title.toString())
            }
        }
    }

    @CommandBody(permission = "freeswitchtitle.command.add", permissionDefault = PermissionDefault.OP)
    val add = subCommand {
        dynamic("uid") {
            suggestionUncheck<CommandSender> { _, _ ->
                FreeSwitchTitleAPI.getTitleUidList()
            }
            player("player") {
                execute<CommandSender> { sender, context, _ ->
                    val player = context.player("player")
                    val uid = context["uid"]
                    addTitle(sender, player.uniqueId, uid)
                }
            }
            execute<Player> { sender, context, _ ->
                val uid = context["uid"]
                addTitle(sender, sender.uniqueId, uid)
            }
        }
    }

    @CommandBody(permission = "freeswitchtitle.command.remove", permissionDefault = PermissionDefault.OP)
    val remove = subCommand {
        dynamic("uid") {
            suggestionUncheck<CommandSender> { _, _ ->
                FreeSwitchTitleAPI.getTitleUidList()
            }
            player("player") {
                execute<CommandSender> { sender, context, _ ->
                    val player = context.player("player")
                    val uid = context["uid"]
                    removeTitle(sender, player.uniqueId, uid)
                }
            }
            execute<Player> { sender, context, _ ->
                val uid = context["uid"]
                removeTitle(sender, sender.uniqueId, uid)
            }
        }
    }

    private fun addTitle(sender: CommandSender, uuid: UUID, uid: String) {
        val title = FreeSwitchTitleAPI.getTitle(uid)
        if (title != null && uuid.addTitle(uid)) {
            sender.sendLang("add-title-success", title.title)
        } else {
            sender.sendLang("add-title-failed")
        }
    }

    private fun removeTitle(sender: CommandSender, uuid: UUID, uid: String) {
        val title = FreeSwitchTitleAPI.getTitle(uid)
        if (title != null && uuid.removeTitle(uid)) {
            sender.sendLang("remove-title-success", title.title)
        } else {
            sender.sendLang("remove-title-failed")
        }
    }
}
