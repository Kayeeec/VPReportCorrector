package org.umlreviewer.statistics.charts.barchart2

import javafx.application.Platform
import javafx.collections.SetChangeListener
import org.jfree.chart.ChartFactory
import org.jfree.chart.JFreeChart
import org.jfree.chart.axis.AxisLocation
import org.jfree.chart.axis.NumberAxis
import org.jfree.chart.plot.PlotOrientation
import org.jfree.chart.renderer.category.BarRenderer
import org.jfree.data.category.DefaultCategoryDataset
import org.umlreviewer.app.errorhandling.ErrorCollector
import org.umlreviewer.enums.DiagramIssue
import org.umlreviewer.enums.DiagramIssueGroup
import org.umlreviewer.statistics.NoDatasetCollector
import org.umlreviewer.statistics.PdfFileData
import org.umlreviewer.statistics.charts.ChartHelpers.removeBarGradientAndAddItemLabels
import org.umlreviewer.statistics.charts.ChartHelpers.setBackgroundAndGridColor
import org.umlreviewer.utils.file.Team
import org.umlreviewer.statistics.enums.DataSelectMode
import org.umlreviewer.statistics.enums.IssueChooserMode
import org.umlreviewer.utils.file.FileTreeHelpers
import org.umlreviewer.utils.t
import tornadofx.*
import java.io.File

class BarChart2WizardViewModel(modelItem: BarChart2WizardParameters = BarChart2WizardParameters()) :
    ItemViewModel<BarChart2WizardParameters>(modelItem) {
    val issueSelectMode = bind(BarChart2WizardParameters::issueSelectModeProperty)
    val issues = bind(BarChart2WizardParameters::issuesProperty)
    val issueGroups = bind(BarChart2WizardParameters::issueGroupsProperty)

    val dataSelectMode = bind(BarChart2WizardParameters::dataSelectModeProperty)
    val teams = bind(BarChart2WizardParameters::teamsProperty)
    val seminarGroups = bind(BarChart2WizardParameters::seminarGroupsProperty)

    val week1 = bind(BarChart2WizardParameters::week1Property)
    val week2 = bind(BarChart2WizardParameters::week2Property)

    fun clear() {
        rebind { item = BarChart2WizardParameters() }
    }

    init {
        /** Bug in set properties in JavaFx - @see https://stackoverflow.com/a/40311865/7677851 **/
        issues.addListener { _: SetChangeListener.Change<*>? -> }
        issueGroups.addListener { _: SetChangeListener.Change<*>? -> }
        teams.addListener { _: SetChangeListener.Change<*>? -> }
        seminarGroups.addListener { _: SetChangeListener.Change<*>? -> }
    }


    fun getGraphs(fxTask: FXTask<*>): List<JFreeChart> {
        val noDatasetCollector = NoDatasetCollector()
        val errorCollector = ErrorCollector("Error occurred while generating  some graphs:")
        val result = if (item.dataSelectMode == DataSelectMode.SEMINAR_GROUP) {
            val total = item.seminarGroups.size
            item.seminarGroups.mapIndexed { index, group ->
                fxTask.updateMessage("Generating graph ${index + 1} of $total...")
                getGraph(group, noDatasetCollector, errorCollector)
            }.filterNotNull()
        } else {
            val total = item.teams.size
            item.teams.mapIndexed {index, team ->
                fxTask.updateMessage("Generating graph ${index + 1} of $total...")
                getGraph(team, noDatasetCollector, errorCollector)
            }.filterNotNull()
        }
        Platform.runLater {
            noDatasetCollector.verify()
            errorCollector.verify()
        }
        return result
    }

    private fun getGraph(seminarGroup: File, noDatasetCollector: NoDatasetCollector, errorCollector: ErrorCollector): JFreeChart? {
        val graphTitle = item.getTitle(seminarGroup)
        try {
            val pdfs1 = FileTreeHelpers.getPdfs(item.week1!!.number, seminarGroup)
                .map { PdfFileData(it.toFile(), seminarGroup) }
            val pdfs2 = FileTreeHelpers.getPdfs(item.week2!!.number, seminarGroup)
                .map { PdfFileData(it.toFile(), seminarGroup) }
            return computeDatasetAndCreateChart(pdfs1, pdfs2, noDatasetCollector, graphTitle)
        } catch (e: Throwable) {
            errorCollector.addError("Failed to create '${graphTitle}'.", e)
        }
        return null
    }

    private fun getGraph(team: Team, noDatasetCollector: NoDatasetCollector, errorCollector: ErrorCollector): JFreeChart? {
        val graphTitle = item.getTitle(team)
        try {
            val pdfs1 = FileTreeHelpers.getPdfs(item.week1!!.number, team)
                .map { PdfFileData(it.toFile(), team.seminarGroupFolder) }
            val pdfs2 = FileTreeHelpers.getPdfs(item.week2!!.number, team)
                .map { PdfFileData(it.toFile(), team.seminarGroupFolder) }
            return computeDatasetAndCreateChart(pdfs1, pdfs2, noDatasetCollector, graphTitle)
        } catch (e: Throwable) {
            errorCollector.addError("Failed to create '${graphTitle}'.", e)
        }
        return null
    }

    private fun getIssueGroupDataset(pdfs1: List<PdfFileData>, pdfs2: List<PdfFileData>): DefaultCategoryDataset? {
        val dataset = DefaultCategoryDataset()
        val allIssueCountsPerGroupWeek1 = mutableMapOf<DiagramIssueGroup, Int>()
        val allIssueCountsPerGroupWeek2 = mutableMapOf<DiagramIssueGroup, Int>()
        item.issueGroups.forEach {
            allIssueCountsPerGroupWeek1[it] = 0
            allIssueCountsPerGroupWeek2[it] = 0
        }
        pdfs1.forEach { pdfFileData: PdfFileData ->
            val issueCountsPerGroup = pdfFileData.getIssueCountsPerGroup()
            issueCountsPerGroup.keys.forEach { key ->
                if (allIssueCountsPerGroupWeek1.containsKey(key)){
                    allIssueCountsPerGroupWeek1[key] = allIssueCountsPerGroupWeek1[key]!!.plus(issueCountsPerGroup[key]!!)
                }
            }
        }
        allIssueCountsPerGroupWeek1.forEach { entry ->
            dataset.setValue(entry.value, "Week 1", t(entry.key.name))
        }

        pdfs2.forEach { pdfFileData: PdfFileData ->
            val issueCountsPerGroup = pdfFileData.getIssueCountsPerGroup()
            issueCountsPerGroup.keys.forEach { key ->
                if (allIssueCountsPerGroupWeek2.containsKey(key)){
                    allIssueCountsPerGroupWeek2[key] = allIssueCountsPerGroupWeek2[key]!!.plus(issueCountsPerGroup[key]!!)
                }
            }
        }
        if (allIssueCountsPerGroupWeek1.values.none { it > 0 } && allIssueCountsPerGroupWeek2.values.none { it > 0 }){
            return null
        }
        allIssueCountsPerGroupWeek2.forEach { entry ->
            dataset.setValue(entry.value, "Week 2", t(entry.key.name))
        }
        return dataset
    }

    private fun getIssueDataset(pdfs1: List<PdfFileData>, pdfs2: List<PdfFileData>): DefaultCategoryDataset? {
        val dataset = DefaultCategoryDataset()
        val allIssueCountsMapWeek1 = mutableMapOf<DiagramIssue, Int>()
        val allIssueCountsMapWeek2 = mutableMapOf<DiagramIssue, Int>()
        item.issues.forEach {
            allIssueCountsMapWeek1[it] = 0
            allIssueCountsMapWeek2[it] = 0
        }
        pdfs1.forEach { pdfFileData ->
            pdfFileData.issues.forEach { di ->
                if (allIssueCountsMapWeek1.containsKey(di)) {
                    allIssueCountsMapWeek1[di] = (allIssueCountsMapWeek1[di] ?: 0).plus(1)
                }
            }
        }
        allIssueCountsMapWeek1.forEach { entry ->
            dataset.setValue(entry.value, "Week 1", t(entry.key.name))
        }
        pdfs2.forEach { pdfFileData ->
            pdfFileData.issues.forEach { di ->
                if (allIssueCountsMapWeek2.containsKey(di)) {
                    allIssueCountsMapWeek2[di] = (allIssueCountsMapWeek2[di] ?: 0).plus(1)
                }
            }
        }
        if (allIssueCountsMapWeek1.values.none { it > 0 } && allIssueCountsMapWeek2.values.none { it > 0 }){
            return null
        }
        allIssueCountsMapWeek2.forEach { entry ->
            dataset.setValue(entry.value,"Week 2", t(entry.key.name))
        }
        return dataset
    }

    private fun computeDatasetAndCreateChart(
        pdfs1: List<PdfFileData>,
        pdfs2: List<PdfFileData>,
        noDatasetCollector: NoDatasetCollector,
        graphTitle: String
    ): JFreeChart? {
        val categoryAxisLabel = if (item.issueSelectMode == IssueChooserMode.SINGLE_ISSUE) "Issues" else "Issue groups"
        val dataset = if (item.issueSelectMode == IssueChooserMode.SINGLE_ISSUE) getIssueDataset(pdfs1, pdfs2)
            else getIssueGroupDataset(pdfs1, pdfs2)
        if (pdfs1.isEmpty() || dataset == null || dataset.rowCount == 0) {
            noDatasetCollector.add(graphTitle)
            return null
        }
        val chart = ChartFactory.createBarChart(
            graphTitle,
            categoryAxisLabel,
            "Number of issues",
            dataset,
            PlotOrientation.HORIZONTAL,
            true,
            true,
            false
        )
        chart.categoryPlot.rangeAxisLocation = AxisLocation.BOTTOM_OR_LEFT
        chart.categoryPlot.rangeAxis.standardTickUnits = NumberAxis.createIntegerTickUnits()
        val barRenderer = chart.categoryPlot.renderer as BarRenderer
        barRenderer.itemMargin = 0.0
        chart.categoryPlot.setBackgroundAndGridColor()
        barRenderer.removeBarGradientAndAddItemLabels()

        return chart
    }

}
