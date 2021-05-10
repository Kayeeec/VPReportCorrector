package org.umlreviewer.statistics.charts.barchart3

import javafx.application.Platform
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.SetChangeListener
import org.jfree.chart.ChartFactory
import org.jfree.chart.JFreeChart
import org.jfree.chart.axis.CategoryLabelPositions
import org.jfree.chart.axis.NumberAxis
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator
import org.jfree.chart.plot.PlotOrientation
import org.jfree.chart.renderer.category.StackedBarRenderer
import org.jfree.chart.renderer.category.StandardBarPainter
import org.jfree.chart.title.TextTitle
import org.jfree.chart.ui.HorizontalAlignment
import org.jfree.chart.ui.RectangleInsets
import org.jfree.data.category.DefaultCategoryDataset
import org.umlreviewer.enums.DiagramIssueGroup
import org.umlreviewer.statistics.NoDatasetCollector
import org.umlreviewer.statistics.PdfFileData
import org.umlreviewer.statistics.charts.ChartHelpers
import org.umlreviewer.statistics.charts.ChartHelpers.setBackgroundAndGridColor
import org.umlreviewer.statistics.components.Team
import org.umlreviewer.statistics.components.Week
import org.umlreviewer.utils.FileTreeHelpers
import org.umlreviewer.utils.t
import tornadofx.*
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

class BarChart3WizardViewModel(modelItem: BarChart3WizardParameters = BarChart3WizardParameters()) :
    ItemViewModel<BarChart3WizardParameters>(modelItem) {

    val diagramTypes = bind(BarChart3WizardParameters::diagramTypesProperty)
    val teams = bind(BarChart3WizardParameters::teamsProperty)
    val week: SimpleObjectProperty<Week> = bind(BarChart3WizardParameters::week)

    fun clear() {
        rebind { item = BarChart3WizardParameters() }
    }

    init {
        /** Bug in set properties in JavaFx - @see https://stackoverflow.com/a/40311865/7677851 **/
        diagramTypes.addListener { _: SetChangeListener.Change<*>? -> }
        teams.addListener { _: SetChangeListener.Change<*>? -> }
    }


    fun getGraph(fxTask: FXTask<*>): JFreeChart? {
        log.info("getGraphs")
        val noDatasetCollector = NoDatasetCollector()
        fxTask.updateMessage("Generating graph...")

        val dataset = getDataset(noDatasetCollector)
        val graph = if (dataset == null) null else ChartFactory.createStackedBarChart(
            item.getTitle(),
            "Teams",
            "Number of issues",
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        )
        if (item.useTeamLongNames()) {
            graph?.categoryPlot?.domainAxis?.categoryLabelPositions = CategoryLabelPositions.UP_45
        } else {
            graph?.addSubtitle(getSeminarGroupSubtitle())
        }
        if (graph != null) {
            val plot = graph.categoryPlot
            val renderer = plot?.renderer as StackedBarRenderer?
            renderer?.barPainter = StandardBarPainter()
            renderer?.defaultItemLabelGenerator = StandardCategoryItemLabelGenerator()
            renderer?.defaultItemLabelsVisible = true
            graph.addSubtitle(getDetailsSubtitles())
            plot.setBackgroundAndGridColor()
            plot.rangeAxis.standardTickUnits = NumberAxis.createIntegerTickUnits()
        }

        Platform.runLater {
            noDatasetCollector.verify()
        }
        return graph
    }

    private fun getDataset(noDatasetCollector: NoDatasetCollector): DefaultCategoryDataset? {
        val countsPerTeamSum = mutableMapOf<Team, Int>()
        val countsPerTeamAndGroup = mutableMapOf<Team, MutableMap<DiagramIssueGroup, Int>>()
        item.teams.forEach {
            countsPerTeamSum[it] = 0
            countsPerTeamAndGroup[it] = item.diagramTypes.associateWith { 0 }.toMutableMap()
        }

        item.teams.forEach { team ->
            val pdfs = FileTreeHelpers.getPdfs(item.week!!.number, team).map { PdfFileData(it.toFile(), team.seminarGroupFolder) }
            pdfs.forEach { pdfData ->
                pdfData.getIssueCountsPerGroup()
                    .forEach {(diagramType, count) ->
                        if (item.diagramTypes.contains(diagramType)) {
                            countsPerTeamAndGroup[team]!![diagramType] = countsPerTeamAndGroup[team]!![diagramType]!!.plus(count)
                            countsPerTeamSum[team] = countsPerTeamSum[team]!!.plus(count)
                        }
                    }
            }
        }
        if (countsPerTeamSum.values.none { it > 0 }) {
            noDatasetCollector.add(item.getTitle())
            return null
        }
        val dataset = DefaultCategoryDataset()
        val useLongNames = item.useTeamLongNames()
        countsPerTeamAndGroup.forEach { (team, countsPerGroup) ->
            val teamName = if (useLongNames) team.longName else team.name
            countsPerGroup.forEach { (diagramIssueGroup, count) ->
                dataset.addValue(count, t(diagramIssueGroup.name), teamName)
            }
        }

        return dataset
    }

    private fun getSeminarGroupSubtitle(): TextTitle {
        val firstTeam = item.teams.elementAt(0)
        return ChartHelpers.getSeminarGroupSubtitle(firstTeam.seminarGroupFolder)
    }

    private fun getDetailsSubtitles(): TextTitle {
        val useLongName = item.useTeamLongNames()
        val s = if (useLongName) ""
            else "Seminar group: ${ChartHelpers.getSeminarGroupName(item.teams.elementAt(0).seminarGroupFolder)}\n"
        val t = if (useLongName) item.teams.joinToString(", ") { it.longName }
        else item.teams.joinToString(", ") { it.name }
        val dg = item.diagramTypes.joinToString(", ") { t(it.name) }
        val title = TextTitle(
            "${s}Teams: $t \nDiagram types: $dg",
            ChartHelpers.CHART_SUBTITLE_FONT
        )
        title.textAlignment = HorizontalAlignment.LEFT
        title.padding = RectangleInsets(10.0, 1.0, 10.0, 1.0)
        title.visible = false
        title.id = ChartHelpers.HIDDEN_DETAILS_TITLE
        return title
    }
}

