package top.zoyn.freeswitchtitle.data

data class TitleDisplayEffect(
    val enabled: Boolean = false,
    val text: String = "",
    val image: String = "",
    val yOffset: Double = 2.55,
    val scale: Float = 1.0f,
    val shadow: Boolean = false,
    val seeThrough: Boolean = false,
)
