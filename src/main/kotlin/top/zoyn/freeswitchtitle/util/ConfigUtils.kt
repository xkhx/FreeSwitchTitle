package top.zoyn.freeswitchtitle.util

import taboolib.common5.cchar
import taboolib.library.xseries.XMaterial
import top.zoyn.freeswitchtitle.FreeSwitchTitle
import top.zoyn.freeswitchtitle.data.TitleParticleEffect
import top.zoyn.freeswitchtitle.data.TitleParticleShape
import top.zoyn.freeswitchtitle.data.TitleRarity
import top.zoyn.freeswitchtitle.hook.economy.CurrencyType
import top.zoyn.freeswitchtitle.hook.permission.PermissionMode
import top.zoyn.freeswitchtitle.util.TitleUtils.titleConfig
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.jvm.optionals.getOrNull

/**
 * 配置文件相关工具类。
 */
object ConfigUtils {

    val configVersion: Int
        get() = FreeSwitchTitle.config.getInt("config-version", 1)

    val shopEnable: Boolean
        get() {
            val raw = FreeSwitchTitle.config["shop"]
            return if (raw is Boolean) raw else FreeSwitchTitle.config.getBoolean("shop.enable", false)
        }

    val shopCurrency: CurrencyType
        get() = CurrencyType.match(FreeSwitchTitle.config.getString("shop.currency") ?: "VAULT")

    val shopLogPurchases: Boolean
        get() = FreeSwitchTitle.config.getBoolean("shop.log-purchases", true)

    val collectionRewards: Map<Int, List<String>>
        get() {
            val section = FreeSwitchTitle.config.getConfigurationSection("collection.rewards") ?: return emptyMap()
            return section.getKeys(false)
                .mapNotNull { key ->
                    val threshold = key.toIntOrNull()?.takeIf { it > 0 } ?: return@mapNotNull null
                    val rewards = section.getStringList(key)
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                    if (rewards.isEmpty()) null else threshold to rewards
                }
                .toMap()
        }

    val enableChat: Boolean
        get() = FreeSwitchTitle.config.getBoolean("chat.show", false)

    val titlePath: String
        get() = FreeSwitchTitle.config.getString("title.path") ?: error("config.yml title.path not found")

    val prefix: String
        get() = FreeSwitchTitle.config.getString("title.prefix") ?: error("config.yml title.prefix not found")

    val format: String
        get() = FreeSwitchTitle.config.getString("chat.format") ?: error("config.yml chat.format not found")

    val permissionMode: PermissionMode
        get() = PermissionMode.match(FreeSwitchTitle.config.getString("permission.mode") ?: "NONE")

    val permissionSafeCheck: Boolean
        get() = FreeSwitchTitle.config.getBoolean("permission.safe-check", true)

    val permissionRecordKey: String
        get() = FreeSwitchTitle.config.getString("permission.record-key") ?: "fst_granted_permissions"

    val groupManagerGiveCommand: String
        get() = FreeSwitchTitle.config.getString("permission.group-manager.give") ?: "manuaddp {player} {permission}"

    val groupManagerTakeCommand: String
        get() = FreeSwitchTitle.config.getString("permission.group-manager.take") ?: "manudelp {player} {permission}"

    val title: String
        get() = FreeSwitchTitle.guiConfig.getString("gui.title.all-title") ?: error("gui.yml gui.title.all-title not found")

    val shopTitle: String
        get() = FreeSwitchTitle.guiConfig.getString("gui.title.shop-title") ?: error("gui.yml gui.title.shop-title not found")

    val myTitle: String
        get() = FreeSwitchTitle.guiConfig.getString("gui.title.my-title") ?: error("gui.yml gui.title.my-title not found")

    val collectionTitle: String
        get() = FreeSwitchTitle.guiConfig.getString("gui.title.collection-title") ?: "称号图鉴"

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

    val categoryType: XMaterial
        get() = getGuiMaterial("gui.category.type", "COMPASS")

    val categorySlot: Char
        get() = (FreeSwitchTitle.guiConfig.getString("gui.category.slot") ?: "C").cchar

    val categoryName: String
        get() = FreeSwitchTitle.guiConfig.getString("gui.category.name") ?: "&f分类筛选"

    val categoryLore: List<String>
        get() = FreeSwitchTitle.guiConfig.getStringList("gui.category.lore").ifEmpty {
            listOf("&7当前分类: &f{category}", "&a点击切换分类")
        }

    val categoryMenuTitle: String
        get() = FreeSwitchTitle.guiConfig.getString("gui.title.category-title") ?: "选择称号分类"

    val categoryAllType: XMaterial
        get() = getGuiMaterial("gui.category-menu.all.type", "BOOK")

    val categoryAllName: String
        get() = FreeSwitchTitle.guiConfig.getString("gui.category-menu.all.name") ?: "&a全部分类"

    val categoryAllLore: List<String>
        get() = FreeSwitchTitle.guiConfig.getStringList("gui.category-menu.all.lore").ifEmpty { listOf("&7查看全部称号") }

    val categoryItemType: XMaterial
        get() = getGuiMaterial("gui.category-menu.item.type", "PAPER")

    val categoryItemName: String
        get() = FreeSwitchTitle.guiConfig.getString("gui.category-menu.item.name") ?: "&f{category}"

    val categoryItemLore: List<String>
        get() = FreeSwitchTitle.guiConfig.getStringList("gui.category-menu.item.lore").ifEmpty { listOf("&7点击查看该分类") }

    val lockedCollectionType: XMaterial
        get() = getGuiMaterial("gui.locked-collection.type", "GRAY_DYE")

    val lockedCollectionName: String
        get() = FreeSwitchTitle.guiConfig.getString("gui.locked-collection.name") ?: "&8???"

    val lockedCollectionLore: List<String>
        get() = FreeSwitchTitle.guiConfig.getStringList("gui.locked-collection.lore").ifEmpty { listOf("&7尚未解锁该称号") }

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

    val statusUsing: String
        get() = FreeSwitchTitle.guiConfig.getString("gui.status.using") ?: "&a状态: 正在使用"

    val statusOwned: String
        get() = FreeSwitchTitle.guiConfig.getString("gui.status.owned") ?: "&e状态: 已拥有"

    val statusNotOwned: String
        get() = FreeSwitchTitle.guiConfig.getString("gui.status.not-owned") ?: "&7状态: 未拥有"

    val statusNoPermission: String
        get() = FreeSwitchTitle.guiConfig.getString("gui.status.no-permission") ?: "&c状态: 无权限购买"

    val statusVaultPrice: String
        get() = FreeSwitchTitle.guiConfig.getString("gui.status.vault-price") ?: "&6金币价格: {0}"

    val statusPointsPrice: String
        get() = FreeSwitchTitle.guiConfig.getString("gui.status.points-price") ?: "&b点券价格: {0}"

    val statusFreePrice: String
        get() = FreeSwitchTitle.guiConfig.getString("gui.status.free-price") ?: "&a价格: 免费"

    val statusClickBuy: String
        get() = FreeSwitchTitle.guiConfig.getString("gui.status.click-buy") ?: "&a点击购买"

    val statusClickEquip: String
        get() = FreeSwitchTitle.guiConfig.getString("gui.status.click-equip") ?: "&a点击佩戴"

    fun getGuiLoreTemplate(path: String, fallback: List<String>): List<String> {
        val lore = FreeSwitchTitle.guiConfig.getStringList("gui.lore.$path")
        return lore.ifEmpty { fallback }
    }

    fun getGuiMaterial(path: String, default: String? = null): XMaterial {
        val type = FreeSwitchTitle.guiConfig.getString(path) ?: default ?: error("gui.yml $path not found")
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

    fun getTitleLore(uid: String): List<String> {
        val title = getTitle(uid)
        val category = getTitleCategory(uid)
        val rarity = getTitleRarity(uid)
        val duration = getTitleDurationText(uid)
        val vaultPrice = getTitleVaultPrice(uid).toString()
        val pointsPrice = getTitlePointsPrice(uid).toString()
        return titleConfig.getStringList("$uid.lore")
            .map { line ->
                line
                    .replace("{uid}", uid)
                    .replace("{title}", title)
                    .replace("{category}", category)
                    .replace("{rarity}", rarity.name.lowercase())
                    .replace("{rarity_name}", rarity.displayName)
                    .replace("{rarity_color}", rarity.color)
                    .replace("{duration}", duration)
                    .replace("{vault_price}", vaultPrice)
                    .replace("{points_price}", pointsPrice)
            }
    }

    fun getTitleJoinMessage(uid: String): String = titleConfig.getString("$uid.join-message") ?: ""

    fun getTitleCategory(uid: String): String = titleConfig.getString("$uid.category")
        ?.trim()
        ?.takeIf { it.isNotBlank() }
        ?: "default"

    fun getTitleRarity(uid: String): TitleRarity = TitleRarity.match(titleConfig.getString("$uid.rarity"))

    fun getTitleHidden(uid: String): Boolean = titleConfig.getBoolean("$uid.hidden", false)

    fun getTitleDurationMillis(uid: String): Long = TitleDurationUtils.parse(titleConfig.getString("$uid.duration"))

    fun getTitleDurationText(uid: String): String = TitleDurationUtils.format(getTitleDurationMillis(uid))

    fun getTitleShopEnable(uid: String): Boolean = titleConfig.getBoolean("$uid.shop.enable", false)

    fun getTitleShopAvailableFrom(uid: String): Long? = parseTitleShopDateTime(uid, "shop.available-from")

    fun getTitleShopAvailableUntil(uid: String): Long? = parseTitleShopDateTime(uid, "shop.available-until")

    fun getTitleShopCurrency(uid: String): CurrencyType {
        return CurrencyType.match(titleConfig.getString("$uid.shop.currency") ?: FreeSwitchTitle.config.getString("shop.currency") ?: "VAULT")
    }

    fun getTitleVaultPrice(uid: String): Double = titleConfig.getDouble("$uid.shop.vault-price", 0.0)

    fun getTitlePointsPrice(uid: String): Int = titleConfig.getInt("$uid.shop.points-price", 0)

    fun getTitleShopPermission(uid: String): String = titleConfig.getString("$uid.shop.permission") ?: ""

    fun getTitleRequiredPermissions(uid: String): List<String> = titleConfig.getStringList("$uid.requirements.permissions")
        .map { it.trim() }
        .filter { it.isNotEmpty() }

    fun getTitlePermissions(uid: String): List<String> = titleConfig.getStringList("$uid.permission")

    fun getTitleParticleEffect(uid: String): TitleParticleEffect {
        val path = "$uid.effects.particle"
        val raw = titleConfig[path]
        if (raw is String) {
            return getParticlePreset(raw, uid)
        }
        val preset = titleConfig.getString("$path.preset")?.trim().orEmpty()
        if (preset.isNotEmpty()) {
            return getParticlePreset(preset, uid)
        }
        return readParticleEffectFromTitle(path)
    }

    private fun getParticlePreset(id: String, uid: String): TitleParticleEffect {
        val normalized = id.trim()
        val path = "particles.$normalized"
        if (normalized.isEmpty() || !FreeSwitchTitle.particleConfig.contains(path)) {
            FreeSwitchTitle.sendConsoleMessage("§e[FreeSwitchTitle] 称号 $uid 引用了不存在的粒子预设: $id")
            return TitleParticleEffect()
        }
        return readParticleEffectFromPreset(path, normalized)
    }

    private fun readParticleEffectFromTitle(path: String): TitleParticleEffect {
        return TitleParticleEffect(
            enabled = titleConfig.getBoolean("$path.enable", false),
            particle = titleConfig.getString("$path.type") ?: "END_ROD",
            shape = TitleParticleShape.match(titleConfig.getString("$path.shape")),
            intervalTicks = titleConfig.getLong("$path.interval", 10L).coerceAtLeast(1L),
            count = titleConfig.getInt("$path.count", 1).coerceAtLeast(1),
            radius = titleConfig.getDouble("$path.radius", 0.8).coerceAtLeast(0.0),
            height = titleConfig.getDouble("$path.height", 0.0),
            points = titleConfig.getInt("$path.points", 24).coerceAtLeast(3),
            speed = titleConfig.getDouble("$path.speed", 0.0),
            offsetX = titleConfig.getDouble("$path.offset.x", 0.0),
            offsetY = titleConfig.getDouble("$path.offset.y", 0.0),
            offsetZ = titleConfig.getDouble("$path.offset.z", 0.0),
        )
    }

    private fun readParticleEffectFromPreset(path: String, id: String): TitleParticleEffect {
        return TitleParticleEffect(
            preset = id,
            enabled = FreeSwitchTitle.particleConfig.getBoolean("$path.enable", true),
            particle = FreeSwitchTitle.particleConfig.getString("$path.type") ?: "END_ROD",
            shape = TitleParticleShape.match(FreeSwitchTitle.particleConfig.getString("$path.shape")),
            intervalTicks = FreeSwitchTitle.particleConfig.getLong("$path.interval", 10L).coerceAtLeast(1L),
            count = FreeSwitchTitle.particleConfig.getInt("$path.count", 1).coerceAtLeast(1),
            radius = FreeSwitchTitle.particleConfig.getDouble("$path.radius", 0.8).coerceAtLeast(0.0),
            height = FreeSwitchTitle.particleConfig.getDouble("$path.height", 0.0),
            points = FreeSwitchTitle.particleConfig.getInt("$path.points", 24).coerceAtLeast(3),
            speed = FreeSwitchTitle.particleConfig.getDouble("$path.speed", 0.0),
            offsetX = FreeSwitchTitle.particleConfig.getDouble("$path.offset.x", 0.0),
            offsetY = FreeSwitchTitle.particleConfig.getDouble("$path.offset.y", 0.0),
            offsetZ = FreeSwitchTitle.particleConfig.getDouble("$path.offset.z", 0.0),
        )
    }

    fun getParticlePresetIds(): List<String> {
        return FreeSwitchTitle.particleConfig.getConfigurationSection("particles")
            ?.getKeys(false)
            ?.toList()
            .orEmpty()
    }

    fun getParticlePresetForValidation(id: String): TitleParticleEffect {
        return readParticleEffectFromPreset("particles.$id", id)
    }

    fun getTitleEquipActions(uid: String): List<String> = getTitleActions(uid, "equip")

    fun getTitleUnequipActions(uid: String): List<String> = getTitleActions(uid, "unequip")

    fun getTitleBuyActions(uid: String): List<String> = getTitleActions(uid, "buy")

    fun getTitleExpireActions(uid: String): List<String> = getTitleActions(uid, "expire")

    fun getTitleObtainActions(uid: String): List<String> = getTitleActions(uid, "obtain")

    fun getTitleRemoveActions(uid: String): List<String> = getTitleActions(uid, "remove")

    fun getTitleResetActions(uid: String): List<String> = getTitleActions(uid, "reset")

    private fun getTitleActions(uid: String, type: String): List<String> {
        val actions = titleConfig.getStringList("$uid.actions.$type")
        return actions.ifEmpty { titleConfig.getStringList("$uid.commands.$type") }
    }

    private fun parseTitleShopDateTime(uid: String, path: String): Long? {
        val raw = titleConfig.getString("$uid.$path")?.trim().orEmpty()
        if (raw.isBlank()) return null
        return runCatching {
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT).parse(raw)?.time
        }.getOrElse {
            FreeSwitchTitle.sendConsoleMessage("§e[FreeSwitchTitle] 称号 $uid 的 $path 时间格式无效: $raw")
            null
        }
    }
}
