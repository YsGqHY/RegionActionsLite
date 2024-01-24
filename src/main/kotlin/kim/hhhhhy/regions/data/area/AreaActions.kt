package kim.hhhhhy.regions.data.area

import taboolib.library.configuration.ConfigurationSection

data class AreaActions(
    val enter: String?,
    val leave: String?,
    val tick: String?
){
    constructor(section: ConfigurationSection?): this(
        section?.getString("enter"),
        section?.getString("leave"),
        section?.getString("tick")
    )

}
