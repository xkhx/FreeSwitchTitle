package top.zoyn.freeswitchtitle.data

import java.util.Locale

data class TitleParticleEffect(
    val preset: String = "",
    val enabled: Boolean = false,
    val particle: String = "END_ROD",
    val shape: TitleParticleShape = TitleParticleShape.HALO,
    val intervalTicks: Long = 10L,
    val count: Int = 1,
    val radius: Double = 0.8,
    val height: Double = 0.0,
    val points: Int = 24,
    val speed: Double = 0.0,
    val offsetX: Double = 0.0,
    val offsetY: Double = 0.0,
    val offsetZ: Double = 0.0,
)

enum class TitleParticleShape {
    HALO,
    RING,
    AURA,
    SPIRAL,
    TRAIL,
    WINGS;

    companion object {
        fun match(value: String?): TitleParticleShape {
            val raw = value?.trim()?.uppercase(Locale.getDefault()).orEmpty()
            return entries.firstOrNull { it.name == raw } ?: HALO
        }
    }
}
