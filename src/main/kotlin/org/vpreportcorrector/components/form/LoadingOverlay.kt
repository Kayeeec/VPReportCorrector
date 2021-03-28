package org.vpreportcorrector.components.form

import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.value.ObservableValue
import javafx.event.EventTarget
import javafx.geometry.Pos
import javafx.scene.layout.VBox
import javafx.scene.text.TextAlignment
import tornadofx.*

fun EventTarget.loadingOverlay(taskStatus: TaskStatus, op: VBox.() -> Unit = {}): VBox {
    val vbox = vbox {
        visibleWhen { taskStatus.running }
        fitToParentSize()
        style {
            backgroundColor += c("white", 0.5)
            alignment = Pos.CENTER
        }
        progressindicator {
            progressProperty().bind(taskStatus.progress)
        }
        textflow {
            style {
                textAlignment = TextAlignment.CENTER
                padding = box(10.px)
            }
            text(taskStatus.message)
        }
    }
    return opcr(this, vbox, op)
}

fun EventTarget.loadingOverlay(op: VBox.() -> Unit = {}): VBox {
    val vbox = vbox {
        fitToParentSize()
        style {
            backgroundColor += c("white", 0.5)
            alignment = Pos.CENTER
        }
        progressindicator()
    }
    return opcr(this, vbox, op)
}

fun EventTarget.loadingOverlay(message: ObservableValue<String>, op: VBox.() -> Unit = {}): VBox {
    val vbox = vbox {
        fitToParentSize()
        style {
            backgroundColor += c("white", 0.5)
            alignment = Pos.CENTER
        }
        progressindicator()
        textflow {
            visibleWhen { message.isNotBlank() }
            style {
                textAlignment = TextAlignment.CENTER
                padding = box(10.px)
            }
            text(message)
        }
    }
    return opcr(this, vbox, op)
}
