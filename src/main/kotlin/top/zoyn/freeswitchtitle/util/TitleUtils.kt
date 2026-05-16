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
import top.zoyn.freeswitchtitle.FreeSwitchTitle
import top.zoyn.freeswitchtitle.data.TitleData
import java.io.File
import java.util.UUID

object TitleUtils {

    private const val TITLE_LIST_KEY = "title_list"
    private const val USING_KEY = "using"

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
            titleMap[uid] = TitleData(uid, title, material, lore, joinMessage)
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
        val data = uuid.getPlayerDataContainer()
        val text = data[TITLE_LIST_KEY].orEmpty()
        if (text.isBlank()) return emptyList()
        return text.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
    }

    fun addTitle(uuid: UUID, uid: String): Boolean {
        if (!titleMap.containsKey(uid)) return false
        val titleUidList = getPlayerTitleUidList(uuid).toMutableList()
        if (titleUidList.contains(uid)) return false
        titleUidList.add(uid)
        uuid.getPlayerDataContainer()[TITLE_LIST_KEY] = titleUidList.joinToString(", ")
        return true
    }

    fun removeTitle(uuid: UUID, uid: String): Boolean {
        if (!titleMap.containsKey(uid)) return false
        val titleUidList = getPlayerTitleUidList(uuid).toMutableList()
        if (!titleUidList.remove(uid)) return false
        uuid.getPlayerDataContainer()[TITLE_LIST_KEY] = titleUidList.joinToString(", ")
        if (getUsing(uuid) == uid) {
            reset(uuid)
        }
        return true
    }

    fun getUsing(uuid: UUID): String? {
        val uid = uuid.getPlayerDataContainer()[USING_KEY]
        return uid?.takeIf { it.isNotBlank() && titleMap.containsKey(it) }
    }

    fun using(uuid: UUID, uid: String): Boolean {
        if (!titleMap.containsKey(uid)) return false
        if (!getPlayerTitleUidList(uuid).contains(uid)) return false
        uuid.getPlayerDataContainer()[USING_KEY] = uid
        return true
    }

    fun reset(uuid: UUID) {
        uuid.getPlayerDataContainer()[USING_KEY] = ""
    }

    @Awake(LifeCycle.DISABLE)
    fun uninstallTitle() {
        titleMap.clear()
    }
}
