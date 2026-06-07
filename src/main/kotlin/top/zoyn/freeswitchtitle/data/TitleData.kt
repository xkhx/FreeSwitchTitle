package top.zoyn.freeswitchtitle.data

import org.bukkit.inventory.ItemStack
import taboolib.library.xseries.XMaterial
import taboolib.platform.util.buildItem
import top.zoyn.freeswitchtitle.hook.economy.CurrencyType
import top.zoyn.freeswitchtitle.util.TitleDurationUtils

data class TitleData(
    val uid: String,
    val title: String,
    val material: XMaterial,
    val lore: List<String>,
    val joinMessage: String,
    val category: String,
    val rarity: TitleRarity,
    val hidden: Boolean,
    val durationMillis: Long,
    val shopEnable: Boolean,
    val shopAvailableFrom: Long?,
    val shopAvailableUntil: Long?,
    val shopAvailableFromRaw: String,
    val shopAvailableUntilRaw: String,
    val shopCurrency: CurrencyType,
    val vaultPrice: Double,
    val pointsPrice: Int,
    val shopPermission: String,
    val requiredPermissions: List<String>,
    val permissions: List<String>,
    val particleEffect: TitleParticleEffect,
    val buffEffect: TitleBuffEffect,
    val displayEffect: TitleDisplayEffect,
    val equipActions: List<String>,
    val unequipActions: List<String>,
    val buyActions: List<String>,
    val expireActions: List<String>,
    val obtainActions: List<String>,
    val removeActions: List<String>,
    val resetActions: List<String>,
) {
    val durationText: String
        get() = TitleDurationUtils.format(durationMillis)

    fun isShopAvailableNow(now: Long = System.currentTimeMillis()): Boolean {
        return (shopAvailableFrom == null || now >= shopAvailableFrom) &&
            (shopAvailableUntil == null || now <= shopAvailableUntil)
    }

    fun buildDisplayItem(extraLore: List<String> = emptyList()): ItemStack {
        return buildItem(material) {
            name = title
            lore.addAll(this@TitleData.lore)
            lore.addAll(extraLore)
            colored()
        }
    }

    val item: ItemStack by lazy {
        buildDisplayItem()
    }
}
