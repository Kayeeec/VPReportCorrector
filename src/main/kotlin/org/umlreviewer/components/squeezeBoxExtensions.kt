package org.umlreviewer.components

import javafx.scene.control.TitledPane
import tornadofx.SqueezeBox

fun SqueezeBox.expandAll() {
    this.childrenUnmodifiable.filterIsInstance<TitledPane>().forEach {
        it.isExpanded = true
    }
}

fun SqueezeBox.collapseAll() {
    this.childrenUnmodifiable.filterIsInstance<TitledPane>().forEach {
        it.isExpanded = false
    }
}
