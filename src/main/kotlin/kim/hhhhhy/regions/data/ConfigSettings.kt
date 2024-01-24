package kim.hhhhhy.regions.data

import kim.hhhhhy.regions.data.area.AreaType
import kim.hhhhhy.regions.listeners.AreaListener
import org.bukkit.Bukkit
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.console
import taboolib.common.platform.function.submitAsync
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

    @Awake(LifeCycle.ACTIVE)
    fun runTickAction() {
        submitAsync(now = false, period = actionTick) {
            AreaListener.playerSet.forEach { (player, id) ->
                Bukkit.getPlayerExact(player)?.let { p ->
                    AreaSettings.runActions(p, id, AreaType.TICK)
                }
            }
        }
    }

}
