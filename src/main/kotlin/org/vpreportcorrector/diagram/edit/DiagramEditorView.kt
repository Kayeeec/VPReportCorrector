package org.vpreportcorrector.diagram.edit

import javafx.embed.swing.SwingNode
import javafx.scene.control.ToggleButton
import javafx.scene.control.Tooltip
import javafx.scene.layout.Priority
import org.apache.batik.swing.JSVGCanvas
import org.apache.batik.swing.gvt.GVTTreeRendererAdapter
import org.apache.batik.swing.gvt.GVTTreeRendererEvent
import org.apache.batik.swing.svg.JSVGComponent
import org.apache.batik.swing.svg.SVGDocumentLoaderAdapter
import org.apache.batik.swing.svg.SVGDocumentLoaderEvent
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid
import org.kordamp.ikonli.javafx.FontIcon
import org.vpreportcorrector.components.form.loadingOverlay
import org.vpreportcorrector.diagram.DiagramController
import org.vpreportcorrector.diagram.components.DiagramErrorsDrawerView
import tornadofx.*

class DiagramEditorView : View() {
    private val controller: DiagramController by inject()

    private var svgCanvas = JSVGCanvas()
    val sn = SwingNode()

    var diagramErrorsBtn: ToggleButton by singleAssign()
    private val diagramErrorsDrawer = find<DiagramErrorsDrawerView>(scope)

    init {
        controller.loadData()
        svgCanvas.addSVGDocumentLoaderListener(object: SVGDocumentLoaderAdapter() {
            override fun documentLoadingStarted(e: SVGDocumentLoaderEvent) {
                controller.model.loadingLatch.startLoading()
            }
        })
        svgCanvas.addGVTTreeRendererListener(object : GVTTreeRendererAdapter() {
            override fun gvtRenderingCompleted(e: GVTTreeRendererEvent?) {
                controller.model.loadingLatch.endLoading()
            }
        })
    }

    override val root = stackpane {
        fitToParentSize()
        vgrow = Priority.ALWAYS
        hgrow = Priority.ALWAYS
        borderpane {
            fitToParentSize()
            vgrow = Priority.ALWAYS
            hgrow = Priority.ALWAYS
            top = toolbar {
                hgrow = Priority.ALWAYS

                button("", FontIcon(FontAwesomeSolid.EXPAND_ARROWS_ALT)) {
                    tooltip = Tooltip("Fit to view whole image")
                    action {
                        log.info("fit image")
                        // TODO: 18.02.21
                    }
                }
                // TODO: 23.02.21 remaining buttons

                hbox { hgrow = Priority.ALWAYS }

                diagramErrorsBtn = togglebutton("") {
                    isSelected = false
                    graphic = FontIcon(FontAwesomeSolid.TAGS)
                    tooltip = Tooltip("Show errors the diagram contains")
                }

            }
            center = vbox {
                fitToParentSize()
                svgCanvas.setDocumentState(JSVGComponent.ALWAYS_DYNAMIC)
                svgCanvas.enableImageZoomInteractor = true
                svgCanvas.enablePanInteractor = true
                sn.content = svgCanvas
                scrollpane {
                    isPannable = true
                    fitToParentSize()
                    add(sn)
                    svgCanvas.loadSVGDocument(controller.model.pathUriString)
                }
            }

            right = diagramErrorsDrawer.root
            diagramErrorsDrawer.drawerExpandedProperty.bind(diagramErrorsBtn.selectedProperty())
        }

        loadingOverlay {
            visibleWhen { controller.model.loadingLatch.isLoading }
        }
    }
}

