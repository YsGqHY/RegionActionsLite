package kim.hhhhhy.regions.data


import taboolib.common.platform.function.*
import taboolib.common5.Baffle
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.module.lang.sendInfo
import java.util.concurrent.TimeUnit

object ConfigSettings {

    @Config(autoReload = true)
    lateinit var config: Configuration
        private set

    private var cooldown: Long = 3000

    var baffleCache = Baffle.of(cooldown, TimeUnit.MILLISECONDS)

    private var actionTick: Long = 20

    fun reloadConfig() {
        cooldown = config.getLong("CommandBaffle.time", 3000)
        actionTick = config.getLong("AreaSettings.TickAction", 20)
        console().sendInfo("plugin-config-reload")

    }

}
