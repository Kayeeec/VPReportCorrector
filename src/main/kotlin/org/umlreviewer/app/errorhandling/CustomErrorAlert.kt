package org.umlreviewer.app.errorhandling

import javafx.scene.control.Alert
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import tornadofx.*

fun errorWithStacktrace(message: String, error: Throwable) {
    val cause = if (error.cause != null) error.cause?.message else ""
    val msg = if (message.isNotBlank()) "$message\n${error.message}" else error.message

    Alert(Alert.AlertType.ERROR).apply {
        title = "Error"
        isResizable = true
        headerText = msg
        dialogPane.content = VBox().apply {
            prefWidth = 500.0
            prefHeight = 250.0

            gridpane {
                fitToParentSize()
                if (!cause.isNullOrBlank()) {
                    row {
                        label(cause) {
                            style = "-fx-font-weight: bold"
                            isWrapText = true
                            prefWidthProperty().bind(this@apply.widthProperty().minus(10.8*2))
                        }
                    }
                }
                row {
                    label("Stacktrace:") {
                        gridpaneConstraints {
                            marginTop = 10.0
                            marginBottom = 10.0
                        }
                    }
                }
                row {
                    textarea {
                        prefRowCount = 20
                        prefColumnCount = 50
                        text = error.stackTraceToString()
                        isEditable = false
                        fitToParentSize()
                        gridpaneConstraints {
                            vGrow = Priority.ALWAYS
                            hGrow = Priority.ALWAYS
                        }
                    }
                }
            }
        }
        showAndWait()
    }
}
