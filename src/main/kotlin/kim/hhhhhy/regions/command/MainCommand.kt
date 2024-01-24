package kim.hhhhhy.regions.command

import kim.hhhhhy.regions.RegionActionsLite
import org.bukkit.command.CommandSender
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.subCommand
import taboolib.platform.util.sendInfo

@CommandHeader("regionactionslite",
    aliases = ["ral", "ra"],
    permission = "regionactions.command.*"
)
object MainCommand {
    @CommandBody(permission = "regionactions.command.reload")
    val reload = subCommand {
        execute<CommandSender> { sender, _, _->
            RegionActionsLite.reload()
            sender.sendInfo("plugin-reload")
        }
    }
}