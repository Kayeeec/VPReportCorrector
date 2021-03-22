package org.vpreportcorrector.mainview.content

import javafx.scene.control.Tab
import org.vpreportcorrector.diagram.DiagramController

data class TabData(val tab: Tab, val controller: DiagramController, val location: TabLocation)

enum class TabLocation {
    MAIN, DETACHED_WINDOW
}
