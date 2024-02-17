package kim.hhhhhy.regions.listeners

import kim.hhhhhy.regions.data.AreaSettings
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerTeleportEvent
import taboolib.common.platform.event.SubscribeEvent

object AreaListener {
    val playerSet = mutableSetOf<Pair<String, String>>()

    @SubscribeEvent
    fun onPlayerMove(e: PlayerMoveEvent) {
        val from = e.from
        val to = e.to ?: return
        if (from.x == to.x && from.y == to.y && from.z == to.z) return
        val player = e.player
        AreaSettings.check(player, to)
    }

    @SubscribeEvent
    fun onPlayerChangeWorld(e: PlayerChangedWorldEvent) {
        AreaSettings.check(e.player, e.player.location)
    }

    @SubscribeEvent
    fun onTeleport(e: PlayerTeleportEvent) {
        val from = e.from
        val to = e.to ?: return
        if (from.world?.name == to.world?.name && from.x == to.x && from.y == to.y && from.z == to.z) return
        val player = e.player
        AreaSettings.check(player, to)
    }

    @SubscribeEvent
    fun onPlayerJoin(e: PlayerJoinEvent) {
        AreaSettings.check(e.player, e.player.location)
    }
    @SubscribeEvent
    fun onPlayerQuit(e: PlayerQuitEvent) {
        playerSet.removeAll { it.first == e.player.name }
        AreaSettings.stopTick(e.player)
    }

}