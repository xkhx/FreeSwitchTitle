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
import top.zoyn.freeswitchtitle.util.TitleUtils

object FreeSwitchTitle : Plugin() {

    @Config("config.yml")
    lateinit var config: ConfigFile

    @Config("gui.yml")
    lateinit var guiConfig: ConfigFile

    override fun onEnable() {
        sendConsoleMessage("${ChatColor.GREEN}> ${ChatColor.GOLD}FreeSwitchTitle 启动中...")
        loadPlayerData()
        reload()
        Metrics(1259, pluginVersion, Platform.BUKKIT)
        sendConsoleMessage("${ChatColor.GREEN}> 作者: ${ChatColor.WHITE}星空 ${ChatColor.GREEN}| 版本: ${ChatColor.WHITE}$pluginVersion")
        sendConsoleMessage("${ChatColor.GREEN}> ${ChatColor.GOLD}FreeSwitchTitle 启动成功")
    }

    fun reload() {
        config.reload()
        guiConfig.reload()
        TitleUtils.loadTitleData()
    }

    fun sendConsoleMessage(message: String) {
        bukkitPlugin.server.consoleSender.sendMessage(message)
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
}
