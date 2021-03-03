package org.vpreportcorrector.mainview

import de.codecentric.centerdevice.javafxsvg.SvgImageLoaderFactory
import de.codecentric.centerdevice.javafxsvg.dimension.AttributeDimensionProvider
import javafx.geometry.Orientation
import javafx.scene.Node
import javafx.scene.control.SplitPane
import javafx.scene.layout.Priority
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid
import org.kordamp.ikonli.javafx.FontIcon
import org.vpreportcorrector.app.SettingsChanged
import org.vpreportcorrector.app.Styles.Companion.paddedContainer
import org.vpreportcorrector.app.Styles.Companion.sideButton
import org.vpreportcorrector.filesexplorer.FilesExplorerView
import org.vpreportcorrector.import.openSimpleImportDialog
import org.vpreportcorrector.settings.SettingsModalView
import org.vpreportcorrector.utils.t
import tornadofx.*

class MainView : View() {
    private val globalDataModel: GlobalDataModel by inject()
    private val filesExplorerView: FilesExplorerView by inject()
    private val contentView: ContentView by inject()

    private var filesExplorerVisible = true
    private var filesExplorerDividerPosition: Double = 0.3
    private var filesExplorerNode: Node? = null

    init {
        title = t("appName")
        globalDataModel.loadPreferencesData()
        subscribe<SettingsChanged> { globalDataModel.loadPreferencesData() }
        SvgImageLoaderFactory.install(AttributeDimensionProvider())
    }

    private val centerSplitPane = splitpane {
        orientation = Orientation.HORIZONTAL
        setDividerPositions(filesExplorerDividerPosition)
        add(filesExplorerView)
        add(contentView)
        SplitPane.setResizableWithParent(this.items[0], false)
    }

    override val root = borderpane {
        top = menubar {
            hgrow = Priority.ALWAYS
            removeClass(paddedContainer)
            menu(t("files")) {
                item(t("settings"), "Shortcut+Alt+S") {
                    action {
                        find<SettingsModalView>().openModal()
                    }
                }
                item(t("import"), "Shortcut+Alt+I") {
                    enableWhen { globalDataModel.workingDirectory.isNotNull }
                    action {
                        openSimpleImportDialog()
                    }
                }
            }
        }
        left = vbox {
            group {
                button(t("directory"), FontIcon(FontAwesomeSolid.FOLDER)) {
                    action {
                        toggleFilesExplorerPane()
                    }
                    rotate = -90.0
                    addClass(sideButton)
                    tooltip(t("directoryTooltip"))
                }
            }
        }
        center = centerSplitPane
    }

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
}
