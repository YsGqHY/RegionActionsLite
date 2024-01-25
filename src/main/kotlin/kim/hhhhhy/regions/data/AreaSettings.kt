package kim.hhhhhy.regions.data

import kim.hhhhhy.regions.data.area.AreaActions
import kim.hhhhhy.regions.data.area.AreaPosition
import kim.hhhhhy.regions.data.area.AreaType
import kim.hhhhhy.regions.data.area.AreaType.*
import kim.hhhhhy.regions.utils.evalKether
import org.bukkit.Location
import org.bukkit.entity.Player
import taboolib.common.platform.function.console
import taboolib.common.platform.function.info
import taboolib.common5.mirrorNow
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.module.lang.sendInfo
import java.util.concurrent.ConcurrentHashMap


data class AreaSettings(
    val position: AreaPosition,
    val actions: AreaActions
) {
    companion object {
        @Config("areas.yml", autoReload = true)
        lateinit var areasConfig: Configuration
            private set

        private val areasData = ConcurrentHashMap<String, AreaSettings>()

        fun reloadArea() {
            areasData.clear()
            val section = areasConfig.getConfigurationSection("Areas") ?: return
            section.getKeys(false).forEach { id ->
                val root = section.getConfigurationSection(id)!!
                val position = AreaPosition(root.getConfigurationSection("position") ?: error("缺少坐标设置"))
                val actions = AreaActions(root.getConfigurationSection("actions"))
                areasData[id] = AreaSettings(position, actions)
            }
            console().sendInfo("plugin-areas-reload")
        }

        fun getAreas(location: Location): List<String> {
            val x = location.x
            val y = location.y
            val z = location.z
            /**
             * xMax=-500.0, yMax=0.0, zMax=340.0, xMin=-510.0, yMin=110.0, zMin=350.0)
             */
            return areasData.filter {
                it.value.position.world == location.world?.name
                    && (x in it.value.position.xMax .. it.value.position.xMin || x in it.value.position.xMin .. it.value.position.xMax)
                    && (y in it.value.position.yMax .. it.value.position.yMin || y in it.value.position.yMin .. it.value.position.yMax)
                    && (z in it.value.position.zMax .. it.value.position.zMin || z in it.value.position.zMin .. it.value.position.zMax)
            }.map { it.key }

        }

        private fun runEnterAction(player: Player, id: String) {
            mirrorNow("RegionActionsLite:Actions:Enter") {
                val actions = areasData[id]!!.actions.enter
                if (ConfigSettings.baffleCache.hasNext("${player.name}-Enter-$id").not()) {
                    return@mirrorNow
                }
                actions?.evalKether(player)
            }
        }
        private fun runLeaveAction(player: Player, id: String) {
            mirrorNow("RegionActionsLite:Actions:Leave") {
                val actions = areasData[id]!!.actions.leave
                if (ConfigSettings.baffleCache.hasNext("${player.name}-Leave-$id").not()) {
                    return@mirrorNow
                }
                actions?.evalKether(player)
            }
        }
        private fun runTickAction(player: Player, id: String) {
            mirrorNow("RegionActionsLite:Actions:Tick") {
                val actions = areasData[id]!!.actions.tick
                if (ConfigSettings.baffleCache.hasNext("${player.name}-Tick-$id").not()) {
                    return@mirrorNow
                }
                actions?.evalKether(player)
            }
        }

        /**
         * 执行一次区域类型动作
         */
        fun runActions(player: Player, id: String, type: AreaType) {
            when (type) {
                ENTER -> {
                    runEnterAction(player, id)
                }
                LEAVE -> {
                    runLeaveAction(player, id)
                }
                TICK -> {
                    runTickAction(player, id)
                }
                ALL -> {
                    runEnterAction(player, id)
                    runLeaveAction(player, id)
                    runTickAction(player, id)
                }
            }
        }
    }
}
