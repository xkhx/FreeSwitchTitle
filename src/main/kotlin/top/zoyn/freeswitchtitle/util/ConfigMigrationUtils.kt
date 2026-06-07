package top.zoyn.freeswitchtitle.util

import taboolib.common.platform.function.getDataFolder
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type
import taboolib.platform.util.bukkitPlugin
import top.zoyn.freeswitchtitle.FreeSwitchTitle
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ConfigMigrationUtils {

    private const val CURRENT_CONFIG_VERSION = 2
    private val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ROOT)

    fun migrateAll() {
        migrateConfig()
        migrateGui()
        migrateLang()
        migrateParticles()
        migrateEffects()
    }

    private fun migrateConfig() {
        var changed = false
        changed = setIfMissing(FreeSwitchTitle.config, "config-version", CURRENT_CONFIG_VERSION) || changed
        changed = setIfMissing(FreeSwitchTitle.config, "shop.confirm-purchase", true) || changed
        changed = setIfMissing(FreeSwitchTitle.config, "preview.enable", true) || changed
        changed = setIfMissing(FreeSwitchTitle.config, "preview.duration", "10s") || changed
        changed = setIfMissing(FreeSwitchTitle.config, "preview.cooldown", "5s") || changed
        changed = setIfMissing(FreeSwitchTitle.config, "display.enable", true) || changed
        changed = setIfMissing(FreeSwitchTitle.config, "display.update-interval", 2) || changed
        changed = setIfMissing(FreeSwitchTitle.config, "display.default-y-offset", 2.55) || changed
        changed = setIfMissing(FreeSwitchTitle.config, "display.default-scale", 1.0) || changed
        changed = setIfMissing(FreeSwitchTitle.config, "display.default-shadow", false) || changed
        changed = setIfMissing(FreeSwitchTitle.config, "display.default-see-through", false) || changed
        changed = setIfMissing(FreeSwitchTitle.config, "display.resource-pack.enable", true) || changed
        changed = setIfMissing(FreeSwitchTitle.config, "display.resource-pack.image-folder", "images") || changed
        changed = setIfMissing(FreeSwitchTitle.config, "display.resource-pack.output-folder", "resourcepack") || changed
        changed = setIfMissing(FreeSwitchTitle.config, "display.resource-pack.namespace", "freeswitchtitle") || changed
        changed = setIfMissing(FreeSwitchTitle.config, "display.resource-pack.start-codepoint", "0xE001") || changed
        changed = setIfMissing(FreeSwitchTitle.config, "display.resource-pack.default-height", 16) || changed
        changed = setIfMissing(FreeSwitchTitle.config, "display.resource-pack.default-ascent", 8) || changed
        if (FreeSwitchTitle.config.getInt("config-version", 1) < CURRENT_CONFIG_VERSION) {
            FreeSwitchTitle.config["config-version"] = CURRENT_CONFIG_VERSION
            changed = true
        }
        saveIfChanged("config.yml", FreeSwitchTitle.config, changed)
    }

    private fun migrateGui() {
        val config = FreeSwitchTitle.guiConfig
        var changed = false
        changed = setIfMissing(config, "gui.title.category-title", "选择称号分类") || changed
        changed = setIfMissing(config, "gui.category.type", "COMPASS") || changed
        changed = setIfMissing(config, "gui.category.slot", "C") || changed
        changed = setIfMissing(config, "gui.category.name", "&f分类筛选") || changed
        changed = setIfMissing(config, "gui.category.lore", listOf("&7当前分类: &f{category}", "&a点击切换分类")) || changed
        changed = setIfMissing(config, "gui.category-menu.all.type", "BOOK") || changed
        changed = setIfMissing(config, "gui.category-menu.all.name", "&a全部分类") || changed
        changed = setIfMissing(config, "gui.category-menu.all.lore", listOf("&7查看全部称号")) || changed
        changed = setIfMissing(config, "gui.category-menu.item.type", "PAPER") || changed
        changed = setIfMissing(config, "gui.category-menu.item.name", "&f{category}") || changed
        changed = setIfMissing(config, "gui.category-menu.item.lore", listOf("&7点击查看该分类")) || changed
        changed = setIfMissing(config, "gui.locked-collection.type", "GRAY_DYE") || changed
        changed = setIfMissing(config, "gui.locked-collection.name", "&8???") || changed
        changed = setIfMissing(config, "gui.locked-collection.lore", listOf("&7尚未解锁该称号", "&7稀有度: {rarity_color}{rarity_name}", "&7收藏进度: &f{collected}/{total} &7({progress})")) || changed
        changed = setIfMissing(config, "gui.confirm.title", "确认购买 {title}") || changed
        changed = setIfMissing(config, "gui.confirm.rows", 3) || changed
        changed = setIfMissing(config, "gui.confirm.info.type", "BOOK") || changed
        changed = setIfMissing(config, "gui.confirm.info.slot", 13) || changed
        changed = setIfMissing(config, "gui.confirm.info.name", "&f{title}") || changed
        changed = setIfMissing(config, "gui.confirm.info.lore", listOf("&7价格: &f{price}", "&7期限: &f{duration}", "&7稀有度: {rarity_color}{rarity_name}")) || changed
        changed = setIfMissing(config, "gui.confirm.yes.type", "LIME_STAINED_GLASS_PANE") || changed
        changed = setIfMissing(config, "gui.confirm.yes.slot", 11) || changed
        changed = setIfMissing(config, "gui.confirm.yes.name", "&a确认购买") || changed
        changed = setIfMissing(config, "gui.confirm.yes.lore", listOf("&7点击确认购买")) || changed
        changed = setIfMissing(config, "gui.confirm.no.type", "RED_STAINED_GLASS_PANE") || changed
        changed = setIfMissing(config, "gui.confirm.no.slot", 15) || changed
        changed = setIfMissing(config, "gui.confirm.no.name", "&c取消") || changed
        changed = setIfMissing(config, "gui.confirm.no.lore", listOf("&7点击取消购买")) || changed
        changed = appendListIfMissing(config, "gui.lore.shop.buy", "&7预览命令: &f/fst preview {uid}") || changed
        saveIfChanged("gui.yml", config, changed)
    }

    private fun migrateLang() {
        val langFile = File(getDataFolder(), "lang/zh_CN.yml")
        if (!langFile.exists()) return
        val config = Configuration.loadFromFile(langFile, Type.YAML)
        var changed = false
        val defaults = mapOf(
            "command-description-preview" to "预览称号粒子效果",
            "show-title-line-particle" to "&7粒子效果: &f{0}",
            "show-title-line-buff" to "&7增益效果: &f{0}",
            "preview-start" to "&a正在预览称号 &f{0}&a，持续 &f{1}&a。",
            "preview-end" to "&7称号预览已结束。",
            "preview-disabled" to "&c称号预览功能未启用。",
            "preview-title-not-found" to "&c预览失败，称号不存在。",
            "preview-cooling" to "&c预览冷却中，请等待 &f{0}&c。",
            "preview-no-effect" to "&e称号 &f{0} &e没有可预览的粒子效果。",
            "purchase-confirm-cancelled" to "&7已取消购买。",
            "collection-title-locked" to "&e该称号尚未解锁。",
            "validate-header" to "&8&m                  &r &b配置检查 &7称号 {0} / 分类 {1} / 错误 {2} / 警告 {3} &8&m                  ",
            "validate-error" to "&c[错误] &f{0}",
            "validate-warning" to "&e[警告] &f{0}",
            "validate-success" to "&a未发现配置问题。",
            "validate-footer" to "&8&m                                                     ",
        )
        defaults.forEach { (path, value) ->
            changed = setIfMissing(config, path, value) || changed
        }
        saveIfChanged("lang/zh_CN.yml", config, changed)
    }

    private fun migrateParticles() {
        ensureExtraConfigFile("particles.yml")
        val config = FreeSwitchTitle.particleConfig
        var changed = false
        changed = setParticlePreset(config, "halo", "END_ROD", "HALO", 10, 1, 0.8, 0.0, 24, 0.0) || changed
        changed = setParticlePreset(config, "flame-spiral", "FLAME", "SPIRAL", 5, 1, 0.7, 0.0, 18, 0.0) || changed
        changed = setParticlePreset(config, "cloud-trail", "CLOUD", "TRAIL", 5, 1, 0.25, 0.0, 8, 0.0) || changed
        changed = setParticlePreset(config, "angel-wings", "END_ROD", "WINGS", 4, 1, 1.35, 0.0, 22, 0.0) || changed
        saveIfChanged("particles.yml", config, changed)
    }

    private fun migrateEffects() {
        ensureExtraConfigFile("effects.yml")
        val config = FreeSwitchTitle.effectConfig
        var changed = false
        changed = setPotionPreset(config, "speed", "SPEED", 1) || changed
        changed = setPotionPreset(config, "night-vision", "NIGHT_VISION", 0) || changed
        changed = setPotionPreset(config, "fire-immune", "FIRE_RESISTANCE", 0) || changed
        changed = setAttributePreset(config, "warrior", "GENERIC_MAX_HEALTH", 4.0, "ADD_NUMBER") || changed
        changed = setAttributePreset(config, "warrior", "GENERIC_ATTACK_DAMAGE", 2.0, "ADD_NUMBER") || changed
        changed = setAttributePreset(config, "swift", "GENERIC_MOVEMENT_SPEED", 0.05, "ADD_SCALAR") || changed
        saveIfChanged("effects.yml", config, changed)
    }

    private fun setParticlePreset(
        config: Configuration,
        id: String,
        type: String,
        shape: String,
        interval: Int,
        count: Int,
        radius: Double,
        height: Double,
        points: Int,
        speed: Double
    ): Boolean {
        val path = "particles.$id"
        var changed = false
        changed = setIfMissing(config, "$path.enable", true) || changed
        changed = setIfMissing(config, "$path.type", type) || changed
        changed = setIfMissing(config, "$path.shape", shape) || changed
        changed = setIfMissing(config, "$path.interval", interval) || changed
        changed = setIfMissing(config, "$path.count", count) || changed
        changed = setIfMissing(config, "$path.radius", radius) || changed
        changed = setIfMissing(config, "$path.height", height) || changed
        changed = setIfMissing(config, "$path.points", points) || changed
        changed = setIfMissing(config, "$path.speed", speed) || changed
        changed = setIfMissing(config, "$path.offset.x", 0.0) || changed
        changed = setIfMissing(config, "$path.offset.y", 0.0) || changed
        changed = setIfMissing(config, "$path.offset.z", 0.0) || changed
        return changed
    }

    private fun setPotionPreset(config: Configuration, id: String, type: String, amplifier: Int): Boolean {
        val path = "potions.$id.effects.$type"
        var changed = false
        changed = setIfMissing(config, "$path.amplifier", amplifier) || changed
        changed = setIfMissing(config, "$path.ambient", true) || changed
        changed = setIfMissing(config, "$path.particles", false) || changed
        changed = setIfMissing(config, "$path.icon", true) || changed
        return changed
    }

    private fun setAttributePreset(config: Configuration, id: String, attribute: String, amount: Double, operation: String): Boolean {
        val path = "attributes.$id.$attribute"
        var changed = false
        changed = setIfMissing(config, "$path.amount", amount) || changed
        changed = setIfMissing(config, "$path.operation", operation) || changed
        return changed
    }

    private fun ensureExtraConfigFile(name: String) {
        val file = File(getDataFolder(), name)
        if (file.exists()) return
        file.parentFile?.mkdirs()
        bukkitPlugin.getResource(name)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }
        FreeSwitchTitle.sendConsoleMessage("§a[FreeSwitchTitle] 已生成缺失配置文件: $name")
    }

    private fun setIfMissing(config: Configuration, path: String, value: Any): Boolean {
        if (config.contains(path)) return false
        config[path] = value
        return true
    }

    private fun appendListIfMissing(config: Configuration, path: String, value: String): Boolean {
        val list = config.getStringList(path).toMutableList()
        if (list.contains(value)) return false
        list += value
        config[path] = list
        return true
    }

    private fun saveIfChanged(name: String, config: Configuration, changed: Boolean) {
        if (!changed) return
        val file = config.file ?: File(getDataFolder(), name)
        backup(file)
        config.saveToFile(file)
        FreeSwitchTitle.sendConsoleMessage("§a[FreeSwitchTitle] 已补齐配置节点: $name")
    }

    private fun backup(file: File) {
        if (!file.exists()) return
        val backup = File(file.parentFile, "${file.name}.bak_migrate_${dateFormat.format(Date())}")
        file.copyTo(backup, overwrite = false)
    }
}
