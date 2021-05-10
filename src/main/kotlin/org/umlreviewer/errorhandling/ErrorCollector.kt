package org.umlreviewer.errorhandling

import javafx.geometry.Orientation
import javafx.scene.control.Alert
import tornadofx.*

class ErrorCollector(val header: String) {
    private val errors = mutableListOf<CustomError>()

    fun addError(message: String, error: Throwable) {
        errors.add(CustomError(message, error))
    }

    fun verify(){
        if (errors.isNotEmpty()) {
            Alert(Alert.AlertType.ERROR).apply {
                title = if (errors.size > 1) "An error has occurred:" else "Multiple errors have occurred:"
                isResizable = true
                headerText = header
                dialogPane.content = tabpane {
                    errors.forEachIndexed { index, customError ->
                        tab("Error ${index + 1}") {
                            isClosable = false
                            form {
                                fieldset(customError.message, labelPosition = Orientation.VERTICAL) {
                                    field("Details:") {
                                        textarea {
                                            text = customError.error.stackTraceToString()
                                            prefRowCount = 20
                                            prefColumnCount = 50
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                showAndWait()
            }
        }
    }
}

data class CustomError(val message: String, val error: Throwable)
