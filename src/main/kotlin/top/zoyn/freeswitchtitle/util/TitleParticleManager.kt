package top.zoyn.freeswitchtitle.util

import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import org.bukkit.scheduler.BukkitTask
import taboolib.platform.util.bukkitPlugin
import top.zoyn.freeswitchtitle.FreeSwitchTitle
import top.zoyn.freeswitchtitle.data.TitleData
import top.zoyn.freeswitchtitle.data.TitleParticleEffect
import top.zoyn.freeswitchtitle.data.TitleParticleShape
import java.util.Locale
import java.util.UUID
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object TitleParticleManager {

    private val tasks = mutableMapOf<UUID, BukkitTask>()
    private val ticks = mutableMapOf<UUID, Int>()
    private val previewTasks = mutableMapOf<UUID, BukkitTask>()
    private val previewTicks = mutableMapOf<UUID, Int>()

    fun start(player: Player, title: TitleData) {
        stop(player.uniqueId)
        val effect = title.particleEffect
        if (!effect.enabled) return
        val particle = parseParticle(effect.particle) ?: run {
            FreeSwitchTitle.sendConsoleMessage("§e[FreeSwitchTitle] 称号 ${title.uid} 的粒子类型无效: ${effect.particle}")
            return
        }
        ticks[player.uniqueId] = 0
        tasks[player.uniqueId] = bukkitPlugin.server.scheduler.runTaskTimer(
            bukkitPlugin,
            Runnable {
                if (!player.isOnline || TitleUtils.getUsing(player.uniqueId) != title.uid) {
                    stop(player.uniqueId)
                    return@Runnable
                }
                val tick = ticks.compute(player.uniqueId) { _, value -> (value ?: 0) + 1 } ?: 1
                spawn(player, effect, particle, tick)
            },
            0L,
            effect.intervalTicks
        )
    }

    fun startPreview(player: Player, title: TitleData, durationTicks: Long, onEnd: () -> Unit = {}) {
        stopPreview(player.uniqueId)
        val effect = title.particleEffect
        if (!effect.enabled) return
        val particle = parseParticle(effect.particle) ?: run {
            FreeSwitchTitle.sendConsoleMessage("§e[FreeSwitchTitle] 称号 ${title.uid} 的预览粒子类型无效: ${effect.particle}")
            return
        }
        var remain = durationTicks.coerceAtLeast(1L)
        previewTicks[player.uniqueId] = 0
        previewTasks[player.uniqueId] = bukkitPlugin.server.scheduler.runTaskTimer(
            bukkitPlugin,
            Runnable {
                if (!player.isOnline || remain <= 0L) {
                    stopPreview(player.uniqueId)
                    onEnd()
                    return@Runnable
                }
                remain -= effect.intervalTicks
                val tick = previewTicks.compute(player.uniqueId) { _, value -> (value ?: 0) + 1 } ?: 1
                spawn(player, effect, particle, tick)
            },
            0L,
            effect.intervalTicks
        )
    }

    fun stopPreview(player: Player) {
        stopPreview(player.uniqueId)
    }

    fun stopPreview(uuid: UUID) {
        previewTasks.remove(uuid)?.cancel()
        previewTicks.remove(uuid)
    }

    fun stop(player: Player) {
        stop(player.uniqueId)
    }

    fun stop(uuid: UUID) {
        tasks.remove(uuid)?.cancel()
        ticks.remove(uuid)
    }

    fun stopAll() {
        tasks.values.forEach { it.cancel() }
        previewTasks.values.forEach { it.cancel() }
        tasks.clear()
        ticks.clear()
        previewTasks.clear()
        previewTicks.clear()
    }

    private fun parseParticle(name: String): Particle? {
        return runCatching { Particle.valueOf(name.trim().uppercase(Locale.getDefault())) }.getOrNull()
    }

    private fun spawn(player: Player, effect: TitleParticleEffect, particle: Particle, tick: Int) {
        when (effect.shape) {
            TitleParticleShape.HALO -> spawnRing(player, effect, particle, player.location.y + 1.85)
            TitleParticleShape.RING -> spawnRing(player, effect, particle, player.location.y + effect.height)
            TitleParticleShape.AURA -> spawnAura(player, effect, particle)
            TitleParticleShape.SPIRAL -> spawnSpiral(player, effect, particle, tick)
            TitleParticleShape.TRAIL -> spawnTrail(player, effect, particle)
            TitleParticleShape.WINGS -> spawnWings(player, effect, particle, tick)
        }
    }

    private fun spawnRing(player: Player, effect: TitleParticleEffect, particle: Particle, y: Double) {
        val base = player.location
        repeat(effect.points) { index ->
            val angle = 2 * PI * index / effect.points
            player.world.spawnParticle(
                particle,
                base.x + cos(angle) * effect.radius,
                y,
                base.z + sin(angle) * effect.radius,
                effect.count,
                effect.offsetX,
                effect.offsetY,
                effect.offsetZ,
                effect.speed
            )
        }
    }

    private fun spawnAura(player: Player, effect: TitleParticleEffect, particle: Particle) {
        val base = player.location.add(0.0, effect.height + 0.9, 0.0)
        player.world.spawnParticle(
            particle,
            base,
            effect.points,
            effect.radius,
            0.8,
            effect.radius,
            effect.speed
        )
    }

    private fun spawnSpiral(player: Player, effect: TitleParticleEffect, particle: Particle, tick: Int) {
        val base = player.location
        repeat(effect.points) { index ->
            val progress = index.toDouble() / effect.points
            val angle = tick * 0.45 + progress * PI * 4
            player.world.spawnParticle(
                particle,
                base.x + cos(angle) * effect.radius,
                base.y + effect.height + progress * 2.0,
                base.z + sin(angle) * effect.radius,
                effect.count,
                effect.offsetX,
                effect.offsetY,
                effect.offsetZ,
                effect.speed
            )
        }
    }

    private fun spawnTrail(player: Player, effect: TitleParticleEffect, particle: Particle) {
        val base = player.location.add(0.0, effect.height + 0.15, 0.0)
        player.world.spawnParticle(
            particle,
            base,
            effect.points,
            effect.radius,
            0.1,
            effect.radius,
            effect.speed
        )
    }

    private fun spawnWings(player: Player, effect: TitleParticleEffect, particle: Particle, tick: Int) {
        val location = player.location
        val forward = location.direction.setY(0).normalizeSafely()
        val right = Vector(-forward.z, 0.0, forward.x).normalizeSafely()
        val back = forward.multiply(-0.45)
        val base = location.toVector().add(back).add(Vector(0.0, 1.15 + effect.height, 0.0))
        val width = effect.radius.coerceAtLeast(0.2)
        val height = (effect.radius * 1.25).coerceAtLeast(0.4)
        val flap = sin(tick * 0.35) * 0.08
        val samples = effect.points.coerceAtLeast(12)
        repeat(samples) { index ->
            val t = index.toDouble() / (samples - 1).coerceAtLeast(1)
            spawnWingPoint(player, particle, effect, base, right, t, 1.0, width, height, flap)
            spawnWingPoint(player, particle, effect, base, right, t, -1.0, width, height, flap)
        }
    }

    private fun spawnWingPoint(
        player: Player,
        particle: Particle,
        effect: TitleParticleEffect,
        base: Vector,
        right: Vector,
        t: Double,
        side: Double,
        width: Double,
        height: Double,
        flap: Double
    ) {
        val spread = sin(t * PI)
        val outerX = side * width * spread
        val outerY = height * (0.95 - t) - 0.25 * t
        val innerX = side * width * 0.42 * spread
        val innerY = height * (0.55 - t) - 0.2 * t
        val featherX = side * width * (0.15 + 0.85 * spread)
        val featherY = height * (0.25 - t * 0.75)
        spawnRelative(player, particle, effect, base, right, outerX + side * flap, outerY)
        spawnRelative(player, particle, effect, base, right, innerX, innerY)
        if (t > 0.25) {
            spawnRelative(player, particle, effect, base, right, featherX, featherY)
        }
    }

    private fun spawnRelative(player: Player, particle: Particle, effect: TitleParticleEffect, base: Vector, right: Vector, x: Double, y: Double) {
        val point = base.clone().add(right.clone().multiply(x)).add(Vector(0.0, y, 0.0))
        player.world.spawnParticle(
            particle,
            point.x,
            point.y,
            point.z,
            effect.count,
            effect.offsetX,
            effect.offsetY,
            effect.offsetZ,
            effect.speed
        )
    }

    private fun Vector.normalizeSafely(): Vector {
        return if (lengthSquared() <= 1.0E-6) Vector(0.0, 0.0, 1.0) else normalize()
    }
}
