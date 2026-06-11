package top.zoyn.freeswitchtitle.command

import org.bukkit.Particle
import org.bukkit.attribute.Attribute
import org.bukkit.command.CommandSender
import org.bukkit.potion.PotionEffectType
import org.bukkit.entity.Player
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.PermissionDefault
import taboolib.common.platform.command.mainCommand
import taboolib.common.platform.command.player
import taboolib.common.platform.command.subCommand
import taboolib.expansion.createDescriptionHelper
import taboolib.platform.util.sendLang
import top.zoyn.freeswitchtitle.FreeSwitchTitle
import top.zoyn.freeswitchtitle.api.FreeSwitchTitleAPI
import top.zoyn.freeswitchtitle.gui.type.GuiType
import top.zoyn.freeswitchtitle.data.TitleData
import top.zoyn.freeswitchtitle.hook.economy.CurrencyType
import top.zoyn.freeswitchtitle.hook.economy.EconomyManager
import top.zoyn.freeswitchtitle.hook.economy.PlayerPointsEconomy
import top.zoyn.freeswitchtitle.hook.economy.PurchaseSource
import top.zoyn.freeswitchtitle.hook.economy.VaultEconomy
import top.zoyn.freeswitchtitle.util.ConfigUtils
import top.zoyn.freeswitchtitle.util.TitleImageFontManager
import top.zoyn.freeswitchtitle.util.TitlePreviewManager
import top.zoyn.freeswitchtitle.util.addTitle
import top.zoyn.freeswitchtitle.util.openTitleListMenu
import top.zoyn.freeswitchtitle.util.removeTitle
import top.zoyn.freeswitchtitle.util.resetCurrentTitle
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

@CommandHeader(
    name = "freeswitchtitle",
    aliases = ["fst"],
    permission = "freeswitchtitle.command",
    permissionDefault = PermissionDefault.TRUE
)
object FreeSwitchTitleCommand {

    private fun titleCategorySuggestions(): List<String> {
        return (listOf("all") + FreeSwitchTitleAPI.getTitleCategoryList()).distinct()
    }

    @CommandBody
    val main = mainCommand {
        createDescriptionHelper()
    }

    @CommandBody(permission = "freeswitchtitle.command.open", permissionDefault = PermissionDefault.TRUE, description = "@command-description-open")
    val open = subCommand {
        dynamic("category") {
            suggestionUncheck<Player> { _, _ ->
                titleCategorySuggestions()
            }
            execute<Player> { sender, context, _ ->
                sender.openTitleListMenu(GuiType.PLAYER_LIST, category = context["category"])
            }
        }
        execute<Player> { sender, _, _ ->
            sender.openTitleListMenu(GuiType.PLAYER_LIST)
        }
    }

    @CommandBody(permission = "freeswitchtitle.command.shop", permissionDefault = PermissionDefault.TRUE, description = "@command-description-shop")
    val shop = subCommand {
        dynamic("category") {
            suggestionUncheck<Player> { _, _ ->
                titleCategorySuggestions()
            }
            execute<Player> { sender, context, _ ->
                if (!ConfigUtils.shopEnable) {
                    sender.sendLang("command-shop-disabled")
                    return@execute
                }
                sender.openTitleListMenu(GuiType.TITLE_SHOP, category = context["category"])
            }
        }
        execute<Player> { sender, _, _ ->
            if (!ConfigUtils.shopEnable) {
                sender.sendLang("command-shop-disabled")
                return@execute
            }
            sender.openTitleListMenu(GuiType.TITLE_SHOP)
        }
    }

    @CommandBody(permission = "freeswitchtitle.command.collection", permissionDefault = PermissionDefault.TRUE, description = "@command-description-collection")
    val collection = subCommand {
        dynamic("category") {
            suggestionUncheck<Player> { _, _ ->
                titleCategorySuggestions()
            }
            execute<Player> { sender, context, _ ->
                sender.openTitleListMenu(GuiType.TITLE_COLLECTION, category = context["category"])
            }
        }
        execute<Player> { sender, _, _ ->
            sender.openTitleListMenu(GuiType.TITLE_COLLECTION)
        }
    }

    @CommandBody(permission = "freeswitchtitle.command.reset", permissionDefault = PermissionDefault.TRUE, description = "@command-description-reset")
    val reset = subCommand {
        execute<Player> { sender, _, _ ->
            if (sender.resetCurrentTitle()) {
                sender.sendLang("reset-title-message")
            } else {
                sender.sendLang("reset-title-failed")
            }
        }
    }

    @CommandBody(permission = "freeswitchtitle.command.balance", permissionDefault = PermissionDefault.TRUE, description = "@command-description-balance")
    val balance = subCommand {
        player("player") {
            execute<CommandSender> { sender, context, _ ->
                if (!sender.hasPermission("freeswitchtitle.command.balance.other")) {
                    sender.sendLang("command-no-permission")
                    return@execute
                }
                val target = context.player("player").cast<Player>()
                sender.sendLang("command-balance-other", target.name, VaultEconomy.getBalance(target), PlayerPointsEconomy.getBalance(target))
            }
        }
        execute<Player> { sender, _, _ ->
            sender.sendLang("command-balance", VaultEconomy.getBalance(sender), PlayerPointsEconomy.getBalance(sender))
        }
    }

    @CommandBody(permission = "freeswitchtitle.command.renew", permissionDefault = PermissionDefault.TRUE, description = "@command-description-renew")
    val renew = subCommand {
        dynamic("uid") {
            suggestionUncheck<Player> { _, _ ->
                FreeSwitchTitleAPI.getTitleUidList()
            }
            execute<Player> { sender, context, _ ->
                val uid = context["uid"]
                val title = FreeSwitchTitleAPI.getTitle(uid)
                val result = EconomyManager.renew(sender, uid)
                EconomyManager.sendRenewResult(sender, result, title)
            }
        }
    }

    @CommandBody(permission = "freeswitchtitle.command.buy", permissionDefault = PermissionDefault.TRUE, description = "@command-description-buy")
    val buy = subCommand {
        dynamic("uid") {
            suggestionUncheck<Player> { _, _ ->
                FreeSwitchTitleAPI.getTitleUidList()
            }
            execute<Player> { sender, context, _ ->
                val uid = context["uid"]
                val title = FreeSwitchTitleAPI.getTitle(uid)
                val result = EconomyManager.purchase(sender, uid, PurchaseSource.COMMAND)
                EconomyManager.sendResult(sender, result, title)
            }
        }
    }

    @CommandBody(permission = "freeswitchtitle.command.preview", permissionDefault = PermissionDefault.TRUE, description = "@command-description-preview")
    val preview = subCommand {
        dynamic("uid") {
            suggestionUncheck<Player> { _, _ ->
                FreeSwitchTitleAPI.getTitleUidList()
            }
            execute<Player> { sender, context, _ ->
                val uid = context["uid"]
                val title = FreeSwitchTitleAPI.getTitle(uid) ?: run {
                    sender.sendLang("preview-title-not-found")
                    return@execute
                }
                TitlePreviewManager.preview(sender, title)
            }
        }
    }

    @CommandBody(permission = "freeswitchtitle.command.previewdisplay", permissionDefault = PermissionDefault.TRUE, description = "@command-description-previewdisplay")
    val previewDisplay = subCommand {
        dynamic("uid") {
            suggestionUncheck<Player> { _, _ ->
                FreeSwitchTitleAPI.getTitleUidList()
            }
            execute<Player> { sender, context, _ ->
                val uid = context["uid"]
                val title = FreeSwitchTitleAPI.getTitle(uid) ?: run {
                    sender.sendLang("preview-title-not-found")
                    return@execute
                }
                TitlePreviewManager.previewDisplay(sender, title)
            }
        }
    }

    @CommandBody(permission = "freeswitchtitle.command.resourcepack", permissionDefault = PermissionDefault.OP, description = "@command-description-resourcepack")
    val resourcePack = subCommand {
        execute<CommandSender> { sender, _, _ ->
            sendResourcePackStatus(sender)
        }
    }

    @CommandBody(permission = "freeswitchtitle.command.look", permissionDefault = PermissionDefault.TRUE, description = "@command-description-look")
    val look = subCommand {
        player("player") {
            dynamic("category") {
                suggestionUncheck<Player> { _, _ ->
                    titleCategorySuggestions()
                }
                execute<Player> { sender, context, _ ->
                    val target = context.player("player")
                    sender.openTitleListMenu(GuiType.LOOK_PLAYER, target.uniqueId, context["category"])
                }
            }
            execute<Player> { sender, context, _ ->
                val target = context.player("player")
                sender.openTitleListMenu(GuiType.LOOK_PLAYER, target.uniqueId)
            }
        }
    }

    @CommandBody(permission = "freeswitchtitle.command.list", permissionDefault = PermissionDefault.OP, description = "@command-description-list")
    val list = subCommand {
        dynamic("category") {
            suggestionUncheck<CommandSender> { _, _ ->
                titleCategorySuggestions()
            }
            execute<CommandSender> { sender, context, _ ->
                if (sender !is Player) {
                    sendTitleList(sender, context["category"])
                    return@execute
                }
                sender.openTitleListMenu(GuiType.TITLE_LIST, category = context["category"])
            }
        }
        execute<CommandSender> { sender, _, _ ->
            if (sender !is Player) {
                sendTitleList(sender, "all")
                return@execute
            }
            sender.openTitleListMenu(GuiType.TITLE_LIST)
        }
    }

    @CommandBody(permission = "freeswitchtitle.command.reload", permissionDefault = PermissionDefault.OP, description = "@command-description-reload")
    val reload = subCommand {
        execute<CommandSender> { sender, _, _ ->
            FreeSwitchTitle.reload()
            sender.sendLang("command-reload-success")
        }
    }

    @CommandBody(permission = "freeswitchtitle.command.show", permissionDefault = PermissionDefault.OP, description = "@command-description-show")
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
                sendTitleDetail(sender, title)
            }
        }
    }

    @CommandBody(permission = "freeswitchtitle.command.validate", permissionDefault = PermissionDefault.OP, description = "@command-description-validate")
    val validate = subCommand {
        execute<CommandSender> { sender, _, _ ->
            sendValidationReport(sender)
        }
    }

    @CommandBody(permission = "freeswitchtitle.command.add", permissionDefault = PermissionDefault.OP, description = "@command-description-add")
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

    @CommandBody(permission = "freeswitchtitle.command.remove", permissionDefault = PermissionDefault.OP, description = "@command-description-remove")
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

    @CommandBody(permission = "freeswitchtitle.command.set", permissionDefault = PermissionDefault.OP, description = "@command-description-set")
    val set = subCommand {
        player("player") {
            dynamic("uid") {
                suggestionUncheck<CommandSender> { _, _ ->
                    FreeSwitchTitleAPI.getTitleUidList()
                }
                execute<CommandSender> { sender, context, _ ->
                    val player = context.player("player").cast<Player>()
                    val uid = context["uid"]
                    setTitle(sender, player, uid)
                }
            }
        }
    }

    @CommandBody(permission = "freeswitchtitle.command.clear", permissionDefault = PermissionDefault.OP, description = "@command-description-clear")
    val clear = subCommand {
        player("player") {
            execute<CommandSender> { sender, context, _ ->
                val player = context.player("player").cast<Player>()
                if (player.resetCurrentTitle()) {
                    sender.sendLang("clear-title-success")
                } else {
                    sender.sendLang("clear-title-failed")
                }
            }
        }
    }

    private fun sendResourcePackStatus(sender: CommandSender) {
        val zip = TitleImageFontManager.getResourcePackZip()
        sender.sendLang("resourcepack-header")
        sender.sendLang("resourcepack-line-enabled", if (ConfigUtils.displayResourcePackEnable) "是" else "否")
        sender.sendLang("resourcepack-line-image-folder", TitleImageFontManager.getImageFolder().absolutePath)
        sender.sendLang("resourcepack-line-mapping-file", TitleImageFontManager.getMappingFile().absolutePath)
        sender.sendLang("resourcepack-line-output-folder", TitleImageFontManager.getResourcePackFolder().absolutePath)
        sender.sendLang("resourcepack-line-namespace", ConfigUtils.displayResourcePackNamespace)
        sender.sendLang("resourcepack-line-pack-format", ConfigUtils.displayResourcePackFormat)
        sender.sendLang("resourcepack-line-zip", zip.absolutePath, if (zip.exists()) "存在" else "不存在")
        sender.sendLang("resourcepack-line-sha1", TitleImageFontManager.getLastSha1().ifBlank { "未生成" })
        sender.sendLang("resourcepack-line-image-count", TitleImageFontManager.getImageCount())
        if (TitleImageFontManager.getImageCount() <= 0) {
            sender.sendLang("resourcepack-line-empty")
        }
        TitleImageFontManager.getUnsafeOutputWarning().takeIf { it.isNotBlank() }?.let {
            sender.sendLang("resourcepack-line-warning", it)
        }
        sender.sendLang("resourcepack-line-server-url")
        sender.sendLang("resourcepack-line-server-sha1", TitleImageFontManager.getLastSha1().ifBlank { "<sha1>" })
        sender.sendLang("resourcepack-footer")
    }

    private fun sendTitleList(sender: CommandSender, category: String) {
        val titles = FreeSwitchTitleAPI.getTitleDataListByCategory(category)
        sender.sendLang("list-title-header", category, titles.size)
        if (titles.isEmpty()) {
            sender.sendLang("list-title-empty")
            sender.sendLang("list-title-footer")
            return
        }
        titles.forEach { title ->
            sender.sendLang(
                "list-title-entry",
                title.uid,
                title.title,
                title.category,
                title.rarity.displayName,
                title.durationText,
                if (title.shopEnable) "是" else "否",
                formatPrice(title),
                if (title.hidden) "是" else "否"
            )
        }
        sender.sendLang("list-title-footer")
    }

    private fun sendTitleDetail(sender: CommandSender, title: TitleData) {
        sender.sendLang("show-title-header", title.uid)
        sender.sendLang("show-title-line-title", title.title)
        sender.sendLang("show-title-line-category", title.category)
        sender.sendLang("show-title-line-rarity", title.rarity.displayName)
        sender.sendLang("show-title-line-hidden", if (title.hidden) "是" else "否")
        sender.sendLang("show-title-line-duration", title.durationText)
        sender.sendLang("show-title-line-shop", if (title.shopEnable) "是" else "否", title.shopCurrency.name, formatPrice(title))
        sender.sendLang("show-title-line-permissions", title.permissions.joinToString(", ").ifBlank { "无" })
        sender.sendLang("show-title-line-requirements", title.requiredPermissions.joinToString(", ").ifBlank { "无" })
        sender.sendLang("show-title-line-particle", formatParticle(title))
        sender.sendLang("show-title-line-buff", formatBuff(title))
        sender.sendLang("show-title-line-display", formatDisplay(title))
        sender.sendLang("show-title-line-actions", title.equipActions.size, title.unequipActions.size, title.buyActions.size, title.expireActions.size)
        sender.sendLang("show-title-footer")
    }

    private fun formatPrice(title: TitleData): String {
        return when (title.shopCurrency) {
            CurrencyType.VAULT -> "金币 ${title.vaultPrice}"
            CurrencyType.PLAYER_POINTS -> "点券 ${title.pointsPrice}"
            CurrencyType.BOTH -> "金币 ${title.vaultPrice} + 点券 ${title.pointsPrice}"
            CurrencyType.FREE -> "免费"
        }
    }

    private fun formatParticle(title: TitleData): String {
        val effect = title.particleEffect
        if (!effect.enabled) return "无"
        val preset = effect.preset.ifBlank { "内联" }
        return "$preset / ${effect.particle} / ${effect.shape.name}"
    }

    private fun formatBuff(title: TitleData): String {
        val effect = title.buffEffect
        if (!effect.enabled) return "无"
        val potion = effect.potionPreset.ifBlank { if (effect.potions.isNotEmpty()) "内联药水" else "" }
        val attribute = effect.attributePreset.ifBlank { if (effect.attributes.isNotEmpty()) "内联属性" else "" }
        return listOf(potion, attribute).filter { it.isNotBlank() }.joinToString(" / ")
    }

    private fun formatDisplay(title: TitleData): String {
        val effect = title.displayEffect
        if (!effect.enabled) return "无"
        val source = when {
            effect.image.isNotBlank() && effect.text.isNotBlank() -> "图片 ${effect.image}"
            effect.image.isNotBlank() -> "图片 ${effect.image}（未解析到字符）"
            effect.text.isNotBlank() -> "手动字符"
            else -> "已启用但无内容"
        }
        val shadow = if (effect.shadow) "是" else "否"
        val seeThrough = if (effect.seeThrough) "是" else "否"
        return "$source / y=${effect.yOffset} / scale=${effect.scale} / shadow=$shadow / see-through=$seeThrough"
    }

    private fun isValidParticle(name: String): Boolean {
        return runCatching { Particle.valueOf(name.trim().uppercase()) }.isSuccess
    }

    private fun isValidPotion(name: String): Boolean {
        return PotionEffectType.getByName(name.trim().uppercase()) != null
    }

    private fun isValidAttribute(name: String): Boolean {
        return runCatching { Attribute.valueOf(name.trim().uppercase()) }.isSuccess
    }

    private fun parseConfigDateTime(raw: String): Long? {
        if (raw.isBlank()) return null
        return runCatching {
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT).apply { isLenient = false }.parse(raw)?.time
        }.getOrNull()
    }

    private fun sendValidationReport(sender: CommandSender) {
        val titles = FreeSwitchTitleAPI.getTitleDataList()
        val warnings = mutableListOf<String>()
        val errors = mutableListOf<String>()
        if (titles.isEmpty()) {
            errors += "没有加载到任何称号"
        }
        titles.forEach { title ->
            if (title.uid.isBlank()) errors += "存在空 UID 称号"
            if (title.title.isBlank()) errors += "${title.uid}: title 为空"
            if (title.category.isBlank()) warnings += "${title.uid}: category 为空，将无法正确分类"
            if (title.shopEnable) {
                if (title.shopCurrency == CurrencyType.VAULT || title.shopCurrency == CurrencyType.BOTH) {
                    if (title.vaultPrice < 0.0) errors += "${title.uid}: vault-price 不能小于 0"
                }
                if (title.shopCurrency == CurrencyType.PLAYER_POINTS || title.shopCurrency == CurrencyType.BOTH) {
                    if (title.pointsPrice < 0) errors += "${title.uid}: points-price 不能小于 0"
                }
                if (title.shopPermission.isNotBlank() && title.requiredPermissions.contains(title.shopPermission)) {
                    warnings += "${title.uid}: shop.permission 与 requirements.permissions 存在重复"
                }
                val availableFrom = parseConfigDateTime(title.shopAvailableFromRaw)
                val availableUntil = parseConfigDateTime(title.shopAvailableUntilRaw)
                if (title.shopAvailableFromRaw.isNotBlank() && availableFrom == null) {
                    errors += "${title.uid}: shop.available-from 时间格式无效，应为 yyyy-MM-dd HH:mm:ss"
                }
                if (title.shopAvailableUntilRaw.isNotBlank() && availableUntil == null) {
                    errors += "${title.uid}: shop.available-until 时间格式无效，应为 yyyy-MM-dd HH:mm:ss"
                }
                if (availableFrom != null && availableUntil != null && availableFrom > availableUntil) {
                    errors += "${title.uid}: shop.available-from 不能晚于 shop.available-until"
                }
            }
            title.permissions.filter { it.isBlank() }.forEach { _ -> warnings += "${title.uid}: permission 中存在空权限节点" }
            title.requiredPermissions.filter { it.isBlank() }.forEach { _ -> warnings += "${title.uid}: requirements.permissions 中存在空权限节点" }
            if (title.displayEffect.enabled) {
                if (title.displayEffect.image.isBlank() && title.displayEffect.text.isBlank()) {
                    errors += "${title.uid}: display.enable 为 true 时 display.image 或 display.text 至少填写一个"
                }
                if (title.displayEffect.image.isNotBlank()) {
                    if (!TitleImageFontManager.isValidImageName(title.displayEffect.image)) {
                        errors += "${title.uid}: display.image 文件名非法，仅允许 png 文件名包含字母、数字、_、-、."
                    } else if (!ConfigUtils.displayResourcePackEnable) {
                        if (title.displayEffect.text.isBlank()) {
                            errors += "${title.uid}: 自动资源包已关闭，display.image 不会生效，请配置 display.text 手动字符"
                        } else {
                            warnings += "${title.uid}: 自动资源包已关闭，display.image 将被忽略并使用 display.text 回退"
                        }
                    } else if (!TitleImageFontManager.hasImage(title.displayEffect.image)) {
                        if (title.displayEffect.text.isBlank()) {
                            errors += "${title.uid}: display.image 图片不存在: ${title.displayEffect.image}，请放入 ${TitleImageFontManager.getImageFolder().absolutePath} 或配置 display.text 回退"
                        } else {
                            warnings += "${title.uid}: display.image 图片不存在: ${title.displayEffect.image}，将使用 display.text 回退"
                        }
                    }
                }
                if (title.displayEffect.scale <= 0.0f) {
                    errors += "${title.uid}: display.scale 必须大于 0"
                }
                if (title.displayEffect.yOffset < 0.5 || title.displayEffect.yOffset > 5.0) {
                    warnings += "${title.uid}: display.y-offset 建议保持在 0.5 到 5.0 之间"
                }
            }
            if (title.particleEffect.preset.isNotBlank() && !ConfigUtils.hasParticlePreset(title.particleEffect.preset)) {
                errors += "${title.uid}: 粒子预设不存在: ${title.particleEffect.preset}"
            }
            if (title.buffEffect.potionPreset.isNotBlank() && !ConfigUtils.hasPotionPreset(title.buffEffect.potionPreset)) {
                errors += "${title.uid}: 药水效果预设不存在: ${title.buffEffect.potionPreset}"
            }
            if (title.buffEffect.attributePreset.isNotBlank() && !ConfigUtils.hasAttributePreset(title.buffEffect.attributePreset)) {
                errors += "${title.uid}: 属性效果预设不存在: ${title.buffEffect.attributePreset}"
            }
            if (title.particleEffect.enabled && !isValidParticle(title.particleEffect.particle)) {
                errors += "${title.uid}: 粒子类型不存在: ${title.particleEffect.particle}"
            }
            title.buffEffect.potions.forEach { effect ->
                if (!isValidPotion(effect.type)) errors += "${title.uid}: 药水效果不存在: ${effect.type}"
            }
            title.buffEffect.attributes.forEach { effect ->
                if (!isValidAttribute(effect.attribute)) errors += "${title.uid}: 属性效果不存在: ${effect.attribute}"
            }
        }
        ConfigUtils.getParticlePresetIds().forEach { presetId ->
            val effect = ConfigUtils.getParticlePresetForValidation(presetId)
            if (effect.enabled && !isValidParticle(effect.particle)) {
                errors += "particles.$presetId: 粒子类型不存在: ${effect.particle}"
            }
        }
        ConfigUtils.collectionRewards.forEach { (threshold, rewards) ->
            rewards.forEach { rewardUid ->
                if (FreeSwitchTitleAPI.getTitle(rewardUid) == null) {
                    errors += "collection.rewards.$threshold 引用了不存在的称号: $rewardUid"
                }
            }
        }
        sender.sendLang("validate-header", titles.size, FreeSwitchTitleAPI.getTitleCategoryList().size, errors.size, warnings.size)
        errors.forEach { sender.sendLang("validate-error", it) }
        warnings.forEach { sender.sendLang("validate-warning", it) }
        if (errors.isEmpty() && warnings.isEmpty()) {
            sender.sendLang("validate-success")
        }
        sender.sendLang("validate-footer")
    }

    private fun addTitle(sender: CommandSender, uuid: UUID, uid: String) {
        val title = FreeSwitchTitleAPI.getTitle(uid)
        if (title != null && uuid.addTitle(uid)) {
            sender.sendLang("add-title-success", title.title)
        } else {
            sender.sendLang("add-title-failed", title?.title ?: uid)
        }
    }

    private fun setTitle(sender: CommandSender, player: Player, uid: String) {
        val title = FreeSwitchTitleAPI.getTitle(uid) ?: run {
            sender.sendLang("set-title-failed")
            return
        }
        if (!FreeSwitchTitleAPI.hasTitle(player, uid)) {
            FreeSwitchTitleAPI.addTitle(player, uid)
        }
        if (FreeSwitchTitleAPI.setPlayerCurrentTitle(player, uid)) {
            sender.sendLang("set-title-success", title.title)
        } else {
            sender.sendLang("set-title-failed")
        }
    }

    private fun removeTitle(sender: CommandSender, uuid: UUID, uid: String) {
        val title = FreeSwitchTitleAPI.getTitle(uid)
        if (title != null && uuid.removeTitle(uid)) {
            sender.sendLang("remove-title-success", title.title)
        } else {
            sender.sendLang("remove-title-failed", title?.title ?: uid)
        }
    }
}
