package kim.hhhhhy.regions.data

import kim.hhhhhy.regions.data.area.AreaActions
import kim.hhhhhy.regions.data.area.AreaPosition
import kim.hhhhhy.regions.data.area.AreaType
import kim.hhhhhy.regions.data.area.AreaType.*
import kim.hhhhhy.regions.listeners.AreaListener
import kim.hhhhhy.regions.utils.evalKether
import org.bukkit.Location
import org.bukkit.entity.Player
import taboolib.module.navigation.BoundingBox
import taboolib.common.platform.function.console
import taboolib.common.platform.function.info
import taboolib.common.platform.function.submit
import taboolib.common.platform.service.PlatformExecutor
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.module.lang.sendInfo
import taboolib.platform.util.onlinePlayers
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.max
import kotlin.math.min


data class AreaSettings(
    val position: AreaPosition,
    val actions: AreaActions,
    val tickPeriod: Long
) {
    companion object {
        @Config("areas.yml", autoReload = true)
        lateinit var areasConfig: Configuration
            private set

        private val areasData = ConcurrentHashMap<String, AreaSettings>()

        /**
         * player.name to id -> task
         */
        private val playerAreas = ConcurrentHashMap<String, MutableMap<String, PlatformExecutor.PlatformTask>>()

        fun reloadArea() {
            areasData.clear()
            val section = areasConfig.getConfigurationSection("Areas") ?: return
            section.getKeys(false).forEach { id ->
                val root = section.getConfigurationSection(id)!!
                val position = AreaPosition(root.getConfigurationSection("position") ?: error("缺少坐标设置"))
                val actions = AreaActions(root.getConfigurationSection("actions"))
                val tickPeriod = root.getLong("tickPeriod", ConfigSettings.config.getLong("AreaSettings.TickAction", 20))
                areasData[id] = AreaSettings(position, actions, tickPeriod)
            }
            playerAreas.clear()
            onlinePlayers.forEach {
                stopTick(it)
                check(it, it.location)
            }
            console().sendInfo("plugin-areas-reload")
        }

        private fun getAreas(location: Location): List<String> {
            val x = location.blockX
            val y = location.blockY
            val z = location.blockZ
            val worldName = location.world?.name

            return areasData
                .filter { (_, area) ->
                    val pos = area.position
                    val isSameWorld = pos.world == worldName
                    val box = BoundingBox(
                        min(pos.xMin, pos.xMax), min(pos.yMin, pos.yMax), min(pos.zMin, pos.zMax),
                        max(pos.xMin, pos.xMax), max(pos.yMin, pos.yMax), max(pos.zMin, pos.zMax)
                    )
                    isSameWorld && x >= box.minX && x <= box.maxX && y >= box.minY && y <= box.maxY && z >= box.minZ && z <= box.maxZ
                }
                .map { (key, _) -> key }
        }

        private fun runEnterAction(player: Player, id: String) {
            val actions = areasData[id]?.actions?.enter
            if (ConfigSettings.baffleCache.hasNext("${player.name}-Enter-$id").not()) {
                return
            }
            actions?.evalKether(player)
            startTick(player, id)
        }
        private fun runLeaveAction(player: Player, id: String) {
            stopTick(player, id)
            val actions = areasData[id]?.actions?.leave
            if (ConfigSettings.baffleCache.hasNext("${player.name}-Leave-$id").not()) {
                return
            }
            actions?.evalKether(player)
        }
        private fun runTickAction(player: Player, id: String) {
            val actions = areasData[id]?.actions?.tick
            actions?.evalKether(player)
        }

        private fun startTick(player: Player, id: String) {
            val period = areasData[id]!!.tickPeriod
            val tasks = playerAreas.computeIfAbsent(player.name) { ConcurrentHashMap() }
            tasks[id] = submit(period = period) {
                if (playerAreas[player.name]?.contains(id) == true) {
                    runTickAction(player, id)
                } else {
                    return@submit
                }
            }
        }

        fun stopTick(player: Player, id: String? = null) {
            if (id.isNullOrBlank()) {
                playerAreas[player.name]?.forEach { (_, task) ->
                    task.cancel()
                }
                playerAreas.remove(player.name)
                return
            }
            playerAreas[player.name]?.get(id)?.cancel()
            playerAreas[player.name]?.remove(id)
        }

        /**
         * 执行一次区域类型动作
         */
        private fun runActions(player: Player, id: String, type: AreaType) {
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

        fun check(player: Player, location: Location) {
            val areas = getAreas(location)

            if (areas.isNotEmpty()) {

                // 退出某个区域
                val filter = AreaListener.playerSet.filter { it.first == player.name && it !in areas.map { id -> player.name to id }.toSet() }
                if (filter.isNotEmpty()) {
                    filter.forEach { (name, id) ->
                        if (AreaListener.playerSet.remove(name to id)) {
                            runActions(player, id, LEAVE)
                        }
                    }
                }

                // 进入区域
                areas.forEach {
                    if (AreaListener.playerSet.add(player.name to it)) {
                        runActions(player, it, ENTER)
                    }
                }
            } else {

                // 退出所有区域
                val filter = AreaListener.playerSet.filter { it.first == player.name }

                if (filter.isNotEmpty()) {
                    filter.forEach { (name, id) ->
                        if (AreaListener.playerSet.remove(name to id)) {
                            runActions(player, id, LEAVE)
                        }
                    }
                }
            }
        }
    }
}
