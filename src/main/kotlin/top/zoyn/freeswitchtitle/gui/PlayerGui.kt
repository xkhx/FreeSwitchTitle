package top.zoyn.freeswitchtitle.gui

import org.bukkit.entity.Player
import taboolib.common.util.replaceWithOrder
import taboolib.library.xseries.XMaterial
import taboolib.module.chat.colored
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.Chest
import taboolib.module.ui.type.PageableChest
import taboolib.platform.compat.replacePlaceholder
import taboolib.platform.util.buildItem
import taboolib.platform.util.sendLang
import top.zoyn.freeswitchtitle.api.FreeSwitchTitleAPI
import top.zoyn.freeswitchtitle.data.TitleData
import top.zoyn.freeswitchtitle.gui.type.GuiType
import top.zoyn.freeswitchtitle.hook.economy.CurrencyType
import top.zoyn.freeswitchtitle.hook.economy.EconomyManager
import top.zoyn.freeswitchtitle.hook.economy.PurchaseResult
import top.zoyn.freeswitchtitle.hook.economy.PurchaseSource
import top.zoyn.freeswitchtitle.gui.type.GuiType.LOOK_PLAYER
import top.zoyn.freeswitchtitle.gui.type.GuiType.PLAYER_LIST
import top.zoyn.freeswitchtitle.gui.type.GuiType.TITLE_COLLECTION
import top.zoyn.freeswitchtitle.gui.type.GuiType.TITLE_LIST
import top.zoyn.freeswitchtitle.gui.type.GuiType.TITLE_SHOP
import top.zoyn.freeswitchtitle.util.ConfigUtils
import top.zoyn.freeswitchtitle.util.TitlePreviewManager
import top.zoyn.freeswitchtitle.util.TitleUtils
import top.zoyn.freeswitchtitle.util.getCurrentTitle
import top.zoyn.freeswitchtitle.util.getOwnedTitle
import top.zoyn.freeswitchtitle.util.getPlayerName
import top.zoyn.freeswitchtitle.util.setCurrentTitle
import java.util.UUID

object PlayerGui {

    fun openTitleListMenu(player: Player, type: GuiType, uuid: UUID? = null, category: String? = null) {
        player.openMenu<PageableChest<TitleData>> {
            title = when (type) {
                PLAYER_LIST -> ConfigUtils.myTitle.replaceWithOrder(player.name).colored()
                TITLE_LIST -> ConfigUtils.title.colored()
                TITLE_SHOP -> ConfigUtils.shopTitle.colored()
                TITLE_COLLECTION -> ConfigUtils.collectionTitle.colored()
                LOOK_PLAYER -> {
                    val name = uuid?.getPlayerName() ?: "获取失败"
                    ConfigUtils.myTitle.replaceWithOrder(name).colored()
                }
            }
            map(*ConfigUtils.guiMap.toTypedArray())
            slotsBy(ConfigUtils.slotBy)
            set(ConfigUtils.borderSlot, buildItem(ConfigUtils.borderType) {
                name = ConfigUtils.borderName
                colored()
            })
            set(ConfigUtils.infoSlot, buildItem(ConfigUtils.infoType) {
                name = ConfigUtils.infoName
                if (XMaterial.matchXMaterial(material) == XMaterial.PLAYER_HEAD) {
                    skullOwner = player.name
                }
                lore.addAll(ConfigUtils.infoLore.replacePlaceholder(player))
                colored()
            })
            set(ConfigUtils.categorySlot, buildItem(ConfigUtils.categoryType) {
                name = ConfigUtils.categoryName
                lore.addAll(renderCategoryLore(player, category))
                colored()
            }) {
                openCategoryMenu(player, type, uuid)
            }
            elements {
                elements(player, type, uuid, category)
            }
            onGenerate { _, title, _, _ ->
                buildTitleItem(player, title, type, uuid)
            }
            onClick { _, title ->
                click(player, title, type)
            }
            setNextPage(getFirstSlot(ConfigUtils.nextSlot)) { _, hasNextPage ->
                if (hasNextPage) {
                    buildItem(ConfigUtils.nextType) {
                        name = ConfigUtils.nextName
                        colored()
                    }
                } else {
                    buildItem(ConfigUtils.nextLastType) {
                        name = ConfigUtils.nextLastName
                        colored()
                    }
                }
            }
            setPreviousPage(getFirstSlot(ConfigUtils.previousSlot)) { _, hasPreviousPage ->
                if (hasPreviousPage) {
                    buildItem(ConfigUtils.previousType) {
                        name = ConfigUtils.previousName
                        colored()
                    }
                } else {
                    buildItem(ConfigUtils.previousFirstType) {
                        name = ConfigUtils.previousFirstName
                        colored()
                    }
                }
            }
        }
    }

    private fun elements(player: Player, type: GuiType, uuid: UUID?, category: String?): List<TitleData> {
        val titles = when (type) {
            PLAYER_LIST -> player.getOwnedTitle()
            TITLE_LIST -> FreeSwitchTitleAPI.getTitleDataList()
            TITLE_SHOP -> if (ConfigUtils.shopEnable) FreeSwitchTitleAPI.getTitleDataList().filter { it.shopEnable && it.isShopAvailableNow() } else emptyList()
            TITLE_COLLECTION -> FreeSwitchTitleAPI.getCollectionTitleDataList()
            LOOK_PLAYER -> uuid?.getOwnedTitle() ?: emptyList()
        }
        return filterByCategory(titles, category)
    }

    private fun filterByCategory(titles: List<TitleData>, category: String?): List<TitleData> {
        val normalized = category?.trim().orEmpty()
        if (normalized.isBlank() || normalized.equals("all", ignoreCase = true) || normalized == "*") {
            return titles
        }
        return titles.filter { it.category.equals(normalized, ignoreCase = true) }
    }

    private fun click(player: Player, title: TitleData, type: GuiType) {
        when (type) {
            PLAYER_LIST -> {
                if (player.setCurrentTitle(title.uid)) {
                    player.sendLang("switch-title-success", title.title)
                    player.closeInventory()
                }
            }
            TITLE_COLLECTION -> {
                if (!FreeSwitchTitleAPI.hasTitle(player, title.uid)) {
                    player.sendLang("collection-title-locked")
                    return
                }
                player.sendLang("view-only-title")
            }
            TITLE_LIST, LOOK_PLAYER -> player.sendLang("view-only-title")
            TITLE_SHOP -> {
                if (ConfigUtils.shopConfirmPurchase && !FreeSwitchTitleAPI.hasTitle(player, title.uid)) {
                    openPurchaseConfirmMenu(player, title)
                    return
                }
                purchase(player, title)
            }
        }
    }

    private fun openPurchaseConfirmMenu(player: Player, title: TitleData) {
        player.openMenu<Chest>(replaceTitlePlaceholders(player, title, ConfigUtils.confirmTitle).colored()) {
            rows(ConfigUtils.confirmRows)
            set(ConfigUtils.confirmInfoSlot, buildItem(ConfigUtils.confirmInfoType) {
                name = replaceTitlePlaceholders(player, title, ConfigUtils.confirmInfoName)
                lore.addAll(ConfigUtils.confirmInfoLore.map { replaceTitlePlaceholders(player, title, it) })
                colored()
            })
            set(ConfigUtils.confirmYesSlot, buildItem(ConfigUtils.confirmYesType) {
                name = replaceTitlePlaceholders(player, title, ConfigUtils.confirmYesName)
                lore.addAll(ConfigUtils.confirmYesLore.map { replaceTitlePlaceholders(player, title, it) })
                colored()
            }) {
                purchase(player, title)
            }
            set(ConfigUtils.confirmNoSlot, buildItem(ConfigUtils.confirmNoType) {
                name = replaceTitlePlaceholders(player, title, ConfigUtils.confirmNoName)
                lore.addAll(ConfigUtils.confirmNoLore.map { replaceTitlePlaceholders(player, title, it) })
                colored()
            }) {
                player.closeInventory()
                player.sendLang("purchase-confirm-cancelled")
            }
        }
    }

    private fun purchase(player: Player, title: TitleData) {
        val result = EconomyManager.purchase(player, title.uid, PurchaseSource.GUI)
        EconomyManager.sendResult(player, result, title)
        if (result == PurchaseResult.SUCCESS) {
            player.closeInventory()
        }
    }

    private fun buildTitleItem(player: Player, title: TitleData, type: GuiType, uuid: UUID?): org.bukkit.inventory.ItemStack {
        if (type == TITLE_COLLECTION && !FreeSwitchTitleAPI.hasTitle(player, title.uid)) {
            return buildItem(ConfigUtils.lockedCollectionType) {
                name = renderCollectionLockText(player, player.uniqueId, title, ConfigUtils.lockedCollectionName)
                lore.addAll(ConfigUtils.lockedCollectionLore.map { renderCollectionLockText(player, player.uniqueId, title, it) })
                colored()
            }
        }
        return title.buildDisplayItem(extraLore(player, title, type, uuid))
    }

    private fun extraLore(player: Player, title: TitleData, type: GuiType, uuid: UUID?): List<String> {
        val owned = FreeSwitchTitleAPI.hasTitle(player, title.uid)
        val using = player.getCurrentTitle()?.uid == title.uid
        return when (type) {
            PLAYER_LIST -> if (using) {
                renderLore(player, player.uniqueId, title, "player-list.using", listOf(ConfigUtils.statusUsing))
            } else {
                renderLore(player, player.uniqueId, title, "player-list.equip", listOf(ConfigUtils.statusClickEquip))
            }
            TITLE_LIST -> if (owned) {
                renderLore(player, player.uniqueId, title, "title-list.owned", listOf(ConfigUtils.statusOwned))
            } else {
                renderLore(player, player.uniqueId, title, "title-list.not-owned", listOf(ConfigUtils.statusNotOwned))
            }
            LOOK_PLAYER -> {
                val target = uuid ?: player.uniqueId
                val targetOwned = FreeSwitchTitleAPI.hasTitle(target, title.uid)
                if (targetOwned) {
                    renderLore(player, target, title, "look-player.owned", listOf(ConfigUtils.statusOwned))
                } else {
                    renderLore(player, target, title, "look-player.not-owned", listOf(ConfigUtils.statusNotOwned))
                }
            }
            TITLE_SHOP -> {
                val lore = when {
                    owned -> renderLore(player, player.uniqueId, title, "shop.owned", listOf(ConfigUtils.statusOwned))
                    title.shopPermission.isNotBlank() && !player.hasPermission(title.shopPermission) ->
                        renderLore(player, player.uniqueId, title, "shop.no-permission", listOf(ConfigUtils.statusNoPermission))
                    else -> renderLore(player, player.uniqueId, title, "shop.buy", listOf(ConfigUtils.statusClickBuy))
                }
                lore + when (title.shopCurrency) {
                    CurrencyType.VAULT -> renderLore(player, player.uniqueId, title, "shop.vault-price", listOf(ConfigUtils.statusVaultPrice), title.vaultPrice)
                    CurrencyType.PLAYER_POINTS -> renderLore(player, player.uniqueId, title, "shop.points-price", listOf(ConfigUtils.statusPointsPrice), title.pointsPrice)
                    CurrencyType.BOTH -> {
                        renderLore(player, player.uniqueId, title, "shop.vault-price", listOf(ConfigUtils.statusVaultPrice), title.vaultPrice) +
                            renderLore(player, player.uniqueId, title, "shop.points-price", listOf(ConfigUtils.statusPointsPrice), title.pointsPrice)
                    }
                    CurrencyType.FREE -> renderLore(player, player.uniqueId, title, "shop.free-price", listOf(ConfigUtils.statusFreePrice))
                }
            }
            TITLE_COLLECTION -> if (owned) {
                renderLore(player, player.uniqueId, title, "collection.owned", listOf("&a状态: 已收集"))
            } else {
                renderLore(player, player.uniqueId, title, "collection.not-owned", listOf("&7状态: 未收集"))
            }
        }
    }

    private fun openCategoryMenu(player: Player, type: GuiType, uuid: UUID?) {
        player.openMenu<Chest>(ConfigUtils.categoryMenuTitle.colored()) {
            rows(3)
            val categories = listOf("all") + FreeSwitchTitleAPI.getTitleCategoryList()
            categories.distinct().take(27).forEachIndexed { index, category ->
                val isAll = category.equals("all", ignoreCase = true)
                set(index, buildItem(if (isAll) ConfigUtils.categoryAllType else ConfigUtils.categoryItemType) {
                    name = (if (isAll) ConfigUtils.categoryAllName else ConfigUtils.categoryItemName).replace("{category}", displayCategory(category))
                    lore.addAll((if (isAll) ConfigUtils.categoryAllLore else ConfigUtils.categoryItemLore).map { it.replace("{category}", displayCategory(category)) })
                    colored()
                }) {
                    openTitleListMenu(player, type, uuid, category)
                }
            }
        }
    }

    private fun renderCategoryLore(player: Player, category: String?): List<String> {
        return ConfigUtils.categoryLore
            .map { it.replace("{category}", displayCategory(category)) }
            .replacePlaceholder(player)
            .colored()
    }

    private fun displayCategory(category: String?): String {
        val normalized = category?.trim().orEmpty()
        return if (normalized.isBlank() || normalized.equals("all", ignoreCase = true) || normalized == "*") "全部" else normalized
    }

    private fun renderCollectionLockText(player: Player, owner: UUID, title: TitleData, line: String): String {
        return line
            .replace("{uid}", title.uid)
            .replace("{category}", title.category)
            .replace("{rarity}", title.rarity.name.lowercase())
            .replace("{rarity_name}", title.rarity.displayName)
            .replace("{rarity_color}", title.rarity.color)
            .replace("{duration}", title.durationText)
            .replace("{collected}", FreeSwitchTitleAPI.getPlayerCollectionCount(owner).toString())
            .replace("{total}", FreeSwitchTitleAPI.getCollectionTotal().toString())
            .replace("{progress}", FreeSwitchTitleAPI.getPlayerCollectionProgress(owner))
            .replacePlaceholder(player)
            .colored()
    }

    private fun replaceTitlePlaceholders(player: Player, title: TitleData, line: String): String {
        return line
            .replace("{uid}", title.uid)
            .replace("{title}", title.title)
            .replace("{category}", title.category)
            .replace("{rarity}", title.rarity.name.lowercase())
            .replace("{rarity_name}", title.rarity.displayName)
            .replace("{rarity_color}", title.rarity.color)
            .replace("{duration}", title.durationText)
            .replace("{expire}", TitleUtils.getTitleExpireText(player.uniqueId, title))
            .replace("{price}", formatPrice(title))
            .replace("{vault_price}", title.vaultPrice.toString())
            .replace("{points_price}", title.pointsPrice.toString())
            .replace("{particle}", title.particleEffect.preset.ifBlank { if (title.particleEffect.enabled) title.particleEffect.particle else "无" })
            .replace("{potion}", title.buffEffect.potionPreset.ifBlank { "无" })
            .replace("{attribute}", title.buffEffect.attributePreset.ifBlank { "无" })
            .replacePlaceholder(player)
            .colored()
    }

    private fun formatPrice(title: TitleData): String {
        return when (title.shopCurrency) {
            CurrencyType.VAULT -> "金币 ${title.vaultPrice}"
            CurrencyType.PLAYER_POINTS -> "点券 ${title.pointsPrice}"
            CurrencyType.BOTH -> "金币 ${title.vaultPrice} + 点券 ${title.pointsPrice}"
            CurrencyType.FREE -> "免费"
        }
    }

    private fun renderLore(
        player: Player,
        owner: UUID,
        title: TitleData,
        path: String,
        fallback: List<String>,
        vararg args: Any
    ): List<String> {
        return ConfigUtils.getGuiLoreTemplate(path, fallback)
            .map { line ->
                line.replaceWithOrder(*args)
                    .replace("{uid}", title.uid)
                    .replace("{title}", title.title)
                    .replace("{category}", title.category)
                    .replace("{rarity}", title.rarity.name.lowercase())
                    .replace("{rarity_name}", title.rarity.displayName)
                    .replace("{rarity_color}", title.rarity.color)
                    .replace("{duration}", title.durationText)
                    .replace("{expire}", TitleUtils.getTitleExpireText(owner, title))
                    .replace("{vault_price}", title.vaultPrice.toString())
                    .replace("{points_price}", title.pointsPrice.toString())
                    .replace("{collected}", FreeSwitchTitleAPI.getPlayerCollectionCount(owner).toString())
                    .replace("{total}", FreeSwitchTitleAPI.getCollectionTotal().toString())
                    .replace("{progress}", FreeSwitchTitleAPI.getPlayerCollectionProgress(owner))
            }
            .replacePlaceholder(player)
            .colored()
    }
}
