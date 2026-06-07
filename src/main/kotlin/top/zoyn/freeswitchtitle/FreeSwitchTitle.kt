package top.zoyn.freeswitchtitle

import org.bukkit.ChatColor
import taboolib.common.platform.Platform
import taboolib.common.platform.Plugin
import taboolib.common.platform.function.disablePlugin
import taboolib.common.platform.function.pluginVersion
import taboolib.expansion.setupPlayerDatabase
import taboolib.module.configuration.Config
import taboolib.module.configuration.ConfigFile
import taboolib.module.metrics.Metrics
import taboolib.platform.util.bukkitPlugin
import top.zoyn.freeswitchtitle.util.ConfigMigrationUtils
import top.zoyn.freeswitchtitle.util.TitleBuffManager
import top.zoyn.freeswitchtitle.util.TitleParticleManager
import top.zoyn.freeswitchtitle.util.TitlePreviewManager
import top.zoyn.freeswitchtitle.util.TitleUtils
import top.zoyn.freeswitchtitle.util.getCurrentTitle

object FreeSwitchTitle : Plugin() {

    private var titleExpiryTaskId = -1
    private var titleBuffRefreshTaskId = -1

    @Config("config.yml")
    lateinit var config: ConfigFile

    @Config("gui.yml")
    lateinit var guiConfig: ConfigFile

    @Config("particles.yml")
    lateinit var particleConfig: ConfigFile

    @Config("effects.yml")
    lateinit var effectConfig: ConfigFile

    override fun onEnable() {
        sendConsoleMessage("${ChatColor.GREEN}> ${ChatColor.GOLD}FreeSwitchTitle 启动中...")
        loadPlayerData()
        reload()
        startTitleExpiryTask()
        startTitleBuffRefreshTask()
        Metrics(1259, pluginVersion, Platform.BUKKIT)
        sendConsoleMessage("${ChatColor.GREEN}> 作者: ${ChatColor.WHITE}星空 ${ChatColor.GREEN}| 版本: ${ChatColor.WHITE}$pluginVersion")
        sendConsoleMessage("${ChatColor.GREEN}> ${ChatColor.GOLD}FreeSwitchTitle 启动成功")
    }

    override fun onDisable() {
        TitlePreviewManager.stopAll()
        TitleBuffManager.clearAll()
        TitleParticleManager.stopAll()
        if (titleExpiryTaskId != -1) {
            bukkitPlugin.server.scheduler.cancelTask(titleExpiryTaskId)
            titleExpiryTaskId = -1
        }
        if (titleBuffRefreshTaskId != -1) {
            bukkitPlugin.server.scheduler.cancelTask(titleBuffRefreshTaskId)
            titleBuffRefreshTaskId = -1
        }
    }

    fun reload() {
        reloadConfigFiles()
        ConfigMigrationUtils.migrateAll()
        reloadConfigFiles()
        TitleUtils.loadTitleData()
        refreshOnlineTitleEffects()
    }

    private fun reloadConfigFiles() {
        config.reload()
        guiConfig.reload()
        particleConfig.reload()
        effectConfig.reload()
    }

    fun sendConsoleMessage(message: String) {
        bukkitPlugin.server.consoleSender.sendMessage(message)
    }

    private fun refreshOnlineTitleEffects() {
        TitlePreviewManager.stopAll()
        bukkitPlugin.server.onlinePlayers.forEach { player ->
            TitleBuffManager.clear(player)
            TitleParticleManager.stop(player)
            player.getCurrentTitle()?.let { title ->
                TitleBuffManager.apply(player, title)
                TitleParticleManager.start(player, title)
            }
        }
    }

    private fun refreshOnlineTitleBuffs() {
        bukkitPlugin.server.onlinePlayers.forEach { player ->
            player.getCurrentTitle()
                ?.takeIf { it.buffEffect.enabled }
                ?.let { TitleBuffManager.apply(player, it) }
        }
    }

    private fun loadPlayerData() {
        runCatching {
            if (config.getBoolean("sql.enable")) {
                setupPlayerDatabase(config.getConfigurationSection("sql")!!)
            } else {
                setupPlayerDatabase()
            }
        }.getOrElse {
            sendConsoleMessage("${ChatColor.GREEN}> ${ChatColor.RED}数据加载失败!")
            it.printStackTrace()
            disablePlugin()
            return
        }
        val type = if (config.getBoolean("sql.enable")) "MySQL" else "SQLite"
        sendConsoleMessage("${ChatColor.GREEN}> ${ChatColor.RESET}$type ${ChatColor.AQUA}加载完成")
    }

    private fun startTitleExpiryTask() {
        if (titleExpiryTaskId != -1) {
            bukkitPlugin.server.scheduler.cancelTask(titleExpiryTaskId)
        }
        titleExpiryTaskId = bukkitPlugin.server.scheduler
            .runTaskTimer(bukkitPlugin, Runnable { TitleUtils.cleanupOnlinePlayers() }, 20L * 60L, 20L * 60L * 10L)
            .taskId
    }

    private fun startTitleBuffRefreshTask() {
        if (titleBuffRefreshTaskId != -1) {
            bukkitPlugin.server.scheduler.cancelTask(titleBuffRefreshTaskId)
        }
        titleBuffRefreshTaskId = bukkitPlugin.server.scheduler
            .runTaskTimer(bukkitPlugin, Runnable { refreshOnlineTitleBuffs() }, 20L * 60L * 5L, 20L * 60L * 5L)
            .taskId
    }
}
