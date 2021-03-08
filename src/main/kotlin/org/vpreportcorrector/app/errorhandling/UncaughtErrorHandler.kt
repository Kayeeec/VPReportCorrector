package org.vpreportcorrector.app.errorhandling

import javafx.application.Platform.runLater
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType.ERROR
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import tornadofx.*
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Modified version of tornadofx.DefaultErrorHandler.
 * Recreated because tornadofx.DefaultErrorHandler has a bug - does not show the error stacktrace (line 65).
 */
class UncaughtErrorHandler : Thread.UncaughtExceptionHandler {
    val log = Logger.getLogger("ErrorHandler")

    class ErrorEvent(val thread: Thread, val error: Throwable) {
        internal var consumed = false
        fun consume() {
            consumed = true
        }
    }

    companion object {
        // By default, all error messages are shown. Override to decide if certain errors should be handled another way.
        // Call consume to avoid error dialog.
        var filter: (ErrorEvent) -> Unit = {
            /**
             * Consume uncaught icepdf errors, for there are weird bugs and the dialogs are annoying.
             * IcePDF viewer still usable though.
             */
            if (it.error.stackTraceToString().contains("icepdf")) {
                it.consume()
            }
        }
    }

    override fun uncaughtException(t: Thread, error: Throwable) {
        log.log(Level.SEVERE, "Uncaught error", error)

        if (isCycle(error)) {
            log.log(Level.INFO, "Detected cycle handling error, aborting.", error)
        } else {
            val event = ErrorEvent(t, error)
            filter(event)

            if (!event.consumed) {
                event.consume()
                runLater {
                    showErrorDialog(error)
                }
            }
        }

    }

    private fun isCycle(error: Throwable) = error.stackTrace.any {
        it.className.startsWith("${javaClass.name}\$uncaughtException$")
    }

    private fun showErrorDialog(error: Throwable) {
        val cause = Label(if (error.cause != null) error.cause?.message else "").apply {
            style = "-fx-font-weight: bold"
        }

        val textarea = TextArea().apply {
            prefRowCount = 20
            prefColumnCount = 50
            text = error.stackTraceToString()
            isEditable = false
        }

        Alert(ERROR).apply {
            title = error.message ?: "An error occured"
            isResizable = true
            headerText = if (error.stackTrace.isNullOrEmpty()) "Error" else "Error in " + error.stackTrace[0].toString()
            dialogPane.content = VBox().apply {
                add(cause)
                if (error is RestException) {
                    try {

                        title = "HTTP Request Error: $title"
                        form {
                            fieldset(error.message) {
                                val response = error.response
                                if (response != null) {
                                    field("Status") {
                                        label("${response.statusCode} ${response.reason}")
                                    }

                                    val c = response.text()

                                    if (c != null) {
                                        tabpane {
                                            background = Color.TRANSPARENT.asBackground()

                                            tab("Plain text") {
                                                textarea(c)
                                            }
                                            tab("HTML") {
                                                if (response.header("Content-Type")?.contains("html", true) == true)
                                                    select()
                                                webview {
                                                    engine.loadContent(c)
                                                }
                                            }
                                            tab("Stacktrace") {
                                                add(textarea)
                                            }
                                            tabs.withEach { isClosable = false }
                                        }
                                    } else {
                                        add(textarea)
                                    }
                                } else {
                                    add(textarea)
                                }
                            }
                        }

                    } catch (e: Exception) {
                        add(textarea)
                    }
                } else {
                    add(textarea)
                }
            }

            showAndWait()
        }
    }

}
