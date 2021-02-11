package org.vpreportcorrector.diagram.view

import javafx.scene.control.ToggleButton
import javafx.scene.control.Tooltip
import javafx.scene.image.Image
import javafx.scene.layout.Priority
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid
import org.kordamp.ikonli.javafx.FontIcon
import org.vpreportcorrector.components.form.loadingOverlay
import org.vpreportcorrector.diagram.DiagramController
import org.vpreportcorrector.diagram.components.DiagramErrorsDrawerView
import org.vpreportcorrector.diagram.components.gesturepane.CustomGesturePane
import tornadofx.*

class DiagramViewerView : View() {
    val controller: DiagramController by inject()
    private val image = Image(controller.model.pathUriString)
    private var imageView = imageview(image) {
        isPreserveRatio = true
    }
    private val gesturePane = CustomGesturePane(imageView)

    private var diagramErrorsBtn: ToggleButton by singleAssign()
    private val diagramErrorsDrawer = find<DiagramErrorsDrawerView>()

    init {
        controller.loadData()
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
                        gesturePane.reset()
                    }
                }

                hbox { hgrow = Priority.ALWAYS }

                diagramErrorsBtn = togglebutton("") {
                    isSelected = false
                    graphic = FontIcon(FontAwesomeSolid.TAGS)
                    tooltip = Tooltip("Show diagram errors")
                }
            }

            center = vbox {
                fitToParentSize()
                add(gesturePane)
                gesturePane.fitToParentSize()
                gesturePane.reset()
            }

            right = diagramErrorsDrawer.root
            diagramErrorsDrawer.drawerExpandedProperty.bind(diagramErrorsBtn.selectedProperty())
        }

        loadingOverlay {
            visibleWhen { controller.model.loadingLatch.isLoading }
        }
    }
}
