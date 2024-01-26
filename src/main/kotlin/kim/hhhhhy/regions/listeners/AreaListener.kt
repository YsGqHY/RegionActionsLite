package kim.hhhhhy.regions.listeners

import kim.hhhhhy.regions.data.AreaSettings
import kim.hhhhhy.regions.data.area.AreaType
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.platform.event.SubscribeEvent

object AreaListener {
    private val playerSet = mutableSetOf<Pair<String, String>>()

    @SubscribeEvent
    fun onPlayerMove(e: PlayerMoveEvent) {
        val from = e.from
        val to = e.to ?: return
        if (from.x == to.x && from.y == to.y && from.z == to.z) return
        val player = e.player
        check(player, to)
    }

    @SubscribeEvent
    fun onPlayerJoin(e: PlayerJoinEvent) {
        check(e.player, e.player.location)
    }
    @SubscribeEvent
    fun onPlayerQuit(e: PlayerQuitEvent) {
        playerSet.removeAll { it.first == e.player.name }
    }


    private fun check(player: Player, location: Location) {
        val areas = AreaSettings.getAreas(location)

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

}