package org.umlreviewer.mainview

import javafx.geometry.Orientation
import javafx.scene.Node
import javafx.scene.control.SplitPane
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid
import org.kordamp.ikonli.javafx.FontIcon
import org.umlreviewer.styles.Styles
import org.umlreviewer.filesexplorer.FilesExplorerView
import org.umlreviewer.mainview.content.ContentView
import org.umlreviewer.utils.t
import tornadofx.*

class EditorView : View("Editor") {
    private val filesExplorerView: FilesExplorerView by inject()
    private val contentView: ContentView by inject()

    private var filesExplorerVisible = true
    private var filesExplorerDividerPosition: Double = 0.25
    private var filesExplorerNode: Node? = null

    private fun toggleFilesExplorerPane() {
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

    private val centerSplitPane = splitpane {
        fitToParentSize()
        orientation = Orientation.HORIZONTAL
        setDividerPositions(filesExplorerDividerPosition)
        add(filesExplorerView)
        add(contentView)
        SplitPane.setResizableWithParent(this.items[0], false)
    }

    override val root = borderpane {
        fitToParentSize()
        left = vbox {
            group {
                togglebutton(t("directory")) {
                    action {
                        toggleFilesExplorerPane()
                    }
                    isSelected = filesExplorerVisible
                    graphic = FontIcon(FontAwesomeSolid.FOLDER)
                    rotate = -90.0
                    addClass(Styles.sideButton, Styles.flatButton)
                    tooltip(t("directoryTooltip"))
                }
            }
        }

        center = centerSplitPane
    }

    override fun onDock() {
        // TODO KB:
        log.info("editor on dock")
    }

    override fun onUndock() {
        // TODO: 19.04.21
        log.info("editor on undock")
    }
}
