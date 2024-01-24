package kim.hhhhhy.regions

import kim.hhhhhy.regions.data.AreaSettings
import kim.hhhhhy.regions.data.ConfigSettings
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Plugin
import taboolib.common.platform.function.info

object RegionActionsLite : Plugin() {

    override fun onEnable() {
        info("区域命令！")
    }

    fun reload() {
        ConfigSettings.reloadConfig()
        AreaSettings.reloadArea()
    }

    @Awake(LifeCycle.ENABLE)
    fun init() {
        reload()
    }

    @Awake(LifeCycle.ACTIVE)
    fun autoReload() {
        ConfigSettings.config.onReload {
            info("检测到 config.yml 变动")
            ConfigSettings.reloadConfig()
        }

        AreaSettings.areasConfig.onReload {
            AreaSettings.reloadArea()
        }
    }
}