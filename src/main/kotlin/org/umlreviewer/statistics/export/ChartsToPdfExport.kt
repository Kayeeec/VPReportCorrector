package org.umlreviewer.statistics.export

import de.rototor.pdfbox.graphics2d.PdfBoxGraphics2D
import javafx.stage.FileChooser
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.jfree.chart.JFreeChart
import org.umlreviewer.app.errorhandling.errorWithStacktrace
import org.umlreviewer.utils.DesktopApi
import org.umlreviewer.utils.file.getUserHomeDirectory
import org.umlreviewer.utils.t
import tornadofx.FileChooserMode
import tornadofx.chooseFile
import tornadofx.fail
import tornadofx.success
import java.awt.Rectangle
import java.awt.geom.AffineTransform
import java.io.File
import kotlin.math.ceil

class ChartsToPdfExport: ChartExport() {
    companion object {
        private const val MARGIN = 36.0
        private const val DIAGRAM_GUTTER = 36.0
        private val targetWidth = PDRectangle.A4.width - 2*MARGIN
        private val targetHeight = ((PDRectangle.A4.height - 2*MARGIN) - DIAGRAM_GUTTER)/2
        private val ratio = targetWidth/targetHeight
        val g2Rectangle = Rectangle(900, (900/ratio).toInt())
        val scale = (targetWidth/g2Rectangle.width)
        val x = MARGIN
        val y = PDRectangle.A4.height - x - (g2Rectangle.height*scale)
    }

    override fun exportCharts(charts: Collection<JFreeChart>) {
        if (charts.isEmpty()) return
        val pdfFile = getPdfFileLocation() ?: return
        runAsync {
            val pdfDocument = PDDocument()
            val chartsArray = charts.toTypedArray()
            val totalPages = ceil(chartsArray.size/2.0).toInt()
            pdfDocument.use { pdf ->
                for (i in 0..chartsArray.lastIndex step 2) {
                    updateMessage("Exporting graphs to PDF - page ${(i/2)+1}/${totalPages}...")
                    addPageWithGraphs(Pair(chartsArray[i], chartsArray.getOrNull(i+1)), pdf)
                }
                pdf.save(pdfFile)
            }
        } fail {
            log.severe(it.stackTraceToString())
            errorWithStacktrace("Error occurred exporting graphs to PDF.", it)
        } success {
            openExportedPdf(pdfFile)
        }
    }

    private fun addPageWithGraphs(charts: Pair<JFreeChart, JFreeChart?>, pdfDocument: PDDocument) {
        val page = PDPage(PDRectangle.A4)
        pdfDocument.addPage(page)

        val g2Chart1 = getChartGraphics(pdfDocument, charts.first)
        val g2Chart2 = getChartGraphics(pdfDocument, charts.second)

        PDPageContentStream(pdfDocument, page, PDPageContentStream.AppendMode.APPEND, false).use {
            if (g2Chart1 != null) {
                val transform1 = AffineTransform.getTranslateInstance(x, y)
                transform1.concatenate(AffineTransform.getScaleInstance(scale, scale))
                val xform1 = g2Chart1.xFormObject
                xform1.setMatrix(transform1)
                it.drawForm(xform1)
            }

            if (g2Chart2 != null) {
                val transform2 = AffineTransform.getTranslateInstance(x, x)
                transform2.concatenate(AffineTransform.getScaleInstance(scale, scale))
                val xform2 = g2Chart2.xFormObject
                xform2.setMatrix(transform2)
                it.drawForm(xform2)
            }
        }
    }

    private fun getChartGraphics(
        pdfDocument: PDDocument,
        chart: JFreeChart?
    ): PdfBoxGraphics2D? {
        if (chart == null) return null
        val g2d = PdfBoxGraphics2D(pdfDocument, g2Rectangle.width, g2Rectangle.height)
        chart.draw(g2d, g2Rectangle)
        g2d.dispose()
        return g2d
    }

    private fun getPdfFileLocation(): File? {
        val files = chooseFile(
            title = "Export to PDF",
            filters = arrayOf(
                FileChooser.ExtensionFilter(
                    t("pdfExtensionFilterDescription"),
                    "*.pdf", "*.PDF"
                )
            ),
            initialDirectory = getUserHomeDirectory(),
            initialFileName = "chartExport.pdf",
            mode = FileChooserMode.Save
        )
        return if (files.isNotEmpty()) files[0] else null
    }

    private fun openExportedPdf(pdfFile: File) {
        try {
            DesktopApi.open(pdfFile)
        } catch (e: Throwable) {
            errorWithStacktrace(
                "Failed to open exported PDF in a system way.\nPath to exported file: '${pdfFile.canonicalPath}'.",
                e
            )
        }
    }
}
