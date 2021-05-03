package org.vpreportcorrector.statistics.charts.barchart1

import javafx.application.Platform
import javafx.collections.SetChangeListener
import org.jfree.chart.ChartFactory
import org.jfree.chart.JFreeChart
import org.jfree.chart.axis.AxisLocation
import org.jfree.chart.plot.PlotOrientation
import org.jfree.data.category.DefaultCategoryDataset
import org.vpreportcorrector.app.errorhandling.ErrorCollector
import org.vpreportcorrector.enums.DiagramIssue
import org.vpreportcorrector.enums.DiagramIssueGroup
import org.vpreportcorrector.statistics.NoDatasetCollector
import org.vpreportcorrector.statistics.PdfFileData
import org.vpreportcorrector.statistics.components.Team
import org.vpreportcorrector.statistics.enums.DataSelectMode
import org.vpreportcorrector.statistics.enums.IssueChooserMode
import org.vpreportcorrector.utils.FileTreeHelpers
import org.vpreportcorrector.utils.t
import tornadofx.*
import java.io.File

class BarChart1WizardViewModel(modelItem: BarChart1WizardParameters = BarChart1WizardParameters()) :
    ItemViewModel<BarChart1WizardParameters>(modelItem) {
    val issueSelectMode = bind(BarChart1WizardParameters::issueSelectModeProperty)
    val issues = bind(BarChart1WizardParameters::issuesProperty)
    val issueGroups = bind(BarChart1WizardParameters::issueGroupsProperty)

    val dataSelectMode = bind(BarChart1WizardParameters::dataSelectModeProperty)
    val teams = bind(BarChart1WizardParameters::teamsProperty)
    val seminarGroups = bind(BarChart1WizardParameters::seminarGroupsProperty)

    val week = bind(BarChart1WizardParameters::weekProperty)

    fun clear() {
        rebind { item = BarChart1WizardParameters() }
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
            val pdfs = FileTreeHelpers.getPdfs(item.week!!.number, seminarGroup)
                .map { PdfFileData(it.toFile(), seminarGroup) }
            return computeDatasetAndCreateChart(pdfs, noDatasetCollector, graphTitle)
        } catch (e: Throwable) {
            errorCollector.addError("Failed to create '${graphTitle}'.", e)
        }
        return null
    }

    private fun getGraph(team: Team, noDatasetCollector: NoDatasetCollector, errorCollector: ErrorCollector): JFreeChart? {
        val graphTitle = item.getTitle(team)
        try {
            val pdfs = FileTreeHelpers.getPdfs(item.week!!.number, team)
                .map { PdfFileData(it.toFile(), team.seminarGroupFolder) }
            return computeDatasetAndCreateChart(pdfs, noDatasetCollector, graphTitle)
        } catch (e: Throwable) {
            errorCollector.addError("Failed to create '${graphTitle}'.", e)
        }
        return null
    }

    private fun getIssueGroupDataset(pdfs: List<PdfFileData>, categoryAxisLabel: String): DefaultCategoryDataset? {
        val dataset = DefaultCategoryDataset()
        val allIssueCountsPerGroup = mutableMapOf<DiagramIssueGroup, Int>()
        item.issueGroups.forEach { allIssueCountsPerGroup[it] = 0 }

        pdfs.forEach { pdfFileData: PdfFileData ->
            val issueCountsPerGroup = pdfFileData.getIssueCountsPerGroup()
            issueCountsPerGroup.keys.forEach { key ->
                if (allIssueCountsPerGroup.containsKey(key)){
                    allIssueCountsPerGroup[key] = allIssueCountsPerGroup[key]!!.plus(issueCountsPerGroup[key]!!)
                }
            }
        }
        if (allIssueCountsPerGroup.values.none { it > 0 }) return null
        allIssueCountsPerGroup.forEach { entry ->
            dataset.setValue(entry.value, categoryAxisLabel, t(entry.key.name))
        }
        return dataset
    }

    private fun getIssueDataset(pdfs: List<PdfFileData>, categoryAxisLabel: String): DefaultCategoryDataset? {
        val dataset = DefaultCategoryDataset()
        val allIssueCountsMap = mutableMapOf<DiagramIssue, Int>()
        item.issues.forEach { allIssueCountsMap[it] = 0 }
        pdfs.forEach { pdfFileData ->
            pdfFileData.issues.forEach { di ->
                if (allIssueCountsMap.containsKey(di)) {
                    allIssueCountsMap[di] = (allIssueCountsMap[di] ?: 0).plus(1)
                }
            }
        }
        if (allIssueCountsMap.values.none { it > 0 }) return null
        allIssueCountsMap.forEach { entry ->
            dataset.setValue(entry.value, categoryAxisLabel, t(entry.key.name))
        }
        return dataset
    }

    private fun computeDatasetAndCreateChart(
        pdfs: List<PdfFileData>,
        noDatasetCollector: NoDatasetCollector,
        graphTitle: String
    ): JFreeChart? {
        val valueAxisLabel = "Number of issues"
        val categoryAxisLabel = if (item.issueSelectMode == IssueChooserMode.SINGLE_ISSUE) "Issues" else "Issue groups"
        val dataset = if (item.issueSelectMode == IssueChooserMode.SINGLE_ISSUE) getIssueDataset(pdfs, valueAxisLabel)
        else getIssueGroupDataset(pdfs, valueAxisLabel)
        if (pdfs.isEmpty() || dataset == null || dataset.rowCount == 0) {
            noDatasetCollector.add(graphTitle)
            return null
        }
        val chart = ChartFactory.createBarChart(
            graphTitle,
            categoryAxisLabel,
            valueAxisLabel,
            dataset,
            PlotOrientation.HORIZONTAL,
            false,
            true,
            false
        )
        chart.categoryPlot.rangeAxisLocation = AxisLocation.BOTTOM_OR_LEFT
        return chart
    }

}
