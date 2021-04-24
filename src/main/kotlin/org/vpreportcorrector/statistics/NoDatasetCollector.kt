package org.vpreportcorrector.statistics

import javafx.geometry.Orientation
import javafx.scene.control.Alert
import tornadofx.*

class NoDatasetCollector {
    private val graphTitles = mutableSetOf<String>()

    fun add(graphTitle: String) {
        graphTitles.add(graphTitle)
    }

    fun verify(){
        if (graphTitles.isNotEmpty()) {
            Alert(Alert.AlertType.INFORMATION).apply {
                title = "Some graphs not created"
                isResizable = true
                headerText = "The following graphs were not created because their dataset was empty (no PDFs or data found for given criteria)."
                dialogPane.content = form {
                    fieldset(labelPosition = Orientation.VERTICAL) {
                        field {
                            textarea {
                                text = graphTitles.joinToString("\n")
                                prefRowCount = 20
                                prefColumnCount = 50
                                isEditable = false
                            }
                        }
                    }
                }
                showAndWait()
            }
        }
    }
}


