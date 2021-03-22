package org.vpreportcorrector.diagram.components

import javafx.beans.property.SimpleBooleanProperty
import org.vpreportcorrector.app.Styles
import org.vpreportcorrector.components.NoSelectionModel
import org.vpreportcorrector.diagram.DiagramModel
import org.vpreportcorrector.utils.t
import tornadofx.*

class DiagramErrorsView : View() {
    private val model: DiagramModel by inject()

    override val root = borderpane {
        addClass(Styles.diagramErrorsFragment)
        prefWidth = 300.0
        fitToParentSize()
        center = form {
            fieldset("Diagram errors") {
                fitToParentSize()
                listview(model.diagramErrorsProperty){
                    selectionModel = NoSelectionModel<Pair<String, SimpleBooleanProperty>>()
                    isFocusTraversable = false
                    fitToParentSize()
                    cellFormat {
                        graphic = checkbox(text = t(it.first), property = it.second) {
                            isWrapText = true
                            prefWidthProperty().bind(this@listview.widthProperty().minus(16.0 + 2*7.6 + 3))
                            enableWhen { model.isEditingProperty }
                        }
                    }
                }
            }
        }
    }
}
