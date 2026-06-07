package top.zoyn.freeswitchtitle.util

import org.bukkit.ChatColor
import taboolib.common.LifeCycle
import taboolib.common.io.newFile
import taboolib.common.platform.Awake
import taboolib.common.platform.function.getDataFolder
import taboolib.common.util.replaceWithOrder
import taboolib.expansion.getPlayerDataContainer
import taboolib.module.chat.colored
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type
import taboolib.platform.util.bukkitPlugin
import taboolib.platform.util.onlinePlayers
import taboolib.platform.util.sendLang
import top.zoyn.freeswitchtitle.FreeSwitchTitle
import top.zoyn.freeswitchtitle.api.TitleOperationResult
import top.zoyn.freeswitchtitle.data.TitleData
import top.zoyn.freeswitchtitle.event.TitleEquipEvent
import top.zoyn.freeswitchtitle.event.TitleExpireEvent
import top.zoyn.freeswitchtitle.event.TitleGrantEvent
import top.zoyn.freeswitchtitle.event.TitleRemoveEvent
import top.zoyn.freeswitchtitle.event.TitleResetEvent
import top.zoyn.freeswitchtitle.event.TitleUnequipEvent
import top.zoyn.freeswitchtitle.hook.permission.PermissionGrantStore
import top.zoyn.freeswitchtitle.hook.permission.PermissionManager
import java.io.File
import java.util.UUID

object TitleUtils {

    private const val CURRENT_PLAYER_DATA_VERSION = 1
    private const val DATA_VERSION_KEY = "data-version"
    private const val TITLE_LIST_KEY = "title_list"
    private const val USING_KEY = "using"
    private const val TITLE_EXPIRE_KEY = "title_expire_map"
    private const val TITLE_OBTAIN_TIME_KEY = "title_obtain_time_map"
    private const val TITLE_OBTAIN_SOURCE_KEY = "title_obtain_source_map"
    private const val COLLECTION_REWARD_CLAIMED_KEY = "collection_reward_claimed"
    private const val MIGRATION_BACKUP_KEY = "migration_backup_v1"

    private val titleMap = linkedMapOf<String, TitleData>()

    lateinit var titleConfig: Configuration
        private set

    fun loadTitleData() {
        titleMap.clear()
        titleConfig = loadTitleConfig()
        FreeSwitchTitle.sendConsoleMessage("${ChatColor.GREEN}> ${ChatColor.RESET}称号配置文件加载: ${ChatColor.WHITE}${titleConfig.file?.absolutePath}")
        for (uid in titleConfig.getKeys(false)) {
            val title = ConfigUtils.getTitle(uid).colored()
            val material = ConfigUtils.getTitleMaterial(uid)
            val lore = ConfigUtils.getTitleLore(uid).colored()
            val joinMessage = ConfigUtils.getTitleJoinMessage(uid).colored()
            titleMap[uid] = TitleData(
                uid = uid,
                title = title,
                material = material,
                lore = lore,
                joinMessage = joinMessage,
                category = ConfigUtils.getTitleCategory(uid),
                rarity = ConfigUtils.getTitleRarity(uid),
                hidden = ConfigUtils.getTitleHidden(uid),
                durationMillis = ConfigUtils.getTitleDurationMillis(uid),
                shopEnable = ConfigUtils.getTitleShopEnable(uid),
                shopAvailableFrom = ConfigUtils.getTitleShopAvailableFrom(uid),
                shopAvailableUntil = ConfigUtils.getTitleShopAvailableUntil(uid),
                shopCurrency = ConfigUtils.getTitleShopCurrency(uid),
                vaultPrice = ConfigUtils.getTitleVaultPrice(uid),
                pointsPrice = ConfigUtils.getTitlePointsPrice(uid),
                shopPermission = ConfigUtils.getTitleShopPermission(uid),
                requiredPermissions = ConfigUtils.getTitleRequiredPermissions(uid),
                permissions = ConfigUtils.getTitlePermissions(uid),
                equipActions = ConfigUtils.getTitleEquipActions(uid),
                unequipActions = ConfigUtils.getTitleUnequipActions(uid),
                buyActions = ConfigUtils.getTitleBuyActions(uid),
                expireActions = ConfigUtils.getTitleExpireActions(uid),
                obtainActions = ConfigUtils.getTitleObtainActions(uid),
                removeActions = ConfigUtils.getTitleRemoveActions(uid),
                resetActions = ConfigUtils.getTitleResetActions(uid)
            )
        }
        FreeSwitchTitle.sendConsoleMessage("${ChatColor.GREEN}> ${ChatColor.WHITE}${titleMap.size} ${ChatColor.RESET}个称号加载完成!")
    }

    private fun loadTitleConfig(): Configuration {
        val path = ConfigUtils.titlePath.replaceWithOrder(getDataFolder().absolutePath)
        val folder = File(path)
        val file = File(folder, "title.yml")
        if (!file.exists()) {
            newFile(file).writeBytes(
                bukkitPlugin.getResource("titledata/title.yml")?.readBytes()
                    ?: error("resource not found: titledata/title.yml")
            )
        }
        return Configuration.loadFromFile(file, Type.YAML)
    }

    fun getTitleData(uid: String): TitleData? = titleMap[uid]

    fun getTitleDataList(): List<TitleData> = titleMap.values.toList()

    fun getTitleCategoryList(): List<String> = titleMap.values
        .map { it.category }
        .filter { it.isNotBlank() }
        .distinct()

    fun getTitleDataListByCategory(category: String): List<TitleData> {
        val normalized = category.trim()
        if (normalized.isBlank() || normalized.equals("all", ignoreCase = true) || normalized == "*") {
            return getTitleDataList()
        }
        return titleMap.values.filter { it.category.equals(normalized, ignoreCase = true) }
    }

    fun getCollectibleTitleDataList(): List<TitleData> = getTitleDataList()

    fun getVisibleCollectionTitleDataList(uuid: UUID): List<TitleData> {
        val owned = getPlayerTitleUidList(uuid).toSet()
        return getCollectibleTitleDataList().filter { !it.hidden || it.uid in owned }
    }

    fun getCollectionTotal(): Int = getCollectibleTitleDataList().size

    fun getCollectionCount(uuid: UUID): Int {
        val loaded = titleMap.keys
        return getPlayerTitleUidList(uuid).count { it in loaded }
    }

    fun getCollectionProgress(uuid: UUID): String {
        val total = getCollectionTotal()
        if (total <= 0) return "0%"
        return "${getCollectionCount(uuid) * 100 / total}%"
    }

    fun getTitleUidListAll(): List<String> = titleMap.keys.toList()

    fun migratePlayerData(uuid: UUID) {
        runCatching {
            migratePlayerDataRaw(uuid)
        }.getOrElse {
            FreeSwitchTitle.sendConsoleMessage("§c[FreeSwitchTitle] 玩家数据迁移失败: $uuid - ${it.message}")
        }
    }

    fun migrateOnlinePlayers() {
        onlinePlayers.forEach { migratePlayerData(it.uniqueId) }
    }

    fun getPlayerDataVersion(uuid: UUID): Int {
        return uuid.getPlayerDataContainer()[DATA_VERSION_KEY]?.toIntOrNull() ?: 0
    }

    private fun setPlayerDataVersion(uuid: UUID, version: Int) {
        uuid.getPlayerDataContainer()[DATA_VERSION_KEY] = version.toString()
    }

    private fun migratePlayerDataRaw(uuid: UUID) {
        if (getPlayerDataVersion(uuid) >= CURRENT_PLAYER_DATA_VERSION) return
        val data = uuid.getPlayerDataContainer()
        if (data[MIGRATION_BACKUP_KEY].isNullOrBlank()) {
            data[MIGRATION_BACKUP_KEY] = buildMigrationBackup(uuid)
        }
        val now = System.currentTimeMillis()
        val obtainTimes = getTitleObtainTimeMap(uuid).toMutableMap()
        val obtainSources = getTitleObtainSourceMap(uuid).toMutableMap()
        getPlayerTitleUidListRaw(uuid).forEach { uid ->
            obtainTimes.putIfAbsent(uid, now)
            obtainSources.putIfAbsent(uid, "MIGRATION")
        }
        saveTitleObtainTimeMap(uuid, obtainTimes)
        saveTitleObtainSourceMap(uuid, obtainSources)
        setPlayerDataVersion(uuid, CURRENT_PLAYER_DATA_VERSION)
    }

    private fun buildMigrationBackup(uuid: UUID): String {
        val data = uuid.getPlayerDataContainer()
        return listOf(
            "$TITLE_LIST_KEY=${data[TITLE_LIST_KEY].orEmpty()}",
            "$USING_KEY=${data[USING_KEY].orEmpty()}",
            "$TITLE_EXPIRE_KEY=${data[TITLE_EXPIRE_KEY].orEmpty()}",
            "$COLLECTION_REWARD_CLAIMED_KEY=${data[COLLECTION_REWARD_CLAIMED_KEY].orEmpty()}",
            "${ConfigUtils.permissionRecordKey}=${data[ConfigUtils.permissionRecordKey].orEmpty()}"
        ).joinToString("||")
    }

    fun getPlayerTitleUidList(uuid: UUID): List<String> {
        migratePlayerData(uuid)
        cleanupExpiredTitles(uuid)
        return getPlayerTitleUidListRaw(uuid)
    }

    private fun getPlayerTitleUidListRaw(uuid: UUID): List<String> {
        val data = uuid.getPlayerDataContainer()
        val text = data[TITLE_LIST_KEY].orEmpty()
        if (text.isBlank()) return emptyList()
        return text.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
    }

    private fun savePlayerTitleUidList(uuid: UUID, uidList: List<String>) {
        uuid.getPlayerDataContainer()[TITLE_LIST_KEY] = uidList.distinct().joinToString(", ")
    }

    fun addTitle(uuid: UUID, uid: String, source: String = "COMMAND"): Boolean {
        return addTitleResult(uuid, uid, source) == TitleOperationResult.SUCCESS
    }

    fun addTitleResult(uuid: UUID, uid: String, source: String = "COMMAND"): TitleOperationResult {
        return addTitleInternal(uuid, uid, source, checkRewards = true)
    }

    private fun addTitleInternal(uuid: UUID, uid: String, source: String, checkRewards: Boolean): TitleOperationResult {
        migratePlayerData(uuid)
        val title = titleMap[uid] ?: return TitleOperationResult.TITLE_NOT_FOUND
        cleanupExpiredTitles(uuid)
        val titleUidList = getPlayerTitleUidListRaw(uuid).toMutableList()
        if (titleUidList.contains(uid)) return TitleOperationResult.ALREADY_OWNED
        val player = bukkitPlugin.server.getOfflinePlayer(uuid)
        if (callCancellable(TitleGrantEvent(player, title, source))) return TitleOperationResult.EVENT_CANCELLED
        titleUidList.add(uid)
        savePlayerTitleUidList(uuid, titleUidList)
        val now = System.currentTimeMillis()
        setTitleExpireAt(uuid, uid, if (title.durationMillis > 0L) now + title.durationMillis else null)
        setTitleObtainMeta(uuid, uid, now, source)
        onlinePlayers.firstOrNull { it.uniqueId == uuid }?.let { onlinePlayer ->
            TitleEffectUtils.runObtain(onlinePlayer, title)
        }
        if (checkRewards) {
            checkCollectionRewards(uuid)
        }
        return TitleOperationResult.SUCCESS
    }

    fun removeTitle(uuid: UUID, uid: String, reason: String = "COMMAND"): Boolean {
        return removeTitleResult(uuid, uid, reason) == TitleOperationResult.SUCCESS
    }

    fun removeTitleResult(uuid: UUID, uid: String, reason: String = "COMMAND"): TitleOperationResult {
        migratePlayerData(uuid)
        val title = titleMap[uid] ?: return TitleOperationResult.TITLE_NOT_FOUND
        cleanupExpiredTitles(uuid)
        val wasUsing = getUsing(uuid) == uid
        val titleUidList = getPlayerTitleUidListRaw(uuid).toMutableList()
        if (!titleUidList.contains(uid)) return TitleOperationResult.NOT_OWNED
        val offlinePlayer = bukkitPlugin.server.getOfflinePlayer(uuid)
        if (callCancellable(TitleRemoveEvent(offlinePlayer, title, reason))) return TitleOperationResult.EVENT_CANCELLED
        titleUidList.remove(uid)
        savePlayerTitleUidList(uuid, titleUidList)
        setTitleExpireAt(uuid, uid, null)
        clearTitleObtainMeta(uuid, uid)
        if (wasUsing) {
            reset(uuid)
        }
        onlinePlayers.firstOrNull { it.uniqueId == uuid }?.let { player ->
            TitleEffectUtils.runRemove(player, title)
        }
        return TitleOperationResult.SUCCESS
    }

    fun getUsing(uuid: UUID): String? {
        migratePlayerData(uuid)
        cleanupExpiredTitles(uuid)
        val uid = uuid.getPlayerDataContainer()[USING_KEY]
        return uid?.takeIf { it.isNotBlank() && titleMap.containsKey(it) }
    }

    fun using(uuid: UUID, uid: String): Boolean {
        return usingResult(uuid, uid) == TitleOperationResult.SUCCESS
    }

    fun usingResult(uuid: UUID, uid: String): TitleOperationResult {
        migratePlayerData(uuid)
        val title = titleMap[uid] ?: return TitleOperationResult.TITLE_NOT_FOUND
        if (!getPlayerTitleUidList(uuid).contains(uid)) return TitleOperationResult.NOT_OWNED
        val current = getUsing(uuid)
        if (current == uid) return TitleOperationResult.SUCCESS
        val player = onlinePlayers.firstOrNull { it.uniqueId == uuid } ?: return TitleOperationResult.PLAYER_OFFLINE
        val oldTitle = current?.let { titleMap[it] }
        if (callCancellable(TitleEquipEvent(player, title, oldTitle))) return TitleOperationResult.EVENT_CANCELLED
        if (!PermissionManager.grant(player, title)) {
            return TitleOperationResult.PERMISSION_FAILED
        }
        oldTitle?.let {
            val retainedPermissions = it.permissions.intersect(title.permissions.toSet())
            PermissionGrantStore.getGrantedForTitle(uuid, it.uid)
                .intersect(retainedPermissions)
                .forEach { permission ->
                    PermissionGrantStore.markGranted(uuid, title.uid, permission)
                    PermissionGrantStore.unmarkGranted(uuid, it.uid, permission)
                }
            PermissionManager.revoke(player, it, retainedPermissions)
            TitleEffectUtils.runUnequip(player, it)
            callEvent(TitleUnequipEvent(player, it, "SWITCH"))
        }
        uuid.getPlayerDataContainer()[USING_KEY] = uid
        TitleEffectUtils.runEquip(player, title)
        return TitleOperationResult.SUCCESS
    }

    fun reset(uuid: UUID): Boolean {
        return resetResult(uuid) == TitleOperationResult.SUCCESS
    }

    fun resetResult(uuid: UUID): TitleOperationResult {
        migratePlayerData(uuid)
        val current = getUsing(uuid) ?: return TitleOperationResult.NO_CURRENT_TITLE
        val title = titleMap[current] ?: return TitleOperationResult.TITLE_NOT_FOUND
        val offlinePlayer = bukkitPlugin.server.getOfflinePlayer(uuid)
        if (callCancellable(TitleResetEvent(offlinePlayer, title))) return TitleOperationResult.EVENT_CANCELLED
        val player = onlinePlayers.firstOrNull { it.uniqueId == uuid }
        if (player != null) {
            PermissionManager.revoke(player, title)
            TitleEffectUtils.runUnequip(player, title)
            TitleEffectUtils.runReset(player, title)
            callEvent(TitleUnequipEvent(player, title, "RESET"))
        }
        uuid.getPlayerDataContainer()[USING_KEY] = ""
        return TitleOperationResult.SUCCESS
    }

    fun cleanupOnlinePlayers() {
        onlinePlayers.forEach { cleanupExpiredTitles(it.uniqueId) }
    }

    fun cleanupExpiredTitles(uuid: UUID): List<String> {
        migratePlayerData(uuid)
        val titleUidList = getPlayerTitleUidListRaw(uuid)
        if (titleUidList.isEmpty()) return emptyList()

        val expirations = getTitleExpireMap(uuid).toMutableMap()
        val now = System.currentTimeMillis()
        val expired = titleUidList.filter { uid -> (expirations[uid] ?: 0L) in 1L..now }
        val known = titleUidList.filter { titleMap.containsKey(it) }
        val updated = known.filterNot { it in expired }

        if (updated.size != titleUidList.size) {
            savePlayerTitleUidList(uuid, updated)
        }

        if (expired.isNotEmpty()) {
            expired.forEach {
                expirations.remove(it)
                clearTitleObtainMeta(uuid, it)
            }
            saveTitleExpireMap(uuid, expirations)
        }

        val current = uuid.getPlayerDataContainer()[USING_KEY]
        if (!current.isNullOrBlank() && current !in updated) {
            val player = onlinePlayers.firstOrNull { it.uniqueId == uuid }
            titleMap[current]?.let { oldTitle ->
                if (player != null) {
                    PermissionManager.revoke(player, oldTitle)
                    TitleEffectUtils.runUnequip(player, oldTitle)
                    callEvent(TitleUnequipEvent(player, oldTitle, "EXPIRE"))
                }
            }
            uuid.getPlayerDataContainer()[USING_KEY] = ""
        }

        val player = onlinePlayers.firstOrNull { it.uniqueId == uuid }
        if (player != null) {
            expired.mapNotNull { titleMap[it] }
                .forEach {
                    TitleEffectUtils.runExpire(player, it)
                    callEvent(TitleExpireEvent(player, it))
                    player.sendLang("title-expired", it.title)
                }
        }
        return expired
    }

    fun renewTitle(uuid: UUID, uid: String): Boolean {
        migratePlayerData(uuid)
        val title = titleMap[uid] ?: return false
        if (title.durationMillis <= 0L) return false
        if (!hasTitle(uuid, uid)) return false
        val now = System.currentTimeMillis()
        val currentExpireAt = getTitleExpireMap(uuid)[uid]?.takeIf { it > 0L }
        val base = maxOf(currentExpireAt ?: 0L, now)
        setTitleExpireAt(uuid, uid, base + title.durationMillis)
        return true
    }

    fun getTitleExpireAt(uuid: UUID, uid: String): Long? {
        cleanupExpiredTitles(uuid)
        return getTitleExpireMap(uuid)[uid]?.takeIf { it > 0L }
    }

    fun getTitleExpireText(uuid: UUID, title: TitleData): String {
        if (!hasTitle(uuid, title.uid)) return title.durationText
        return TitleDurationUtils.formatRemaining(getTitleExpireAt(uuid, title.uid))
    }

    fun getTitleObtainTimeAt(uuid: UUID, uid: String): Long? {
        migratePlayerData(uuid)
        return getTitleObtainTimeMap(uuid)[uid]?.takeIf { it > 0L }
    }

    fun getTitleObtainSource(uuid: UUID, uid: String): String {
        migratePlayerData(uuid)
        return getTitleObtainSourceMap(uuid)[uid].orEmpty()
    }

    fun hasTitle(uuid: UUID, uid: String): Boolean {
        return getPlayerTitleUidList(uuid).contains(uid)
    }

    fun checkCollectionRewards(uuid: UUID): List<String> {
        migratePlayerData(uuid)
        val rewards = ConfigUtils.collectionRewards
        if (rewards.isEmpty()) return emptyList()
        val count = getCollectionCount(uuid)
        val claimed = getClaimedCollectionRewards(uuid).toMutableSet()
        val granted = mutableListOf<String>()
        rewards
            .filterKeys { threshold -> count >= threshold && threshold !in claimed }
            .toSortedMap()
            .forEach { (threshold, uidList) ->
                uidList.forEach { rewardUid ->
                    if (addTitleInternal(uuid, rewardUid, "COLLECTION_REWARD", checkRewards = false) == TitleOperationResult.SUCCESS) {
                        granted += rewardUid
                    }
                }
                claimed += threshold
            }
        saveClaimedCollectionRewards(uuid, claimed)
        if (granted.isNotEmpty()) {
            val player = onlinePlayers.firstOrNull { it.uniqueId == uuid }
            player?.sendLang("collection-reward-granted", granted.joinToString(", ") { titleMap[it]?.title ?: it })
        }
        return granted
    }

    private fun callEvent(event: org.bukkit.event.Event) {
        bukkitPlugin.server.pluginManager.callEvent(event)
    }

    private fun callCancellable(event: org.bukkit.event.Cancellable): Boolean {
        bukkitPlugin.server.pluginManager.callEvent(event as org.bukkit.event.Event)
        return event.isCancelled
    }

    private fun getClaimedCollectionRewards(uuid: UUID): Set<Int> {
        val text = uuid.getPlayerDataContainer()[COLLECTION_REWARD_CLAIMED_KEY].orEmpty()
        if (text.isBlank()) return emptySet()
        return text.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .filter { it > 0 }
            .toSet()
    }

    private fun saveClaimedCollectionRewards(uuid: UUID, claimed: Set<Int>) {
        uuid.getPlayerDataContainer()[COLLECTION_REWARD_CLAIMED_KEY] = claimed.sorted().joinToString(",")
    }

    private fun getTitleExpireMap(uuid: UUID): Map<String, Long> {
        return getLongMap(uuid, TITLE_EXPIRE_KEY)
    }

    private fun saveTitleExpireMap(uuid: UUID, expirations: Map<String, Long>) {
        saveLongMap(uuid, TITLE_EXPIRE_KEY, expirations)
    }

    private fun getTitleObtainTimeMap(uuid: UUID): Map<String, Long> {
        return getLongMap(uuid, TITLE_OBTAIN_TIME_KEY)
    }

    private fun saveTitleObtainTimeMap(uuid: UUID, obtainTimes: Map<String, Long>) {
        saveLongMap(uuid, TITLE_OBTAIN_TIME_KEY, obtainTimes)
    }

    private fun getTitleObtainSourceMap(uuid: UUID): Map<String, String> {
        val text = uuid.getPlayerDataContainer()[TITLE_OBTAIN_SOURCE_KEY].orEmpty()
        if (text.isBlank()) return emptyMap()
        return text.split(",")
            .mapNotNull { entry ->
                val parts = entry.split("=", limit = 2)
                val uid = parts.getOrNull(0)?.trim().orEmpty()
                val source = parts.getOrNull(1)?.trim().orEmpty()
                if (uid.isNotEmpty() && source.isNotEmpty()) uid to source else null
            }
            .toMap()
    }

    private fun saveTitleObtainSourceMap(uuid: UUID, sources: Map<String, String>) {
        uuid.getPlayerDataContainer()[TITLE_OBTAIN_SOURCE_KEY] = sources
            .filter { it.key.isNotBlank() && it.value.isNotBlank() }
            .entries
            .joinToString(",") { "${it.key}=${it.value}" }
    }

    private fun getLongMap(uuid: UUID, key: String): Map<String, Long> {
        val text = uuid.getPlayerDataContainer()[key].orEmpty()
        if (text.isBlank()) return emptyMap()
        return text.split(",")
            .mapNotNull { entry ->
                val parts = entry.split("=", limit = 2)
                val uid = parts.getOrNull(0)?.trim().orEmpty()
                val value = parts.getOrNull(1)?.trim()?.toLongOrNull()
                if (uid.isNotEmpty() && value != null && value > 0L) uid to value else null
            }
            .toMap()
    }

    private fun saveLongMap(uuid: UUID, key: String, values: Map<String, Long>) {
        uuid.getPlayerDataContainer()[key] = values
            .filter { it.key.isNotBlank() && it.value > 0L }
            .entries
            .joinToString(",") { "${it.key}=${it.value}" }
    }

    private fun setTitleObtainMeta(uuid: UUID, uid: String, time: Long, source: String) {
        val obtainTimes = getTitleObtainTimeMap(uuid).toMutableMap()
        val obtainSources = getTitleObtainSourceMap(uuid).toMutableMap()
        obtainTimes[uid] = time
        obtainSources[uid] = source
        saveTitleObtainTimeMap(uuid, obtainTimes)
        saveTitleObtainSourceMap(uuid, obtainSources)
    }

    private fun clearTitleObtainMeta(uuid: UUID, uid: String) {
        val obtainTimes = getTitleObtainTimeMap(uuid).toMutableMap()
        val obtainSources = getTitleObtainSourceMap(uuid).toMutableMap()
        obtainTimes.remove(uid)
        obtainSources.remove(uid)
        saveTitleObtainTimeMap(uuid, obtainTimes)
        saveTitleObtainSourceMap(uuid, obtainSources)
    }

    private fun setTitleExpireAt(uuid: UUID, uid: String, expireAt: Long?) {
        val expirations = getTitleExpireMap(uuid).toMutableMap()
        if (expireAt != null && expireAt > 0L) {
            expirations[uid] = expireAt
        } else {
            expirations.remove(uid)
        }
        saveTitleExpireMap(uuid, expirations)
    }

    @Awake(LifeCycle.DISABLE)
    fun uninstallTitle() {
        titleMap.clear()
    }
}
