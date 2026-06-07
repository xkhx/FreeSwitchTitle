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
    fun getTitleCategoryList(): List<String> = TitleUtils.getTitleCategoryList()

    @JvmStatic
    fun getTitleDataListByCategory(category: String): List<TitleData> = TitleUtils.getTitleDataListByCategory(category)

    @JvmStatic
    fun getCollectionTotal(): Int = TitleUtils.getCollectionTotal()

    @JvmStatic
    fun getPlayerCollectionCount(player: OfflinePlayer): Int = getPlayerCollectionCount(player.uniqueId)

    @JvmStatic
    fun getPlayerCollectionCount(uuid: UUID): Int = TitleUtils.getCollectionCount(uuid)

    @JvmStatic
    fun getPlayerCollectionProgress(player: OfflinePlayer): String = getPlayerCollectionProgress(player.uniqueId)

    @JvmStatic
    fun getPlayerCollectionProgress(uuid: UUID): String = TitleUtils.getCollectionProgress(uuid)

    @JvmStatic
    fun getVisibleCollectionTitleDataList(player: OfflinePlayer): List<TitleData> = getVisibleCollectionTitleDataList(player.uniqueId)

    @JvmStatic
    fun getVisibleCollectionTitleDataList(uuid: UUID): List<TitleData> = TitleUtils.getVisibleCollectionTitleDataList(uuid)

    @JvmStatic
    fun checkCollectionRewards(player: OfflinePlayer): List<String> = checkCollectionRewards(player.uniqueId)

    @JvmStatic
    fun checkCollectionRewards(uuid: UUID): List<String> = TitleUtils.checkCollectionRewards(uuid)

    @JvmStatic
    fun getPlayerDataVersion(player: OfflinePlayer): Int = getPlayerDataVersion(player.uniqueId)

    @JvmStatic
    fun getPlayerDataVersion(uuid: UUID): Int = TitleUtils.getPlayerDataVersion(uuid)

    @JvmStatic
    fun getTitleObtainTimeAt(player: OfflinePlayer, uid: String): Long? = getTitleObtainTimeAt(player.uniqueId, uid)

    @JvmStatic
    fun getTitleObtainTimeAt(uuid: UUID, uid: String): Long? = TitleUtils.getTitleObtainTimeAt(uuid, uid)

    @JvmStatic
    fun getTitleObtainSource(player: OfflinePlayer, uid: String): String = getTitleObtainSource(player.uniqueId, uid)

    @JvmStatic
    fun getTitleObtainSource(uuid: UUID, uid: String): String = TitleUtils.getTitleObtainSource(uuid, uid)

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
        return setPlayerCurrentTitleResult(player, uid) == TitleOperationResult.SUCCESS
    }

    @JvmStatic
    fun setPlayerCurrentTitle(uuid: UUID, uid: String): Boolean {
        return setPlayerCurrentTitleResult(uuid, uid) == TitleOperationResult.SUCCESS
    }

    @JvmStatic
    fun setPlayerCurrentTitleResult(player: OfflinePlayer, uid: String): TitleOperationResult {
        return setPlayerCurrentTitleResult(player.uniqueId, uid)
    }

    @JvmStatic
    fun setPlayerCurrentTitleResult(uuid: UUID, uid: String): TitleOperationResult {
        return TitleUtils.usingResult(uuid, uid)
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
    fun renewTitle(player: OfflinePlayer, uid: String): Boolean {
        return renewTitle(player.uniqueId, uid)
    }

    @JvmStatic
    fun renewTitle(uuid: UUID, uid: String): Boolean {
        return TitleUtils.renewTitle(uuid, uid)
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
        return resetPlayerTitleResult(player) == TitleOperationResult.SUCCESS
    }

    @JvmStatic
    fun resetPlayerTitle(uuid: UUID): Boolean {
        return resetPlayerTitleResult(uuid) == TitleOperationResult.SUCCESS
    }

    @JvmStatic
    fun resetPlayerTitleResult(player: OfflinePlayer): TitleOperationResult {
        return resetPlayerTitleResult(player.uniqueId)
    }

    @JvmStatic
    fun resetPlayerTitleResult(uuid: UUID): TitleOperationResult {
        return TitleUtils.resetResult(uuid)
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
        return grantTitle(player, uid) == TitleOperationResult.SUCCESS
    }

    @JvmStatic
    fun addTitle(uuid: UUID, uid: String): Boolean {
        return grantTitle(uuid, uid) == TitleOperationResult.SUCCESS
    }

    @JvmStatic
    fun grantTitle(player: OfflinePlayer, uid: String, source: String = "API"): TitleOperationResult {
        return grantTitle(player.uniqueId, uid, source)
    }

    @JvmStatic
    fun grantTitle(uuid: UUID, uid: String, source: String = "API"): TitleOperationResult {
        return TitleUtils.addTitleResult(uuid, uid, source)
    }

    @JvmStatic
    fun removeTitle(player: OfflinePlayer, uid: String): Boolean {
        return removeTitleResult(player, uid) == TitleOperationResult.SUCCESS
    }

    @JvmStatic
    fun removeTitle(uuid: UUID, uid: String): Boolean {
        return removeTitleResult(uuid, uid) == TitleOperationResult.SUCCESS
    }

    @JvmStatic
    fun removeTitleResult(player: OfflinePlayer, uid: String, reason: String = "API"): TitleOperationResult {
        return removeTitleResult(player.uniqueId, uid, reason)
    }

    @JvmStatic
    fun removeTitleResult(uuid: UUID, uid: String, reason: String = "API"): TitleOperationResult {
        return TitleUtils.removeTitleResult(uuid, uid, reason)
    }

    @JvmStatic
    fun getTitle(uid: String): TitleData? {
        return TitleUtils.getTitleData(uid)
    }
}
