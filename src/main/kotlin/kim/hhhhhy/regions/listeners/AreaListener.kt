package kim.hhhhhy.regions.listeners

import kim.hhhhhy.regions.data.AreaSettings
import kim.hhhhhy.regions.data.area.AreaType
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.info

object AreaListener {
    val playerSet = mutableSetOf<Pair<String, String>>()

    /***
     * 玩家进入区域前
     *      playerSet 没有该玩家 to 区域id
     *      areas 不为空
     * 玩家进入区域后
     *      playerSet 添加该玩家 to 区域id
     *      执行区域id Kether enter
     * 玩家退出区域
     *      playerSet 有该玩家 to 区域id
     *      areas不包括该玩家的区域id
     *      执行区域id Kether leave
     */
    @SubscribeEvent
    fun onPlayerMove(e: PlayerMoveEvent) {
        val to = e.to ?: return
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