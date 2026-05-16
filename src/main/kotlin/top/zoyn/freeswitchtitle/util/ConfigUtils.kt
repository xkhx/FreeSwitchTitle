package top.zoyn.freeswitchtitle.util

import taboolib.common5.cchar
import taboolib.library.xseries.XMaterial
import top.zoyn.freeswitchtitle.FreeSwitchTitle
import top.zoyn.freeswitchtitle.util.TitleUtils.titleConfig
import kotlin.jvm.optionals.getOrNull

/**
 * 配置文件相关工具类。
 */
object ConfigUtils {

    val enableShop: Boolean
        get() = FreeSwitchTitle.config.getBoolean("shop", false)

    val enableChat: Boolean
        get() = FreeSwitchTitle.config.getBoolean("chat.show", false)

    val titlePath: String
        get() = FreeSwitchTitle.config.getString("title.path") ?: error("config.yml title.path not found")

    val prefix: String
        get() = FreeSwitchTitle.config.getString("title.prefix") ?: error("config.yml title.prefix not found")

    val format: String
        get() = FreeSwitchTitle.config.getString("chat.format") ?: error("config.yml chat.format not found")

    val title: String
        get() = FreeSwitchTitle.guiConfig.getString("gui.title.all-title") ?: error("gui.yml gui.title.all-title not found")

    val shopTitle: String
        get() = FreeSwitchTitle.guiConfig.getString("gui.title.shop-title") ?: error("gui.yml gui.title.shop-title not found")

    val myTitle: String
        get() = FreeSwitchTitle.guiConfig.getString("gui.title.my-title") ?: error("gui.yml gui.title.my-title not found")

    val guiMap: List<String>
        get() = FreeSwitchTitle.guiConfig.getStringList("gui.map")

    val slotBy: Char
        get() = (FreeSwitchTitle.guiConfig.getString("gui.slotBy") ?: error("gui.yml gui.slotBy not found")).cchar

    val borderType: XMaterial
        get() = getGuiMaterial("gui.border.type")

    val borderSlot: Char
        get() = (FreeSwitchTitle.guiConfig.getString("gui.border.slot") ?: error("gui.yml gui.border.slot not found")).cchar

    val borderName: String
        get() = FreeSwitchTitle.guiConfig.getString("gui.border.name") ?: error("gui.yml gui.border.name not found")

    val infoType: XMaterial
        get() = getGuiMaterial("gui.info.type")

    val infoSlot: Char
        get() = (FreeSwitchTitle.guiConfig.getString("gui.info.slot") ?: error("gui.yml gui.info.slot not found")).cchar

    val infoName: String
        get() = FreeSwitchTitle.guiConfig.getString("gui.info.name") ?: error("gui.yml gui.info.name not found")

    val infoLore: List<String>
        get() = FreeSwitchTitle.guiConfig.getStringList("gui.info.lore")

    val previousType: XMaterial
        get() = getGuiMaterial("gui.previous.type")

    val previousFirstType: XMaterial
        get() = getGuiMaterial("gui.previous.first-type")

    val previousSlot: Char
        get() = (FreeSwitchTitle.guiConfig.getString("gui.previous.slot") ?: error("gui.yml gui.previous.slot not found")).cchar

    val previousName: String
        get() = FreeSwitchTitle.guiConfig.getString("gui.previous.name") ?: error("gui.yml gui.previous.name not found")

    val previousFirstName: String
        get() = FreeSwitchTitle.guiConfig.getString("gui.previous.first-name") ?: error("gui.yml gui.previous.first-name not found")

    val nextType: XMaterial
        get() = getGuiMaterial("gui.next.type")

    val nextLastType: XMaterial
        get() = getGuiMaterial("gui.next.last-type")

    val nextSlot: Char
        get() = (FreeSwitchTitle.guiConfig.getString("gui.next.slot") ?: error("gui.yml gui.next.slot not found")).cchar

    val nextName: String
        get() = FreeSwitchTitle.guiConfig.getString("gui.next.name") ?: error("gui.yml gui.next.name not found")

    val nextLastName: String
        get() = FreeSwitchTitle.guiConfig.getString("gui.next.last-name") ?: error("gui.yml gui.next.last-name not found")

    fun getGuiMaterial(path: String): XMaterial {
        val type = FreeSwitchTitle.guiConfig.getString(path) ?: error("gui.yml $path not found")
        return getMaterial(type)
    }

    private fun getMaterial(type: String): XMaterial {
        return XMaterial.matchXMaterial(type).getOrNull() ?: error("Material $type not found")
    }

    fun getTitleMaterial(uid: String): XMaterial {
        val type = titleConfig.getString("$uid.material") ?: error("title.yml $uid.material not found")
        return getMaterial(type)
    }

    fun getTitle(uid: String): String = titleConfig.getString("$uid.title") ?: error("title.yml $uid.title not found")

    fun getTitleLore(uid: String): List<String> = titleConfig.getStringList("$uid.lore")

    fun getTitleJoinMessage(uid: String): String = titleConfig.getString("$uid.join-message") ?: ""
}
