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
) {
    val item: ItemStack by lazy {
        buildItem(material) {
            name = title
            lore.addAll(this@TitleData.lore)
            colored()
        }
    }
}
