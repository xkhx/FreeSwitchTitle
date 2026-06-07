package top.zoyn.freeswitchtitle.util

import org.bukkit.entity.Display
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.bukkit.scheduler.BukkitTask
import taboolib.module.chat.colored
import taboolib.platform.util.bukkitPlugin
import top.zoyn.freeswitchtitle.FreeSwitchTitle
import top.zoyn.freeswitchtitle.data.TitleData
import java.util.UUID

object TitleDisplayManager {

    private data class DisplayState(
        val titleUid: String,
        val entity: TextDisplay,
        val task: BukkitTask,
    )

    private val displays = mutableMapOf<UUID, DisplayState>()

    fun start(player: Player, title: TitleData) {
        stop(player.uniqueId)
        val effect = title.displayEffect
        if (!effect.enabled || effect.text.isBlank()) return
        val location = player.location.add(0.0, effect.yOffset, 0.0)
        val entity = player.world.spawn(location, TextDisplay::class.java) { display ->
            display.text = effect.text.colored()
            display.billboard = Display.Billboard.CENTER
            display.isShadowed = effect.shadow
            display.isSeeThrough = effect.seeThrough
            display.isPersistent = false
            display.setGravity(false)
            display.isInvulnerable = true
            applyScale(display, effect.scale)
        }
        val task = bukkitPlugin.server.scheduler.runTaskTimer(
            bukkitPlugin,
            Runnable {
                if (!player.isOnline || TitleUtils.getUsing(player.uniqueId) != title.uid || entity.isDead) {
                    stop(player.uniqueId)
                    return@Runnable
                }
                runCatching {
                    entity.teleport(player.location.add(0.0, effect.yOffset, 0.0))
                }.getOrElse {
                    FreeSwitchTitle.sendConsoleMessage("§e[FreeSwitchTitle] 更新图片称号显示失败: ${player.name} - ${it.message}")
                    stop(player.uniqueId)
                }
            },
            ConfigUtils.displayUpdateIntervalTicks,
            ConfigUtils.displayUpdateIntervalTicks
        )
        displays[player.uniqueId] = DisplayState(title.uid, entity, task)
    }

    private fun applyScale(display: TextDisplay, scale: Float) {
        runCatching {
            val vector3fClass = Class.forName("org.joml.Vector3f")
            val axisAngle4fClass = Class.forName("org.joml.AxisAngle4f")
            val transformationClass = Class.forName("org.bukkit.util.Transformation")
            val vectorConstructor = vector3fClass.getConstructor(
                Float::class.javaPrimitiveType,
                Float::class.javaPrimitiveType,
                Float::class.javaPrimitiveType
            )
            val axisConstructor = axisAngle4fClass.getConstructor(
                Float::class.javaPrimitiveType,
                Float::class.javaPrimitiveType,
                Float::class.javaPrimitiveType,
                Float::class.javaPrimitiveType
            )
            val transformationConstructor = transformationClass.getConstructor(
                vector3fClass,
                axisAngle4fClass,
                vector3fClass,
                axisAngle4fClass
            )
            val safeScale = scale.coerceAtLeast(0.01f)
            val translation = vectorConstructor.newInstance(0.0f, 0.0f, 0.0f)
            val leftRotation = axisConstructor.newInstance(0.0f, 0.0f, 1.0f, 0.0f)
            val scaleVector = vectorConstructor.newInstance(safeScale, safeScale, safeScale)
            val rightRotation = axisConstructor.newInstance(0.0f, 0.0f, 1.0f, 0.0f)
            val transformation = transformationConstructor.newInstance(translation, leftRotation, scaleVector, rightRotation)
            display.javaClass.getMethod("setTransformation", transformationClass).invoke(display, transformation)
        }.getOrElse {
            FreeSwitchTitle.sendConsoleMessage("§e[FreeSwitchTitle] 当前服务端不支持设置图片称号缩放: ${it.message}")
        }
    }

    fun stop(player: Player) {
        stop(player.uniqueId)
    }

    fun stop(uuid: UUID) {
        displays.remove(uuid)?.let { state ->
            state.task.cancel()
            if (!state.entity.isDead) {
                state.entity.remove()
            }
        }
    }

    fun stopAll() {
        displays.keys.toList().forEach { stop(it) }
        displays.clear()
    }
}
