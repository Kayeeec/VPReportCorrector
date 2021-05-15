package org.umlreviewer.statistics

import org.jfree.chart.JFreeChart
import org.jfree.chart.fx.ChartViewer
import tornadofx.*

class ChartPreviewFragment : Fragment() {
    val chart: JFreeChart by param()

    init {
        title = "Chart preview: ${chart.title.text}"
    }

    override fun onBeforeShow() {
        modalStage?.let {
            it.minWidth = 900.0
            it.minHeight = 600.0
            it.centerOnScreen()
            it.toFront()
        }
    }


    override val root = ChartViewer(chart)
}
