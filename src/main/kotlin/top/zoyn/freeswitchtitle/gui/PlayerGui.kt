package top.zoyn.freeswitchtitle.gui

import org.bukkit.entity.Player
import taboolib.common.util.replaceWithOrder
import taboolib.library.xseries.XMaterial
import taboolib.module.chat.colored
import taboolib.module.ui.openMenu
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
import top.zoyn.freeswitchtitle.gui.type.GuiType.LOOK_PLAYER
import top.zoyn.freeswitchtitle.gui.type.GuiType.PLAYER_LIST
import top.zoyn.freeswitchtitle.gui.type.GuiType.TITLE_LIST
import top.zoyn.freeswitchtitle.gui.type.GuiType.TITLE_SHOP
import top.zoyn.freeswitchtitle.util.ConfigUtils
import top.zoyn.freeswitchtitle.util.getCurrentTitle
import top.zoyn.freeswitchtitle.util.getOwnedTitle
import top.zoyn.freeswitchtitle.util.getPlayerName
import top.zoyn.freeswitchtitle.util.setCurrentTitle
import java.util.UUID

object PlayerGui {

    fun openTitleListMenu(player: Player, type: GuiType, uuid: UUID? = null) {
        player.openMenu<PageableChest<TitleData>> {
            title = when (type) {
                PLAYER_LIST -> ConfigUtils.myTitle.replaceWithOrder(player.name).colored()
                TITLE_LIST -> ConfigUtils.title.colored()
                TITLE_SHOP -> ConfigUtils.shopTitle.colored()
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
            elements {
                elements(player, type, uuid)
            }
            onGenerate { _, title, _, _ ->
                title.buildDisplayItem(extraLore(player, title, type, uuid))
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

    private fun elements(player: Player, type: GuiType, uuid: UUID?): List<TitleData> {
        return when (type) {
            PLAYER_LIST -> player.getOwnedTitle()
            TITLE_LIST -> FreeSwitchTitleAPI.getTitleDataList()
            TITLE_SHOP -> if (ConfigUtils.shopEnable) FreeSwitchTitleAPI.getTitleDataList().filter { it.shopEnable } else emptyList()
            LOOK_PLAYER -> uuid?.getOwnedTitle() ?: emptyList()
        }
    }

    private fun click(player: Player, title: TitleData, type: GuiType) {
        when (type) {
            PLAYER_LIST -> {
                if (player.setCurrentTitle(title.uid)) {
                    player.sendLang("switch-title-success", title.title)
                    player.closeInventory()
                }
            }
            TITLE_LIST, LOOK_PLAYER -> player.sendLang("view-only-title")
            TITLE_SHOP -> {
                val result = EconomyManager.purchase(player, title.uid)
                EconomyManager.sendResult(player, result, title)
                if (result == PurchaseResult.SUCCESS) {
                    player.closeInventory()
                }
            }
        }
    }

    private fun extraLore(player: Player, title: TitleData, type: GuiType, uuid: UUID?): List<String> {
        val lore = mutableListOf<String>()
        val owned = FreeSwitchTitleAPI.hasTitle(player, title.uid)
        val using = player.getCurrentTitle()?.uid == title.uid
        when (type) {
            PLAYER_LIST -> lore += if (using) ConfigUtils.statusUsing else ConfigUtils.statusClickEquip
            TITLE_LIST -> lore += if (owned) ConfigUtils.statusOwned else ConfigUtils.statusNotOwned
            LOOK_PLAYER -> {
                val targetOwned = title.uid in (uuid ?: player.uniqueId).getOwnedTitle().map { it.uid }
                lore += if (targetOwned) ConfigUtils.statusOwned else ConfigUtils.statusNotOwned
            }
            TITLE_SHOP -> {
                lore += when {
                    owned -> ConfigUtils.statusOwned
                    title.shopPermission.isNotBlank() && !player.hasPermission(title.shopPermission) -> ConfigUtils.statusNoPermission
                    else -> ConfigUtils.statusClickBuy
                }
                when (ConfigUtils.shopCurrency) {
                    CurrencyType.VAULT -> lore += ConfigUtils.statusVaultPrice.replaceWithOrder(title.vaultPrice)
                    CurrencyType.PLAYER_POINTS -> lore += ConfigUtils.statusPointsPrice.replaceWithOrder(title.pointsPrice)
                    CurrencyType.BOTH -> {
                        lore += ConfigUtils.statusVaultPrice.replaceWithOrder(title.vaultPrice)
                        lore += ConfigUtils.statusPointsPrice.replaceWithOrder(title.pointsPrice)
                    }
                }
            }
        }
        return lore.colored()
    }
}
