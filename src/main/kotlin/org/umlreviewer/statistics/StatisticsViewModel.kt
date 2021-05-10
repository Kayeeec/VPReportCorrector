package org.umlreviewer.statistics

import javafx.beans.property.SimpleListProperty
import org.jfree.chart.JFreeChart
import org.umlreviewer.components.LoadingLatch
import org.umlreviewer.components.WithLoading
import org.umlreviewer.statistics.export.ChartsToPdfExport
import tornadofx.ViewModel
import tornadofx.asObservable


class StatisticsViewModel: ViewModel(), WithLoading by LoadingLatch() {
    val graphs = SimpleListProperty(mutableListOf<JFreeChart>().asObservable())

    private val pdfExport by inject<ChartsToPdfExport>()

    fun exportToPdf() {
        pdfExport.exportCharts(graphs)
    }
}


