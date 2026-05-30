package top.zoyn.freeswitchtitle.data

import org.bukkit.inventory.ItemStack
import taboolib.library.xseries.XMaterial
import taboolib.platform.util.buildItem

data class TitleData(
    val uid: String,
    val title: String,
    val material: XMaterial,
    val lore: List<String>,
    val joinMessage: String,
    val shopEnable: Boolean,
    val vaultPrice: Double,
    val pointsPrice: Int,
    val shopPermission: String,
    val permissions: List<String>,
    val equipActions: List<String>,
    val unequipActions: List<String>,
    val buyActions: List<String>,
) {
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
