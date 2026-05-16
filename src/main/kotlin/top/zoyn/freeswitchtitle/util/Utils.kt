package top.zoyn.freeswitchtitle.util

import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import top.zoyn.freeswitchtitle.api.FreeSwitchTitleAPI
import top.zoyn.freeswitchtitle.gui.PlayerGui
import top.zoyn.freeswitchtitle.gui.type.GuiType
import taboolib.platform.util.bukkitPlugin
import java.util.UUID

fun OfflinePlayer.getTitleText() = FreeSwitchTitleAPI.getPlayerTitle(this)

fun OfflinePlayer.getCurrentTitle() = FreeSwitchTitleAPI.getPlayerCurrentTitle(this)

fun OfflinePlayer.setCurrentTitle(uid: String) = FreeSwitchTitleAPI.setPlayerCurrentTitle(this, uid)

fun OfflinePlayer.getOwnedTitle() = FreeSwitchTitleAPI.getPlayerOwnedTitle(this)

fun OfflinePlayer.addTitle(uid: String) = FreeSwitchTitleAPI.addTitle(this, uid)

fun OfflinePlayer.removeTitle(uid: String) = FreeSwitchTitleAPI.removeTitle(this, uid)

fun OfflinePlayer.resetCurrentTitle() = FreeSwitchTitleAPI.resetPlayerTitle(this)

fun UUID.getTitleText() = FreeSwitchTitleAPI.getPlayerTitle(this)

fun UUID.getCurrentTitle() = FreeSwitchTitleAPI.getPlayerCurrentTitle(this)

fun UUID.setCurrentTitle(uid: String) = FreeSwitchTitleAPI.setPlayerCurrentTitle(this, uid)

fun UUID.getOwnedTitle() = FreeSwitchTitleAPI.getPlayerOwnedTitle(this)

fun UUID.addTitle(uid: String) = FreeSwitchTitleAPI.addTitle(this, uid)

fun UUID.removeTitle(uid: String) = FreeSwitchTitleAPI.removeTitle(this, uid)

fun UUID.resetCurrentTitle() = FreeSwitchTitleAPI.resetPlayerTitle(this)

fun UUID.getPlayerName() = bukkitPlugin.server.getOfflinePlayer(this).name

fun Player.openTitleListMenu(type: GuiType, uuid: UUID? = null) = PlayerGui.openTitleListMenu(this, type, uuid)
