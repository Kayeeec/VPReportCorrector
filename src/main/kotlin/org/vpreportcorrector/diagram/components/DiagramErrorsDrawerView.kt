package org.vpreportcorrector.diagram.components

import javafx.beans.property.SimpleBooleanProperty
import javafx.geometry.Side
import tornadofx.*

class DiagramErrorsDrawerView : View() {
    val drawerExpandedProperty = SimpleBooleanProperty(false)
    private val diagramErrorsFragment = find<DiagramErrorsView>()

    override val root = drawer(side = Side.RIGHT, floatingContent = true) {
        buttonArea.apply {
            prefWidth = 0.0
            minWidth = 0.0
            maxWidth = 0.0
        }
        item("Diagram errors", expanded = false) {
            expandedProperty.bind(drawerExpandedProperty)
            add(diagramErrorsFragment)
        }
    }
}
