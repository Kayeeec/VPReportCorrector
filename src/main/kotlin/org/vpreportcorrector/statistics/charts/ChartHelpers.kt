package org.vpreportcorrector.statistics.charts

import org.jfree.chart.labels.StandardCategoryItemLabelGenerator
import org.jfree.chart.plot.CategoryPlot
import org.jfree.chart.renderer.category.BarRenderer
import org.jfree.chart.renderer.category.StandardBarPainter
import org.jfree.chart.title.TextTitle
import org.jfree.chart.ui.HorizontalAlignment
import org.jfree.chart.ui.RectangleInsets
import org.vpreportcorrector.utils.Helpers.getWorkingDirectory
import java.awt.Color
import java.awt.Font
import java.io.File

object ChartHelpers {
    fun getSeminarGroupSubtitle(seminarGroup: File): TextTitle {
        val title = TextTitle("Seminar group: ${getSeminarGroupName(seminarGroup)}", CHART_SUBTITLE_FONT)
        title.textAlignment = HorizontalAlignment.LEFT
        title.padding = RectangleInsets(10.0, 1.0, 10.0, 1.0)
        return title
    }

    fun getSeminarGroupName(seminarGroup: File): String {
        val workDir = getWorkingDirectory()?.toFile()
        return if (workDir == null) seminarGroup.name
            else seminarGroup.toRelativeString(workDir)
    }

    val CHART_SUBTITLE_FONT = Font("SansSerif", Font.PLAIN, 12)
    const val HIDDEN_DETAILS_TITLE = "HIDDEN_DETAILS_TITLE"

    fun isHiddenDetailsTitle(o: Any?): Boolean {
        return o != null && o is TextTitle && !o.visible && o.id == HIDDEN_DETAILS_TITLE
    }

    fun BarRenderer.removeBarGradientAndAddItemLabels() {
        this.barPainter = StandardBarPainter()
        this.defaultItemLabelGenerator = StandardCategoryItemLabelGenerator()
        this.defaultItemLabelsVisible = true
        this.defaultItemLabelPaint = Color.BLACK
    }

    fun CategoryPlot.setBackgroundAndGridColor() {
        this.backgroundPaint = Color.WHITE
        this.domainGridlinePaint = Color.GRAY
        this.rangeGridlinePaint = Color.GRAY
    }

}
