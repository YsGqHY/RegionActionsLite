package kim.hhhhhy.regions.listeners

import kim.hhhhhy.regions.data.AreaSettings
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerTeleportEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.platform.util.isBlockMovement
import taboolib.platform.util.isMovement

object AreaListener {
    val playerSet = mutableSetOf<Pair<String, String>>()

    @SubscribeEvent
    fun onPlayerMove(e: PlayerMoveEvent) {
        val to = e.to ?: return
        if (!e.isMovement()) {
            return
        }
        val player = e.player
        AreaSettings.check(player, to, e.from, e)
    }

    @SubscribeEvent
    fun onPlayerChangeWorld(e: PlayerChangedWorldEvent) {
        AreaSettings.stopTick(e.player)
        AreaSettings.check(e.player, e.player.location, e.from.spawnLocation, e)
    }

    @SubscribeEvent
    fun onTeleport(e: PlayerTeleportEvent) {
        val from = e.from
        val to = e.to ?: return
        val player = e.player
        AreaSettings.check(player, to, from, e)
    }

    @SubscribeEvent
    fun onPlayerJoin(e: PlayerJoinEvent) {
        AreaSettings.check(e.player, e.player.location, e.player.location, e)
    }

    @SubscribeEvent
    fun onPlayerQuit(e: PlayerQuitEvent) {
        playerSet.removeAll { it.first == e.player.name }
        AreaSettings.stopTick(e.player)
    }

}