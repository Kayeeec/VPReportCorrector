package org.vpreportcorrector.components.form

import javafx.beans.property.SimpleDoubleProperty
import javafx.event.EventTarget
import javafx.geometry.Pos
import javafx.scene.layout.VBox
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
