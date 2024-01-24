package kim.hhhhhy.regions.data.area

import taboolib.common5.cdouble
import taboolib.library.configuration.ConfigurationSection

data class AreaPosition(
    val world: String,
    val xMax: Double,
    val yMax: Double,
    val zMax: Double,
    val xMin: Double,
    val yMin: Double,
    val zMin: Double,
) {
    constructor(section: ConfigurationSection): this(
        section.getString("world")!!,
        section.getPosition("xMax"),
        section.getPosition("yMax"),
        section.getPosition("zMax"),
        section.getPosition("xMin"),
        section.getPosition("yMin"),
        section.getPosition("zMin")
    )
    companion object {

        private fun ConfigurationSection.getPosition(value: String): Double {
            return when (value) {
                "xMax" -> getString("max")!!.replace(" ", "").split(",").getOrElse(0) { 0 }.cdouble
                "yMax" -> getString("max")!!.replace(" ", "").split(",").getOrElse(1) { 0 }.cdouble
                "zMax" -> getString("max")!!.replace(" ", "").split(",").getOrElse(2) { 0 }.cdouble
                "xMin" -> getString("min")!!.replace(" ", "").split(",").getOrElse(0) { 0 }.cdouble
                "yMin" -> getString("min")!!.replace(" ", "").split(",").getOrElse(1) { 0 }.cdouble
                "zMin" -> getString("min")!!.replace(" ", "").split(",").getOrElse(2) { 0 }.cdouble
                else -> 0.0
            }
        }
    }
}
