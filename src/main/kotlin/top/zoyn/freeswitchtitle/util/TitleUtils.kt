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
import top.zoyn.freeswitchtitle.data.TitleData
import top.zoyn.freeswitchtitle.hook.permission.PermissionGrantStore
import top.zoyn.freeswitchtitle.hook.permission.PermissionManager
import java.io.File
import java.util.UUID

object TitleUtils {

    private const val TITLE_LIST_KEY = "title_list"
    private const val USING_KEY = "using"
    private const val TITLE_EXPIRE_KEY = "title_expire_map"

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
                durationMillis = ConfigUtils.getTitleDurationMillis(uid),
                shopEnable = ConfigUtils.getTitleShopEnable(uid),
                vaultPrice = ConfigUtils.getTitleVaultPrice(uid),
                pointsPrice = ConfigUtils.getTitlePointsPrice(uid),
                shopPermission = ConfigUtils.getTitleShopPermission(uid),
                permissions = ConfigUtils.getTitlePermissions(uid),
                equipActions = ConfigUtils.getTitleEquipActions(uid),
                unequipActions = ConfigUtils.getTitleUnequipActions(uid),
                buyActions = ConfigUtils.getTitleBuyActions(uid)
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

    fun getTitleUidListAll(): List<String> = titleMap.keys.toList()

    fun getPlayerTitleUidList(uuid: UUID): List<String> {
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

    fun addTitle(uuid: UUID, uid: String): Boolean {
        val title = titleMap[uid] ?: return false
        cleanupExpiredTitles(uuid)
        val titleUidList = getPlayerTitleUidListRaw(uuid).toMutableList()
        if (titleUidList.contains(uid)) return false
        titleUidList.add(uid)
        savePlayerTitleUidList(uuid, titleUidList)
        setTitleExpireAt(uuid, uid, if (title.durationMillis > 0L) System.currentTimeMillis() + title.durationMillis else null)
        return true
    }

    fun removeTitle(uuid: UUID, uid: String): Boolean {
        if (!titleMap.containsKey(uid)) return false
        cleanupExpiredTitles(uuid)
        val titleUidList = getPlayerTitleUidListRaw(uuid).toMutableList()
        if (!titleUidList.remove(uid)) return false
        savePlayerTitleUidList(uuid, titleUidList)
        setTitleExpireAt(uuid, uid, null)
        if (getUsing(uuid) == uid) {
            reset(uuid)
        }
        return true
    }

    fun getUsing(uuid: UUID): String? {
        cleanupExpiredTitles(uuid)
        val uid = uuid.getPlayerDataContainer()[USING_KEY]
        return uid?.takeIf { it.isNotBlank() && titleMap.containsKey(it) }
    }

    fun using(uuid: UUID, uid: String): Boolean {
        val title = titleMap[uid] ?: return false
        if (!getPlayerTitleUidList(uuid).contains(uid)) return false
        val current = getUsing(uuid)
        if (current == uid) return true
        val player = onlinePlayers.firstOrNull { it.uniqueId == uuid }
        if (player != null) {
            if (!PermissionManager.grant(player, title)) {
                return false
            }
            current?.let { titleMap[it] }?.let { oldTitle ->
                val retainedPermissions = oldTitle.permissions.intersect(title.permissions.toSet())
                PermissionGrantStore.getGrantedForTitle(uuid, oldTitle.uid)
                    .intersect(retainedPermissions)
                    .forEach { permission ->
                        PermissionGrantStore.markGranted(uuid, title.uid, permission)
                        PermissionGrantStore.unmarkGranted(uuid, oldTitle.uid, permission)
                    }
                PermissionManager.revoke(player, oldTitle, retainedPermissions)
                TitleEffectUtils.runUnequip(player, oldTitle)
            }
        }
        uuid.getPlayerDataContainer()[USING_KEY] = uid
        if (player != null) {
            TitleEffectUtils.runEquip(player, title)
        }
        return true
    }

    fun reset(uuid: UUID): Boolean {
        val current = getUsing(uuid) ?: return false
        val player = onlinePlayers.firstOrNull { it.uniqueId == uuid }
        if (player != null) {
            titleMap[current]?.let { oldTitle ->
                PermissionManager.revoke(player, oldTitle)
                TitleEffectUtils.runUnequip(player, oldTitle)
            }
        }
        uuid.getPlayerDataContainer()[USING_KEY] = ""
        return true
    }

    fun cleanupOnlinePlayers() {
        onlinePlayers.forEach { cleanupExpiredTitles(it.uniqueId) }
    }

    fun cleanupExpiredTitles(uuid: UUID): List<String> {
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
            expired.forEach { expirations.remove(it) }
            saveTitleExpireMap(uuid, expirations)
        }

        val current = uuid.getPlayerDataContainer()[USING_KEY]
        if (!current.isNullOrBlank() && current !in updated) {
            val player = onlinePlayers.firstOrNull { it.uniqueId == uuid }
            titleMap[current]?.let { oldTitle ->
                if (player != null) {
                    PermissionManager.revoke(player, oldTitle)
                    TitleEffectUtils.runUnequip(player, oldTitle)
                }
            }
            uuid.getPlayerDataContainer()[USING_KEY] = ""
        }

        val player = onlinePlayers.firstOrNull { it.uniqueId == uuid }
        if (player != null) {
            expired.mapNotNull { titleMap[it] }
                .forEach { player.sendLang("title-expired", it.title) }
        }
        return expired
    }

    fun getTitleExpireAt(uuid: UUID, uid: String): Long? {
        cleanupExpiredTitles(uuid)
        return getTitleExpireMap(uuid)[uid]?.takeIf { it > 0L }
    }

    fun getTitleExpireText(uuid: UUID, title: TitleData): String {
        if (!hasTitle(uuid, title.uid)) return title.durationText
        return TitleDurationUtils.formatRemaining(getTitleExpireAt(uuid, title.uid))
    }

    fun hasTitle(uuid: UUID, uid: String): Boolean {
        return getPlayerTitleUidList(uuid).contains(uid)
    }

    private fun getTitleExpireMap(uuid: UUID): Map<String, Long> {
        val text = uuid.getPlayerDataContainer()[TITLE_EXPIRE_KEY].orEmpty()
        if (text.isBlank()) return emptyMap()
        return text.split(",")
            .mapNotNull { entry ->
                val parts = entry.split("=", limit = 2)
                val uid = parts.getOrNull(0)?.trim().orEmpty()
                val expireAt = parts.getOrNull(1)?.trim()?.toLongOrNull()
                if (uid.isNotEmpty() && expireAt != null && expireAt > 0L) uid to expireAt else null
            }
            .toMap()
    }

    private fun saveTitleExpireMap(uuid: UUID, expirations: Map<String, Long>) {
        uuid.getPlayerDataContainer()[TITLE_EXPIRE_KEY] = expirations
            .filterValues { it > 0L }
            .entries
            .joinToString(",") { "${it.key}=${it.value}" }
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
