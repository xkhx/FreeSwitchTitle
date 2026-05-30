package top.zoyn.freeswitchtitle.api

import org.bukkit.OfflinePlayer
import top.zoyn.freeswitchtitle.data.TitleData
import top.zoyn.freeswitchtitle.util.ConfigUtils
import top.zoyn.freeswitchtitle.util.TitleUtils
import java.util.UUID

/**
 * FreeSwitchTitle 对外 API。
 */
object FreeSwitchTitleAPI {

    @JvmStatic
    fun getPlayerTitle(player: OfflinePlayer): String {
        return getPlayerCurrentTitle(player)?.title ?: ConfigUtils.prefix
    }

    @JvmStatic
    fun getPlayerTitle(uuid: UUID): String {
        return getPlayerCurrentTitle(uuid)?.title ?: ConfigUtils.prefix
    }

    @JvmStatic
    fun getTitleUidList(): List<String> = TitleUtils.getTitleUidListAll()

    @JvmStatic
    fun getTitleDataList(): List<TitleData> = TitleUtils.getTitleDataList()

    @JvmStatic
    fun getPlayerCurrentTitle(player: OfflinePlayer): TitleData? {
        val uid = TitleUtils.getUsing(player.uniqueId) ?: return null
        return TitleUtils.getTitleData(uid)
    }

    @JvmStatic
    fun getPlayerCurrentTitle(uuid: UUID): TitleData? {
        val uid = TitleUtils.getUsing(uuid) ?: return null
        return TitleUtils.getTitleData(uid)
    }

    @JvmStatic
    fun setPlayerCurrentTitle(player: OfflinePlayer, uid: String): Boolean {
        return TitleUtils.using(player.uniqueId, uid)
    }

    @JvmStatic
    fun setPlayerCurrentTitle(uuid: UUID, uid: String): Boolean {
        return TitleUtils.using(uuid, uid)
    }

    @JvmStatic
    fun getPlayerOwnedTitle(player: OfflinePlayer): List<TitleData> {
        return getPlayerOwnedTitle(player.uniqueId)
    }

    @JvmStatic
    fun getPlayerOwnedTitle(uuid: UUID): List<TitleData> {
        return TitleUtils.getPlayerTitleUidList(uuid).mapNotNull { uid -> TitleUtils.getTitleData(uid) }
    }

    @JvmStatic
    fun getTitleExpireAt(player: OfflinePlayer, uid: String): Long? {
        return getTitleExpireAt(player.uniqueId, uid)
    }

    @JvmStatic
    fun getTitleExpireAt(uuid: UUID, uid: String): Long? {
        return TitleUtils.getTitleExpireAt(uuid, uid)
    }

    @JvmStatic
    fun getTitleExpireText(player: OfflinePlayer, uid: String): String {
        return getTitleExpireText(player.uniqueId, uid)
    }

    @JvmStatic
    fun getTitleExpireText(uuid: UUID, uid: String): String {
        val title = TitleUtils.getTitleData(uid) ?: return ""
        return TitleUtils.getTitleExpireText(uuid, title)
    }

    @JvmStatic
    fun resetPlayerTitle(player: OfflinePlayer): Boolean {
        return TitleUtils.reset(player.uniqueId)
    }

    @JvmStatic
    fun resetPlayerTitle(uuid: UUID): Boolean {
        return TitleUtils.reset(uuid)
    }

    @JvmStatic
    fun hasTitle(player: OfflinePlayer, uid: String): Boolean {
        return TitleUtils.hasTitle(player.uniqueId, uid)
    }

    @JvmStatic
    fun hasTitle(uuid: UUID, uid: String): Boolean {
        return TitleUtils.hasTitle(uuid, uid)
    }

    @JvmStatic
    fun addTitle(player: OfflinePlayer, uid: String): Boolean {
        return TitleUtils.addTitle(player.uniqueId, uid)
    }

    @JvmStatic
    fun addTitle(uuid: UUID, uid: String): Boolean {
        return TitleUtils.addTitle(uuid, uid)
    }

    @JvmStatic
    fun removeTitle(player: OfflinePlayer, uid: String): Boolean {
        return TitleUtils.removeTitle(player.uniqueId, uid)
    }

    @JvmStatic
    fun removeTitle(uuid: UUID, uid: String): Boolean {
        return TitleUtils.removeTitle(uuid, uid)
    }

    @JvmStatic
    fun getTitle(uid: String): TitleData? {
        return TitleUtils.getTitleData(uid)
    }
}
