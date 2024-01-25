package kim.hhhhhy.regions.listeners

import kim.hhhhhy.regions.data.AreaSettings
import kim.hhhhhy.regions.data.area.AreaType
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.info

object AreaListener {
    val playerSet = mutableSetOf<Pair<String, String>>()

    @SubscribeEvent
    fun onPlayerMove(e: PlayerMoveEvent) {
        val from = e.from
        val to = e.to ?: return
        if (from.x == to.x && from.y == to.y && from.z == to.z) return
        val player = e.player
        val areas = AreaSettings.getAreas(to)

        if (areas.isNotEmpty()) {

            // 退出某个区域
            val filter = playerSet.filter { it.first == player.name && it !in areas.map { id -> player.name to id }.toSet() }
            if (filter.isNotEmpty()) {
                filter.forEach { (name, id) ->
                    if (playerSet.remove(name to id)) {
                        AreaSettings.runActions(player, id, AreaType.LEAVE)
                    }
                }
            }

            // 进入区域
            areas.forEach {
                if (playerSet.add(player.name to it)) {
                    AreaSettings.runActions(player, it, AreaType.ENTER)
                }
            }
        } else {

            // 退出所有区域
            val filter = playerSet.filter { it.first == player.name }

            if (filter.isNotEmpty()) {
                filter.forEach { (name, id) ->
                    if (playerSet.remove(name to id)) {
                        AreaSettings.runActions(player, id, AreaType.LEAVE)
                    }
                }
            }
        }

    }

    @SubscribeEvent
    fun onPlayerQuit(e: PlayerQuitEvent) {
        playerSet.removeAll { it.first == e.player.name }
    }

}