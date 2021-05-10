package org.umlreviewer.diagram.components

import javafx.embed.swing.SwingNode
import org.icepdf.ri.common.SwingViewBuilder
import org.umlreviewer.diagram.DiagramViewConstants.DEFAULT_PDF_VIEWER_ICON_SIZE
import javax.swing.JComponent

class PdfViewerComponents(factory: SwingViewBuilder) {
    val showHideUtilityPane = toSwingNode(factory.buildShowHideUtilityPaneButton())

    val fitPage = toSwingNode(factory.buildFitPageButton())
    val pan = toSwingNode(factory.buildPanToolButton())
    val textSelecion = toSwingNode(factory.buildTextSelectToolButton())

    val selectAnnotation = toSwingNode(factory.buildSelectToolButton(DEFAULT_PDF_VIEWER_ICON_SIZE))
    val lineAnnotation = toSwingNode(factory.buildLineAnnotationToolButton(DEFAULT_PDF_VIEWER_ICON_SIZE))
    val lineArrowAnnotation = toSwingNode(factory.buildLineArrowAnnotationToolButton(DEFAULT_PDF_VIEWER_ICON_SIZE))

    val squareAnnotation = toSwingNode(factory.buildSquareAnnotationToolButton(DEFAULT_PDF_VIEWER_ICON_SIZE))
    val circleAnnotation = toSwingNode(factory.buildCircleAnnotationToolButton(DEFAULT_PDF_VIEWER_ICON_SIZE))
    val inkAnnotation = toSwingNode(factory.buildInkAnnotationToolButton(DEFAULT_PDF_VIEWER_ICON_SIZE))

    val freeTextAnnotation = toSwingNode(factory.buildFreeTextAnnotationToolButton(DEFAULT_PDF_VIEWER_ICON_SIZE))
    val textAnnotation = toSwingNode(factory.buildTextAnnotationToolButton(DEFAULT_PDF_VIEWER_ICON_SIZE))

    private fun toSwingNode(jComponent: JComponent): SwingNode {
        val sn = SwingNode()
        sn.content = jComponent
        return sn
    }
}
