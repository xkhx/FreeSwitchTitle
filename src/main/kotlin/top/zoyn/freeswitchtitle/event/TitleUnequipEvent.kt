package top.zoyn.freeswitchtitle.event

import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import top.zoyn.freeswitchtitle.data.TitleData

class TitleUnequipEvent(
    val player: Player,
    val title: TitleData,
    val reason: String
) : Event() {

    override fun getHandlers(): HandlerList = HANDLERS

    companion object {
        @JvmStatic
        private val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = HANDLERS
    }
}
