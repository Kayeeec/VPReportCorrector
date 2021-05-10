package org.umlreviewer.mainview.content

import javafx.scene.control.Tab
import org.umlreviewer.diagram.DiagramViewModel

data class TabData(val tab: Tab, val viewModel: DiagramViewModel, val location: TabLocation)

enum class TabLocation {
    MAIN, DETACHED_WINDOW
}
