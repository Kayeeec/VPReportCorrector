package org.umlreviewer.statistics.export

import org.jfree.chart.JFreeChart
import tornadofx.*

abstract class ChartExport: Controller() {
    abstract fun exportCharts(charts: Collection<JFreeChart>)
}
