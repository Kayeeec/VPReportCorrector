package org.vpreportcorrector.mainview

import javafx.geometry.Orientation
import javafx.scene.Node
import org.vpreportcorrector.filesexplorer.FilesExplorerView
import tornadofx.*

class MainView : View("Hello TornadoFX") {
    private var filesExplorerVisible = true
    private var filesExplorerDividerPosition: Double = 0.3
    private var filesExplorerNode: Node? = null

    private val centerSplitPane = splitpane {
        orientation = Orientation.HORIZONTAL
        setDividerPositions(0.3)
        add(FilesExplorerView()) // TODO: 06.01.21 min and max size
        add(ContentView())
    }
    override val root = borderpane {
        left = vbox { // TODO: 06.01.21 improve style --arrow instead
            button("Files") {
                action {
                    toggleFilesExplorerPane()
                }
            }
        }
        center = centerSplitPane
    }

    fun toggleFilesExplorerPane() {
        if (filesExplorerVisible) {
            filesExplorerNode = centerSplitPane.items[0]
            filesExplorerDividerPosition = centerSplitPane.dividerPositions[0]
            centerSplitPane.items.removeAt(0)
            filesExplorerVisible = false
        } else if (filesExplorerNode !== null) {
            centerSplitPane.items.add(0, filesExplorerNode)
            centerSplitPane.setDividerPosition(0, filesExplorerDividerPosition)
            filesExplorerVisible = true
        }
    }
}
