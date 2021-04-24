package org.vpreportcorrector.statistics

import javafx.beans.property.SimpleListProperty
import org.jfree.chart.JFreeChart
import org.vpreportcorrector.components.LoadingLatch
import org.vpreportcorrector.components.WithLoading
import org.vpreportcorrector.statistics.export.ChartsToPdfExport
import tornadofx.ViewModel
import tornadofx.asObservable


class StatisticsViewModel: ViewModel(), WithLoading by LoadingLatch() {
    val graphs = SimpleListProperty(mutableListOf<JFreeChart>().asObservable())

    private val pdfExport by inject<ChartsToPdfExport>()

    fun exportToPdf() {
        pdfExport.exportCharts(graphs)
    }
}


